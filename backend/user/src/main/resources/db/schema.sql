-- ==============================
-- 智能用户中心数据库
-- 兼容 MySQL 8.x + utf8mb4 + InnoDB
-- 用户模块完整建表语句
-- ==============================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_agent_user
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_agent_user;

-- ----------------------------
-- 表结构：用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(64) NOT NULL COMMENT '用户名',
                        `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
                        `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
                        `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                        `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0禁用）',
                        `role_id` BIGINT DEFAULT NULL COMMENT '角色ID（RBAC）',
                        `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始化用户数据
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `role_id`)
VALUES
    ('admin', '$2a$10$Suz8CdeNcToWdsVX1ONuWeBpcSNxjQ5gHkVqHY6479X8O49Tv2RE6', '管理员', 'https://example.com/avatar/admin.png', 1),
    ('user', '$2a$10$Suz8CdeNcToWdsVX1ONuWeBpcSNxjQ5gHkVqHY6479X8O49Tv2RE6', '普通用户', 'https://example.com/avatar/user.png', 2);

-- ----------------------------
-- 表结构：用户Token表
-- ----------------------------
DROP TABLE IF EXISTS `user_token`;
CREATE TABLE `user_token` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `user_id` BIGINT NOT NULL,
                              `access_token` VARCHAR(512) NOT NULL,
                              `refresh_token` VARCHAR(512) NOT NULL,
                              `device_type` VARCHAR(32) DEFAULT 'web' COMMENT '设备类型',
                              `expire_time` DATETIME NOT NULL,
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户Token表';

-- ----------------------------
-- 表结构：角色表
-- ----------------------------

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                        `role_name` VARCHAR(64) NOT NULL,
                        `role_code` VARCHAR(64) NOT NULL,
                        `description` VARCHAR(255),
                        `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 初始化角色
INSERT INTO `role` (`role_name`, `role_code`, `description`)
VALUES
    ('管理员', 'ADMIN', '系统最高权限角色'),
    ('普通用户', 'USER', '普通业务用户');

-- ----------------------------
-- 表结构：权限表
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `name` VARCHAR(64) NOT NULL,
                              `code` VARCHAR(64) NOT NULL,
                              `type` TINYINT COMMENT '1菜单 2接口',
                              `path` VARCHAR(255),
                              `method` VARCHAR(16),
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 初始化基础权限
INSERT INTO `permission` (`name`, `code`, `type`, `path`, `method`)
VALUES
    ('用户管理', 'user:manage', 1, '/user', 'GET'),
    ('角色管理', 'role:manage', 1, '/role', 'GET');

-- ----------------------------
-- 表结构：角色权限关联表
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT,
                                   `role_id` BIGINT NOT NULL,
                                   `permission_id` BIGINT NOT NULL,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 管理员拥有全部权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES (1,1),(1,2);
-- 普通用户仅拥有用户管理权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES (2,1);

-- ----------------------------
-- 表结构：登录日志表
-- ----------------------------
DROP TABLE IF EXISTS `user_login_log`;
CREATE TABLE `user_login_log` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `user_id` BIGINT,
                                  `username` VARCHAR(64),
                                  `ip` VARCHAR(64),
                                  `device` VARCHAR(128),
                                  `status` TINYINT COMMENT '1成功 0失败',
                                  `message` VARCHAR(255),
                                  `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- ----------------------------
-- 表结构：第三方登录表
-- ----------------------------
DROP TABLE IF EXISTS `user_oauth`;
CREATE TABLE `user_oauth` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `user_id` BIGINT NOT NULL,
                              `oauth_type` VARCHAR(32) COMMENT 'github/wechat/google',
                              `oauth_id` VARCHAR(128) COMMENT '第三方唯一ID',
                              `access_token` VARCHAR(512),
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_oauth` (`oauth_type`, `oauth_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方登录表';

-- ----------------------------
-- 表结构：用户设备表
-- ----------------------------
DROP TABLE IF EXISTS `user_device`;
CREATE TABLE `user_device` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                               `user_id` BIGINT NOT NULL,
                               `device_id` VARCHAR(128),
                               `device_type` VARCHAR(32),
                               `last_login_time` DATETIME,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设备表';