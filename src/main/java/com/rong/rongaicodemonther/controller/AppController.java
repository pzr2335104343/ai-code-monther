package com.rong.rongaicodemonther.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.rong.rongaicodemonther.annotation.AuthCheck;
import com.rong.rongaicodemonther.common.BaseResponse;
import com.rong.rongaicodemonther.common.DeleteRequest;
import com.rong.rongaicodemonther.common.ResultUtils;
import com.rong.rongaicodemonther.constant.UserConstant;
import com.rong.rongaicodemonther.exception.BusinessException;
import com.rong.rongaicodemonther.exception.ErrorCode;
import com.rong.rongaicodemonther.exception.ThrowUtils;
import com.rong.rongaicodemonther.model.dto.AppAddRequest;
import com.rong.rongaicodemonther.model.dto.AppAdminUpdateRequest;
import com.rong.rongaicodemonther.model.dto.AppQueryRequest;
import com.rong.rongaicodemonther.model.dto.AppUpdateRequest;
import com.rong.rongaicodemonther.model.entity.App;
import com.rong.rongaicodemonther.model.entity.User;
import com.rong.rongaicodemonther.model.vo.AppVO;
import com.rong.rongaicodemonther.service.AppService;
import com.rong.rongaicodemonther.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    // 【用户】创建应用（须填写 initPrompt）
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest addRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(addRequest.getInitPrompt() == null || addRequest.getInitPrompt().isEmpty(), ErrorCode.PARAMS_ERROR, "initPrompt 必填");
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        BeanUtil.copyProperties(addRequest, app);
        app.setUserId(loginUser.getId());
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    // 【用户】根据 id 修改自己的应用（仅名称）
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

    // 【用户】根据 id 删除自己的应用
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

    // 【用户】根据 id 查看应用详情（仅本人）
    @GetMapping("/get/my")
    public BaseResponse<AppVO> getMyApp(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

    // 【用户】分页查询自己的应用列表（支持名称，每页≤20）
    @PostMapping("/list/my/page")
    public BaseResponse<Page<AppVO>> listMyAppByPage(@RequestBody AppQueryRequest queryRequest, HttpServletRequest request) {
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

    // 【用户】分页查询精选的应用列表（支持名称，每页≤20）
    @PostMapping("/list/good/page")
    public BaseResponse<Page<AppVO>> listGoodAppByPage(@RequestBody AppQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        if (queryRequest.getPageSize() > 20) {
            queryRequest.setPageSize(20);
        }
        // 精选逻辑：按 priority 降序
        queryRequest.setSortField("priority");
        queryRequest.setSortOrder("descend");
        // 不限定 userId
        QueryWrapper wrapper = appService.getQueryWrapper(queryRequest);
        Page<App> page = appService.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), wrapper);
        Page<AppVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(appService.getAppVOList(page.getRecords()));
        return ResultUtils.success(voPage);
    }

    // 【管理员】根据 id 删除任意应用
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = appService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    // 【管理员】根据 id 更新任意应用（名称、封面、优先级）
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateApp(@RequestBody AppAdminUpdateRequest updateRequest) {
        if (updateRequest == null || updateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = new App();
        BeanUtil.copyProperties(updateRequest, app);
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // 【管理员】分页查询应用列表（除时间外任意字段，数量不限）
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppByPage(@RequestBody AppQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper wrapper = appService.getQueryWrapper(queryRequest);
        Page<App> page = appService.page(Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()), wrapper);
        Page<AppVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(appService.getAppVOList(page.getRecords()));
        return ResultUtils.success(voPage);
    }

    // 【管理员】根据 id 查看应用详情
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }
}
