package com.rong.rongaicodemonther.model.dto;

import com.rong.rongaicodemonther.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    private Long id;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 应用名称（支持模糊查询）
     */
    private String appName;

    private String cover;

    private String codeGenType;

    private String deployKey;

    private Integer priority;

    private static final long serialVersionUID = 1L;
}


