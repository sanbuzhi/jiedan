# 数据库数据构造计划

## 当前用户信息
- **用户ID**: 1
- **推荐码**: 52F236
- **当前积分**: 0

## 数据库表结构分析

### 1. users（用户表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| phone | String | 手机号/微信openid |
| nickname | String | 昵称 |
| avatar | String | 头像URL |
| referral_code | String | 推荐码（唯一） |
| referrer_id | Long | 推荐人ID |
| total_points | Integer | 总积分 |
| is_active | Boolean | 是否激活 |
| created_at | DateTime | 创建时间 |
| updated_at | DateTime | 更新时间 |

### 2. exchange_items（积分兑换商品表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 商品名称 |
| description | String | 商品描述 |
| image_url | String | 商品图片URL |
| points_required | Integer | 所需积分 |
| cash_price | Decimal | 现金价格 |
| stock | Integer | 库存 |
| is_active | Boolean | 是否上架 |
| created_at | DateTime | 创建时间 |
| updated_at | DateTime | 更新时间 |

### 3. point_records（积分记录表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| user_id | Long | 用户ID |
| type | String | 类型（REGISTER/REFERRAL/EXCHANGE等） |
| amount | Integer | 变动金额（正为收入，负为支出） |
| balance | Integer | 变动后余额 |
| description | String | 描述 |
| related_user_id | Long | 关联用户ID |
| rule_id | Long | 规则ID |
| created_at | DateTime | 创建时间 |

### 4. referral_relationships（推荐关系表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| user_id | Long | 被推荐人ID |
| referrer_id | Long | 推荐人ID |
| level | Integer | 推荐层级（1/2/3） |
| created_at | DateTime | 创建时间 |

### 5. exchange_orders（兑换订单表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| user_id | Long | 用户ID |
| item_id | Long | 商品ID |
| points_used | Integer | 使用积分 |
| cash_paid | Decimal | 支付现金 |
| status | Enum | 状态（PENDING/PROCESSING/COMPLETED/CANCELLED/FAILED） |
| remark | String | 备注 |
| created_at | DateTime | 创建时间 |
| updated_at | DateTime | 更新时间 |

### 6. rules（积分规则表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 规则名称 |
| code | String | 规则代码（唯一） |
| description | String | 规则描述 |
| type | String | 类型 |
| points | Integer | 积分值 |
| max_level | Integer | 最大层级 |
| is_active | Boolean | 是否激活 |
| created_at | DateTime | 创建时间 |
| updated_at | DateTime | 更新时间 |

---

## 数据构造计划

### 阶段1: 基础数据（必须先插入）

#### 1.1 积分规则表 (rules)
```sql
-- 注册奖励规则
INSERT INTO rules (name, code, description, type, points, max_level, is_active, created_at, updated_at) 
VALUES ('注册奖励', 'REGISTER_REWARD', '新用户注册奖励积分', 'REGISTER', 50, 1, true, NOW(), NOW());

-- 一级推荐奖励
INSERT INTO rules (name, code, description, type, points, max_level, is_active, created_at, updated_at) 
VALUES ('一级推荐奖励', 'REFERRAL_LEVEL1', '直接推荐用户注册奖励', 'REFERRAL', 100, 3, true, NOW(), NOW());

-- 二级推荐奖励
INSERT INTO rules (name, code, description, type, points, max_level, is_active, created_at, updated_at) 
VALUES ('二级推荐奖励', 'REFERRAL_LEVEL2', '间接推荐用户注册奖励', 'REFERRAL', 50, 3, true, NOW(), NOW());

-- 三级推荐奖励
INSERT INTO rules (name, code, description, type, points, max_level, is_active, created_at, updated_at) 
VALUES ('三级推荐奖励', 'REFERRAL_LEVEL3', '三级推荐用户注册奖励', 'REFERRAL', 30, 3, true, NOW(), NOW());
```

#### 1.2 积分兑换商品表 (exchange_items)
```sql
-- 商品1: 系统开发抵扣券
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

-- 商品2: 功能改造抵扣券
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

-- 商品3: 优先服务券
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

-- 商品4: 积分+现金混合商品示例
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
```

