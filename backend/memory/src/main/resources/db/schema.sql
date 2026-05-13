-- 1. 创建数据库
DROP DATABASE IF EXISTS `smart-agent-memory`;
CREATE DATABASE IF NOT EXISTS `smart-agent-memory`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `smart-agent-memory`;

-- 2. 当前记忆快照表（每个 session 仅一条）
DROP TABLE IF EXISTS `memory_current`;
CREATE TABLE `memory_current` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `summary_content` LONGTEXT NOT NULL COMMENT '当前会话摘要',
    `summarized_round` INT NOT NULL DEFAULT 0 COMMENT '已总结到的轮次',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_session` (`session_id`) USING BTREE,
    KEY `idx_user` (`user_id`) USING BTREE,
    KEY `idx_updated` (`updated_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='当前记忆快照表';

-- 3. 记忆历史表（保留每次摘要的快照版本）
DROP TABLE IF EXISTS `memory_history`;
CREATE TABLE `memory_history` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `start_round` INT NOT NULL COMMENT '本次摘要起始轮次',
    `end_round` INT NOT NULL COMMENT '本次摘要结束轮次',
    `summary_content` LONGTEXT NOT NULL COMMENT '摘要结果',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_session_round` (`session_id`, `end_round`) USING BTREE,
    KEY `idx_user` (`user_id`) USING BTREE,
    KEY `idx_created` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记忆历史表';
