===FILE:back/tongquyouyi.sql===
```sql
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS tongquyouyi 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci
COMMENT '童趣友谊商城数据库';

USE tongquyouyi;

-- 临时禁用外键检查，方便建表
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 公共表 - 系统字典表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    remark VARCHAR(256) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_code (dict_code),
    KEY idx_dict_type (dict_type),
    KEY idx_dict_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表';

-- ----------------------------
-- 2. 会员管理 - 会员等级表
-- ----------------------------
DROP TABLE IF EXISTS member_level;
CREATE TABLE member_level (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    level_name VARCHAR(32) NOT NULL COMMENT '等级名称',
    discount DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '会员折扣',
    min_points INT NOT NULL DEFAULT 0 COMMENT '最低积分要求',
    max_points INT DEFAULT NULL COMMENT '最高积分要求（NULL表示无上限）',
    icon VARCHAR(255) DEFAULT NULL COMMENT '等级图标',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_level_name (level_name),
    KEY idx_level_points (min_points, max_points)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级表';

-- ----------------------------
-- 3. 会员管理 - 会员表
-- ----------------------------
DROP TABLE IF EXISTS member;
CREATE TABLE member (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(32) NOT NULL COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值（BCrypt）',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    phone VARCHAR(11) NOT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    gender TINYINT NOT NULL DEFAULT 2 COMMENT '性别：0女 1男 2未知',
    birthday DATE DEFAULT NULL COMMENT '生日',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    points INT NOT NULL DEFAULT 0 COMMENT '会员积分',
    member_level_id BIGINT NOT NULL DEFAULT 1 COMMENT '会员等级ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_username (username),
    UNIQUE KEY uk_member_phone (phone),
    KEY idx_member_level (member_level_id),
    KEY idx_member_status (status),
    CONSTRAINT fk_member_level FOREIGN KEY (member_level_id) REFERENCES member_level (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- ----------------------------
-- 4. 商品管理 - 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID（0表示根分类）',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    level TINYINT NOT NULL DEFAULT 1 COMMENT '分类层级（1根 2子 3孙）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_category_parent (parent_id),
    KEY idx_category_status (status),
    KEY idx_category_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ----------------------------
-- 5. 商品管理 - 商品表
-- ----------------------------
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    category_id BIGINT NOT NULL COMMENT '商品分类ID',
    product_code VARCHAR(50) NOT NULL COMMENT '商品编码',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称',
    subtitle VARCHAR(256) DEFAULT NULL COMMENT '商品副标题',
    main_image VARCHAR(255) DEFAULT NULL COMMENT '商品主图',
    sub_images TEXT DEFAULT NULL COMMENT '商品副图（逗号分隔URL）',
    detail TEXT DEFAULT NULL COMMENT '商品详情（富文本）',
    original_price DECIMAL(10,2) NOT NULL COMMENT '商品原价',
    selling_price DECIMAL(10,2) NOT NULL COMMENT '商品售价',
    stock_warn INT NOT NULL DEFAULT 10 COMMENT '库存预警值',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架 2待审核',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (product_code),
    KEY idx_product_category (category_id),
    KEY idx_product_status (status),
    KEY idx_product_price (selling_price),
    FULLTEXT KEY ft_product_name (product_name, subtitle),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ----------------------------
-- 6. 库存管理 - 库存表
-- ----------------------------
DROP TABLE IF EXISTS stock;
CREATE TABLE stock (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    total_stock INT NOT NULL DEFAULT 0 COMMENT '总库存',
    available_stock INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    frozen_stock INT NOT NULL DEFAULT 0 COMMENT '冻结库存',
    lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_product (product_id),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- ----------------------------
-- 7. 订单管理 - 订单主表
-- ----------------------------
DROP TABLE IF EXISTS order_info;
CREATE TABLE order_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    receiver_name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(11) NOT NULL COMMENT '收货人手机号',
    receiver_province VARCHAR(32) DEFAULT NULL COMMENT '收货人省份',
    receiver_city VARCHAR(32) DEFAULT NULL COMMENT '收货人城市',
    receiver_district VARCHAR(32) DEFAULT NULL COMMENT '收货人区县',
    receiver_detail VARCHAR(256) NOT NULL COMMENT '收货人详细地址',
    postal_code VARCHAR(6) DEFAULT NULL COMMENT '邮编',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    freight_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
    payable_amount DECIMAL(10,2) NOT NULL COMMENT '应付金额',
    pay_method TINYINT DEFAULT NULL COMMENT '支付方式：1微信 2支付宝 3余额',
    pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0未支付 1已支付 2已退款',
    order_status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0待支付 1待发货 2已发货 3已签收 4已完成 5已取消 6退款中 7已退款',
    payment_time DATETIME DEFAULT NULL COMMENT '支付时间',
    delivery_time DATETIME DEFAULT NULL COMMENT '发货时间',
    receive_time DATETIME DEFAULT NULL COMMENT '签收时间',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    cancel_reason VARCHAR(256) DEFAULT NULL COMMENT '取消原因',
    remark VARCHAR(512) DEFAULT NULL COMMENT '订单备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_member (member_id),
    KEY idx_order_pay_status (pay_status),
    KEY idx_order_status (order_status),
    KEY idx_order_create_time (create_time),
    CONSTRAINT fk_order_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- ----------------------------
-- 8. 订单管理 - 订单详情表
-- ----------------------------
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_code VARCHAR(50) DEFAULT NULL COMMENT '商品编码（快照）',
    product_name VARCHAR(128) DEFAULT NULL COMMENT '商品名称（快照）',
    main_image VARCHAR(255) DEFAULT NULL COMMENT '商品主图（快照）',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '商品原价（快照）',
    selling_price DECIMAL(10,2) DEFAULT NULL COMMENT '商品售价（快照）',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    sub_total DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_item_order (order_id),
    KEY idx_order_item_product (product_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES order_info (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 预设数据
-- ----------------------------
-- 1. 会员等级预设
INSERT INTO member_level (level_name, discount, min_points, max_points, status) VALUES
('普通会员', 1.00, 0, 999, 1),
('白银会员', 0.98, 1000, 4999, 1),
('黄金会员', 0.95, 5000, 19999, 1),
('钻石会员', 0.90, 20000, NULL, 1);

-- 2. 系统字典预设
INSERT INTO sys_dict (dict_code, dict_name, dict_type, sort, status) VALUES
-- 支付方式
('pay_method_1', '微信支付', 'pay_method', 1, 1),
('pay_method_2', '支付宝支付', 'pay_method', 2, 1),
('pay_method_3', '余额支付', 'pay_method', 3, 1),
-- 性别
('gender_0', '女', 'gender', 1, 1),
('gender_1', '男', 'gender', 2, 1),
('gender_2', '未知', 'gender', 3, 1),
-- 商品状态
('product_status_0', '下架', 'product_status', 1, 1),
('product_status_1', '上架', 'product_status', 2, 1),
('product_status_2', '待审核', 'product_status', 3, 1);

-- 3. 商品分类预设
INSERT INTO product_category (parent_id, category_name, category_code, sort, level, status) VALUES
(0, '益智玩具', 'toy_intellectual', 1, 1, 1),
(0, '户外用品', 'toy_outdoor', 2, 1, 1),
(0, '文具用品', 'toy_stationery', 3, 1, 1),
(1, '积木拼装', 'toy_intellectual_block', 1, 2, 1),
(1, '拼图', 'toy_intellectual_puzzle', 2, 2, 1);

-- 4. 商品预设
INSERT INTO product (category_id, product_code, product_name, subtitle, main_image, original_price, selling_price, stock_warn, status) VALUES
(4, 'P001', '乐高式城市警察积木', '兼容乐高 6岁+ 男孩女孩益智拼装玩具', '/images/product/P001_main.jpg', 199.00, 129.00, 20, 1),
(4, 'P002', '启蒙公主城堡积木', '兼容乐高 5岁+ 女孩梦幻城堡拼装玩具', '/images/product/P002_main.jpg', 259.00, 169.00, 15, 1),
(5, 'P003', '1000片世界地图拼图', '加厚木质 成人儿童通用 益智减压', '/images/product/P003_main.jpg', 89.00, 59.00, 25, 1),
(3, 'P004', '迪士尼联名36色水彩笔', '无毒可水洗 幼儿园小学生专用', '/images/product/P004_main.jpg', 49.00, 29.90, 50, 1);

-- 5. 库存预设
INSERT INTO stock (product_id, total_stock, available_stock, frozen_stock) VALUES
(1, 150, 150, 0),
(2, 120, 120, 0),
(3, 200, 200, 0),
(4, 300, 300, 0);

-- 6. 测试会员预设（密码：123456 BCrypt加密）
INSERT INTO member (username, password_hash, nickname, phone, member_level_id, points, balance) VALUES
('test001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试小明', '13800138000', 1, 800, 50.00);
```