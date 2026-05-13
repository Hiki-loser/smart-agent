package com.smartagent.knowledge.vo;

import lombok.Data;

import java.util.List;

/**
 * 查询结果VO
 * 用于返回知识库查询结果给前端
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class QueryResultVO {

    /**
     * 查询ID
     */
    private String queryId;

    /**
     * 查询内容
     */
    private String query;

    /**
     * 回答内容
     */
    private String answer;

    /**
     * 相关文档片段
     */
    private List<DocumentFragmentVO> documentFragments;

    /**
     * 执行时间（毫秒）
     */
    private long executionTime;

    /**
     * 文档片段VO
     */
    @Data
    public static class DocumentFragmentVO {

        /**
         * 文档ID
         */
        private Long documentId;

        /**
         * 文档名称
         */
        private String documentName;

        /**
         * 片段内容
         */
        private String content;

        /**
         * 相似度
         */
        private Double similarity;

        /**
         * 页码（如果适用）
         */
        private Integer pageNumber;
    }
}
