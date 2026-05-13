package com.smartagent.knowledge.dto;

import lombok.Data;

/**
 * 创建知识库DTO
 * 用于创建知识库的请求参数
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class CreateKnowledgeBaseDTO {

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;
}
