===FILE:back/tongquyouyi.sql===
```sql
-- 数据库初始化脚本
-- 适配版本：MySQL 8.0.35
-- 引擎：InnoDB
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_0900_ai_ci
-- 创建时间：202X-XX-XX

-- 1. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `tongquyouyi_db` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_0900_ai_ci;

USE `tongquyouyi_db`;

-- 2. 公共基础表：区域字典表
CREATE TABLE IF NOT EXISTS `t_common_area` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父区域ID（0=顶级省市区）',
    `area_code` VARCHAR(20) NOT NULL COMMENT '行政区划编码',
    `area_name` VARCHAR(50) NOT NULL COMMENT '区域名称',
    `area_level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '区域层级（1=省/直辖市/自治区，2=市，3=区/县）',
    `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序字段（升序）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_area_code` (`area_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公共区域字典表';

-- 3. 公共基础表：数据字典主表
CREATE TABLE IF NOT EXISTS `t_common_dict` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码（唯一标识）',
    `dict_name` VARCHAR(50) NOT NULL COMMENT '字典名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '字典描述',
    `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序字段（升序）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_code` (`dict_code`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公共数据字典主表';

-- 4. 公共基础表：数据字典明细表
CREATE TABLE IF NOT EXISTS `t_common_dict_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_id` BIGINT UNSIGNED NOT NULL COMMENT '关联字典主表ID',
    `item_code` VARCHAR(50) NOT NULL COMMENT '字典项编码（同一字典下唯一）',
    `item_name` VARCHAR(50) NOT NULL COMMENT '字典项名称',
    `item_value` VARCHAR(100) DEFAULT NULL COMMENT '字典项值（可选扩展字段）',
    `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序字段（升序）',
    `is_enabled` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否启用（0=否，1=是）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_id_item_code` (`dict_id`, `item_code`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公共数据字典明细表';

-- 5. 商品管理表：商品分类表
CREATE TABLE IF NOT EXISTS `t_goods_category` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID（0=顶级分类）',
    `category_code` VARCHAR(50) NOT NULL COMMENT '分类编码（唯一标识）',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '分类图标URL',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序字段（升序）',
    `is_enabled` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否启用（0=否，1=是）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- 6. 商品管理表：商品SPU表
CREATE TABLE IF NOT EXISTS `t_goods_spu` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `spu_code` VARCHAR(50) NOT NULL COMMENT 'SPU编码（唯一标识）',
    `spu_name` VARCHAR(200) NOT NULL COMMENT 'SPU商品名称',
    `category_id` BIGINT UNSIGNED NOT NULL COMMENT '关联分类表ID',
    `brand_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联品牌表ID（预留）',
    `main_image_url` VARCHAR(500) NOT NULL COMMENT '商品主图URL',
    `sub_image_urls` TEXT DEFAULT NULL COMMENT '商品副图URL列表（JSON格式数组）',
    `description` TEXT DEFAULT NULL COMMENT '商品富文本描述',
    `is_hot` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否热门（0=否，1=是）',
    `is_new` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否新品（0=否，1=是）',
    `is_recommend` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否推荐（0=否，1=是）',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '商品状态（0=下架，1=上架，2=草稿）',
    `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序字段（升序）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_code` (`spu_code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_hot` (`is_hot`),
    KEY `idx_is_new` (`is_new`),
    KEY `idx_is_recommend` (`is_recommend`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_deleted` (`is_deleted`),
    FULLTEXT KEY `ft_spu_name` (`spu_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU表';

-- 7. 商品管理表：商品SKU表
CREATE TABLE IF NOT EXISTS `t_goods_sku` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sku_code` VARCHAR(50) NOT NULL COMMENT 'SKU编码（唯一标识）',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT '关联SPU表ID',
    `spec_values` TEXT NOT NULL COMMENT 'SKU规格值（JSON格式数组，如[{"spec_id":1,"spec_name":"颜色","spec_value":"红色"},{"spec_id":2,"spec_name":"尺寸","spec_value":"S"}]）',
    `spec_simple` VARCHAR(200) DEFAULT NULL COMMENT 'SKU规格简化文本（用于前端展示，如“红色 S”）',
    `sku_image_url` VARCHAR(500) DEFAULT NULL COMMENT 'SKU图片URL（为空时使用SPU主图）',
    `original_price` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '原价',
    `sell_price` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '售价',
    `cost_price` DECIMAL(10,2) UNSIGNED DEFAULT NULL COMMENT '成本价（仅后台可见）',
    `weight` DECIMAL(8,2) UNSIGNED DEFAULT NULL COMMENT '重量（单位：kg）',
    `volume` DECIMAL(10,2) UNSIGNED DEFAULT NULL COMMENT '体积（单位：m³）',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'SKU状态（0=下架，1=上架）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_status` (`status`),
    KEY `idx_sell_price` (`sell_price`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- 8. 库存管理表：商品库存表
CREATE TABLE IF NOT EXISTS `t_inventory_goods` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT '关联SKU表ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '关联仓库表ID（默认1号主仓）',
    `stock_quantity` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    `lock_quantity` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '锁定库存数量（下单未付款/未发货）',
    `available_quantity` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存数量（stock_quantity - lock_quantity）',
    `warning_quantity` INT UNSIGNED NOT NULL DEFAULT 10 COMMENT '库存预警阈值',
    `is_warning` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否触发预警（0=否，1=是）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_id_warehouse_id` (`sku_id`, `warehouse_id`),
    KEY `idx_available_quantity` (`available_quantity`),
    KEY `idx_is_warning` (`is_warning`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品库存表';

-- 9. 库存管理表：库存流水表
CREATE TABLE IF NOT EXISTS `t_inventory_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `log_code` VARCHAR(50) NOT NULL COMMENT '流水编码（唯一标识）',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT '关联SKU表ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '关联仓库表ID',
    `change_type` TINYINT UNSIGNED NOT NULL COMMENT '变动类型（1=采购入库，2=销售出库，3=退货入库，4=调拨出库，5=调拨入库，6=盘点调整，7=其他）',
    `change_quantity` INT NOT NULL COMMENT '变动数量（正数=增加，负数=减少）',
    `before_quantity` INT UNSIGNED NOT NULL COMMENT '变动前库存数量',
    `after_quantity` INT UNSIGNED NOT NULL COMMENT '变动后库存数量',
    `related_bill_code` VARCHAR(50) DEFAULT NULL COMMENT '关联业务单据编码（如订单号、采购单号）',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID（预留）',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名（预留）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_log_code` (`log_code`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_warehouse_id` (`warehouse_id`),
    KEY `idx_change_type` (`change_type`),
    KEY `idx_related_bill_code` (`related_bill_code`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存流水表';

-- 10. 会员管理表：会员表
CREATE TABLE IF NOT EXISTS `t_member` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_code` VARCHAR(50) NOT NULL COMMENT '会员编码（唯一标识）',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '登录用户名（可选，第三方登录时为空）',
    `password` VARCHAR(255) DEFAULT NULL COMMENT '登录密码（BCrypt加密，第三方登录时为空）',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号（唯一标识）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '会员昵称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '会员头像URL',
    `gender` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '性别（0=未知，1=男，2=女）',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `level_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '关联会员等级表ID（默认普通会员）',
    `points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '当前积分',
    `total_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计积分',
    `balance` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '当前余额',
    `total_recharge` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '累计充值金额',
    `total_consume` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
    `register_channel` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '注册渠道（1=手机号注册，2=微信，3=支付宝，4=其他）',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '会员状态（0=禁用，1=正常）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_code` (`member_code`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_level_id` (`level_id`),
    KEY `idx_status` (`status`),
    KEY `idx_register_channel` (`register_channel`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员表';

-- 11. 会员管理表：会员等级表
CREATE TABLE IF NOT EXISTS `t_member_level` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_code` VARCHAR(50) NOT NULL COMMENT '等级编码（唯一标识）',
    `level_name` VARCHAR(50) NOT NULL COMMENT '等级名称',
    `level_icon_url` VARCHAR(500) DEFAULT NULL COMMENT '等级图标URL',
    `min_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '升级所需最低积分',
    `discount_rate` DECIMAL(3,2) UNSIGNED NOT NULL DEFAULT 1.00 COMMENT '会员折扣率（1.00=无折扣，0.95=95折）',
    `points_multiplier` DECIMAL(3,2) UNSIGNED NOT NULL DEFAULT 1.00 COMMENT '积分倍率（1.00=正常倍率，1.2=1.2倍）',
    `privileges` TEXT DEFAULT NULL COMMENT '会员权益（JSON格式数组）',
    `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序字段（升序）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level_code` (`level_code`),
    KEY `idx_min_points` (`min_points`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员等级表';

-- 12. 会员管理表：会员收货地址表
CREATE TABLE IF NOT EXISTS `t_member_address` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id` BIGINT UNSIGNED NOT NULL COMMENT '关联会员表ID',
    `consignee_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `consignee_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    `province_id` BIGINT UNSIGNED NOT NULL COMMENT '关联省ID',
    `city_id` BIGINT UNSIGNED NOT NULL COMMENT '关联市ID',
    `district_id` BIGINT UNSIGNED NOT NULL COMMENT '关联区/县ID',
    `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `full_address` VARCHAR(500) NOT NULL COMMENT '完整地址（省市区+详细地址）',
    `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
    `is_default` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认地址（0=否，1=是）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员收货地址表';

-- 13. 订单管理表：订单主表
CREATE TABLE IF NOT EXISTS `t_order` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_code` VARCHAR(50) NOT NULL COMMENT '订单编号（唯一标识，建议格式：TQYY+年月日+8位随机数）',
    `member_id` BIGINT UNSIGNED NOT NULL COMMENT '关联会员表ID',
    `member_name` VARCHAR(50) DEFAULT NULL COMMENT '下单时会员昵称/姓名（冗余）',
    `member_phone` VARCHAR(20) DEFAULT NULL COMMENT '下单时会员手机号（冗余）',
    `total_quantity` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单商品总数量',
    `total_amount` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '订单总金额（商品总价）',
    `discount_amount` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '优惠总金额（包含满减、折扣、优惠券等）',
    `freight_amount` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '运费金额',
    `pay_amount` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '实付金额（total_amount - discount_amount + freight_amount）',
    `balance_pay` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '余额支付金额',
    `points_pay` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '积分支付数量',
    `points_deduct` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
    `coupon_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联优惠券表ID（预留）',
    `coupon_deduct` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '优惠券抵扣金额',
    `consignee_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名（冗余）',
    `consignee_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号（冗余）',
    `consignee_province` VARCHAR(50) NOT NULL COMMENT '收货人省（冗余）',
    `consignee_city` VARCHAR(50) NOT NULL COMMENT '收货人市（冗余）',
    `consignee_district` VARCHAR(50) NOT NULL COMMENT '收货人区/县（冗余）',
    `consignee_detail` VARCHAR(200) NOT NULL COMMENT '收货人详细地址（冗余）',
    `consignee_full` VARCHAR(500) NOT NULL COMMENT '收货人完整地址（冗余）',
    `order_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态（0=待付款，1=待发货，2=已发货，3=已完成，4=已取消，5=退款中，6=已退款）',
    `pay_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '支付状态（0=未支付，1=已支付，2=部分支付，3=已退款）',
    `pay_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式（1=微信支付，2=支付宝，3=余额支付，4=积分+混合，5=其他）',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `pay_trade_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付交易号',
    `ship_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '配送方式（1=快递，2=自提，3=同城配送）',
    `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `ship_company` VARCHAR(50) DEFAULT NULL COMMENT '快递公司名称',
    `ship_no` VARCHAR(100) DEFAULT NULL COMMENT '快递单号',
    `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注（用户填写）',
    `admin_remark` VARCHAR(500) DEFAULT NULL COMMENT '管理员备注（仅后台可见）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_code` (`order_code`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_pay_type` (`pay_type`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_pay_time` (`pay_time`),
    KEY `idx_ship_time` (`ship_time`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

-- 14. 订单管理表：订单明细表
CREATE TABLE IF NOT EXISTS `t_order_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '关联订单主表ID',
    `order_code` VARCHAR(50) NOT NULL COMMENT '订单编号（冗余）',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT '关联SPU表ID',
    `spu_code` VARCHAR(50) NOT NULL COMMENT 'SPU编码（冗余）',
    `spu_name` VARCHAR(200) NOT NULL COMMENT 'SPU商品名称（冗余）',
    `spu_main_image_url` VARCHAR(500) NOT NULL COMMENT 'SPU主图URL（冗余）',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT '关联SKU表ID',
    `sku_code` VARCHAR(50) NOT NULL COMMENT 'SKU编码（冗余）',
    `sku_spec_simple` VARCHAR(200) NOT NULL COMMENT 'SKU规格简化文本（冗余）',
    `sku_image_url` VARCHAR(500) DEFAULT NULL COMMENT 'SKU图片URL（冗余）',
    `original_price` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '下单时原价（冗余）',
    `sell_price` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '下单时售价（冗余）',
    `purchase_quantity` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '购买数量',
    `total_amount` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '该商品小计金额（sell_price * purchase_quantity）',
    `discount_amount` DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '该商品优惠金额',
    `points_give` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '该商品赠送积分',
    `is_commented` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已评价（0=否，1=是）',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除（0=否，1=是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_code` (`order_code`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_is_commented` (`is_commented`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- 15. 订单管理表：订单状态日志表
CREATE TABLE IF NOT EXISTS `t_order_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '关联订单主表ID',
    `order_code` VARCHAR(50) NOT NULL COMMENT '订单编号（冗余）',
    `action` VARCHAR(50) NOT NULL COMMENT '操作动作（如“创建订单”、“付款成功”、“发货”等）',
    `before_status` TINYINT UNSIGNED DEFAULT NULL COMMENT '操作前订单状态',
    `after_status` TINYINT UNSIGNED DEFAULT NULL COMMENT '操作后订单状态',
    `operator_type` TINYINT UNSIGNED NOT NULL COMMENT '操作人类型（1=系统，2=会员，3=管理员）',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '操作备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_code` (`order_code`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单状态日志表';

-- ==================== 预设数据 ====================
-- 1. 预设数据字典主表
INSERT INTO `t_common_dict` (`dict_code`, `dict_name`, `description`, `sort_order`) VALUES
('ORDER_STATUS', '订单状态', '订单全流程状态枚举', 1),
('PAY_STATUS', '支付状态', '订单支付状态枚举', 2),
('PAY_TYPE', '支付方式', '订单支付方式枚举', 3),
('SHIP_TYPE', '配送方式', '订单配送方式枚举', 4),
('INVENTORY_CHANGE_TYPE', '库存变动类型', '库存流水变动类型枚举', 5),
('REGISTER_CHANNEL', '注册渠道', '会员注册渠道枚举', 6),
('GENDER', '性别', '会员性别枚举', 7);

-- 2. 预设数据字典明细表
INSERT INTO `t_common_dict_item` (`dict_id`, `item_code`, `item_name`, `item_value`, `sort_order`, `is_enabled`) VALUES
-- 订单状态（dict_id=1）
(1, 'WAIT_PAY', '待付款', '0', 1, 1),
(1, 'WAIT_SHIP', '待发货', '1', 2, 1),
(1, 'SHIPPED', '已发货', '2', 3, 1),
(1, 'COMPLETED', '已完成', '3', 4, 1),
(1, 'CANCELED', '已取消', '4', 5, 1),
(1, 'REFUNDING', '退款中', '5', 6, 1),
(1, 'REFUNDED', '已退款', '6', 7, 1),
-- 支付状态（dict_id=2）
(2, 'UNPAID', '未支付', '0', 1, 1),
(2, 'PAID', '已支付', '1', 2, 1),
(2, 'PARTIAL_PAID', '部分支付', '2', 3, 1),
(2, 'REFUNDED', '已退款', '3', 4, 1),
-- 支付方式（dict_id=3）
(3, 'WECHAT', '微信支付', '1', 1, 1),
(3, 'ALIPAY', '支付宝', '2', 2, 1),
(3, 'BALANCE', '余额支付', '3', 3, 1),
(3, 'MIXED', '积分+混合', '4', 4, 1),
(3, 'OTHER', '其他', '5', 5, 1),
-- 配送方式（dict_id=4）
(4, 'EXPRESS', '快递', '1', 1, 1),
(4, 'SELF_PICKUP', '自提', '2', 2, 1),
(4, 'LOCAL_DELIVERY', '同城配送', '3', 3, 1),
-- 库存变动类型（dict_id=5）
(5, 'PURCHASE_IN', '采购入库', '1', 1, 1),
(5, 'SALE_OUT', '销售出库', '2', 2, 1),
(5, 'RETURN_IN', '退货入库', '3', 3, 1),
(5, 'TRANSFER_OUT', '调拨出库', '4', 4, 1),
(5, 'TRANSFER_IN', '调拨入库', '5', 5, 1),
(5, 'INVENTORY_ADJUST', '盘点调整', '6', 6, 1),
(5, 'OTHER', '其他', '7', 7, 1),
-- 注册渠道（dict_id=6）
(6, 'PHONE', '手机号注册', '1', 1, 1),
(6, 'WECHAT', '微信', '2', 2, 1),
(6, 'ALIPAY', '支付宝', '3', 3, 1),
(6, 'OTHER', '其他', '4', 4, 1),
-- 性别（dict_id=7）
(7, 'UNKNOWN', '未知', '0', 1, 1),
(7, 'MALE', '男', '1', 2, 1),
(7, 'FEMALE', '女', '2', 3, 1);

-- 3. 预设会员等级表
INSERT INTO `t_member_level` (`level_code`, `level_name`, `min_points`, `discount_rate`, `points_multiplier`, `sort_order`) VALUES
('NORMAL', '普通会员', 0, 1.00, 1.00, 1),
('SILVER', '白银会员', 1000, 0.98, 1.10, 2),
('GOLD', '黄金会员', 5000, 0.95, 1.20, 3),
('PLATINUM', '铂金会员', 20000, 0.90, 1.50, 4),
('DIAMOND', '钻石会员', 50000, 0.85, 2.00, 5);

-- 4. 预设商品分类表
INSERT INTO `t_goods_category` (`parent_id`, `category_code`, `category_name`, `sort_order`, `is_enabled`) VALUES
(0, 'FOOD', '食品饮料', 1, 1),
(0, 'CLOTHING', '服装鞋包', 2, 1),
(0, 'ELECTRONICS', '数码家电', 3, 1),
(1, 'SNACKS', '零食', 1, 1),
(1, 'DRINKS', '饮料', 2, 1),
(2, 'MENS', '男装', 1, 1),
(2, 'WOMENS', '女装', 2, 1),
(3, 'PHONES', '手机', 1, 1),
(3, 'COMPUTERS', '电脑', 2, 1);

-- 5. 预设商品SPU表
INSERT INTO `t_goods_spu` (`spu_code`, `spu_name`, `category_id`, `main_image_url`, `is_hot`, `is_recommend`, `status`, `sort_order`) VALUES
('SPU202X001', '精选原味坚果礼盒装500g', 4, 'https://example.com/images/spu202x001_main.jpg', 1, 1, 1, 1),
('SPU202X002', '男士纯棉圆领短袖T恤', 6, 'https://example.com/images/spu202x002_main.jpg', 1, 0, 1, 2);

-- 6. 预设商品SKU表
INSERT INTO `t_goods_sku` (`sku_code`, `spu_id`, `spec_values`, `spec_simple`, `original_price`, `sell_price`, `cost_price`, `status`) VALUES
('SKU202X001001', 1, '[{"spec_id":1,"spec_name":"规格","spec_value":"500g"}]', '500g', 99.00, 69.00, 35.00, 1),
('SKU202X002001', 2, '[{"spec_id":2,"spec_name":"颜色","spec_value":"白色"},{"spec_id":3,"spec_name":"尺寸","spec_value":"M"}]', '白色 M', 129.00, 79.00, 30.00, 1),
('SKU202X002002', 2, '[{"spec_id":2,"spec_name":"颜色","spec_value":"白色"},{"spec_id":3,"spec_name":"尺寸","spec_value":"L"}]', '白色 L', 129.00, 79.00, 30.00, 1),
('SKU202X002003', 2, '[{"spec_id":2,"spec_name":"颜色","spec_value":"黑色"},{"spec_id":3,"spec_name":"尺寸","spec_value":"M"}]', '黑色 M', 129.00, 79.00, 30.00, 1),
('SKU202X002004', 2, '[{"spec_id":2,"spec_name":"颜色","spec_value":"黑色"},{"spec_id":3,"spec_name":"尺寸","spec_value":"L"}]', '黑色 L', 129.00, 79.00, 30.00, 1);

-- 7. 预设商品库存表
INSERT INTO `t_inventory_goods` (`sku_id`, `stock_quantity`, `lock_quantity`, `available_quantity`, `warning_quantity`) VALUES
(1, 1000, 0, 1000, 50),
(2, 500, 0, 500, 30),
(3, 500, 0, 500, 30),
(4, 500, 0, 500, 30),
(5, 500, 0, 500, 30);
```