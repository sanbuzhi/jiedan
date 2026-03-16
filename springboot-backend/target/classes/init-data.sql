-- ============================================
-- 完整数据初始化脚本
-- 用户ID: 1, 数据库账号: root/root
-- ============================================

-- ============================================
-- 阶段1: 创建缺失的表
-- ============================================

-- 积分兑换商品表
CREATE TABLE IF NOT EXISTS exchange_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    image_url VARCHAR(500) COMMENT '商品图片URL',
    points_required INT NOT NULL COMMENT '所需积分',
    cash_price DECIMAL(10, 2) DEFAULT 0.00 COMMENT '现金价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否上架',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 兑换订单表
CREATE TABLE IF NOT EXISTS exchange_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    item_id BIGINT NOT NULL COMMENT '商品ID',
    points_used INT NOT NULL COMMENT '使用积分',
    cash_paid DECIMAL(10, 2) DEFAULT 0.00 COMMENT '支付现金',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 积分规则表
CREATE TABLE IF NOT EXISTS rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则代码',
    description TEXT COMMENT '规则描述',
    type VARCHAR(50) NOT NULL COMMENT '类型',
    points INT NOT NULL COMMENT '积分值',
    max_level INT NOT NULL DEFAULT 3 COMMENT '最大层级',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否激活',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 积分记录表
CREATE TABLE IF NOT EXISTS point_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '类型',
    amount INT NOT NULL COMMENT '变动金额',
    balance INT NOT NULL COMMENT '变动后余额',
    description TEXT COMMENT '描述',
    related_user_id BIGINT COMMENT '关联用户ID',
    rule_id BIGINT COMMENT '规则ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统模板表
CREATE TABLE IF NOT EXISTS system_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '模板代码',
    description VARCHAR(500) COMMENT '描述',
    category VARCHAR(50) COMMENT '分类',
    keywords VARCHAR(500) COMMENT '关键词',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统角色表
CREATE TABLE IF NOT EXISTS system_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL COMMENT '角色代码',
    description VARCHAR(500) COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 阶段2: 插入积分规则
-- ============================================
INSERT INTO rules (id, name, code, description, type, points, max_level, is_active) VALUES
(1, '注册奖励', 'REGISTER_REWARD', '新用户注册奖励积分', 'REGISTER', 50, 1, true),
(2, '一级推荐奖励', 'REFERRAL_LEVEL1', '直接推荐用户注册奖励', 'REFERRAL', 100, 3, true),
(3, '二级推荐奖励', 'REFERRAL_LEVEL2', '间接推荐用户注册奖励', 'REFERRAL', 50, 3, true),
(4, '三级推荐奖励', 'REFERRAL_LEVEL3', '三级推荐用户注册奖励', 'REFERRAL', 30, 3, true)
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- 阶段3: 插入兑换商品（使用网络图片）
-- ============================================
INSERT INTO exchange_items (id, name, description, image_url, points_required, cash_price, stock, is_active) VALUES
(1, '系统开发抵扣券', '可用于抵扣系统开发费用，100积分=1元', 'https://picsum.photos/400/300?random=1', 1000, 0.00, 100, true),
(2, '功能改造抵扣券', '可用于抵扣功能改造费用，100积分=1元', 'https://picsum.photos/400/300?random=2', 500, 0.00, 100, true),
(3, '优先服务券', '享受优先技术支持服务', 'https://picsum.photos/400/300?random=3', 200, 0.00, 50, true),
(4, '高级功能包', '解锁高级功能，积分抵扣后需支付差价', 'https://picsum.photos/400/300?random=4', 500, 99.00, 20, true),
(5, 'VIP会员月卡', '享受VIP会员特权一个月', 'https://picsum.photos/400/300?random=5', 300, 0.00, 200, true),
(6, '技术支持券', '获得一次专业技术支持服务', 'https://picsum.photos/400/300?random=6', 150, 0.00, 100, true)
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- 阶段4: 创建推荐团队（三级推荐结构）
-- ============================================

