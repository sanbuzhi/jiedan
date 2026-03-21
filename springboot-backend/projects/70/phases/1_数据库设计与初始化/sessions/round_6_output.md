===FILE:back/tongquyouyi.sql===
```sql
-- =============================================
-- 数据库：tongquyouyi（童趣优衣/通用电商数据库）
-- 版本：MySQL 8.0.35
-- 字符集：utf8mb4（支持emoji等4字节字符）
-- 排序规则：utf8mb4_0900_ai_ci（MySQL 8.0+推荐，不区分大小写和重音）
-- 存储引擎：InnoDB（支持事务、外键、行锁）
-- 作者：代码生成助手
-- 日期：202X-XX-XX
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS tongquyouyi 
DEFAULT CHARSET utf8mb4 
COLLATE utf8mb4_0900_ai_ci;

-- 使用数据库
USE tongquyouyi;

-- =============================================
-- 模块1：公共表（字典、地区、操作日志基础）
-- =============================================

-- 1.1 字典类型表
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dict_type_code VARCHAR(64) NOT NULL COMMENT '字典类型编码（唯一）',
    dict_type_name VARCHAR(64) NOT NULL COMMENT '字典类型名称',
    sort INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用，1启用）',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type_code (dict_type_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- 1.2 字典项表
DROP TABLE IF EXISTS sys_dict_item;
CREATE TABLE sys_dict_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    dict_type_id BIGINT UNSIGNED NOT NULL COMMENT '字典类型ID',
    dict_item_code VARCHAR(64) NOT NULL COMMENT '字典项编码',
    dict_item_value VARCHAR(64) NOT NULL COMMENT '字典项值',
    sort INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用，1启用）',
    is_default TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认（0否，1是）',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type_item_code (dict_type_id, dict_item_code),
    KEY idx_dict_type_id (dict_type_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典项表';

-- 1.3 省市区地区表（简化三级）
DROP TABLE IF EXISTS sys_region;
CREATE TABLE sys_region (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID（省=0，市=省ID，区=市ID）',
    region_code VARCHAR(20) NOT NULL COMMENT '地区编码（唯一）',
    region_name VARCHAR(64) NOT NULL COMMENT '地区名称',
    region_level TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '地区级别（1省，2市，3区）',
    sort INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用，1启用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_region_code (region_code),
    KEY idx_parent_id (parent_id),
    KEY idx_region_level (region_level),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='省市区地区表';

-- =============================================
-- 模块2：会员管理表
-- =============================================

-- 2.1 会员等级表
DROP TABLE IF EXISTS member_level;
CREATE TABLE member_level (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    level_no VARCHAR(32) NOT NULL COMMENT '等级编码（唯一）',
    level_name VARCHAR(32) NOT NULL COMMENT '等级名称',
    level_icon VARCHAR(255) DEFAULT NULL COMMENT '等级图标',
    discount DECIMAL(3,1) UNSIGNED NOT NULL DEFAULT 10.0 COMMENT '折扣率（0-10，10.0不打折）',
    points_rate DECIMAL(3,1) UNSIGNED NOT NULL DEFAULT 1.0 COMMENT '积分倍率（消费1元得points_rate积分）',
    min_points BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '升级所需最低累计积分',
    max_points BIGINT UNSIGNED DEFAULT NULL COMMENT '升级所需最高累计积分（NULL表示不设上限）',
    sort INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用，1启用）',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_level_no (level_no),
    KEY idx_min_max_points (min_points, max_points),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员等级表';

-- 2.2 会员表
DROP TABLE IF EXISTS member;
CREATE TABLE member (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    member_no VARCHAR(32) NOT NULL COMMENT '会员编号（唯一）',
    phone VARCHAR(16) NOT NULL COMMENT '手机号（唯一登录凭证）',
    nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    real_name VARCHAR(32) DEFAULT NULL COMMENT '真实姓名',
    id_card VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    gender TINYINT UNSIGNED DEFAULT 0 COMMENT '性别（0未知，1男，2女）',
    birthday DATE DEFAULT NULL COMMENT '生日',
    level_id BIGINT UNSIGNED NOT NULL COMMENT '会员等级ID',
    points BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '当前可用积分',
    total_points BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计获得积分',
    balance DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    total_consume DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
    wechat_openid VARCHAR(64) DEFAULT NULL COMMENT '微信OpenID',
    wechat_unionid VARCHAR(64) DEFAULT NULL COMMENT '微信UnionID',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用，1正常）',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_no (member_no),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_wechat_openid (wechat_openid),
    KEY idx_level_id (level_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员表';

-- =============================================
-- 模块3：商品管理表
-- =============================================

-- 3.1 商品分类表
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级分类ID（0表示一级分类）',
    category_no VARCHAR(32) NOT NULL COMMENT '分类编码（唯一）',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    category_icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    category_banner VARCHAR(255) DEFAULT NULL COMMENT '分类banner',
    sort INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用，1启用）',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_no (category_no),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status),
    KEY idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- 3.2 商品SPU表（标准产品单元）
DROP TABLE IF EXISTS product_spu;
CREATE TABLE product_spu (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    spu_no VARCHAR(32) NOT NULL COMMENT 'SPU编码（唯一）',
    category_id BIGINT UNSIGNED NOT NULL COMMENT '商品分类ID',
    brand_name VARCHAR(64) DEFAULT NULL COMMENT '品牌名称',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    main_image VARCHAR(255) NOT NULL COMMENT '主图URL',
    sub_images JSON DEFAULT NULL COMMENT '副图URL列表（JSON数组）',
    detail_html TEXT DEFAULT NULL COMMENT '商品详情（HTML格式）',
    detail_text TEXT DEFAULT NULL COMMENT '商品详情（纯文本格式）',
    sale_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
    view_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
    comment_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评价数',
    is_hot TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否热销（0否，1是）',
    is_new TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否新品（0否，1是）',
    is_recommend TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐（0否，1是）',
    sort INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0下架，1上架）',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_spu_no (spu_no),
    KEY idx_category_id (category_id),
    KEY idx_brand_name (brand_name),
    KEY idx_sale_count (sale_count DESC),
    KEY idx_create_time (create_time DESC),
    KEY idx_status (status),
    KEY idx_hot_new_recommend (is_hot DESC, is_new DESC, is_recommend DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU表';

-- 3.3 商品SKU表（库存单元）
DROP TABLE IF EXISTS product_sku;
CREATE TABLE product_sku (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    sku_no VARCHAR(32) NOT NULL COMMENT 'SKU编码（唯一）',
    spu_id BIGINT UNSIGNED NOT NULL COMMENT '关联SPU ID',
    specs JSON NOT NULL COMMENT '规格组合（JSON对象，如{"颜色":"黑色","尺码":"L"}）',
    specs_desc VARCHAR(255) DEFAULT NULL COMMENT '规格组合描述（冗余字段，方便查询）',
    price DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '销售价格',
    original_price DECIMAL(10,2) UNSIGNED DEFAULT NULL COMMENT '原价',
    sku_image VARCHAR(255) DEFAULT NULL COMMENT 'SKU图片URL（为空则取SPU主图）',
    sale_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'SKU销量',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（0禁用，1启用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_no (sku_no),
    KEY idx_spu_id (spu_id),
    KEY idx_status (status),
    KEY idx_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- =============================================
-- 模块4：库存管理表
-- =============================================

-- 4.1 库存表（单仓库版，后续可扩展多仓库）
DROP TABLE IF EXISTS stock;
CREATE TABLE stock (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    sku_id BIGINT UNSIGNED NOT NULL COMMENT '关联SKU ID',
    available_stock BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存',
    locked_stock BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '锁定库存（订单未支付/未发货）',
    total_stock BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存（冗余字段=可用+锁定）',
    warning_stock BIGINT UNSIGNED NOT NULL DEFAULT 10 COMMENT '预警库存',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_id (sku_id),
    KEY idx_available_stock (available_stock),
    KEY idx_warning_stock (available_stock, warning_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';

-- =============================================
-- 模块5：订单管理表
-- =============================================

-- 5.1 订单收货地址快照表（防止地址修改影响历史订单）
DROP TABLE IF EXISTS order_address_snapshot;
CREATE TABLE order_address_snapshot (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT UNSIGNED NOT NULL COMMENT '关联订单ID',
    receiver_name VARCHAR(32) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(16) NOT NULL COMMENT '收货人手机号',
    province_id BIGINT UNSIGNED NOT NULL COMMENT '省ID',
    province_name VARCHAR(64) NOT NULL COMMENT '省名称',
    city_id BIGINT UNSIGNED NOT NULL COMMENT '市ID',
    city_name VARCHAR(64) NOT NULL COMMENT '市名称',
    district_id BIGINT UNSIGNED NOT NULL COMMENT '区ID',
    district_name VARCHAR(64) NOT NULL COMMENT '区名称',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    zip_code VARCHAR(10) DEFAULT NULL COMMENT '邮编',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单收货地址快照表';

-- 5.2 订单主表
DROP TABLE IF EXISTS order_info;
CREATE TABLE order_info (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号（唯一，含时间戳等）',
    member_id BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
    member_level_id BIGINT UNSIGNED NOT NULL COMMENT '下单时会员等级ID（冗余）',
    member_level_discount DECIMAL(3,1) UNSIGNED NOT NULL DEFAULT 10.0 COMMENT '下单时会员折扣率（冗余）',
    total_goods_amount DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '商品总金额（不含运费）',
    freight_amount DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '运费金额',
    coupon_amount DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '优惠券减免金额',
    points_amount DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
    total_amount DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '订单总金额（商品+运费）',
    pay_amount DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '实际支付金额',
    pay_type TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式（1微信支付，2支付宝支付，3余额支付，4积分+余额+...组合）',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    delivery_type TINYINT UNSIGNED DEFAULT NULL COMMENT '配送方式（1快递，2自提）',
    delivery_time DATETIME DEFAULT NULL COMMENT '发货时间',
    logistics_no VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
    logistics_company VARCHAR(64) DEFAULT NULL COMMENT '物流公司',
    receive_time DATETIME DEFAULT NULL COMMENT '收货时间',
    order_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态（0待支付，1已支付待发货，2已发货待收货，3已收货完成，4已取消，5退款中，6已退款）',
    pay_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支付状态（0未支付，1已支付，2部分退款，3全额退款）',
    delivery_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '配送状态（0未发货，1已发货，2部分签收，3已签收）',
    member_remark VARCHAR(255) DEFAULT NULL COMMENT '会员备注',
    admin_remark VARCHAR(255) DEFAULT NULL COMMENT '管理员备注',
    cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_member_id (member_id),
    KEY idx_order_status (order_status),
    KEY idx_pay_status (pay_status),
    KEY idx_create_time (create_time DESC),
    KEY idx_pay_time (pay_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

-- 5.3 订单明细表
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT UNSIGNED NOT NULL COMMENT '关联订单ID',
    spu_id BIGINT UNSIGNED NOT NULL COMMENT '关联SPU ID',
    sku_id BIGINT UNSIGNED NOT NULL COMMENT '关联SKU ID',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称（冗余）',
    specs JSON NOT NULL COMMENT '规格组合（冗余）',
    specs_desc VARCHAR(255) DEFAULT NULL COMMENT '规格组合描述（冗余）',
    product_image VARCHAR(255) NOT NULL COMMENT '商品图片（冗余）',
    price DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '下单时销售价格（冗余）',
    original_price DECIMAL(10,2) UNSIGNED DEFAULT NULL COMMENT '下单时原价（冗余）',
    quantity INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '购买数量',
    total_amount DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '小计金额（冗余=price*quantity）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_spu_id (spu_id),
    KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- 5.4 订单操作日志表
DROP TABLE IF EXISTS order_log;
CREATE TABLE order_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT UNSIGNED NOT NULL COMMENT '关联订单ID',
    operator_type TINYINT UNSIGNED NOT NULL COMMENT '操作人类型（0系统，1会员，2管理员）',
    operator_id BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID（系统操作则为NULL）',
    operator_name VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名/昵称（系统操作则为"系统"）',
    action VARCHAR(64) NOT NULL COMMENT '操作动作',
    old_order_status TINYINT UNSIGNED DEFAULT NULL COMMENT '操作前订单状态',
    new_order_status TINYINT UNSIGNED DEFAULT NULL COMMENT '操作后订单状态',
    old_pay_status TINYINT UNSIGNED DEFAULT NULL COMMENT '操作前支付状态',
    new_pay_status TINYINT UNSIGNED DEFAULT NULL COMMENT '操作后支付状态',
    old_delivery_status TINYINT UNSIGNED DEFAULT NULL COMMENT '操作前配送状态',
    new_delivery_status TINYINT UNSIGNED DEFAULT NULL COMMENT '操作后配送状态',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_operator_type (operator_type),
    KEY idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单操作日志表';

-- =============================================
-- 模块6：预设数据初始化
-- =============================================

-- 6.1 预设字典类型
INSERT INTO sys_dict_type (dict_type_code, dict_type_name, sort, remark) VALUES
('order_status', '订单状态', 1, '订单主表order_status字段字典'),
('pay_status', '支付状态', 2, '订单主表pay_status字段字典'),
('delivery_status', '配送状态', 3, '订单主表delivery_status字段字典'),
('pay_type', '支付方式', 4, '订单主表pay_type字段字典'),
('delivery_type', '配送方式', 5, '订单主表delivery_type字段字典'),
('member_gender', '会员性别', 6, '会员表gender字段字典'),
('product_status', '商品状态', 7, '商品表status字段字典'),
('common_status', '通用状态', 8, '通用表status字段字典');

-- 6.2 预设字典项
INSERT INTO sys_dict_item (dict_type_id, dict_item_code, dict_item_value, sort, is_default, status) VALUES
-- 订单状态（dict_type_id=1）
(1, '0', '待支付', 1, 1, 1),
(1, '1', '已支付待发货', 2, 0, 1),
(1, '2', '已发货待收货', 3, 0, 1),
(1, '3', '已收货完成', 4, 0, 1),
(1, '4', '已取消', 5, 0, 1),
(1, '5', '退款中', 6, 0, 1),
(1, '6', '已退款', 7, 0, 1),
-- 支付状态（dict_type_id=2）
(2, '0', '未支付', 1, 1, 1),
(2, '1', '已支付', 2, 0, 1),
(2, '2', '部分退款', 3, 0, 1),
(2, '3', '全额退款', 4, 0, 1),
-- 配送状态（dict_type_id=3）
(3, '0', '未发货', 1, 1, 1),
(3, '1', '已发货', 2, 0, 1),
(3, '2', '部分签收', 3, 0, 1),
(3, '3', '已签收', 4, 0, 1),
-- 支付方式（dict_type_id=4）
(4, '1', '微信支付', 1, 1, 1),
(4, '2', '支付宝支付', 2, 0, 1),
(4, '3', '余额支付', 3, 0, 1),
-- 配送方式（dict_type_id=5）
(5, '1', '快递', 1, 1, 1),
(5, '2', '自提', 2, 0, 1),
-- 会员性别（dict_type_id=6）
(6, '0', '未知', 1, 1, 1),
(6, '1', '男', 2, 0, 1),
(6, '2', '女', 3, 0, 1),
-- 商品状态（dict_type_id=7）
(7, '0', '下架', 1, 0, 1),
(7, '1', '上架', 2, 1, 1),
-- 通用状态（dict_type_id=8）
(8, '0', '禁用', 1, 0, 1),
(8, '1', '启用', 2, 1, 1);

-- 6.3 预设地区（简化三级）
INSERT INTO sys_region (parent_id, region_code, region_name, region_level, sort, status) VALUES
-- 一级省
(0, '110000', '北京市', 1, 1, 1),
(0, '310000', '上海市', 1, 2, 1),
(0, '320000', '江苏省', 1, 3, 1),
-- 二级市
(1, '110100', '北京市', 2, 1, 1),
(2, '310100', '上海市', 2, 1, 1),
(3, '320100', '南京市', 2, 1, 1),
(3, '320500', '苏州市', 2, 2, 1),
-- 三级区
(4, '110101', '东城区', 3, 1, 1),
(4, '110105', '朝阳区', 3, 2, 1),
(5, '310101', '黄浦区', 3, 1, 1),
(5, '310115', '浦东新区', 3, 2, 1),
(6, '320102', '玄武区', 3, 1, 1),
(6, '320104', '秦淮区', 3, 2, 1),
(7, '320505', '虎丘区', 3, 1, 1),
(7, '320506', '吴中区', 3, 2, 1);

-- 6.4 预设会员等级
INSERT INTO member_level (level_no, level_name, level_icon, discount, points_rate, min_points, max_points, sort, status, remark) VALUES
('LV001', '普通会员', NULL, 10.0, 1.0, 0, 999, 1, 1, '新注册会员默认等级'),
('LV002', '黄金会员', NULL, 9.0, 1.5, 1000, 4999, 2, 1, '累计消费积分≥1000自动升级'),
('LV003', '钻石会员', NULL, 8.0, 2.0, 5000, NULL, 3, 1, '累计消费积分≥5000自动升级');

-- 6.5 预设测试会员
INSERT INTO member (member_no, phone, nickname, avatar, real_name, id_card, gender, birthday, level_id, points, total_points, balance, total_consume, status) VALUES
('M202X00001', '13800138000', '童趣测试员', 'https://via.placeholder.com/100', '张测试', '110105199001011234', 1, '1990-01-01', 1, 100, 100, 500.00, 0.00, 1),
('M202X00002', '13900139000', '钻石小买家', 'https://via.placeholder.com/100', '李钻石', '310115199202025678', 2, '1992-02-02', 3, 6000, 6000, 2000.00, 12000.00, 1);

-- 6.6 预设商品分类
INSERT INTO product_category (parent_id, category_no, category_name, category_icon, sort, status) VALUES
-- 一级分类
(0, 'C001', '童装', 'https://via.placeholder.com/50', 1, 1),
(0, 'C002', '童鞋', 'https://via.placeholder.com/50', 2, 1),
(0, 'C003', '玩具', 'https://via.placeholder.com/50', 3, 1),
-- 二级分类（童装）
(1, 'C001001', '婴幼装', NULL, 1, 1),
(1, 'C001002', '儿童装', NULL, 2, 1),
(1, 'C001003', '青少年装', NULL, 3, 1);

-- 6.7 预设商品SPU
INSERT INTO product_spu (spu_no, category_id, brand_name, product_name, main_image, sub_images, detail_html, detail_text, is_hot, is_new, is_recommend, sort, status) VALUES
('SPU202X00001', 4, '巴拉巴拉', '巴拉巴拉婴儿纯棉连体衣春秋款', 'https://via.placeholder.com/800x800', '["https://via.placeholder.com/800x800","https://via.placeholder.com/800x800"]', '<p>巴拉巴拉品牌，婴儿纯棉材质，春秋款，柔软舒适</p>', '巴拉巴拉品牌，婴儿纯棉材质，春秋款，柔软舒适', 1, 1, 1, 1, 1),
('SPU202X00002', 5, '迪士尼', '迪士尼儿童T恤男童夏季新款', 'https://via.placeholder.com/800x800', '["https://via.placeholder.com/800x800","https://via.placeholder.com/800x800"]', '<p>迪士尼正版授权，男童夏季T恤，透气吸汗</p>', '迪士尼正版授权，男童夏季T恤，透气吸汗', 1, 0, 1, 2, 1);

-- 6.8 预设商品SKU
INSERT INTO product_sku (sku_no, spu_id, specs, specs_desc, price, original_price, sku_image, status) VALUES
('SKU202X00001001', 1, '{"颜色":"粉色","尺码":"59cm"}', '粉色 59cm', 99.00, 129.00, 'https://via.placeholder.com/800x800', 1),
('SKU202X00001002', 1, '{"颜色":"蓝色","尺码":"66cm"}', '蓝色 66cm', 99.00, 129.00, 'https://via.placeholder.com/800x800', 1),
('SKU202X00002001', 2, '{"颜色":"白色米奇","尺码":"110cm"}', '白色米奇 110cm', 59.00, 89.00, 'https://via.placeholder.com/800x800', 1),
('SKU202X00002002', 2, '{"颜色":"红色米妮","尺码":"120cm"}', '红色米妮 120cm', 59.00, 89.00, 'https://via.placeholder.com/800x800', 1);

-- 6.9 预设库存
INSERT INTO stock (sku_id, available_stock, locked_stock, total_stock, warning_stock) VALUES
(1, 100, 0, 100, 10),
(2, 80, 0, 80, 10),
(3, 150, 0, 150, 20),
(4, 120, 0, 120, 20);
```