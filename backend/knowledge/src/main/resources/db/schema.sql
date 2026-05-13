-- 1. 创建数据库（不存在则创建）
CREATE DATABASE IF NOT EXISTS `smart-agent-knowledge`
    DEFAULT CHARACTER SET utf8mb4  -- 支持emoji、特殊字符
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. 使用该数据库
USE `smart-agent-knowledge`;

-- 3. 创建知识库表 knowledge_base
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base` (
                                 `id` BIGINT NOT NULL COMMENT '知识库ID',
                                 `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
                                 `name` VARCHAR(128) DEFAULT NULL COMMENT '知识库名称',
                                 `description` TEXT DEFAULT NULL COMMENT '知识库描述',
                                 `status` TINYINT DEFAULT 0 COMMENT '状态（0：初始化中，1：正常，2：错误）',
                                 `document_count` INT DEFAULT 0 COMMENT '文档数量',
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE,
    -- 业务常用查询索引
                                 KEY `idx_user_id` (`user_id`) USING BTREE,
                                 KEY `idx_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 4. 创建文档表 document
DROP TABLE IF EXISTS `document`;
CREATE TABLE `document` (
                           `id` BIGINT NOT NULL COMMENT '文档ID',
                           `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
                           `name` VARCHAR(255) DEFAULT NULL COMMENT '文档名称',
                           `type` VARCHAR(32) DEFAULT NULL COMMENT '文档类型（pdf, docx, txt等）',
                           `size` BIGINT DEFAULT 0 COMMENT '文档大小（字节）',
                           `status` TINYINT DEFAULT 0 COMMENT '状态（0：上传中，1：处理中，2：完成，3：错误）',
                           `storage_path` VARCHAR(512) DEFAULT NULL COMMENT '存储路径',
                           `page_count` INT DEFAULT 0 COMMENT '页数（如果适用）',
                           `token_count` INT DEFAULT 0 COMMENT 'Token数量',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`) USING BTREE,
    -- 核心关联索引
                           KEY `idx_knowledge_base_id` (`knowledge_base_id`) USING BTREE,
                           KEY `idx_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';
