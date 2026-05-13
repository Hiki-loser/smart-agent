package com.smartagent.common.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结构
 * 封装分页查询的返回数据
 *
 * @param <T> 数据类型
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class PageResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private long page;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 构造方法
     *
     * @param page    当前页码
     * @param size    每页大小
     * @param total   总记录数
     * @param records 数据列表
     */
    public PageResponse(long page, long size, long total, List<T> records) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.records = records;
    }

    /**
     * 构造方法
     */
    public PageResponse() {
    }
}
