package com.smartagent.knowledge.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库VO
 * 用于返回知识库信息给前端
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class KnowledgeBaseVO {

    /**
     * 知识库ID
     */
    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 状态（0：初始化中，1：正常，2：错误）
     */
    private Integer status;

    /**
     * 文档数量
     */
    private Integer documentCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
