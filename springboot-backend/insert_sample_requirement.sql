-- 插入示例项目数据
-- 注意：请先确保有用户数据，这里假设用户ID为1

INSERT INTO requirements (
    user_id,
    user_type,
    user_type_other,
    project_type,
    project_type_other,
    need_online,
    traffic,
    urgency,
    delivery_period,
    ui_style,
    status,
    budget_calculated,
    step_data,
    flow_node_status,
    current_flow_node,
    created_at,
    updated_at
) VALUES (
    1,
    '个人',
    NULL,
    '微信小程序',
    NULL,
    true,
    '{"totalUsers": 5000, "dau": 500, "concurrent": 100}',
    'NORMAL',
    14,
    '现代简约',
    'PROCESSING',
    '{"aiDevelopmentFee": 5000, "platformServiceFee": 1000, "totalBudget": 6000}',
    '{"step1": {"userType": "个人", "projectType": "微信小程序"}, "step2": {"needOnline": true, "traffic": {"totalUsers": 5000}}, "step3": {"urgency": "NORMAL", "deliveryPeriod": 14}}',
    '[
        {"title": "需求提交", "desc": "已提交需求", "status": "completed"},
        {"title": "需求审核", "desc": "审核通过", "status": "completed"},
        {"title": "方案设计", "desc": "正在进行", "status": "active"},
        {"title": "开发实现", "desc": "等待中", "status": "pending"},
        {"title": "测试验收", "desc": "等待中", "status": "pending"},
        {"title": "上线部署", "desc": "等待中", "status": "pending"},
        {"title": "项目交付", "desc": "等待中", "status": "pending"}
    ]',
    2,
    NOW(),
    NOW()
);

-- 插入第二个项目（已完成）
INSERT INTO requirements (
    user_id,
    user_type,
    user_type_other,
    project_type,
    project_type_other,
    need_online,
    traffic,
    urgency,
    delivery_period,
    ui_style,
    status,
    budget_calculated,
    step_data,
    flow_node_status,
    current_flow_node,
    created_at,
    updated_at
) VALUES (
    1,
    '企业',
    '科技公司',
    'H5页面',
    NULL,
    false,
    '{"totalUsers": 10000, "dau": 1000, "concurrent": 200}',
    'URGENT',
    7,
    '商务风格',
    'COMPLETED',
    '{"aiDevelopmentFee": 7500, "platformServiceFee": 1500, "totalBudget": 9000}',
    '{"step1": {"userType": "企业", "projectType": "H5页面"}, "step2": {"needOnline": false}, "step3": {"urgency": "URGENT", "deliveryPeriod": 7}}',
    '[
        {"title": "需求提交", "desc": "已提交需求", "status": "completed"},
        {"title": "需求审核", "desc": "审核通过", "status": "completed"},
        {"title": "方案设计", "desc": "已完成", "status": "completed"},
        {"title": "开发实现", "desc": "已完成", "status": "completed"},
        {"title": "测试验收", "desc": "已完成", "status": "completed"},
        {"title": "打包交付", "desc": "已完成", "status": "completed"},
        {"title": "项目交付", "desc": "已完成", "status": "completed"}
    ]',
    6,
    DATE_SUB(NOW(), INTERVAL 30 DAY),
    DATE_SUB(NOW(), INTERVAL 5 DAY)
);

-- 插入第三个项目（草稿状态）
INSERT INTO requirements (
    user_id,
    user_type,
    user_type_other,
    project_type,
    project_type_other,
    need_online,
    traffic,
    urgency,
    delivery_period,
    ui_style,
    status,
    budget_calculated,
    step_data,
    flow_node_status,
    current_flow_node,
    created_at,
    updated_at
) VALUES (
    1,
    '个人',
    NULL,
    '支付宝小程序',
    NULL,
    true,
    '{"totalUsers": 3000, "dau": 300, "concurrent": 50}',
    'NORMAL',
    21,
    '清新风格',
    'DRAFT',
    NULL,
    '{"step1": {"userType": "个人", "projectType": "支付宝小程序"}}',
    '[
        {"title": "需求提交", "desc": "草稿", "status": "active"},
        {"title": "需求审核", "desc": "等待中", "status": "pending"},
        {"title": "方案设计", "desc": "等待中", "status": "pending"},
        {"title": "开发实现", "desc": "等待中", "status": "pending"},
        {"title": "测试验收", "desc": "等待中", "status": "pending"},
        {"title": "上线部署", "desc": "等待中", "status": "pending"},
        {"title": "项目交付", "desc": "等待中", "status": "pending"}
    ]',
    0,
    NOW(),
    NOW()
);
