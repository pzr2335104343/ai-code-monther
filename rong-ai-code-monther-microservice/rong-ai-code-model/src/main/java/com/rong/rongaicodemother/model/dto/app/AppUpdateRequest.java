package com.rong.rongaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppUpdateRequest implements Serializable {

    private Long id;

    /**
     * 应用名称（仅用户可修改）
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}


