-- 创建数据库
CREATE DATABASE IF NOT EXISTS task_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE task_management;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(128) NOT NULL COMMENT '微信OpenID',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '用户昵称',
    `avatar_url` VARCHAR(256) DEFAULT NULL COMMENT '用户头像',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 任务表
CREATE TABLE IF NOT EXISTS `task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(128) NOT NULL COMMENT '任务标题',
    `content` TEXT DEFAULT NULL COMMENT '任务内容',
    `priority` TINYINT NOT NULL DEFAULT 1 COMMENT '优先级：1-低，2-中，3-高',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未完成，1-已完成',
    `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';