-- AI异步阶段式开发 - 阶段表
-- V8__ai_development_phase.sql

CREATE TABLE IF NOT EXISTS ai_development_phase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL COMMENT '项目ID',
    phase INT NOT NULL COMMENT '阶段号',
    phase_name VARCHAR(100) COMMENT '阶段名称',
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT '状态: NOT_STARTED/PROCESSING/COMPLETED/FAILED',
    total_rounds INT DEFAULT 0 COMMENT '总轮次数',
    total_files INT DEFAULT 0 COMMENT '生成文件数',
    session_id VARCHAR(100) COMMENT 'AI会话ID',
    summary LONGTEXT COMMENT '阶段摘要',
    error_message VARCHAR(1000) COMMENT '错误信息',
    started_at DATETIME COMMENT '开始时间',
    completed_at DATETIME COMMENT '完成时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_phase (project_id, phase),
    INDEX idx_status (status),
    FOREIGN KEY (project_id) REFERENCES ai_development_project(project_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI开发阶段表';
