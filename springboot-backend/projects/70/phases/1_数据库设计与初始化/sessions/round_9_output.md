===FILE:back/tongquyouyi.sql===
```sql
-- =====================================================
-- 数据库：tongquyouyi
-- 版本：MySQL 8.0.35+
-- 字符集：utf8mb4
-- 存储引擎：InnoDB
-- 创建时间：202X-XX-XX
-- 功能：童趣友谊社区电商基础数据库初始化脚本
-- =====================================================

-- 1. 删除旧数据库（谨慎使用，生产环境可注释）
DROP DATABASE IF EXISTS tongquyouyi;

-- 2. 创建新数据库
CREATE DATABASE tongquyouyi
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 3. 使用新数据库
USE tongquyouyi;

-- =====================================================
-- 公共表模块
-- =====================================================

-- 3.1 公共地区字典表
CREATE TABLE t_common_region (
    region_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '地区ID',
    parent_id INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级地区ID，0表示顶级省/直辖市/自治区',
    region_name VARCHAR(100) NOT NULL COMMENT '地区名称',
    region_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '地区类型：1-省/直辖市/自治区，2-地级市，3-区县',
    sort_order INT UNSIGNED NOT NULL DEFAULT 100 COMMENT '排序号（数字越小越靠前）',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公共地区字典表';

-- 3.2 公共操作日志表
CREATE TABLE t_common_operation_log (
    log_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT UNSIGNED COMMENT '操作用户ID（游客或匿名操作为NULL）',
    user_type TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户类型：0-游客/匿名，1-普通会员，2-管理员，3-商家',
    operation_module VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(500) NOT NULL COMMENT '操作描述',
    request_method VARCHAR(10) NOT NULL COMMENT '请求方法：GET/POST/PUT/DELETE等',
    request_url VARCHAR(255) NOT NULL COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数（JSON格式）',
    response_result TEXT COMMENT '响应结果（JSON格式，敏感信息需脱敏）',
    ip_address VARCHAR(50) COMMENT '操作用户IP地址',
    user_agent VARCHAR(500) COMMENT '操作用户UA',
    execution_time INT UNSIGNED COMMENT '请求执行时间（毫秒）',
    is_success TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否成功：0-失败，1-成功',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公共操作日志表';

-- =====================================================
-- 会员管理表模块
-- =====================================================

-- 3.3 会员基础信息表
CREATE TABLE t_member_info (
    member_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '会员ID',
    member_no VARCHAR(32) UNIQUE NOT NULL COMMENT '会员唯一编号（系统自动生成）',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '登录密码哈希（BCrypt算法）',
    salt VARCHAR(64) COMMENT '密码盐值（可选，增强安全性）',
    nickname VARCHAR(50) NOT NULL COMMENT '会员昵称',
    avatar_url VARCHAR(255) COMMENT '会员头像URL',
    gender TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    birthday DATE COMMENT '生日',
    mobile VARCHAR(20) UNIQUE NOT NULL COMMENT '绑定手机号',
    email VARCHAR(100) UNIQUE COMMENT '绑定邮箱',
    real_name VARCHAR(50) COMMENT '真实姓名',
    id_card_no VARCHAR(32) COMMENT '身份证号（脱敏存储）',
    region_province_id INT UNSIGNED COMMENT '所在省份ID',
    region_city_id INT UNSIGNED COMMENT '所在城市ID',
    region_district_id INT UNSIGNED COMMENT '所在区县ID',
    detail_address VARCHAR(255) COMMENT '详细地址',
    member_level TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '会员等级：1-普通，2-铜牌，3-银牌，4-金牌，5-钻石',
    total_points INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计积分',
    available_points INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用积分',
    total_balance DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '账户总余额',
    available_balance DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '可用余额',
    frozen_balance DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '冻结余额',
    register_source TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '注册来源：1-小程序，2-APP，3-PC官网，4-H5',
    register_ip VARCHAR(50) COMMENT '注册IP',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    account_status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '账户状态：0-禁用，1-正常，2-锁定',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_member_no (member_no),
    INDEX idx_username (username),
    INDEX idx_mobile (mobile),
    INDEX idx_member_level (member_level),
    INDEX idx_account_status (account_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员基础信息表';

-- 3.4 会员收货地址表
CREATE TABLE t_member_address (
    address_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    member_id BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
    consignee_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    consignee_mobile VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    region_province_id INT UNSIGNED NOT NULL COMMENT '所在省份ID',
    region_city_id INT UNSIGNED NOT NULL COMMENT '所在城市ID',
    region_district_id INT UNSIGNED NOT NULL COMMENT '所在区县ID',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    zip_code VARCHAR(10) COMMENT '邮政编码',
    is_default TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
    sort_order INT UNSIGNED NOT NULL DEFAULT 100 COMMENT '排序号',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_member_id (member_id),
    INDEX idx_is_default (is_default),
    CONSTRAINT fk_address_member FOREIGN KEY (member_id) REFERENCES t_member_info(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员收货地址表';

-- 3.5 会员积分/余额变动记录表
CREATE TABLE t_member_account_log (
    log_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    member_id BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
    member_no VARCHAR(32) NOT NULL COMMENT '会员唯一编号',
    account_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '账户类型：1-积分，2-余额',
    change_type TINYINT UNSIGNED NOT NULL COMMENT '变动类型：1-充值，2-消费，3-退款，4-签到，5-邀请，6-活动赠送，7-管理员调整',
    change_amount DECIMAL(12,2) NOT NULL COMMENT '变动金额（正数表示增加，负数表示减少）',
    before_balance DECIMAL(12,2) NOT NULL COMMENT '变动前余额/积分',
    after_balance DECIMAL(12,2) NOT NULL COMMENT '变动后余额/积分',
    related_no VARCHAR(32) COMMENT '关联业务编号（订单号/充值单号等）',
    remark VARCHAR(255) COMMENT '备注',
    operator_id BIGINT UNSIGNED COMMENT '操作人ID（管理员调整时必填）',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_member_id (member_id),
    INDEX idx_account_type (account_type),
    INDEX idx_related_no (related_no),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_account_log_member FOREIGN KEY (member_id) REFERENCES t_member_info(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员积分/余额变动记录表';

-- =====================================================
-- 商品管理表模块
-- =====================================================

-- 3.6 商品分类表
CREATE TABLE t_product_category (
    category_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    parent_id INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级分类ID，0表示顶级分类',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    category_icon VARCHAR(255) COMMENT '分类图标URL',
    category_desc VARCHAR(500) COMMENT '分类描述',
    sort_order INT UNSIGNED NOT NULL DEFAULT 100 COMMENT '排序号',
    is_show TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_is_show (is_show),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 3.7 商品品牌表
CREATE TABLE t_product_brand (
    brand_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '品牌ID',
    brand_name VARCHAR(100) UNIQUE NOT NULL COMMENT '品牌名称',
    brand_logo VARCHAR(255) COMMENT '品牌Logo URL',
    brand_desc TEXT COMMENT '品牌描述',
    brand_origin VARCHAR(50) COMMENT '品牌发源地',
    sort_order INT UNSIGNED NOT NULL DEFAULT 100 COMMENT '排序号',
    is_show TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_brand_name (brand_name),
    INDEX idx_is_show (is_show),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品品牌表';

-- 3.8 商品SKU属性组表
CREATE TABLE t_product_attr_group (
    group_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '属性组ID',
    group_name VARCHAR(100) NOT NULL COMMENT '属性组名称',
    category_id INT UNSIGNED NOT NULL COMMENT '所属分类ID（0表示通用）',
    sort_order INT UNSIGNED NOT NULL DEFAULT 100 COMMENT '排序号',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU属性组表';

-- 3.9 商品SKU属性值表
CREATE TABLE t_product_attr_value (
    value_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '属性值ID',
    group_id INT UNSIGNED NOT NULL COMMENT '属性组ID',
    attr_value VARCHAR(100) NOT NULL COMMENT '属性值',
    attr_color VARCHAR(20) COMMENT '颜色值（仅颜色属性组有效，HEX格式）',
    sort_order INT UNSIGNED NOT NULL DEFAULT 100 COMMENT '排序号',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_group_id (group_id),
    CONSTRAINT fk_attr_value_group FOREIGN KEY (group_id) REFERENCES t_product_attr_group(group_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU属性值表';

-- 3.10 商品SPU基础信息表
CREATE TABLE t_product_spu (
    spu_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '商品SPU ID',
    spu_no VARCHAR(32) UNIQUE NOT NULL COMMENT '商品SPU唯一编号',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    product_subtitle VARCHAR(500) COMMENT '商品副标题',
    category_id INT UNSIGNED NOT NULL COMMENT '所属分类ID',
    brand_id INT UNSIGNED COMMENT '所属品牌ID',
    main_image_url VARCHAR(255) NOT NULL COMMENT '商品主图URL',
    banner_image_urls TEXT COMMENT '商品轮播图URL（JSON数组格式）',
    detail_image_urls TEXT COMMENT '商品详情图URL（JSON数组格式）',
    product_tags VARCHAR(255) COMMENT '商品标签（逗号分隔）',
    product_desc TEXT COMMENT '商品描述',
    unit VARCHAR(20) NOT NULL DEFAULT '件' COMMENT '商品单位',
    min_purchase INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '最小起购量',
    max_purchase INT UNSIGNED COMMENT '最大限购量（NULL表示不限）',
    freight_template_id INT UNSIGNED COMMENT '运费模板ID',
    is_new TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否新品：0-否，1-是',
    is_hot TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否热销：0-否，1-是',
    is_recommend TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
    is_vip_only TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否VIP专享：0-否，1-是',
    sort_order INT UNSIGNED NOT NULL DEFAULT 100 COMMENT '排序号',
    sale_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计销量',
    view_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计浏览量',
    collect_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计收藏量',
    review_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计评价数',
    good_review_rate DECIMAL(5,2) UNSIGNED NOT NULL DEFAULT 100.00 COMMENT '好评率（保留两位小数）',
    audit_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
    audit_remark VARCHAR(255) COMMENT '审核备注',
    audit_time DATETIME COMMENT '审核时间',
    audit_user_id BIGINT UNSIGNED COMMENT '审核人ID',
    product_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品状态：0-下架，1-上架，2-仓库中',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_spu_no (spu_no),
    INDEX idx_category_id (category_id),
    INDEX idx_brand_id (brand_id),
    INDEX idx_product_status (product_status),
    INDEX idx_audit_status (audit_status),
    INDEX idx_is_hot (is_hot),
    INDEX idx_is_recommend (is_recommend),
    INDEX idx_sort_order (sort_order),
    INDEX idx_sale_count (sale_count),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SPU基础信息表';

-- 3.11 商品SKU库存信息表
CREATE TABLE t_product_sku (
    sku_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '商品SKU ID',
    sku_no VARCHAR(32) UNIQUE NOT NULL COMMENT '商品SKU唯一编号',
    spu_id BIGINT UNSIGNED NOT NULL COMMENT '商品SPU ID',
    spu_no VARCHAR(32) NOT NULL COMMENT '商品SPU唯一编号',
    sku_attrs TEXT NOT NULL COMMENT 'SKU属性组合（JSON格式，如：[{"group_id":1,"value_id":1,"group_name":"颜色","attr_value":"红色"},{"group_id":2,"value_id":5,"group_name":"尺码","attr_value":"M"}]）',
    sku_attrs_simple VARCHAR(255) NOT NULL COMMENT 'SKU属性组合简化版（用于展示，如：红色;M）',
    sku_image_url VARCHAR(255) COMMENT 'SKU图片URL',
    market_price DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '市场原价',
    sale_price DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '销售价格',
    cost_price DECIMAL(12,2) UNSIGNED COMMENT '成本价格',
    vip_price DECIMAL(12,2) UNSIGNED COMMENT 'VIP专享价格（NULL表示使用普通会员折扣）',
    weight DECIMAL(10,3) UNSIGNED COMMENT '重量（千克）',
    volume DECIMAL(10,3) UNSIGNED COMMENT '体积（立方米）',
    is_default TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认SKU：0-否，1-是',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sku_no (sku_no),
    INDEX idx_spu_id (spu_id),
    INDEX idx_is_default (is_default),
    CONSTRAINT fk_sku_spu FOREIGN KEY (spu_id) REFERENCES t_product_spu(spu_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU库存信息表';

-- =====================================================
-- 库存管理表模块
-- =====================================================

-- 3.12 仓库基础信息表
CREATE TABLE t_warehouse_info (
    warehouse_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '仓库ID',
    warehouse_no VARCHAR(32) UNIQUE NOT NULL COMMENT '仓库唯一编号',
    warehouse_name VARCHAR(100) NOT NULL COMMENT '仓库名称',
    warehouse_manager VARCHAR(50) COMMENT '仓库负责人',
    manager_mobile VARCHAR(20) COMMENT '负责人手机号',
    region_province_id INT UNSIGNED NOT NULL COMMENT '所在省份ID',
    region_city_id INT UNSIGNED NOT NULL COMMENT '所在城市ID',
    region_district_id INT UNSIGNED NOT NULL COMMENT '所在区县ID',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    warehouse_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '仓库类型：1-主仓，2-分仓，3-虚拟仓',
    sort_order INT UNSIGNED NOT NULL DEFAULT 100 COMMENT '排序号',
    is_default TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认发货仓：0-否，1-是',
    is_enable TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_warehouse_no (warehouse_no),
    INDEX idx_is_default (is_default),
    INDEX idx_is_enable (is_enable)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库基础信息表';

-- 3.13 SKU库存实时表
CREATE TABLE t_sku_stock (
    stock_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '库存ID',
    sku_id BIGINT UNSIGNED NOT NULL COMMENT '商品SKU ID',
    sku_no VARCHAR(32) NOT NULL COMMENT '商品SKU唯一编号',
    warehouse_id INT UNSIGNED NOT NULL COMMENT '仓库ID',
    warehouse_no VARCHAR(32) NOT NULL COMMENT '仓库唯一编号',
    total_stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存',
    available_stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存',
    frozen_stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '冻结库存（已下单未发货）',
    warning_stock INT UNSIGNED NOT NULL DEFAULT 10 COMMENT '预警库存',
    is_warning TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否预警：0-否，1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_sku_warehouse (sku_id, warehouse_id),
    INDEX idx_sku_no (sku_no),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_is_warning (is_warning),
    INDEX idx_available_stock (available_stock),
    CONSTRAINT fk_stock_sku FOREIGN KEY (sku_id) REFERENCES t_product_sku(sku_id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_warehouse FOREIGN KEY (warehouse_id) REFERENCES t_warehouse_info(warehouse_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SKU库存实时表';

-- 3.14 库存变动记录表
CREATE TABLE t_stock_log (
    log_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    sku_id BIGINT UNSIGNED NOT NULL COMMENT '商品SKU ID',
    sku_no VARCHAR(32) NOT NULL COMMENT '商品SKU唯一编号',
    warehouse_id INT UNSIGNED NOT NULL COMMENT '仓库ID',
    warehouse_no VARCHAR(32) NOT NULL COMMENT '仓库唯一编号',
    change_type TINYINT UNSIGNED NOT NULL COMMENT '变动类型：1-采购入库，2-销售出库，3-退货入库，4-调拨入库，5-调拨出库，6-盘点盈亏，7-管理员调整',
    change_quantity INT NOT NULL COMMENT '变动数量（正数表示增加，负数表示减少）',
    before_total_stock INT UNSIGNED NOT NULL COMMENT '变动前总库存',
    after_total_stock INT UNSIGNED NOT NULL COMMENT '变动后总库存',
    before_available_stock INT UNSIGNED NOT NULL COMMENT '变动前可用库存',
    after_available_stock INT UNSIGNED NOT NULL COMMENT '变动后可用库存',
    before_frozen_stock INT UNSIGNED NOT NULL COMMENT '变动前冻结库存',
    after_frozen_stock INT UNSIGNED NOT NULL COMMENT '变动后冻结库存',
    related_no VARCHAR(32) COMMENT '关联业务编号（订单号/采购单号/调拨单号等）',
    remark VARCHAR(255) COMMENT '备注',
    operator_id BIGINT UNSIGNED COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sku_id (sku_id),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_change_type (change_type),
    INDEX idx_related_no (related_no),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_stock_log_sku FOREIGN KEY (sku_id) REFERENCES t_product_sku(sku_id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_log_warehouse FOREIGN KEY (warehouse_id) REFERENCES t_warehouse_info(warehouse_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变动记录表';

-- =====================================================
-- 订单管理表模块
-- =====================================================

-- 3.15 订单主表
CREATE TABLE t_order_main (
    order_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(32) UNIQUE NOT NULL COMMENT '订单唯一编号',
    parent_order_no VARCHAR(32) COMMENT '父订单号（拆单时使用）',
    member_id BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
    member_no VARCHAR(32) NOT NULL COMMENT '会员唯一编号',
    member_nickname VARCHAR(50) NOT NULL COMMENT '会员昵称',
    member_mobile VARCHAR(20) NOT NULL COMMENT '会员手机号',
    order_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单类型：1-普通订单，2-拼团订单，3-秒杀订单，4-积分兑换订单',
    order_source TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单来源：1-小程序，2-APP，3-PC官网，4-H5',
    order_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-待发货，2-已发货，3-已签收，4-已完成，5-已取消，6-退款中，7-退款完成，8-退货退款中，9-退货退款完成',
    pay_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-支付中，2-支付成功，3-支付失败，4-已退款',
    delivery_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '配送状态：0-未发货，1-发货中，2-已发货，3-部分签收，4-已签收，5-退货中，6-已退货',
    consignee_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    consignee_mobile VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    region_province_id INT UNSIGNED NOT NULL COMMENT '所在省份ID',
    region_city_id INT UNSIGNED NOT NULL COMMENT '所在城市ID',
    region_district_id INT UNSIGNED NOT NULL COMMENT '所在区县ID',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    zip_code VARCHAR(10) COMMENT '邮政编码',
    goods_amount DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '商品总金额',
    freight_amount DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '运费金额',
    discount_amount DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '优惠总金额',
    coupon_amount DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '优惠券优惠金额',
    points_amount DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
    vip_discount_amount DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT 'VIP折扣金额',
    pay_amount DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '实际支付金额',
    use_points INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用积分数量',
    give_points INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '赠送积分数量',
    coupon_id BIGINT UNSIGNED COMMENT '使用的优惠券ID',
    coupon_no VARCHAR(32) COMMENT '使用的优惠券编号',
    freight_template_id INT UNSIGNED COMMENT '运费模板ID',
    buyer_remark VARCHAR(255) COMMENT '买家留言',
    seller_remark VARCHAR(255) COMMENT '卖家备注',
    admin_remark VARCHAR(255) COMMENT '管理员备注',
    pay_time DATETIME COMMENT '支付时间',
    delivery_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '签收时间',
    complete_time DATETIME COMMENT '订单完成时间',
    cancel_time DATETIME COMMENT '订单取消时间',
    cancel_reason VARCHAR(255) COMMENT '订单取消原因',
    auto_cancel_time DATETIME COMMENT '自动取消时间',
    auto_complete_time DATETIME COMMENT '自动完成时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_no (order_no),
    INDEX idx_parent_order_no (parent_order_no),
    INDEX idx_member_id (member_id),
    INDEX idx_order_type (order_type),
    INDEX idx_order_status (order_status),
    INDEX idx_pay_status (pay_status),
    INDEX idx_delivery_status (delivery_status),
    INDEX idx_create_time (create_time),
    INDEX idx_pay_time (pay_time),
    INDEX idx_complete_time (complete_time),
    CONSTRAINT fk_order_member FOREIGN KEY (member_id) REFERENCES t_member_info(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- 3.16 订单商品明细表
CREATE TABLE t_order_goods (
    goods_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '订单商品ID',
    order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单唯一编号',
    spu_id BIGINT UNSIGNED NOT NULL COMMENT '商品SPU ID',
    spu_no VARCHAR(32) NOT NULL COMMENT '商品SPU唯一编号',
    sku_id BIGINT UNSIGNED NOT NULL COMMENT '商品SKU ID',
    sku_no VARCHAR(32) NOT NULL COMMENT '商品SKU唯一编号',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    product_subtitle VARCHAR(500) COMMENT '商品副标题',
    main_image_url VARCHAR(255) NOT NULL COMMENT '商品主图URL',
    sku_attrs_simple VARCHAR(255) NOT NULL COMMENT 'SKU属性组合简化版',
    unit VARCHAR(20) NOT NULL DEFAULT '件' COMMENT '商品单位',
    purchase_quantity INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '购买数量',
    market_price DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '市场原价',
    sale_price DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '销售价格',
    vip_price DECIMAL(12,2) UNSIGNED COMMENT 'VIP专享价格',
    goods_amount DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '商品小计金额',
    discount_amount DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '优惠小计金额',
    pay_amount DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '实际支付小计金额',
    give_points INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '赠送积分小计',
    warehouse_id INT UNSIGNED COMMENT '发货仓库ID',
    warehouse_no VARCHAR(32) COMMENT '发货仓库编号',
    is_gift TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否赠品：0-否，1-是',
    is_commented TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已评价：0-否，1-是',
    review_id BIGINT UNSIGNED COMMENT '评价ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no),
    INDEX idx_spu_id (spu_id),
    INDEX idx_sku_id (sku_id),
    INDEX idx_is_commented (is_commented),
    CONSTRAINT fk_goods_order FOREIGN KEY (order_id) REFERENCES t_order_main(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_goods_spu FOREIGN KEY (spu_id) REFERENCES t_product_spu(spu_id) ON DELETE CASCADE,
    CONSTRAINT fk_goods_sku FOREIGN KEY (sku_id) REFERENCES t_product_sku(sku_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品明细表';

-- 3.17 订单支付记录表
CREATE TABLE t_order_payment (
    payment_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '支付记录ID',
    payment_no VARCHAR(32) UNIQUE NOT NULL COMMENT '支付流水号',
    order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单唯一编号',
    member_id BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
    member_no VARCHAR(32) NOT NULL COMMENT '会员唯一编号',
    pay_type TINYINT UNSIGNED NOT NULL COMMENT '支付方式：1-微信支付，2-支付宝支付，3-银联支付，4-余额支付，5-积分支付',
    pay_channel TINYINT UNSIGNED NOT NULL COMMENT '支付渠道：1-小程序，2-APP，3-H5，4-PC',
    pay_amount DECIMAL(12,2) UNSIGNED NOT NULL COMMENT '支付金额',
    pay_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付中，2-支付成功，3-支付失败',
    third_payment_no VARCHAR(64) COMMENT '第三方支付流水号',
    pay_time DATETIME COMMENT '支付成功时间',
    pay_fail_reason VARCHAR(255) COMMENT '支付失败原因',
    notify_data TEXT COMMENT '第三方支付回调数据（JSON格式）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_payment_no (payment_no),
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no),
    INDEX idx_member_id (member_id),
    INDEX idx_pay_type (pay_type),
    INDEX idx_pay_status (pay_status),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES t_order_main(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单支付记录表';

-- 3.18 订单配送记录表
CREATE TABLE t_order_delivery (
    delivery_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '配送记录ID',
    delivery_no VARCHAR(32) UNIQUE NOT NULL COMMENT '配送流水号',
    order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单唯一编号',
    member_id BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
    member_no VARCHAR(32) NOT NULL COMMENT '会员唯一编号',
    delivery_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '配送类型：1-快递配送，2-同城配送，3-自提',
    delivery_company_id INT UNSIGNED COMMENT '快递公司ID',
    delivery_company_name VARCHAR(50) COMMENT '快递公司名称',
    tracking_no VARCHAR(64) COMMENT '快递单号',
    warehouse_id INT UNSIGNED COMMENT '发货仓库ID',
    warehouse_no VARCHAR(32) COMMENT '发货仓库编号',
    consignee_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    consignee_mobile VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    region_province_id INT UNSIGNED NOT NULL COMMENT '所在省份ID',
    region_city_id INT UNSIGNED NOT NULL COMMENT '所在城市ID',
    region_district_id INT UNSIGNED NOT NULL COMMENT '所在区县ID',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    delivery_remark VARCHAR(255) COMMENT '配送备注',
    delivery_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '配送状态：0-待发货，1-发货中，2-已发货，3-已签收，4-退货中，5-已退货',
    send_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '签收时间',
    delivery_fee DECIMAL(12,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '配送费',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_delivery_no (delivery_no),
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no),
    INDEX idx_member_id (member_id),
    INDEX idx_tracking_no (tracking_no),
    INDEX idx_delivery_status (delivery_status),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_delivery_order FOREIGN KEY (order_id) REFERENCES t_order_main(order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单配送记录表';

-- =====================================================
-- 预设数据模块
-- =====================================================

-- 4.1 预设地区数据（简化版，仅包含部分顶级、地级市、区县）
INSERT INTO t_common_region (parent_id, region_name, region_type, sort_order) VALUES
(0, '北京市', 1, 1),
(0, '上海市', 1, 2),
(0, '广东省', 1, 3),
(1, '北京市', 2, 1),
(2, '上海市', 2, 1),
(3, '广州市', 2, 1),
(3, '深圳市', 2, 2),
(4, '东城区', 3, 1),
(4, '西城区', 3, 2),
(4, '朝阳区', 3, 3);

-- 4.2 预设仓库数据
INSERT INTO t_warehouse_info (warehouse_no, warehouse_name, warehouse_manager, manager_mobile, region_province_id, region_city_id, region_district_id, detail_address, warehouse_type, is_default) VALUES
('WH202X0001', '北京主仓', '张三', '13800138001', 1, 4, 10, '北京市朝阳区东四环北路100号', 1, 1),
('WH202X0002', '广州分仓', '李四', '13800138002', 3, 6, 1, '广东省广州市天河区珠江新城1号', 2, 0);

-- 4.3 预设商品分类数据
INSERT INTO t_product_category (parent_id, category_name, sort_order, is_show) VALUES
(0, '益智玩具', 1, 1),
(0, '模型玩具', 2, 1),
(0, '户外玩具', 3, 1),
(1, '积木拼插', 1, 1),
(1, '拼图', 2, 1),
(2, '汽车模型', 1, 1),
(2, '飞机模型', 2, 1);

-- 4.4 预设商品品牌数据
INSERT INTO t_product_brand (brand_name, brand_logo, brand_desc, sort_order, is_show) VALUES
('乐高', 'https://example.com/logo/lego.png', '来自丹麦的全球知名积木品牌', 1, 1),
('奥迪双钻', 'https://example.com/logo/audi.png', '中国知名玩具品牌，以四驱车闻名', 2, 1),
('费雪', 'https://example.com/logo/fisher.png', '来自美国的婴幼儿玩具品牌', 3, 1);

-- 4.5 预设商品属性组和属性值数据
INSERT INTO t_product_attr_group (group_name, category_id, sort_order) VALUES
('颜色', 0, 1),
('尺码/规格', 0, 2),
('适用年龄', 0, 3);

INSERT INTO t_product_attr_value (group_id, attr_value, attr_color, sort_order) VALUES
(1, '红色', '#FF0000', 1),
(1, '蓝色', '#0000FF', 2),
(1, '黄色', '#FFFF00', 3),
(2, '基础款', NULL, 1),
(2, '豪华款', NULL, 2),
(2, '收藏款', NULL, 3),
(3, '0-3岁', NULL, 1),
(3, '3-6岁', NULL, 2),
(3, '6-12岁', NULL, 3);

-- 4.6 预设会员等级规则（此处简化，用t_member_info的member_level配合业务逻辑实现）
-- 预设普通测试会员（密码为123456的BCrypt哈希值）
INSERT INTO t_member_info (member_no, username, password_hash, nickname, mobile, member_level, register_source) VALUES
('M202X00000001', 'test001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试会员001', '13900139001', 1, 1),
('M202X00000002', 'test002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试会员002', '13900139002', 2, 1);
```