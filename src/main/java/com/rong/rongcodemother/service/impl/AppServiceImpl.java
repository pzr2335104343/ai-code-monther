package com.rong.rongcodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rong.rongcodemother.ai.AiCodeGenTypeRoutingService;
import com.rong.rongcodemother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.rong.rongcodemother.constant.AppConstant;
import com.rong.rongcodemother.core.AiCodeGeneratorFacade;
import com.rong.rongcodemother.core.builder.VueProjectBuilder;
import com.rong.rongcodemother.core.handler.StreamHandlerExecutor;
import com.rong.rongcodemother.exception.BusinessException;
import com.rong.rongcodemother.exception.ErrorCode;
import com.rong.rongcodemother.exception.ThrowUtils;
import com.rong.rongcodemother.model.dto.app.AppAddRequest;
import com.rong.rongcodemother.model.dto.app.AppQueryRequest;
import com.rong.rongcodemother.model.entity.App;
import com.rong.rongcodemother.mapper.AppMapper;
import com.rong.rongcodemother.model.entity.User;
import com.rong.rongcodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.rong.rongcodemother.model.enums.CodeGenTypeEnum;
import com.rong.rongcodemother.model.vo.AppVO;
import com.rong.rongcodemother.model.vo.UserVO;
import com.rong.rongcodemother.monitor.MonitorContext;
import com.rong.rongcodemother.monitor.MonitorContextHolder;
import com.rong.rongcodemother.service.AppService;
import com.rong.rongcodemother.service.ChatHistoryService;
import com.rong.rongcodemother.service.ScreenshotService;
import com.rong.rongcodemother.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 服务层实现。
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    /**
     * 创建应用
     *
     * @param appAddRequest 创建参数
     * @param loginUser     登录用户
     * @return 应用ID
     */
    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {

        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用AI智能路由选择代码生成类型（多例模式）
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum codeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(codeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        // 2.获取应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3.权限校验,判断用户是否有权限调用该应用（本人）
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限调用该应用");
        }

        // 4.获取应用类型
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR, "应用类型错误");

        // 5. 通过校验后，添加用户消息到对话历史
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 6. 设置监控上下文
        MonitorContextHolder.setContext(
                MonitorContext.builder()
                        .userId(loginUser.getId().toString())
                        .appId(appId.toString())
                        .build()
        );
        // 7. 调用 AI 生成代码（流式）
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenType, appId);
        // 8. 收集 AI 响应内容并在完成后记录到对话历史
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenType)
                .doFinally(signalType -> {
                    // 流结束时清理（无论成功/失败/取消）
                    MonitorContextHolder.clearContext();
                });

    }


    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1.参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2.获取应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3.权限校验,判断用户是否有权限调用该应用（本人）
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署应用");
        }
        // 4.检查是否有部署key
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            // 若果没有，生成6位deployKey(字符+数字)
            deployKey = RandomUtil.randomString(6);
            app.setDeployKey(deployKey);
        }
        // 5.获取原始代码生成路径（应用访问目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + app.getId();
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6.校验路径文件存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用不存在，请先生成");
        }
        // 7.Vue 项目dist目录
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "构建 Vue 项目失败");
            // 检查是否有 dist目录
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "构建 Vue 项目成功但未生成dist目录");
            sourceDir = distDir;
        }

        // 8.复制到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败");
        }
        // 9.更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setEditTime(LocalDateTime.now());
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新部署应用信息失败");
        // 10.返回部署地址
        String appDeployUrl = String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 11.异步生成截图并且更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);

        return appDeployUrl;
    }

    /**
     * 异步生成应用截图并更新应用封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问地址
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程并且执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String coverUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(coverUrl);
            boolean result = this.updateById(updateApp);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用封面失败");
        });


    }


    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO vo = new AppVO();
        BeanUtils.copyProperties(app, vo);
        // 关联用户信息
        User user = userService.getById(app.getUserId());
        UserVO userVO = userService.getUserVO(user);
        vo.setUser(userVO);

        return vo;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }

}
