-- AI异步阶段式开发 - 项目表
-- V7__ai_development_project.sql

CREATE TABLE IF NOT EXISTS ai_development_project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL UNIQUE COMMENT '项目ID',
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    task_doc LONGTEXT COMMENT '任务书内容',
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT '状态: NOT_STARTED/PROCESSING/COMPLETED/FAILED/PAUSED',
    current_phase INT DEFAULT 1 COMMENT '当前阶段',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    total_files INT DEFAULT 0 COMMENT '总文件数',
    error_message VARCHAR(1000) COMMENT '错误信息',
    started_at DATETIME COMMENT '开始时间',
    completed_at DATETIME COMMENT '完成时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI开发项目表';
