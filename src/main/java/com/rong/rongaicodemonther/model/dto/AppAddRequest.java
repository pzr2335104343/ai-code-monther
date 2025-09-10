package com.rong.rongaicodemonther.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面（可选，创建时暂不要求）
     */
    private String cover;

    /**
     * 初始化 Prompt（必填）
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}


