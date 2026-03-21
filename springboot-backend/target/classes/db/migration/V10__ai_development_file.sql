-- AI异步阶段式开发 - 生成文件表
-- V10__ai_development_file.sql

CREATE TABLE IF NOT EXISTS ai_development_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL COMMENT '项目ID',
    phase INT NOT NULL COMMENT '阶段号',
    round_number INT NOT NULL COMMENT '轮次号',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小',
    file_type VARCHAR(20) COMMENT '文件类型',
    is_complete TINYINT DEFAULT 1 COMMENT '是否完整: 0否/1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_project_phase (project_id, phase),
    INDEX idx_file_path (file_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI开发生成文件表';
