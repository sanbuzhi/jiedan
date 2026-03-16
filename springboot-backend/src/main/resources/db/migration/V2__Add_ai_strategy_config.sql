-- 创建AI策略配置表
CREATE TABLE IF NOT EXISTS ai_strategy_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    api_code VARCHAR(50) NOT NULL UNIQUE COMMENT '接口代码，唯一标识',
    api_name VARCHAR(100) NOT NULL COMMENT '接口名称',
    provider VARCHAR(50) NOT NULL COMMENT 'AI提供商',
    model VARCHAR(100) NOT NULL COMMENT 'AI模型',
    temperature DECIMAL(3,2) NOT NULL DEFAULT 0.70 COMMENT '温度参数',
    max_tokens INT NOT NULL DEFAULT 2000 COMMENT '最大token数',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    description TEXT COMMENT '接口描述',
    icon VARCHAR(50) COMMENT '图标名称',
    sort_order INT COMMENT '排序顺序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI策略配置表';

-- 创建索引
CREATE INDEX idx_ai_strategy_config_api_code ON ai_strategy_config(api_code);
CREATE INDEX idx_ai_strategy_config_enabled ON ai_strategy_config(enabled);

-- 插入默认配置
INSERT INTO ai_strategy_config (api_code, api_name, provider, model, temperature, max_tokens, enabled, description, icon, sort_order) VALUES
('clarify-requirement', 'AI明确需求', 'huoshan', 'doubao-seed-2.0-code', 0.70, 2000, 1, '分析并完善用户需求描述，输出需求概述、功能模块清单、用户角色定义等', 'ChatDotRound', 1),
('split-tasks', 'AI拆分任务', 'huoshan', 'doubao-seed-2.0-code', 0.70, 2000, 1, '将需求拆分为可执行的具体任务，包含任务名称、描述、预估工时、优先级等', 'List', 2),
('generate-code', 'AI开发', 'huoshan', 'doubao-seed-2.0-code', 0.30, 3000, 1, '基于需求生成高质量的代码实现，包含完整的类定义和业务方法', 'Document', 3),
('functional-test', 'AI功能测试', 'huoshan', 'doubao-seed-2.0-code', 0.50, 2500, 1, '为代码生成功能测试用例，包含正常场景、边界条件、异常场景测试', 'FirstAidKit', 4),
('security-test', 'AI安全测试', 'huoshan', 'doubao-seed-2.0-code', 0.50, 2500, 1, '扫描代码中的安全漏洞，输出漏洞类型、严重程度、修复建议等', 'Warning', 5);
