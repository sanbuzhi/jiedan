===FILE:back/tongquyouyi.sql===
```sql
-- 数据库初始化脚本
-- 目标环境：MySQL 8.0.35+
-- 引擎：InnoDB
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_0900_ai_ci（兼容emoji及更细粒度排序）

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET FOREIGN_KEY_CHECKS = 0;
SET AUTOCOMMIT = 0;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS tongquyouyi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE tongquyouyi;

-- ----------------------------
-- 1. 公共配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT COMMENT '配置值',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  `config_desc` VARCHAR(255) DEFAULT '' COMMENT '配置说明',
  `group_code` VARCHAR(50) DEFAULT 'common' COMMENT '配置分组编码',
  `sort` INT UNSIGNED DEFAULT 0 COMMENT '排序',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公共配置表';

-- ----------------------------
-- 2. 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_type_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
  `dict_type_code` VARCHAR(100) NOT NULL COMMENT '字典类型编码（唯一）',
  `dict_type_name` VARCHAR(100) NOT NULL COMMENT '字典类型名称',
  `dict_type_desc` VARCHAR(255) DEFAULT '' COMMENT '字典类型说明',
  `sort` INT UNSIGNED DEFAULT 0 COMMENT '排序',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dict_type_id`),
  UNIQUE KEY `uk_dict_type_code` (`dict_type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ----------------------------
-- 3. 字典数据表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_data_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典数据ID',
  `dict_type_id` BIGINT UNSIGNED NOT NULL COMMENT '字典类型ID',
  `dict_data_value` VARCHAR(100) NOT NULL COMMENT '字典数据值',
  `dict_data_label` VARCHAR(100) NOT NULL COMMENT '字典数据标签',
  `dict_data_desc` VARCHAR(255) DEFAULT '' COMMENT '字典数据说明',
  `sort` INT UNSIGNED DEFAULT 0 COMMENT '排序',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dict_data_id`),
  KEY `idx_dict_type_id` (`dict_type_id`),
  UNIQUE KEY `uk_type_value` (`dict_type_id`, `dict_data_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ----------------------------
-- 4. 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
  `category_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT UNSIGNED DEFAULT 0 COMMENT '父分类ID（0表示顶级分类）',
  `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `category_level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '分类层级（1-3级）',
  `category_path` VARCHAR(255) DEFAULT '' COMMENT '分类路径（父ID用逗号分隔，如0,1,2）',
  `category_icon` VARCHAR(500) DEFAULT '' COMMENT '分类图标URL',
  `sort` INT UNSIGNED DEFAULT 0 COMMENT '排序',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_category_level` (`category_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- ----------------------------
-- 5. 商品SPU表（标准商品单元）
-- ----------------------------
DROP TABLE IF EXISTS `goods_spu`;
CREATE TABLE `goods_spu` (
  `spu_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '所属分类ID',
  `spu_name` VARCHAR(200) NOT NULL COMMENT 'SPU名称',
  `spu_main_img` VARCHAR(500) DEFAULT '' COMMENT 'SPU主图URL',
  `spu_sub_imgs` JSON DEFAULT NULL COMMENT 'SPU副图URL列表（JSON数组）',
  `spu_detail` TEXT COMMENT 'SPU详情（HTML/Markdown）',
  `spu_status` TINYINT UNSIGNED DEFAULT 0 COMMENT 'SPU状态：0-草稿 1-待上架 2-已上架 3-已下架 4-已删除',
  `shelf_time` DATETIME DEFAULT NULL COMMENT '上架时间',
  `unshelf_time` DATETIME DEFAULT NULL COMMENT '下架时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`spu_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_spu_status` (`spu_status`),
  KEY `idx_shelf_time` (`shelf_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU表';

-- ----------------------------
-- 6. 商品SKU表（库存单元）
-- ----------------------------
DROP TABLE IF EXISTS `goods_sku`;
CREATE TABLE `goods_sku` (
  `sku_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT '所属SPU ID',
  `sku_code` VARCHAR(100) NOT NULL COMMENT 'SKU编码（唯一）',
  `sku_name` VARCHAR(200) NOT NULL COMMENT 'SKU名称',
  `spec_combination` JSON NOT NULL COMMENT '规格组合（JSON对象，如{"颜色":"红色","尺码":"M"}）',
  `sku_main_img` VARCHAR(500) DEFAULT '' COMMENT 'SKU主图URL',
  `sku_price` DECIMAL(10,2) NOT NULL COMMENT 'SKU销售单价',
  `sku_cost_price` DECIMAL(10,2) DEFAULT NULL COMMENT 'SKU成本价',
  `sku_status` TINYINT UNSIGNED DEFAULT 1 COMMENT 'SKU状态：0-禁用 1-启用 2-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sku_id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_sku_status` (`sku_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- ----------------------------
-- 7. 仓库表
-- ----------------------------
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse` (
  `warehouse_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `warehouse_name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
  `warehouse_address` VARCHAR(500) DEFAULT '' COMMENT '仓库地址',
  `warehouse_contact` VARCHAR(50) DEFAULT '' COMMENT '仓库联系人',
  `warehouse_phone` VARCHAR(20) DEFAULT '' COMMENT '仓库联系电话',
  `sort` INT UNSIGNED DEFAULT 0 COMMENT '排序',
  `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库表';

-- ----------------------------
-- 8. 库存管理表
-- ----------------------------
DROP TABLE IF EXISTS `goods_inventory`;
CREATE TABLE `goods_inventory` (
  `inventory_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
  `available_qty` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '可用库存',
  `locked_qty` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '锁定库存（已下单未发货）',
  `total_qty` INT UNSIGNED GENERATED ALWAYS AS (`available_qty` + `locked_qty`) STORED COMMENT '总库存（虚拟列自动计算）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`inventory_id`),
  UNIQUE KEY `uk_sku_warehouse` (`sku_id`, `warehouse_id`),
  KEY `idx_available_qty` (`available_qty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存管理表';

-- ----------------------------
-- 9. 会员管理表
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `member_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `member_phone` VARCHAR(20) NOT NULL COMMENT '会员手机号（唯一）',
  `member_nickname` VARCHAR(100) DEFAULT '' COMMENT '会员昵称',
  `member_avatar` VARCHAR(500) DEFAULT '' COMMENT '会员头像URL',
  `member_password` VARCHAR(255) NOT NULL COMMENT '会员密码（BCrypt加密）',
  `member_points` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '会员积分',
  `member_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '会员余额',
  `member_level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '会员等级（关联字典member_level）',
  `member_status` TINYINT UNSIGNED DEFAULT 1 COMMENT '会员状态：0-禁用 1-正常',
  `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT '' COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_member_phone` (`member_phone`),
  KEY `idx_member_level` (`member_level`),
  KEY `idx_member_status` (`member_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员管理表';

-- ----------------------------
-- 10. 订单管理表
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `order_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID（内部主键）',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号（唯一对外展示）',
  `member_id` BIGINT UNSIGNED NOT NULL COMMENT '会员ID',
  `order_total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `order_freight` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单运费',
  `order_discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单优惠金额',
  `order_pay_amount` DECIMAL(10,2) NOT NULL COMMENT '订单实付金额',
  `order_points_used` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单使用积分',
  `order_points_gained` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单获得积分',
  `pay_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式（关联字典pay_type）',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `order_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单状态：0-待付款 1-待发货 2-已发货 3-待收货 4-已完成 5-已取消 6-售后中',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT '' COMMENT '取消原因',
  `address_info` JSON NOT NULL COMMENT '收货地址信息（JSON对象，包含收货人、电话、省市区、详细地址）',
  `order_remark` VARCHAR(500) DEFAULT '' COMMENT '订单备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单管理表';

-- ----------------------------
-- 11. 订单明细表
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `item_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
  `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
  `sku_code` VARCHAR(100) NOT NULL COMMENT 'SKU编码（快照）',
  `goods_snapshot` JSON NOT NULL COMMENT '商品快照（JSON对象，包含商品名称、主图、规格组合）',
  `goods_price` DECIMAL(10,2) NOT NULL COMMENT '商品销售单价（快照）',
  `goods_qty` INT UNSIGNED NOT NULL COMMENT '商品数量',
  `goods_total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品小计金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`item_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- ----------------------------
-- 预设数据
-- ----------------------------
-- 1. 预设仓库
INSERT INTO `warehouse` (`warehouse_id`, `warehouse_name`, `warehouse_address`, `warehouse_contact`, `warehouse_phone`, `sort`) VALUES
(1, '总部总仓', '北京市朝阳区xxx路xxx号', '张管理员', '13800138000', 1);

-- 2. 预设字典类型
INSERT INTO `sys_dict_type` (`dict_type_id`, `dict_type_code`, `dict_type_name`, `dict_type_desc`, `sort`) VALUES
(1, 'pay_type', '支付方式', '订单支付方式字典', 1),
(2, 'order_status', '订单状态', '订单状态字典', 2),
(3, 'member_level', '会员等级', '会员等级字典', 3);

-- 3. 预设字典数据
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_data_value`, `dict_data_label`, `sort`) VALUES
(1, '1', '微信支付', 1),
(1, '2', '支付宝支付', 2),
(1, '3', '余额支付', 3),
(2, '0', '待付款', 1),
(2, '1', '待发货', 2),
(2, '2', '已发货', 3),
(2, '3', '待收货', 4),
(2, '4', '已完成', 5),
(2, '5', '已取消', 6),
(2, '6', '售后中', 7),
(3, '1', '普通会员', 1),
(3, '2', '黄金会员', 2),
(3, '3', '钻石会员', 3);

-- 4. 预设公共配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `group_code`, `sort`) VALUES
('site_name', '童趣友益', '网站名称', 'site', 1),
('site_desc', '专注儿童益智玩具', '网站描述', 'site', 2),
('freight_threshold', '99.00', '包邮门槛金额', 'order', 1),
('default_freight', '8.00', '默认运费', 'order', 2);

-- 5. 预设商品分类
INSERT INTO `goods_category` (`category_id`, `parent_id`, `category_name`, `category_level`, `category_path`, `sort`) VALUES
(1, 0, '积木拼插', 1, '0', 1),
(2, 0, '益智桌游', 1, '0', 2),
(3, 0, '绘画手工', 1, '0', 3),
(4, 1, '乐高式积木', 2, '0,1', 1),
(5, 1, '磁力积木', 2, '0,1', 2),
(6, 2, '逻辑推理', 2, '0,2', 1);

-- 6. 预设会员（测试用，密码：123456，BCrypt加密后的结果）
INSERT INTO `member` (`member_id`, `member_phone`, `member_nickname`, `member_password`, `member_points`, `member_balance`, `member_level`) VALUES
(1, '13900139000', '测试会员1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 100, 50.00, 1),
(2, '13900139001', '测试会员2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 500, 200.00, 2);

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;
```