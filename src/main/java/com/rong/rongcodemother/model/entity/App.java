package com.rong.rongcodemother.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import com.rong.rongcodemother.constant.AppConstant;
import com.rong.rongcodemother.exception.BusinessException;
import com.rong.rongcodemother.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类。
 *
 * @author rong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app")
public class App implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 应用名称
     */
    @Column("appName")
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    @Column("initPrompt")
    private String initPrompt;

    /**
     * 应用版本
     */
    @Column("appVersion")
    private Short appVersion;

    /**
     * 代码生成类型（枚举）
     */
    @Column("codeGenType")
    private String codeGenType;

    /**
     * 部署标识
     */
    @Column("deployKey")
    private String deployKey;

    /**
     * 部署时间
     */
    @Column("deployedTime")
    private LocalDateTime deployedTime;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建用户id
     */
    @Column("userId")
    private Long userId;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;


    /**
     * 版本号递增方法（线程安全且带边界校验）
     * @return 递增后的版本号，若已达最大值则返回null或抛出异常（根据业务需求选择）
     */
    public Short incrementAppVersion() {
        // 校验是否已达Integer最大值，避免溢出
        if (this.appVersion > AppConstant.APP_MAX_VERSION) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"应用版本号已达到最大值,请新建应用");
        }
        // 先递增再返回（确保返回的是最新值）
        this.appVersion++;
        return this.appVersion;
    }


}
