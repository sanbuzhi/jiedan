-- 创建系统架构模板表
CREATE TABLE IF NOT EXISTS system_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '模板代码',
    description TEXT COMMENT '模板描述',
    keywords JSON COMMENT '关键词',
    category VARCHAR(50) COMMENT '分类',
    complexity_level VARCHAR(20) COMMENT '复杂度级别',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统架构模板库';

-- 创建业务角色表
CREATE TABLE IF NOT EXISTS system_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL COMMENT '角色代码',
    description TEXT COMMENT '角色描述',
    responsibilities TEXT COMMENT '职责说明',
    sort_order INT COMMENT '排序顺序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (template_id) REFERENCES system_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务角色库';

-- 创建功能点表
CREATE TABLE IF NOT EXISTS system_functions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    name VARCHAR(100) NOT NULL COMMENT '功能名称',
    code VARCHAR(50) NOT NULL COMMENT '功能代码',
    description TEXT COMMENT '功能描述',
    complexity VARCHAR(20) COMMENT '复杂度(LOW/MEDIUM/HIGH)',
    estimated_hours INT COMMENT '预估工时',
    base_price DECIMAL(10, 2) COMMENT '基础价格',
    priority INT COMMENT '优先级',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (role_id) REFERENCES system_roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='功能点库';

