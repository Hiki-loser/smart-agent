package com.smartagent.common.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 返回基类
 * 所有返回 VO 的父类
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    private Long id;
}
