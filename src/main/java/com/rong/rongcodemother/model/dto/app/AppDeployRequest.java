package com.rong.rongcodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 应用版本
     */
    private Short appVersion;

    private static final long serialVersionUID = 1L;
}
