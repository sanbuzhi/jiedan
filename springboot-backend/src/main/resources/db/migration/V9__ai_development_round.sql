-- AI异步阶段式开发 - 轮次表
-- V9__ai_development_round.sql

CREATE TABLE IF NOT EXISTS ai_development_round (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phase_id BIGINT NOT NULL COMMENT '阶段ID',
    round_number INT NOT NULL COMMENT '轮次号',
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' COMMENT '状态: PROCESSING/COMPLETED/FAILED',
    input_prompt LONGTEXT COMMENT '输入Prompt',
    output_content LONGTEXT COMMENT '输出内容',
    tokens_used INT DEFAULT 0 COMMENT '消耗Token',
    files_count INT DEFAULT 0 COMMENT '生成文件数',
    continuation INT DEFAULT 0 COMMENT '续传次数',
    finish_reason VARCHAR(20) COMMENT '结束原因: length/stop/error',
    error_message VARCHAR(1000) COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    completed_at DATETIME COMMENT '完成时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_phase_id (phase_id),
    INDEX idx_round (phase_id, round_number),
    FOREIGN KEY (phase_id) REFERENCES ai_development_phase(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI开发轮次表';