-- 创建需求功能关联表
CREATE TABLE IF NOT EXISTS requirement_functions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL COMMENT '需求ID',
    function_id BIGINT COMMENT '功能点ID',
    is_custom TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自定义',
    custom_name VARCHAR(100) COMMENT '自定义名称',
    custom_description TEXT COMMENT '自定义描述',
    estimated_hours INT COMMENT '预估工时',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (requirement_id) REFERENCES requirements(id) ON DELETE CASCADE,
    FOREIGN KEY (function_id) REFERENCES system_functions(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求功能关联表';

-- 插入酒店PMS系统模板数据
INSERT INTO system_templates (name, code, description, keywords, category, complexity_level, is_active) VALUES
('酒店PMS系统', 'HOTEL_PMS', '酒店物业管理系统，提供前台管理、客房服务、财务管理等核心功能', '["酒店", "PMS", "前台", "客房", "财务"]', '酒店旅游', 'HIGH', 1);

SET @template_id = LAST_INSERT_ID();

-- 插入管理员角色
INSERT INTO system_roles (template_id, name, code, description, responsibilities, sort_order) VALUES
(@template_id, '系统管理员', 'ADMIN', '系统管理员，负责系统配置、用户管理、权限管理等', '系统配置管理、用户权限管理、数据备份与恢复、系统监控', 1);

SET @admin_role_id = LAST_INSERT_ID();

-- 插入管理员功能点
INSERT INTO system_functions (role_id, name, code, description, complexity, estimated_hours, base_price, priority) VALUES
(@admin_role_id, '用户管理', 'USER_MGMT', '创建、编辑、删除系统用户，分配角色权限', 'MEDIUM', 16, 3200.00, 1),
(@admin_role_id, '角色权限管理', 'ROLE_MGMT', '配置角色权限，定义功能访问控制', 'HIGH', 24, 4800.00, 2),
(@admin_role_id, '系统配置', 'SYS_CONFIG', '系统参数配置，包括基础设置、业务规则配置', 'MEDIUM', 12, 2400.00, 3),
(@admin_role_id, '数据备份', 'DATA_BACKUP', '数据库备份与恢复功能', 'MEDIUM', 16, 3200.00, 4),
(@admin_role_id, '操作日志', 'OPERATION_LOG', '查看系统操作日志，审计追踪', 'LOW', 8, 1600.00, 5),
(@admin_role_id, '报表中心', 'REPORT_CENTER', '生成各类系统运营报表', 'HIGH', 32, 6400.00, 6);

-- 插入前台角色
INSERT INTO system_roles (template_id, name, code, description, responsibilities, sort_order) VALUES
(@template_id, '前台接待', 'RECEPTION', '负责客人入住登记、退房结算、预订管理等前台业务', '客人入住登记、退房结算、预订管理、客人咨询、房态管理', 2);

SET @reception_role_id = LAST_INSERT_ID();

-- 插入前台功能点
INSERT INTO system_functions (role_id, name, code, description, complexity, estimated_hours, base_price, priority) VALUES
(@reception_role_id, '入住登记', 'CHECK_IN', '客人入住登记，身份证读取，房卡制作', 'MEDIUM', 16, 3200.00, 1),
(@reception_role_id, '退房结算', 'CHECK_OUT', '退房结算，账单生成，支付处理', 'MEDIUM', 12, 2400.00, 2),
(@reception_role_id, '预订管理', 'RESERVATION', '客房预订、修改、取消，预订查询', 'HIGH', 24, 4800.00, 3),
(@reception_role_id, '房态管理', 'ROOM_STATUS', '实时房态查看与更新，房态图展示', 'MEDIUM', 20, 4000.00, 4),
(@reception_role_id, '客人信息管理', 'GUEST_MGMT', '客人信息录入、查询、历史记录', 'LOW', 12, 2400.00, 5),
(@reception_role_id, 'Walk-in入住', 'WALK_IN', '无预订直接入住处理', 'LOW', 8, 1600.00, 6),
(@reception_role_id, '团队入住', 'GROUP_CHECKIN', '团队客人批量入住处理', 'HIGH', 20, 4000.00, 7),
(@reception_role_id, '换房/续住', 'ROOM_CHANGE', '客人换房、续住业务处理', 'MEDIUM', 12, 2400.00, 8);

-- 插入客房服务角色
INSERT INTO system_roles (template_id, name, code, description, responsibilities, sort_order) VALUES
(@template_id, '客房服务', 'HOUSEKEEPING', '负责客房清洁、维修管理、物品补充等客房服务', '客房清洁管理、维修工单处理、物品库存管理、客房检查', 3);

SET @housekeeping_role_id = LAST_INSERT_ID();

-- 插入客房服务功能点
INSERT INTO system_functions (role_id, name, code, description, complexity, estimated_hours, base_price, priority) VALUES
(@housekeeping_role_id, '清洁任务管理', 'CLEANING_TASK', '客房清洁任务分配、进度跟踪、质量检查', 'MEDIUM', 16, 3200.00, 1),
(@housekeeping_role_id, '维修工单', 'MAINTENANCE', '客房设施设备维修申请与跟踪', 'MEDIUM', 16, 3200.00, 2),
(@housekeeping_role_id, '物品库存', 'INVENTORY', '客房用品库存管理，补充提醒', 'LOW', 12, 2400.00, 3),
(@housekeeping_role_id, '迷你吧管理', 'MINIBAR', '迷你吧消费记录，补充管理', 'MEDIUM', 12, 2400.00, 4),
(@housekeeping_role_id, '客房状态更新', 'ROOM_STATUS_UPDATE', '清洁完成后房态更新', 'LOW', 8, 1600.00, 5),
(@housekeeping_role_id, '失物招领', 'LOST_FOUND', '客人失物登记与招领管理', 'LOW', 8, 1600.00, 6);

-- 插入财务角色
INSERT INTO system_roles (template_id, name, code, description, responsibilities, sort_order) VALUES
(@template_id, '财务管理', 'FINANCE', '负责酒店财务报表、收银对账、成本核算等财务业务', '收银对账、财务报表、成本核算、应收应付管理、夜审', 4);

SET @finance_role_id = LAST_INSERT_ID();

-- 插入财务功能点
INSERT INTO system_functions (role_id, name, code, description, complexity, estimated_hours, base_price, priority) VALUES
(@finance_role_id, '收银对账', 'CASHIER_RECON', '前台收银对账，现金、刷卡、移动支付核对', 'MEDIUM', 16, 3200.00, 1),
(@finance_role_id, '夜审功能', 'NIGHT_AUDIT', '夜间审计，日结处理，报表生成', 'HIGH', 24, 4800.00, 2),
(@finance_role_id, '财务报表', 'FINANCIAL_REPORT', '收入报表、成本报表、利润分析', 'HIGH', 32, 6400.00, 3),
(@finance_role_id, '应收应付', 'AR_AP', '应收账款、应付账款管理', 'MEDIUM', 20, 4000.00, 4),
(@finance_role_id, '发票管理', 'INVOICE_MGMT', '发票开具、红冲、查询管理', 'MEDIUM', 16, 3200.00, 5),
(@finance_role_id, '佣金管理', 'COMMISSION', 'OTA佣金计算与对账', 'MEDIUM', 16, 3200.00, 6),
(@finance_role_id, '预算管理', 'BUDGET_MGMT', '年度/月度预算编制与执行跟踪', 'HIGH', 24, 4800.00, 7);

-- 创建索引
CREATE INDEX idx_system_roles_template_id ON system_roles(template_id);
CREATE INDEX idx_system_functions_role_id ON system_functions(role_id);
CREATE INDEX idx_requirement_functions_requirement_id ON requirement_functions(requirement_id);
CREATE INDEX idx_requirement_functions_function_id ON requirement_functions(function_id);