### 阶段2: 为用户1构造测试数据

#### 2.1 给用户1添加积分记录（增加积分）
```sql
-- 注册奖励积分记录
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at) 
VALUES (1, 'REGISTER', 50, 50, '新用户注册奖励', NULL, 1, NOW());

-- 模拟推荐奖励积分记录（一级推荐）
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at) 
VALUES (1, 'REFERRAL', 100, 150, '推荐用户注册奖励（一级）', 2, 2, DATEADD('MINUTE', 5, NOW()));

-- 模拟推荐奖励积分记录（二级推荐）
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at) 
VALUES (1, 'REFERRAL', 50, 200, '推荐用户注册奖励（二级）', 3, 3, DATEADD('MINUTE', 10, NOW()));

-- 模拟推荐奖励积分记录（三级推荐）
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at) 
VALUES (1, 'REFERRAL', 30, 230, '推荐用户注册奖励（三级）', 4, 4, DATEADD('MINUTE', 15, NOW()));

-- 模拟更多积分收入
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at) 
VALUES (1, 'ACTIVITY', 200, 430, '参与活动奖励', NULL, NULL, DATEADD('HOUR', 1, NOW()));
```

#### 2.2 更新用户1的积分总额
```sql
UPDATE users SET total_points = 430, updated_at = NOW() WHERE id = 1;
```

### 阶段3: 构造推荐关系树（可选）

#### 3.1 创建测试用户（用于构建推荐树）
```sql
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
```

#### 3.2 创建推荐关系
```sql
-- 用户2是用户1的一级推荐
INSERT INTO referral_relationships (user_id, referrer_id, level, created_at) 
VALUES (2, 1, 1, NOW());

-- 用户3是用户1的二级推荐（通过用户2）
INSERT INTO referral_relationships (user_id, referrer_id, level, created_at) 
VALUES (3, 1, 2, NOW());

-- 用户4是用户1的三级推荐（通过用户2->用户3）
INSERT INTO referral_relationships (user_id, referrer_id, level, created_at) 
VALUES (4, 1, 3, NOW());
```

### 阶段4: 构造兑换订单（可选）

#### 4.1 创建兑换订单
```sql
-- 用户1兑换商品1（已完成）
INSERT INTO exchange_orders (user_id, item_id, points_used, cash_paid, status, remark, created_at, updated_at) 
VALUES (1, 1, 1000, 0.00, 'COMPLETED', '兑换系统开发抵扣券', DATEADD('HOUR', -2, NOW()), DATEADD('HOUR', -1, NOW()));

-- 用户1兑换商品2（待处理）
INSERT INTO exchange_orders (user_id, item_id, points_used, cash_paid, status, remark, created_at, updated_at) 
VALUES (1, 2, 500, 0.00, 'PENDING', '兑换功能改造抵扣券', NOW(), NOW());
```

#### 4.2 添加积分支出记录
```sql
-- 兑换支出记录
INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at) 
VALUES (1, 'EXCHANGE', -1000, -570, '兑换商品：系统开发抵扣券', NULL, NULL, DATEADD('HOUR', -2, NOW()));

INSERT INTO point_records (user_id, type, amount, balance, description, related_user_id, rule_id, created_at) 
VALUES (1, 'EXCHANGE', -500, -1070, '兑换商品：功能改造抵扣券', NULL, NULL, NOW());
```

---

## 执行顺序

1. **首先执行阶段1** - 插入基础数据（规则和商品）
2. **然后执行阶段2** - 为用户1添加积分
3. **可选执行阶段3** - 构建推荐树
4. **可选执行阶段4** - 添加兑换订单

## 数据验证SQL

```sql
-- 验证用户1的积分
SELECT id, nickname, total_points, referral_code FROM users WHERE id = 1;

-- 验证积分记录
SELECT * FROM point_records WHERE user_id = 1 ORDER BY created_at DESC;

-- 验证推荐关系
SELECT * FROM referral_relationships WHERE referrer_id = 1;

-- 验证兑换商品
SELECT * FROM exchange_items WHERE is_active = true;

-- 验证兑换订单
SELECT * FROM exchange_orders WHERE user_id = 1;
```
