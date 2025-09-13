package com.rong.rongaicodemonther.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rong.rongaicodemonther.annotation.AuthCheck;
import com.rong.rongaicodemonther.common.BaseResponse;
import com.rong.rongaicodemonther.common.DeleteRequest;
import com.rong.rongaicodemonther.common.ResultUtils;
import com.rong.rongaicodemonther.constant.AppConstant;
import com.rong.rongaicodemonther.constant.UserConstant;
import com.rong.rongaicodemonther.exception.BusinessException;
import com.rong.rongaicodemonther.exception.ErrorCode;
import com.rong.rongaicodemonther.exception.ThrowUtils;
import com.rong.rongaicodemonther.model.dto.app.*;
import com.rong.rongaicodemonther.model.entity.App;
import com.rong.rongaicodemonther.model.entity.User;
import com.rong.rongaicodemonther.model.enums.CodeGenTypeEnum;
import com.rong.rongaicodemonther.model.vo.AppVO;
import com.rong.rongaicodemonther.service.AppService;
import com.rong.rongaicodemonther.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;


@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatGenCode(@RequestParam Long appId, @RequestParam String message, HttpServletRequest request) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用Id错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        // 2. 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码（SSE 流式返回）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);

        return contentFlux
                .map(chunk->{
                    String jsonData = JSONUtil.toJsonStr(Map.of("d", chunk));
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(
                        Mono.just(ServerSentEvent.<String>builder()
                                .event("done")
                                .build()));

    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求
     * @param request       请求
     * @return 应用 id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 暂时设置为多文件生成
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 插入数据库
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }


    /**
     * 【用户】根据 id 修改自己的应用（仅名称）
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null || updateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        App exist = appService.getById(updateRequest.getId());
        ThrowUtils.throwIf(exist == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!exist.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        App app = new App();
        app.setId(updateRequest.getId());
        app.setAppName(updateRequest.getAppName());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 【用户】根据 id 删除自己的应用
     */
    @PostMapping("/delete/my")
    public BaseResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        App exist = appService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(exist == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!exist.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        boolean result = appService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 【用户】根据 id 查看应用详情（仅本人）
     */
    @GetMapping("/get/my")
    public BaseResponse<AppVO> getMyApp(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 【用户】分页查询自己的应用列表（支持名称，每页≤20）
     */
    @PostMapping("/list/my/page")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest queryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        if (queryRequest.getPageSize() > 20) {
            queryRequest.setPageSize(20);
        }
        User loginUser = userService.getLoginUser(request);
        queryRequest.setUserId(loginUser.getId());
        QueryWrapper wrapper = appService.getQueryWrapper(queryRequest);
        Page<App> page = appService.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), wrapper);
        Page<AppVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(appService.getAppVOList(page.getRecords()));
        return ResultUtils.success(voPage);
    }

    /**
     * 【用户】分页查询精选的应用列表（支持名称，每页≤20）
     */
    @PostMapping("/list/good/page")
    public BaseResponse<Page<AppVO>> listGoodAppByPage(@RequestBody AppQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        if (queryRequest.getPageSize() > 20) {
            queryRequest.setPageSize(20);
        }
        queryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper wrapper = appService.getQueryWrapper(queryRequest);
        Page<App> page = appService.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), wrapper);
        Page<AppVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(appService.getAppVOList(page.getRecords()));
        return ResultUtils.success(voPage);
    }

    /**
     * 管理员删除应用
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }


    /**
     * 【管理员】根据 id 更新任意应用（名称、封面、优先级）
     */
    @PostMapping("admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest updateRequest) {
        if (updateRequest == null || updateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App oldApp = appService.getById(updateRequest.getId());
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        BeanUtil.copyProperties(updateRequest, oldApp);
        oldApp.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(oldApp);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页获取应用列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(appService.getAppVOList(appPage.getRecords()));
        return ResultUtils.success(appVOPage);
    }

    /**
     * 【管理员】根据 id 查看应用详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }
}
