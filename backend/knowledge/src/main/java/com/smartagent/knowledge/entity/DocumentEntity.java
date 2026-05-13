package com.smartagent.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档实体
 * 对应数据库 document 表
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
@TableName("document")
public class DocumentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 知识库ID
     */
    @TableField("knowledge_base_id")
    private Long knowledgeBaseId;

    /**
     * 文档名称
     */
    @TableField("name")
    private String name;

    /**
     * 文档类型（pdf, docx, txt等）
     */
    @TableField("type")
    private String type;

    /**
     * 文档大小（字节）
     */
    @TableField("size")
    private Long size;

    /**
     * 状态（0：上传中，1：处理中，2：完成，3：错误）
     */
    @TableField("status")
    private Integer status;

    /**
     * 存储路径
     */
    @TableField("storage_path")
    private String storagePath;

    /**
     * 页数（如果适用）
     */
    @TableField("page_count")
    private Integer pageCount;

    /**
     * Token数量
     */
    @TableField("token_count")
    private Integer tokenCount;

    /**
     * 切片数量
     */
    @TableField("chunk_count")
    private Integer chunkCount;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
