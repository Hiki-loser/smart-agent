package com.smartagent.knowledge.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档VO
 * 用于返回文档信息给前端
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class DocumentVO {

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 文档名称
     */
    private String name;

    /**
     * 文档类型（pdf, docx, txt等）
     */
    private String type;

    /**
     * 文档大小（字节）
     */
    private Long size;

    /**
     * 状态（0：上传中，1：处理中，2：完成，3：错误）
     */
    private Integer status;

    /**
     * 页数（如果适用）
     */
    private Integer pageCount;

    /**
     * Token数量
     */
    private Integer tokenCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
