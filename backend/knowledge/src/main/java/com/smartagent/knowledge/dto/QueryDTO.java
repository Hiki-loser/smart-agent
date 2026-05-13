package com.smartagent.knowledge.dto;

import lombok.Data;

/**
 * 知识库查询DTO
 * 用于知识库查询的请求参数
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class QueryDTO {

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 查询内容
     */
    private String query;

    /**
     * 相似度阈值
     */
    private Double similarityThreshold = 0.7;

    /**
     * 返回结果数量
     */
    private Integer topK = 5;
}
