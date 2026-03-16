-- ============================================
-- 数据库初始化数据脚本
-- 用户ID: 1, 推荐码: 52F236
-- ============================================

-- ============================================
-- 阶段1: 基础数据
-- ============================================

-- 1.1 积分规则表 (rules)
INSERT INTO rules (name, code, description, type, points, max_level, is_active, created_at, updated_at)
VALUES ('注册奖励', 'REGISTER_REWARD', '新用户注册奖励积分', 'REGISTER', 50, 1, true, NOW(), NOW());

INSERT INTO rules (name, code, description, type, points, max_level, is_active, created_at, updated_at)
VALUES ('一级推荐奖励', 'REFERRAL_LEVEL1', '直接推荐用户注册奖励', 'REFERRAL', 100, 3, true, NOW(), NOW());

INSERT INTO rules (name, code, description, type, points, max_level, is_active, created_at, updated_at)
VALUES ('二级推荐奖励', 'REFERRAL_LEVEL2', '间接推荐用户注册奖励', 'REFERRAL', 50, 3, true, NOW(), NOW());

INSERT INTO rules (name, code, description, type, points, max_level, is_active, created_at, updated_at)
VALUES ('三级推荐奖励', 'REFERRAL_LEVEL3', '三级推荐用户注册奖励', 'REFERRAL', 30, 3, true, NOW(), NOW());

-- 1.2 积分兑换商品表 (exchange_items)
INSERT INTO exchange_items (name, description, image_url, points_required, cash_price, stock, is_active, created_at, updated_at)
VALUES (
    '系统开发抵扣券',
    '可用于抵扣系统开发费用，100积分=1元',
    '/images/coupon-dev.png',
    1000,
    0.00,
    100,
    true,
    NOW(),
    NOW()
);

INSERT INTO exchange_items (name, description, image_url, points_required, cash_price, stock, is_active, created_at, updated_at)
VALUES (
    '功能改造抵扣券',
    '可用于抵扣功能改造费用，100积分=1元',
    '/images/coupon-feature.png',
    500,
    0.00,
    100,
    true,
    NOW(),
    NOW()
);

INSERT INTO exchange_items (name, description, image_url, points_required, cash_price, stock, is_active, created_at, updated_at)
VALUES (
    '优先服务券',
    '享受优先技术支持服务',
    '/images/coupon-priority.png',
    200,
    0.00,
    50,
    true,
    NOW(),
    NOW()
);

INSERT INTO exchange_items (name, description, image_url, points_required, cash_price, stock, is_active, created_at, updated_at)
VALUES (
    '高级功能包',
    '解锁高级功能，积分抵扣后需支付差价',
    '/images/package-premium.png',
    500,
    99.00,
    20,
    true,
    NOW(),
    NOW()
);

-- ============================================
-- 阶段2: 为用户1构造测试数据
-- ============================================

-- 2.1 给用户1添加积分记录（增加积分）
-- 注册奖励积分记录
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'REGISTER', 50, 50, '新用户注册奖励', NULL, 1, NOW());

-- 模拟推荐奖励积分记录（一级推荐）- 需要用户2先存在
-- 注意：以下记录依赖阶段3创建的测试用户
-- INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
-- VALUES (1, 'REFERRAL', 100, 150, '推荐用户注册奖励（一级）', 2, 2, DATE_ADD(NOW(), INTERVAL 5 MINUTE));

-- 模拟更多积分收入
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'ACTIVITY', 200, 250, '参与活动奖励', NULL, NULL, DATE_ADD(NOW(), INTERVAL 1 HOUR));

INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'SIGNIN', 50, 300, '每日签到奖励', NULL, NULL, DATE_ADD(NOW(), INTERVAL 2 HOUR));

INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'TASK', 100, 400, '完成任务奖励', NULL, NULL, DATE_ADD(NOW(), INTERVAL 3 HOUR));

INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'BONUS', 30, 430, '系统赠送积分', NULL, NULL, DATE_ADD(NOW(), INTERVAL 4 HOUR));

-- 2.2 更新用户1的积分总额
UPDATE users SET total_points = 430, updated_at = NOW() WHERE id = 1;

-- ============================================
-- 阶段3: 构造推荐关系树
-- ============================================

-- 3.1 创建测试用户（用于构建推荐树）
-- 创建二级推荐用户（被用户1直接推荐）
INSERT INTO users (phone, nickname, avatar, referral_code, referrer_id, total_points, is_active, created_at, updated_at)
VALUES (
    'oWGaF1-TEST002',
    '测试用户2',
    'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132',
    'A1B2C3',
    1,
    0,
    true,
    NOW(),
    NOW()
);

-- 创建三级推荐用户（被用户2推荐，用户1的二级推荐）
INSERT INTO users (phone, nickname, avatar, referral_code, referrer_id, total_points, is_active, created_at, updated_at)
VALUES (
    'oWGaF1-TEST003',
    '测试用户3',
    'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132',
    'D4E5F6',
    2,
    0,
    true,
    NOW(),
    NOW()
);

-- 创建四级推荐用户（被用户3推荐，用户1的三级推荐）
INSERT INTO users (phone, nickname, avatar, referral_code, referrer_id, total_points, is_active, created_at, updated_at)
VALUES (
    'oWGaF1-TEST004',
    '测试用户4',
    'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132',
    'G7H8I9',
    3,
    0,
    true,
    NOW(),
    NOW()
);

-- 3.2 创建推荐关系
-- 用户2是用户1的一级推荐
INSERT INTO referral_relationships (user_id, referrer_id, level, created_at)
VALUES (2, 1, 1, NOW());

-- 用户3是用户1的二级推荐（通过用户2）
INSERT INTO referral_relationships (user_id, referrer_id, level, created_at)
VALUES (3, 1, 2, NOW());

-- 用户4是用户1的三级推荐（通过用户2->用户3）
INSERT INTO referral_relationships (user_id, referrer_id, level, created_at)
VALUES (4, 1, 3, NOW());

-- 添加推荐积分记录（现在用户2/3/4已存在）
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'REFERRAL', 100, 150, '推荐用户注册奖励（一级）', 2, 2, DATE_ADD(NOW(), INTERVAL 5 MINUTE));

INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'REFERRAL', 50, 200, '推荐用户注册奖励（二级）', 3, 3, DATE_ADD(NOW(), INTERVAL 10 MINUTE));

INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'REFERRAL', 30, 230, '推荐用户注册奖励（三级）', 4, 4, DATE_ADD(NOW(), INTERVAL 15 MINUTE));

-- ============================================
-- 阶段4: 构造兑换订单
-- ============================================

-- 4.1 创建兑换订单
-- 用户1兑换商品1（已完成）
INSERT INTO exchange_orders (user_id, item_id, points_used, cash_paid, status, remark, created_at, updated_at)
VALUES (1, 1, 1000, 0.00, 'COMPLETED', '兑换系统开发抵扣券', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- 用户1兑换商品2（待处理）
INSERT INTO exchange_orders (user_id, item_id, points_used, cash_paid, status, remark, created_at, updated_at)
VALUES (1, 2, 500, 0.00, 'PENDING', '兑换功能改造抵扣券', NOW(), NOW());

-- 4.2 添加积分支出记录
-- 兑换支出记录
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'EXCHANGE', -1000, -570, '兑换商品：系统开发抵扣券', NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at)
VALUES (1, 'EXCHANGE', -500, -1070, '兑换商品：功能改造抵扣券', NULL, NULL, NOW());

commit;