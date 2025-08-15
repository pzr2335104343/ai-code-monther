package com.rong.rongaicodemonther.controller;

import com.rong.rongaicodemonther.common.BaseResponse;
import com.rong.rongaicodemonther.common.ResultUtils;
import com.rong.rongaicodemonther.exception.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 * 提供系统健康状态检查的API接口
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    /**
     * 健康检查接口
     * @return 返回系统健康状态，正常情况下返回"ok"
     */
    @GetMapping("/")
    public BaseResponse<?> health() {
        // 调用ResultUtils.success方法封装成功响应
        return ResultUtils.success(ErrorCode.SUCCESS);
    }
}
