package com.rong.rongaicodemonther.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 封装删除请求的数据
 */
@Data  // 使用Lombok的@Data注解，自动生成getter、setter等方法
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
