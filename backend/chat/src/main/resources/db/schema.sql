-- 1. 创建数据库（不存在则创建）
DROP DATABASE IF EXISTS `smart-agent-chat`;
CREATE DATABASE IF NOT EXISTS `smart-agent-chat`
    DEFAULT CHARACTER SET utf8mb4  -- 支持emoji、特殊字符
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. 使用该数据库
USE `smart-agent-chat`;

-- 3. 创建会话表 session
DROP TABLE IF EXISTS `session`;
CREATE TABLE `session` (
                           `id` BIGINT NOT NULL COMMENT '会话ID',
                           `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
                           `title` VARCHAR(128) DEFAULT NULL COMMENT '会话标题',
                           `agent_type` VARCHAR(32) DEFAULT NULL COMMENT '代理类型',
                           `message_count` INT DEFAULT 0 COMMENT '消息数量',
                           `round_count` INT DEFAULT 0 COMMENT '会话轮数（用户+LLM为一轮）',
                           `last_message_at` DATETIME DEFAULT NULL COMMENT '最后消息时间',
                           `status` TINYINT DEFAULT 1 COMMENT '状态（1为活跃，0为删除）',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           PRIMARY KEY (`id`) USING BTREE,
    -- 业务常用查询索引
                           KEY `idx_user_id` (`user_id`) USING BTREE,
                           KEY `idx_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 4. 用户消息表
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message` (
                                `id` BIGINT NOT NULL COMMENT '用户消息ID',
                                `session_id` BIGINT NOT NULL COMMENT '会话ID',
                                `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
                                `round_no` INT NOT NULL COMMENT '消息所属轮次',
                                `content` TEXT DEFAULT NULL COMMENT '用户输入',
                                `tokens` INT DEFAULT 0 COMMENT '用户消息token数',
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`id`) USING BTREE,
                                KEY `idx_um_session_id` (`session_id`) USING BTREE,
                                KEY `idx_um_session_round` (`session_id`, `round_no`) USING BTREE,
                                KEY `idx_um_user_id` (`user_id`) USING BTREE,
                                KEY `idx_um_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息表';

-- 5. LLM 消息表
DROP TABLE IF EXISTS `llm_message`;
CREATE TABLE `llm_message` (
                               `id` BIGINT NOT NULL COMMENT 'LLM消息ID',
                               `session_id` BIGINT NOT NULL COMMENT '会话ID',
                               `round_no` INT NOT NULL COMMENT '消息所属轮次',
                               `model_name` VARCHAR(64) DEFAULT NULL COMMENT '模型名称',
                               `content` LONGTEXT DEFAULT NULL COMMENT 'LLM输出内容',
                               `prompt_tokens` INT DEFAULT 0 COMMENT '输入token',
                               `completion_tokens` INT DEFAULT 0 COMMENT '输出token',
                               `total_tokens` INT DEFAULT 0 COMMENT '总token',
                               `finish_reason` VARCHAR(32) DEFAULT NULL COMMENT '结束原因',
                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               PRIMARY KEY (`id`) USING BTREE,
                               KEY `idx_lm_session_id` (`session_id`) USING BTREE,
                               KEY `idx_lm_session_round` (`session_id`, `round_no`) USING BTREE,
                               KEY `idx_lm_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LLM消息表';

-- 6. LLM 会话执行记录表（流式结束后落 usage）
DROP TABLE IF EXISTS `llm_session`;
CREATE TABLE `llm_session` (
                               `id` BIGINT NOT NULL COMMENT '记录ID',
                               `session_id` BIGINT NOT NULL COMMENT '会话ID',
                               `user_message_id` BIGINT DEFAULT NULL COMMENT '关联用户消息ID',
                               `llm_message_id` BIGINT DEFAULT NULL COMMENT '关联LLM消息ID',
                               `model_name` VARCHAR(64) DEFAULT NULL COMMENT '模型名称',
                               `prompt_tokens` INT DEFAULT 0 COMMENT '输入token',
                               `completion_tokens` INT DEFAULT 0 COMMENT '输出token',
                               `total_tokens` INT DEFAULT 0 COMMENT '总token',
                               `finish_reason` VARCHAR(32) DEFAULT NULL COMMENT '结束原因',
                               `status` VARCHAR(16) NOT NULL DEFAULT 'SUCCESS' COMMENT '执行状态(SUCCESS/FAILED)',
                               `error_message` VARCHAR(512) DEFAULT NULL COMMENT '失败原因',
                               `started_at` DATETIME DEFAULT NULL COMMENT '开始时间',
                               `completed_at` DATETIME DEFAULT NULL COMMENT '结束时间',
                               `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               PRIMARY KEY (`id`) USING BTREE,
                               KEY `idx_ls_session_id` (`session_id`) USING BTREE,
                               KEY `idx_ls_user_message_id` (`user_message_id`) USING BTREE,
                               KEY `idx_ls_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LLM会话执行记录表';

-- 7. 最新会话摘要表（每个会话仅一条）
DROP TABLE IF EXISTS `chat_memory_current`;
CREATE TABLE `chat_memory_current` (
                                       `id` BIGINT NOT NULL COMMENT '主键ID',
                                       `session_id` BIGINT NOT NULL COMMENT '会话ID',
                                       `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                       `summary_content` LONGTEXT NOT NULL COMMENT '当前会话摘要',
                                       `summarized_round` INT NOT NULL DEFAULT 0 COMMENT '已总结到的轮次',
                                       `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       UNIQUE KEY `uk_cmc_session` (`session_id`) USING BTREE,
                                       KEY `idx_cmc_user` (`user_id`) USING BTREE,
                                       KEY `idx_cmc_updated` (`updated_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话最新摘要表';

-- 8. 会话摘要历史表（保留每次总结快照）
DROP TABLE IF EXISTS `chat_memory_history`;
CREATE TABLE `chat_memory_history` (
                                       `id` BIGINT NOT NULL COMMENT '主键ID',
                                       `session_id` BIGINT NOT NULL COMMENT '会话ID',
                                       `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                       `start_round` INT NOT NULL COMMENT '本次总结起始轮次',
                                       `end_round` INT NOT NULL COMMENT '本次总结结束轮次',
                                       `summary_content` LONGTEXT NOT NULL COMMENT '总结结果',
                                       `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       KEY `idx_cmh_session_round` (`session_id`, `end_round`) USING BTREE,
                                       KEY `idx_cmh_user` (`user_id`) USING BTREE,
                                       KEY `idx_cmh_created` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话摘要历史表';