-- 一级推荐用户（2个）
INSERT INTO users (id, phone, nickname, avatar, referral_code, referrer_id, total_points, is_active, created_at, updated_at) VALUES
(2, 'oWGaF1-TEST002', '张三', 'https://picsum.photos/100/100?random=10', 'A1B2C3', 1, 0, true, NOW(), NOW()),
(3, 'oWGaF1-TEST003', '李四', 'https://picsum.photos/100/100?random=11', 'D4E5F6', 1, 0, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 二级推荐用户（2个，分别是张三和李四的推荐）
INSERT INTO users (id, phone, nickname, avatar, referral_code, referrer_id, total_points, is_active, created_at, updated_at) VALUES
(4, 'oWGaF1-TEST004', '王五', 'https://picsum.photos/100/100?random=12', 'G7H8I9', 2, 0, true, NOW(), NOW()),
(5, 'oWGaF1-TEST005', '赵六', 'https://picsum.photos/100/100?random=13', 'J0K1L2', 3, 0, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 三级推荐用户（2个）
INSERT INTO users (id, phone, nickname, avatar, referral_code, referrer_id, total_points, is_active, created_at, updated_at) VALUES
(6, 'oWGaF1-TEST006', '孙七', 'https://picsum.photos/100/100?random=14', 'M3N4O5', 4, 0, true, NOW(), NOW()),
(7, 'oWGaF1-TEST007', '周八', 'https://picsum.photos/100/100?random=15', 'P6Q7R8', 5, 0, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- 阶段5: 为用户1插入积分记录（正确计算）
-- ============================================
-- 先清空用户1的旧积分记录
DELETE FROM point_records WHERE user_id = 1;

-- 插入新的积分记录（按时间顺序，正确计算余额）
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at) VALUES
(1, 'REGISTER', 50, 50, '新用户注册奖励', NULL, 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'SIGNIN', 10, 60, '每日签到奖励', NULL, NULL, DATE_SUB(NOW(), INTERVAL 29 DAY)),
(1, 'SIGNIN', 10, 70, '每日签到奖励', NULL, NULL, DATE_SUB(NOW(), INTERVAL 28 DAY)),
(1, 'TASK', 50, 120, '完成新手任务', NULL, NULL, DATE_SUB(NOW(), INTERVAL 27 DAY)),
(1, 'REFERRAL', 100, 220, '推荐用户注册奖励（一级）- 张三', 2, 2, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, 'REFERRAL', 100, 320, '推荐用户注册奖励（一级）- 李四', 3, 2, DATE_SUB(NOW(), INTERVAL 24 DAY)),
(1, 'REFERRAL', 50, 370, '推荐用户注册奖励（二级）- 王五', 4, 3, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(1, 'REFERRAL', 50, 420, '推荐用户注册奖励（二级）- 赵六', 5, 3, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(1, 'REFERRAL', 30, 450, '推荐用户注册奖励（三级）- 孙七', 6, 4, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(1, 'REFERRAL', 30, 480, '推荐用户注册奖励（三级）- 周八', 7, 4, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(1, 'ACTIVITY', 200, 680, '参与活动奖励', NULL, NULL, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 'SIGNIN', 20, 700, '连续签到7天奖励', NULL, NULL, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1, 'TASK', 100, 800, '完成资料完善任务', NULL, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1, 'BONUS', 50, 850, '系统赠送积分', NULL, NULL, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1, 'SIGNIN', 10, 860, '每日签到奖励', NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 'SIGNIN', 10, 870, '每日签到奖励', NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 'EXCHANGE', -500, 370, '兑换商品：功能改造抵扣券', NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'SIGNIN', 10, 380, '每日签到奖励', NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'SIGNIN', 10, 390, '每日签到奖励', NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'SIGNIN', 10, 400, '每日签到奖励', NULL, NULL, NOW());

-- ============================================
-- 阶段6: 更新用户1的总积分
-- ============================================
UPDATE users SET total_points = 400, updated_at = NOW() WHERE id = 1;

-- ============================================
-- 阶段7: 创建兑换订单
-- ============================================
INSERT INTO exchange_orders (id, user_id, item_id, points_used, cash_paid, status, remark, created_at, updated_at) VALUES
(1, 1, 2, 500, 0.00, 'COMPLETED', '兑换功能改造抵扣券', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 1, 3, 200, 0.00, 'COMPLETED', '兑换优先服务券', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 1, 1, 1000, 0.00, 'PENDING', '申请兑换系统开发抵扣券', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- 阶段8: 插入系统模板数据（支持酒店管理系统搜索）
-- ============================================
INSERT INTO system_templates (id, name, code, description, category, keywords, is_active) VALUES
(1, '酒店管理系统', 'HOTEL_MGMT', '完整的酒店客房管理、预订、入住、结算系统', '酒店旅游', '酒店,客房,预订,入住,管理系统', true),
(2, '连锁酒店管理平台', 'CHAIN_HOTEL', '支持多门店连锁经营的酒店管理系统', '酒店旅游', '连锁,酒店,多门店,管理,系统', true),
(3, '民宿预订系统', 'HOMESTAY', '民宿短租预订管理平台', '酒店旅游', '民宿,短租,预订,管理,系统', true),
(4, '餐饮管理系统', 'RESTAURANT_MGMT', '餐厅点餐、库存、会员管理系统', '餐饮服务', '餐厅,点餐,库存,会员,管理', true),
(5, '库存管理系统', 'INVENTORY_MGMT', '通用库存进销存管理系统', '企业管理', '库存,进销存,管理,系统', true),
(6, '会员管理系统', 'MEMBER_MGMT', '会员积分、等级、营销管理系统', '企业管理', '会员,积分,等级,营销,管理', true),
(7, '酒店CRM系统', 'HOTEL_CRM', '酒店客户关系管理系统', '酒店旅游', '酒店,CRM,客户,关系,管理', true),
(8, '智能酒店系统', 'SMART_HOTEL', 'IoT智能酒店客房控制系统', '酒店旅游', '智能,酒店,IoT,客房,控制', true)
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- 阶段9: 插入系统角色数据
-- ============================================
INSERT INTO system_roles (id, template_id, name, code, description, sort_order) VALUES
(1, 1, '系统管理员', 'ADMIN', '拥有系统所有权限', 1),
(2, 1, '前台接待', 'RECEPTIONIST', '负责客房预订和入住办理', 2),
(3, 1, '客房服务员', 'HOUSEKEEPING', '负责客房清洁和维护', 3),
(4, 1, '财务主管', 'FINANCE', '负责账务管理和报表', 4),
(5, 1, '酒店经理', 'MANAGER', '负责日常运营管理', 5),
(6, 2, '总部管理员', 'HQ_ADMIN', '连锁总部系统管理', 1),
(7, 2, '门店经理', 'STORE_MANAGER', '单店经营管理', 2),
(8, 3, '房东', 'HOST', '民宿房源管理', 1),
(9, 3, '房客', 'GUEST', '预订住宿用户', 2),
(10, 7, '销售经理', 'SALES', '客户开发和维护', 1),
(11, 7, '客服专员', 'CS', '客户服务支持', 2)
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- 验证数据
-- ============================================
SELECT '用户1积分' as check_item, total_points as value FROM users WHERE id = 1
UNION ALL
SELECT '积分记录数', COUNT(*) FROM point_records WHERE user_id = 1
UNION ALL
SELECT '一级推荐数', COUNT(*) FROM users WHERE referrer_id = 1
UNION ALL
SELECT '二级推荐数', COUNT(*) FROM users WHERE referrer_id IN (SELECT id FROM users WHERE referrer_id = 1)
UNION ALL
SELECT '三级推荐数', COUNT(*) FROM users WHERE referrer_id IN (SELECT id FROM users WHERE referrer_id IN (SELECT id FROM users WHERE referrer_id = 1))
UNION ALL
SELECT '兑换商品数', COUNT(*) FROM exchange_items WHERE is_active = true
UNION ALL
SELECT '兑换订单数', COUNT(*) FROM exchange_orders WHERE user_id = 1
UNION ALL
SELECT '系统模板数', COUNT(*) FROM system_templates WHERE is_active = true;
