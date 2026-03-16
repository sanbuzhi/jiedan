-- Feedback Shadow 监控系统数据库初始化脚本
-- 创建项目状态、任务状态、代码上下文、用户反馈等表

-- 项目状态表
CREATE TABLE IF NOT EXISTS project_status (
    project_id VARCHAR(64) PRIMARY KEY COMMENT '项目ID',
    state VARCHAR(50) COMMENT '项目状态: SCAFFOLDING/DEVELOPING/COMPLETED/FAILED',
    current_phase VARCHAR(200) COMMENT '当前阶段描述',
    current_task_id VARCHAR(64) COMMENT '当前执行任务ID',
    completed_tasks TEXT COMMENT '已完成任务ID列表（JSON格式）',
    pending_tasks TEXT COMMENT '待执行任务ID列表（JSON格式）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='项目状态表';

-- 任务状态表
CREATE TABLE IF NOT EXISTS task_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    project_id VARCHAR(64) NOT NULL COMMENT '项目ID',
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    task_name VARCHAR(200) COMMENT '任务名称',
    task_type VARCHAR(50) COMMENT '任务类型: design/frontend/backend/api/test/doc',
    state VARCHAR(50) COMMENT '任务状态: PENDING/IN_PROGRESS/COMPLETED/FAILED',
    priority VARCHAR(10) COMMENT '优先级: P0/P1/P2',
    dependencies TEXT COMMENT '依赖任务ID列表（JSON格式）',
    generated_code_path VARCHAR(500) COMMENT '生成代码的存储路径',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_task_id (task_id),
    INDEX idx_state (state),
    UNIQUE KEY uk_project_task (project_id, task_id)
) COMMENT='任务状态表';

-- 代码上下文表（代码摘要）
CREATE TABLE IF NOT EXISTS code_context (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    project_id VARCHAR(64) NOT NULL COMMENT '项目ID',
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    task_name VARCHAR(200) COMMENT '任务名称',
    class_name VARCHAR(200) COMMENT '类名',
    public_methods TEXT COMMENT 'public方法签名列表（JSON格式）',
    dependencies TEXT COMMENT '依赖的其他类（JSON格式）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_project_id (project_id),
    INDEX idx_task_id (task_id)
) COMMENT='代码上下文表';

-- 用户反馈表
CREATE TABLE IF NOT EXISTS user_feedback (
    feedback_id VARCHAR(64) PRIMARY KEY COMMENT '反馈ID',
    project_id VARCHAR(64) NOT NULL COMMENT '项目ID',
    task_id VARCHAR(64) COMMENT '任务ID（可选）',
    feedback_type VARCHAR(50) COMMENT '反馈类型: CODE_ISSUE/DESIGN_ISSUE/FUNCTION_MISSING/OTHER',
    description TEXT COMMENT '问题描述',
    affected_files TEXT COMMENT '问题文件路径列表（JSON格式）',
    expected_fix TEXT COMMENT '期望的修复方式',
    severity VARCHAR(20) COMMENT '严重程度: CRITICAL/HIGH/MEDIUM/LOW',
    source VARCHAR(20) COMMENT '反馈来源: USER/TEST/AUTO',
    status VARCHAR(20) COMMENT '处理状态: PENDING/PROCESSING/COMPLETED/FAILED',
    repair_result TEXT COMMENT '修复结果',
    repaired_files TEXT COMMENT '修复后的文件路径（JSON格式）',
    repair_attempts INT DEFAULT 0 COMMENT 'AI修复尝试次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (project_id),
    INDEX idx_task_id (task_id),
    INDEX idx_status (status)
) COMMENT='用户反馈表';

-- AI验证记录表（Feedback Shadow验证历史）
CREATE TABLE IF NOT EXISTS ai_validation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    project_id VARCHAR(64) COMMENT '项目ID',
    task_id VARCHAR(64) COMMENT '任务ID',
    validation_type VARCHAR(50) COMMENT '验证类型: DOCUMENT/CODE/SECURITY',
    document_type VARCHAR(50) COMMENT '文档类型: PRD/TASK/CODE/TEST',
    decision VARCHAR(20) COMMENT '决策: ALLOW/REPAIR/REJECT',
    score INT COMMENT '质量评分(0-100)',
    issues TEXT COMMENT '发现的问题（JSON格式）',
    suggestions TEXT COMMENT '改进建议（JSON格式）',
    token_usage INT COMMENT 'Token使用量',
    response_time_ms BIGINT COMMENT '响应时间(毫秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_project_id (project_id),
    INDEX idx_task_id (task_id),
    INDEX idx_decision (decision)
) COMMENT='AI验证记录表';

-- 代码生成记录表
CREATE TABLE IF NOT EXISTS code_generation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    project_id VARCHAR(64) NOT NULL COMMENT '项目ID',
    task_id VARCHAR(64) COMMENT '任务ID',
    generation_type VARCHAR(50) COMMENT '生成类型: SCAFFOLD/INCREMENTAL/REPAIR',
    project_type VARCHAR(50) COMMENT '项目类型: springboot/python/miniprogram',
    status VARCHAR(20) COMMENT '状态: SUCCESS/FAILED/PARTIAL',
    files_generated INT COMMENT '生成文件数量',
    files_path TEXT COMMENT '文件路径列表（JSON格式）',
    compilation_passed BOOLEAN COMMENT '编译是否通过',
    compilation_errors TEXT COMMENT '编译错误（JSON格式）',
    token_usage INT COMMENT 'Token使用量',
    response_time_ms BIGINT COMMENT '响应时间(毫秒)',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_project_id (project_id),
    INDEX idx_task_id (task_id),
    INDEX idx_generation_type (generation_type)
) COMMENT='代码生成记录表';
