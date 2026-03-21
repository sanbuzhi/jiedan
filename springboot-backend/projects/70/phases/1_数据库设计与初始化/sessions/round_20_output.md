===FILE:back/tongquyouyi.sql===
```sql
-- 童趣友谊项目数据库初始化脚本
-- 数据库版本：MySQL 8.0.35
-- 存储引擎：InnoDB
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci

-- 全局设置
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `tongquyouyi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '童趣友谊母婴/儿童用品商城数据库';
USE `tongquyouyi`;

-- ----------------------------
-- 公共表：系统配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT COMMENT '配置值',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ----------------------------
-- 公共表：字典类型表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型（如：gender, pay_type）',
  `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码（联合类型唯一）',
  `dict_label` VARCHAR(100) NOT NULL COMMENT '字典显示值',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号（升序）',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_type_code` (`dict_type`, `dict_code`),
  INDEX `idx_sys_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表';

-- ----------------------------
-- 公共表：后台管理员表
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '管理员账号（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '管理员昵称',
  `role` VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '角色：super_admin-超级管理员，admin-普通管理员',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_admin_username` (`username`),
  INDEX `idx_sys_admin_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员表';

-- ----------------------------
-- 会员管理表
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会员ID（自增主键，大数据友好）',
  `username` VARCHAR(50) NOT NULL COMMENT '会员账号（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '会员昵称',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女（关联sys_dict的gender）',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（唯一，登录/注册用）',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱（唯一）',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额（元）',
  `points` INT NOT NULL DEFAULT 0 COMMENT '可用积分',
  `total_points` INT NOT NULL DEFAULT 0 COMMENT '累计积分',
  `is_vip` TINYINT NOT NULL DEFAULT 0 COMMENT 'VIP标识：0-普通会员，1-VIP会员',
  `vip_expire_date` DATETIME DEFAULT NULL COMMENT 'VIP过期时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常（关联sys_dict的member_status）',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_username` (`username`),
  UNIQUE KEY `uk_member_phone` (`phone`),
  UNIQUE KEY `uk_member_email` (`email`),
  INDEX `idx_member_vip_status` (`is_vip`, `status`),
  INDEX `idx_member_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员管理表';

-- ----------------------------
-- 商品管理表：商品分类
-- ----------------------------
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` INT NOT NULL DEFAULT 0 COMMENT '父分类ID：0-一级分类',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号（升序）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  INDEX `idx_goods_category_parent_status` (`parent_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ----------------------------
-- 商品管理表：商品主表
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID（自增主键，大数据友好）',
  `goods_no` VARCHAR(50) NOT NULL COMMENT '商品编号（唯一，业务用）',
  `goods_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `category_id` INT NOT NULL COMMENT '分类ID（关联goods_category.id）',
  `description` TEXT COMMENT '商品详细描述',
  `main_image` VARCHAR(255) NOT NULL COMMENT '商品主图URL',
  `sub_images` TEXT DEFAULT NULL COMMENT '商品副图URL（JSON数组格式）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品售价（元）',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '商品原价（元）',
  `weight` DECIMAL(10,2) DEFAULT NULL COMMENT '商品重量（克）',
  `unit` VARCHAR(20) NOT NULL DEFAULT '件' COMMENT '商品单位',
  `is_hot` TINYINT NOT NULL DEFAULT 0 COMMENT '热销标识：0-否，1-是',
  `is_new` TINYINT NOT NULL DEFAULT 0 COMMENT '新品标识：0-否，1-是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '商品状态：0-下架，1-上架（关联sys_dict的goods_status）',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号（升序）',
  `sales` INT NOT NULL DEFAULT 0 COMMENT '商品销量',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goods_no` (`goods_no`),
  INDEX `idx_goods_category_status` (`category_id`, `status`),
  INDEX `idx_goods_hot_new` (`is_hot`, `is_new`, `status`),
  INDEX `idx_goods_name` (`goods_name`(50)),
  INDEX `idx_goods_sales` (`sales` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品主表';

-- ----------------------------
-- 库存管理表
-- ----------------------------
DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID（关联goods.id，唯一）',
  `stock_num` INT NOT NULL DEFAULT 0 COMMENT '当前可用库存',
  `locked_num` INT NOT NULL DEFAULT 0 COMMENT '锁定库存（下单未支付占用）',
  `warn_num` INT NOT NULL DEFAULT 10 COMMENT '库存预警阈值',
  `last_in_time` DATETIME DEFAULT NULL COMMENT '最后入库时间',
  `last_out_time` DATETIME DEFAULT NULL COMMENT '最后出库时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_goods_id` (`goods_id`),
  CONSTRAINT `fk_stock_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存管理表';

-- ----------------------------
-- 订单管理表：订单主表
-- ----------------------------
DROP TABLE IF EXISTS `order_master`;
CREATE TABLE `order_master` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID（自增主键）',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号（唯一，格式：YYYYMMDDHHMMSS+6位随机数）',
  `member_id` BIGINT NOT NULL COMMENT '会员ID（关联member.id）',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额（元）',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额（元）',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠总金额（元）',
  `points_used` INT NOT NULL DEFAULT 0 COMMENT '使用积分',
  `points_gained` INT NOT NULL DEFAULT 0 COMMENT '获得积分',
  `freight` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费（元）',
  `pay_type` TINYINT NOT NULL DEFAULT 0 COMMENT '支付方式：0-未选择，1-微信支付，2-支付宝，3-余额（关联sys_dict的pay_type）',
  `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-待发货，2-已发货，3-待收货，4-已完成，5-已取消，6-退款申请中，7-退款成功，8-退款失败（关联sys_dict的order_status）',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '确认收货时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消/退款原因',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `receiver_province` VARCHAR(50) NOT NULL COMMENT '收货人省份',
  `receiver_city` VARCHAR(50) NOT NULL COMMENT '收货人城市',
  `receiver_district` VARCHAR(50) NOT NULL COMMENT '收货人区县',
  `receiver_detail` VARCHAR(500) NOT NULL COMMENT '收货人详细地址',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  INDEX `idx_order_member_status` (`member_id`, `order_status`),
  INDEX `idx_order_status_create` (`order_status`, `gmt_create`),
  INDEX `idx_order_pay_time` (`pay_time`),
  CONSTRAINT `fk_order_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- ----------------------------
-- 订单管理表：订单详情表
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单详情ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID（关联order_master.id）',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID（关联goods.id）',
  `goods_name` VARCHAR(200) NOT NULL COMMENT '商品名称快照',
  `goods_no` VARCHAR(50) NOT NULL COMMENT '商品编号快照',
  `main_image` VARCHAR(255) NOT NULL COMMENT '商品主图快照',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品售价快照（元）',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_price` DECIMAL(10,2) NOT NULL COMMENT '小计金额（元）',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  INDEX `idx_order_detail_order_id` (`order_id`),
  INDEX `idx_order_detail_goods_id` (`goods_id`),
  CONSTRAINT `fk_order_detail_order` FOREIGN KEY (`order_id`) REFERENCES `order_master` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_order_detail_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';

-- ----------------------------
-- 预设数据：系统配置
-- ----------------------------
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES
('site_name', '童趣友谊', '网站名称'),
('site_logo', '/uploads/common/logo_default.png', '网站LOGO URL'),
('contact_phone', '400-123-4567', '客服电话'),
('freight_threshold', '99.00', '运费满减阈值（元，满此金额免运费）'),
('freight_base', '8.00', '基础运费（元）'),
('points_rate', '100', '积分抵扣比例：100积分=1元'),
('vip_points_rate', '150', 'VIP积分抵扣比例：150积分=1元'),
('default_points_gained', '10', '普通会员每消费1元获得积分'),
('vip_points_gained', '15', 'VIP会员每消费1元获得积分'),
('order_auto_cancel_minutes', '30', '订单自动取消时间（分钟）'),
('order_auto_receive_days', '7', '订单自动确认收货时间（天）');

-- ----------------------------
-- 预设数据：系统字典
-- ----------------------------
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_label`, `sort`) VALUES
-- 性别
('gender', '0', '未知', 0),
('gender', '1', '男', 1),
('gender', '2', '女', 2),
-- 支付方式
('pay_type', '0', '未选择', 0),
('pay_type', '1', '微信支付', 1),
('pay_type', '2', '支付宝', 2),
('pay_type', '3', '余额支付', 3),
-- 订单状态
('order_status', '0', '待支付', 0),
('order_status', '1', '待发货', 1),
('order_status', '2', '已发货', 2),
('order_status', '3', '待收货', 3),
('order_status', '4', '已完成', 4),
('order_status', '5', '已取消', 5),
('order_status', '6', '退款申请中', 6),
('order_status', '7', '退款成功', 7),
('order_status', '8', '退款失败', 8),
-- 商品状态
('goods_status', '0', '下架', 0),
('goods_status', '1', '上架', 1),
-- 会员状态
('member_status', '0', '禁用', 0),
('member_status', '1', '正常', 1);

-- ----------------------------
-- 预设数据：后台管理员（密码：admin123，BCrypt加密）
-- ----------------------------
INSERT INTO `sys_admin` (`username`, `password`, `nickname`, `role`, `avatar`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '超级管理员', 'super_admin', '/uploads/common/avatar_admin_default.png');

-- ----------------------------
-- 预设数据：商品分类
-- ----------------------------
INSERT INTO `goods_category` (`category_name`, `parent_id`, `icon`, `sort`) VALUES
-- 一级分类
('玩具', 0, '/uploads/category/icon_toy.png', 1),
('文具', 0, '/uploads/category/icon_stationery.png', 2),
('图书', 0, '/uploads/category/icon_book.png', 3),
('零食', 0, '/uploads/category/icon_snack.png', 4),
('儿童服饰', 0, '/uploads/category/icon_clothes.png', 5),
-- 玩具二级分类
('积木', 1, '/uploads/category/icon_block.png', 1),
('毛绒玩偶', 1, '/uploads/category/icon_doll.png', 2),
('拼图', 1, '/uploads/category/icon_puzzle.png', 3),
('遥控车', 1, '/uploads/category/icon_car.png', 4),
-- 文具二级分类
('笔类', 2, '/uploads/category/icon_pen.png', 1),
('本子', 2, '/uploads/category/icon_notebook.png', 2),
('书包', 2, '/uploads/category/icon_bag.png', 3),
('彩铅蜡笔', 2, '/uploads/category/icon_color_pen.png', 4);

-- ----------------------------
-- 预设数据：商品主表
-- ----------------------------
INSERT INTO `goods` (`goods_no`, `goods_name`, `category_id`, `description`, `main_image`, `sub_images`, `price`, `original_price`, `weight`, `unit`, `is_hot`, `is_new`, `status`, `sort`, `sales`) VALUES
('G20250420001', '启蒙积木 航天火箭发射中心', 6, '启蒙航天系列积木，安全无毒ABS材质，1000+颗粒，适合6-12岁儿童', '/uploads/goods/rocket_main.jpg', '["/uploads/goods/rocket_sub1.jpg","/uploads/goods/rocket_sub2.jpg"]', 129.00, 169.00, 1200.00, '套', 1, 1, 1, 1, 520),
('G20250420002', '迪士尼正版 毛绒玩偶米老鼠', 7, '迪士尼官方授权，柔软短毛绒，填充PP棉，高约30cm，适合0岁以上', '/uploads/goods/mickey_main.jpg', '["/uploads/goods/mickey_sub1.jpg","/uploads/goods/mickey_sub2.jpg"]', 59.00, 79.00, 200.00, '个', 1, 0, 1, 2, 1280),
('G20250420003', '得力 可擦中性笔套装（12支）', 10, '得力品牌，0.5mm子弹头，可擦蓝/黑两色，适合3-12岁学生', '/uploads/goods/pen_main.jpg', '["/uploads/goods/pen_sub1.jpg"]', 19.90, 29.90, 80.00, '盒', 0, 1, 1, 1, 340),
('G20250420004', '得力 A5卡通线圈本（4本装）', 11, '得力品牌，80g米黄护眼纸，60页/本，4种卡通图案', '/uploads/goods/notebook_main.jpg', '["/uploads/goods/notebook_sub1.jpg"]', 24.90, 34.90, 300.00, '套', 0, 1, 1, 2, 210);

-- ----------------------------
-- 预设数据：库存
-- ----------------------------
INSERT INTO `stock` (`goods_id`, `stock_num`, `warn_num`, `last_in_time`) VALUES
(1, 180, 20, '2025-04-15 10:30:00'),
(2, 350, 30, '2025-04-16 14:20:00'),
(3, 500, 50, '2025-04-17 09:15:00'),
(4, 420, 40, '2025-04-18 11:40:00');

-- ----------------------------
-- 预设数据：会员（密码：123456，BCrypt加密）
-- ----------------------------
INSERT INTO `member` (`username`, `password`, `nickname`, `gender`, `phone`, `email`, `avatar`, `balance`, `points`, `total_points`, `is_vip`, `vip_expire_date`, `status`) VALUES
('tongqu_xiaotian', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '童趣小天使', 2, '13800138000', 'xiaotian@tongquyouyi.com', '/uploads/common/avatar_member_default.png', 99.50, 1280, 1800, 1, '2026-04-20 00:00:00', 1),
('tongqu_keai', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '童趣小可爱', 1, '13800138001', 'keai@tongquyouyi.com', '/uploads/common/avatar_member_default.png', 0.00, 320, 320, 0, NULL, 1);

-- ----------------------------
-- 恢复全局设置
-- ----------------------------
SET FOREIGN_KEY_CHECKS = 1;
```