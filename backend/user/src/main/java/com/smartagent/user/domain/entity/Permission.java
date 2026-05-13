package com.smartagent.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限实体
 * 对应数据库 permission 表
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
@TableName("permission")
public class Permission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称
     */
    @TableField("name")
    private String name;

    /**
     * 权限编码
     */
    @TableField("code")
    private String code;

    /**
     * 类型（1菜单 2接口）
     */
    @TableField("type")
    private Integer type;

    /**
     * 路径
     */
    @TableField("path")
    private String path;

    /**
     * 请求方法
     */
    @TableField("method")
    private String method;
}
