package com.smartagent.common.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页请求参数
 * 封装分页查询的请求参数
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认1
     */
    private long page = 1;

    /**
     * 每页大小，默认10
     */
    private long size = 10;
}
