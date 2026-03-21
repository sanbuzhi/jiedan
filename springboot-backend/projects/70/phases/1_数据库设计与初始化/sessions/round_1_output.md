===FILE:back/tongquyouyi.sql===
```sql
-- =============================================
-- 童趣优衣童装零售单店全栈系统 - 数据库初始化脚本
-- 版本：v1.0.0
-- 创建时间：2024-01-01
-- 数据库要求：MySQL 8.0.35+，InnoDB引擎
-- =============================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS `tongquyouyi`;
CREATE DATABASE `tongquyouyi`
    DEFAULT CHARACTER SET `utf8mb4`
    DEFAULT COLLATE `utf8mb4_general_ci`
    DEFAULT ENCRYPTION = 'N';
USE `tongquyouyi`;

-- 2. 禁用外键约束检查（避免建表顺序问题）
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 3. 公共表
-- =============================================

-- 3.1 管理员表
CREATE TABLE `tqy_admin`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username`       VARCHAR(20)  NOT NULL COMMENT '账号（6-20位字母/数字）',
    `password`       VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname`       VARCHAR(20)  NULL DEFAULT NULL COMMENT '昵称',
    `avatar`         VARCHAR(255) NULL DEFAULT NULL COMMENT '头像URL',
    `phone`          VARCHAR(11)  NOT NULL COMMENT '手机号',
    `is_initialized` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否首次初始化（0否1是）',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '管理员表';

-- 3.2 门店信息表
CREATE TABLE `tqy_store_info`
(
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '门店ID（仅1条记录）',
    `name`                 VARCHAR(50)  NOT NULL COMMENT '门店名称',
    `logo`                 VARCHAR(255) NULL DEFAULT NULL COMMENT '门店logo URL',
    `address`              VARCHAR(100) NOT NULL COMMENT '门店地址',
    `contact_phone`        VARCHAR(20)  NOT NULL COMMENT '联系电话',
    `business_hours`       VARCHAR(20)  NOT NULL COMMENT '营业时间（如09:00-21:00）',
    `common_logistics`     TEXT         NOT NULL COMMENT '常用物流（JSON数组）',
    `banners`              TEXT         NULL DEFAULT NULL COMMENT '轮播图（JSON数组）',
    `product_sort_rule`    VARCHAR(20)  NOT NULL DEFAULT 'sales_desc' COMMENT '商品排序规则（sales_desc销量倒序、price_asc价格升序、price_desc价格降序、create_time_desc上架时间倒序）',
    `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '门店信息表';

-- 3.3 操作日志表
CREATE TABLE `tqy_operation_log`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
    `operator_id`    BIGINT       NOT NULL COMMENT '操作人ID',
    `operator_name`  VARCHAR(20)  NOT NULL COMMENT '操作人姓名/昵称',
    `module`         VARCHAR(50)  NOT NULL COMMENT '操作模块',
    `content`        VARCHAR(255) NOT NULL COMMENT '操作内容',
    `ip`             VARCHAR(50)  NULL DEFAULT NULL COMMENT '操作IP',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_operator_module_create` (`operator_id`, `module`, `create_time` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '操作日志表';

-- =============================================
-- 4. 商品管理表
-- =============================================

-- 4.1 商品分类表
CREATE TABLE `tqy_product_category`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(30) NOT NULL COMMENT '分类名称',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    `is_preset`   TINYINT(1)  NOT NULL DEFAULT 1 COMMENT '是否预设分类（0否1是）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '商品分类表';

-- 4.2 商品标签表
CREATE TABLE `tqy_product_tag`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`        VARCHAR(30) NOT NULL COMMENT '标签名称',
    `is_preset`   TINYINT(1)  NOT NULL DEFAULT 1 COMMENT '是否预设标签（0否1是）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '商品标签表';

-- 4.3 商品表
CREATE TABLE `tqy_product`
(
    `id`             BIGINT          NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name`           VARCHAR(50)     NOT NULL COMMENT '商品名称',
    `main_image`     VARCHAR(255)    NOT NULL COMMENT '商品主图URL',
    `detail_images`  TEXT            NULL DEFAULT NULL COMMENT '商品详情图URL（JSON数组）',
    `category_id`    BIGINT          NOT NULL COMMENT '分类ID',
    `description`    TEXT            NULL DEFAULT NULL COMMENT '商品描述（富文本）',
    `tag_price`      DECIMAL(10, 2)  NOT NULL COMMENT '吊牌价',
    `sale_price`     DECIMAL(10, 2)  NOT NULL COMMENT '销售价',
    `status`         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '上下架状态（0下架1上架）',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_status` (`category_id`, `status`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '商品表';

-- 4.4 商品标签关联表
CREATE TABLE `tqy_product_tag_relation`
(
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品标签关联ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `tag_id`     BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '商品标签关联表';

-- 4.5 SKU表
CREATE TABLE `tqy_sku`
(
    `id`                BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    `product_id`        BIGINT          NOT NULL COMMENT '商品ID',
    `attr1_name`        VARCHAR(30)     NULL DEFAULT NULL COMMENT '属性1名称（如颜色）',
    `attr1_value`       VARCHAR(30)     NULL DEFAULT NULL COMMENT '属性1值（如红色）',
    `attr2_name`        VARCHAR(30)     NULL DEFAULT NULL COMMENT '属性2名称（如尺码）',
    `attr2_value`       VARCHAR(30)     NULL DEFAULT NULL COMMENT '属性2值（如100）',
    `sku_code`          VARCHAR(50)     NULL DEFAULT NULL COMMENT 'SKU条码（EAN-13或自定义）',
    `stock`             INT             NOT NULL DEFAULT 0 COMMENT '当前库存',
    `cost_price`        DECIMAL(10, 2)  NOT NULL COMMENT '成本价',
    `min_stock_warning` INT             NOT NULL DEFAULT 10 COMMENT '最低库存预警阈值',
    `max_stock_warning` INT             NULL DEFAULT NULL COMMENT '最高库存预警阈值',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_stock_warning` (`stock`, `min_stock_warning`, `max_stock_warning`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = 'SKU表';

-- 4.6 商品评价表
CREATE TABLE `tqy_product_review`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `product_id`   BIGINT       NOT NULL COMMENT '商品ID',
    `sku_id`       BIGINT       NULL DEFAULT NULL COMMENT 'SKU ID',
    `content`      VARCHAR(500) NOT NULL COMMENT '评价内容',
    `star`         TINYINT(1)   NOT NULL DEFAULT 5 COMMENT '评价星级（1-5）',
    `is_anonymous` TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否匿名（0否1是）',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '商品评价表';

-- =============================================
-- 5. 库存管理表
-- =============================================

-- 5.1 入库单表
CREATE TABLE `tqy_stock_in`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
    `stock_in_no`    VARCHAR(20)  NOT NULL COMMENT '入库单编号（RK-YYYYMMDD-XXX）',
    `type`           VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT '入库类型（NORMAL普通、IMPORT批量、CHECK盘点差异）',
    `supplier`       VARCHAR(50)  NULL DEFAULT NULL COMMENT '供应商',
    `remark`         VARCHAR(200) NULL DEFAULT NULL COMMENT '入库备注',
    `operator_id`    BIGINT       NOT NULL COMMENT '操作人ID',
    `operator_name`  VARCHAR(20)  NOT NULL COMMENT '操作人姓名',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_in_no` (`stock_in_no`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '入库单表';

-- 5.2 入库单明细表
CREATE TABLE `tqy_stock_in_item`
(
    `id`           BIGINT          NOT NULL AUTO_INCREMENT COMMENT '入库单明细ID',
    `stock_in_id`  BIGINT          NOT NULL COMMENT '入库单ID',
    `sku_id`       BIGINT          NOT NULL COMMENT 'SKU ID',
    `sku_name`     VARCHAR(100)    NOT NULL COMMENT 'SKU名称（商品名+属性组合）',
    `quantity`     INT             NOT NULL COMMENT '入库数量',
    `cost_price`   DECIMAL(10, 2)  NOT NULL COMMENT '入库成本价',
    PRIMARY KEY (`id`),
    KEY `idx_stock_in_id` (`stock_in_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '入库单明细表';

-- 5.3 出库单表
CREATE TABLE `tqy_stock_out`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
    `stock_out_no`   VARCHAR(20)  NOT NULL COMMENT '出库单编号（CK-YYYYMMDD-XXX）',
    `type`           VARCHAR(20)  NOT NULL DEFAULT 'SALE' COMMENT '出库类型（SALE销售、MANUAL手工、CHECK盘点差异、RETURN退货）',
    `reason`         VARCHAR(200) NULL DEFAULT NULL COMMENT '出库原因（仅手工/退货出库需要）',
    `remark`         VARCHAR(200) NULL DEFAULT NULL COMMENT '出库备注',
    `operator_id`    BIGINT       NOT NULL COMMENT '操作人ID',
    `operator_name`  VARCHAR(20)  NOT NULL COMMENT '操作人姓名',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_out_no` (`stock_out_no`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '出库单表';

-- 5.4 出库单明细表
CREATE TABLE `tqy_stock_out_item`
(
    `id`            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '出库单明细ID',
    `stock_out_id`  BIGINT          NOT NULL COMMENT '出库单ID',
    `sku_id`        BIGINT          NOT NULL COMMENT 'SKU ID',
    `sku_name`      VARCHAR(100)    NOT NULL COMMENT 'SKU名称',
    `quantity`      INT             NOT NULL COMMENT '出库数量',
    `cost_price`    DECIMAL(10, 2)  NOT NULL COMMENT '出库成本价',
    PRIMARY KEY (`id`),
    KEY `idx_stock_out_id` (`stock_out_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '出库单明细表';

-- 5.5 盘点单表
CREATE TABLE `tqy_stock_check`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '盘点单ID',
    `check_no`       VARCHAR(20)  NOT NULL COMMENT '盘点单编号（PD-YYYYMMDD-XXX）',
    `scope`          VARCHAR(20)  NOT NULL DEFAULT 'ALL' COMMENT '盘点范围（ALL全仓、CATEGORY指定分类、TAG指定标签、SKU指定SKU）',
    `scope_value`    TEXT         NULL DEFAULT NULL COMMENT '盘点范围值（JSON数组）',
    `status`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '盘点状态（0待盘点1已完成）',
    `operator_id`    BIGINT       NOT NULL COMMENT '操作人ID',
    `operator_name`  VARCHAR(20)  NOT NULL COMMENT '操作人姓名',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_check_no` (`check_no`),
    KEY `idx_status_create` (`status`, `create_time` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '盘点单表';

-- 5.6 盘点单明细表
CREATE TABLE `tqy_stock_check_item`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '盘点单明细ID',
    `check_id`      BIGINT       NOT NULL COMMENT '盘点单ID',
    `sku_id`        BIGINT       NOT NULL COMMENT 'SKU ID',
    `sku_name`      VARCHAR(100) NOT NULL COMMENT 'SKU名称',
    `system_stock`  INT          NOT NULL COMMENT '系统库存',
    `actual_stock`  INT          NULL DEFAULT NULL COMMENT '实际库存',
    `difference`    INT          NULL DEFAULT NULL COMMENT '差异数量（actual_stock - system_stock）',
    PRIMARY KEY (`id`),
    KEY `idx_check_id` (`check_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '盘点单明细表';

-- =============================================
-- 6. 会员管理表
-- =============================================

-- 6.1 会员等级表
CREATE TABLE `tqy_member_level`
(
    `id`                          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '会员等级ID',
    `name`                        VARCHAR(10)    NOT NULL COMMENT '等级名称',
    `upgrade_condition_type`      VARCHAR(20)    NOT NULL DEFAULT 'AMOUNT' COMMENT '升级条件类型（AMOUNT累计消费金额、TIMES累计消费次数）',
    `upgrade_condition_value`     DECIMAL(10, 2) NOT NULL COMMENT '升级条件值',
    `discount`                    DECIMAL(3, 1)  NOT NULL DEFAULT 10.0 COMMENT '折扣权益（0.1-10.0）',
    `point_rate`                  DECIMAL(3, 1)  NOT NULL DEFAULT 1.0 COMMENT '积分倍率（1.0-10.0）',
    `exclusive_coupon_template_id` BIGINT         NULL DEFAULT NULL COMMENT '专属优惠券模板ID',
    `sort`                        INT            NOT NULL DEFAULT 0 COMMENT '排序（升序，等级越高排序越大）',
    `is_preset`                   TINYINT(1)     NOT NULL DEFAULT 1 COMMENT '是否预设等级（0否1是）',
    `create_time`                 DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                 DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '会员等级表';

-- 6.2 积分规则表
CREATE TABLE `tqy_member_point_rule`
(
    `id`                    BIGINT         NOT NULL AUTO_INCREMENT COMMENT '积分规则ID（仅1条记录）',
    `consume_per_yuan_point` INT            NOT NULL DEFAULT 1 COMMENT '每消费1元获取N积分',
    `birthday_gift_point`   INT            NULL DEFAULT NULL COMMENT '生日赠送积分',
    `register_gift_point`   INT            NOT NULL DEFAULT 100 COMMENT '注册赠送积分',
    `point_per_hundred_yuan` DECIMAL(10, 2) NOT NULL DEFAULT 1.00 COMMENT '100积分抵扣N元',
    `max_deduct_ratio`      DECIMAL(3, 2)  NOT NULL DEFAULT 0.20 COMMENT '单次订单最高抵扣比例（0.00-1.00）',
    `point_validity_type`   VARCHAR(20)    NOT NULL DEFAULT 'PERMANENT' COMMENT '积分有效期类型（PERMANENT永久、MONTHS自获取之日起N个月）',
    `point_validity_months` INT            NULL DEFAULT NULL COMMENT '积分有效期月数',
    `create_time`           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '积分规则表';

-- 6.3 储值活动表
CREATE TABLE `tqy_member_stored_activity`
(
    `id`             BIGINT          NOT NULL AUTO_INCREMENT COMMENT '储值活动ID',
    `stored_amount`  DECIMAL(10, 2)  NOT NULL COMMENT '储值金额',
    `gift_amount`    DECIMAL(10, 2)  NOT NULL COMMENT '赠送金额',
    `gift_point`     INT             NULL DEFAULT NULL COMMENT '赠送积分',
    `start_time`     DATETIME        NULL DEFAULT NULL COMMENT '活动开始时间',
    `end_time`       DATETIME        NULL DEFAULT NULL COMMENT '活动结束时间',
    `status`         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '活动状态（0未开始1进行中2已结束）',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '储值活动表';

-- 6.4 优惠券模板表
CREATE TABLE `tqy_member_coupon_template`
(
    `id`                    BIGINT         NOT NULL AUTO_INCREMENT COMMENT '优惠券模板ID',
    `name`                  VARCHAR(30)    NOT NULL COMMENT '优惠券名称',
    `type`                  VARCHAR(20)    NOT NULL DEFAULT 'FULL_REDUCTION' COMMENT '优惠券类型（FULL_REDUCTION满减券、DISCOUNT折扣券、NO_THRESHOLD无门槛券）',
    `full_reduction_amount` DECIMAL(10, 2) NULL DEFAULT NULL COMMENT '满减金额（仅满减券需要）',
    `discount_rate`         DECIMAL(3, 1)  NULL DEFAULT NULL COMMENT '折扣率（仅折扣券需要，0.1-10.0）',
    `no_threshold_amount`   DECIMAL(10, 2) NULL DEFAULT NULL COMMENT '无门槛金额（仅无门槛券需要）',
    `use_threshold`         DECIMAL(10, 2) NULL DEFAULT NULL COMMENT '使用门槛（仅满减/折扣券需要）',
    `scope_type`            VARCHAR(20)    NOT NULL DEFAULT 'ALL' COMMENT '适用范围类型（ALL全仓、CATEGORY指定分类、TAG指定标签、SKU指定SKU）',
    `scope_value`           TEXT           NULL DEFAULT NULL COMMENT '适用范围值（JSON数组）',
    `validity_type`         VARCHAR(20)    NOT NULL DEFAULT 'FIXED' COMMENT '有效期类型（FIXED固定日期、DAYS自领取之日起N天）',
    `validity_start_time`   DATETIME       NULL DEFAULT NULL COMMENT '有效期开始时间（仅固定日期需要）',
    `validity_end_time`     DATETIME       NULL DEFAULT NULL COMMENT '有效期结束时间（仅固定日期需要）',
    `validity_days`         INT            NULL DEFAULT NULL COMMENT '有效期天数（仅自领取之日起N天需要）',
    `grant_count`           INT            NOT NULL COMMENT '发放数量',
    `per_limit`             INT            NOT NULL DEFAULT 1 COMMENT '每人限领数量（主动领取限制）',
    `grant_type`            VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE' COMMENT '领取方式（ACTIVE主动领取、DIRECT定向发放）',
    `status`                TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '模板状态（0未开始1进行中2已结束3已暂停）',
    `create_time`           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '优惠券模板表';

-- 6.5 会员表
CREATE TABLE `tqy_member`
(
    `id`                   BIGINT          NOT NULL AUTO_INCREMENT COMMENT '会员ID',
    `phone`                VARCHAR(11)     NOT NULL COMMENT '手机号',
    `nickname`             VARCHAR(20)     NULL DEFAULT NULL COMMENT '昵称',
    `real_name`            VARCHAR(20)     NULL DEFAULT NULL COMMENT '真实姓名',
    `avatar`               VARCHAR(255)    NULL DEFAULT NULL COMMENT '头像URL',
    `birthday`             DATE            NULL DEFAULT NULL COMMENT '家长生日',
    `baby_gender`          VARCHAR(10)     NULL DEFAULT '保密' COMMENT '宝宝性别（男/女/保密）',
    `baby_age_range`       VARCHAR(20)     NULL DEFAULT NULL COMMENT '宝宝年龄范围（0-6个月/7-12个月/1-2岁/3-5岁/6-8岁/9-12岁/13-16岁）',
    `level_id`             BIGINT          NOT NULL COMMENT '会员等级ID',
    `point_balance`        INT             NOT NULL DEFAULT 0 COMMENT '积分余额',
    `stored_balance`       DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '储值余额',
    `total_consume_amount` DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
    `total_consume_times`  INT             NOT NULL DEFAULT 0 COMMENT '累计消费次数',
    `last_consume_time`    DATETIME        NULL DEFAULT NULL COMMENT '最后消费时间',
    `register_channel`     VARCHAR(20)     NOT NULL DEFAULT 'OFFLINE' COMMENT '注册渠道（ONLINE线上/OFFLINE线下）',
    `create_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_level_id` (`level_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '会员表';

-- 6.6 积分变动记录表
CREATE TABLE `tqy_member_point_log`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '积分变动ID',
    `member_id`      BIGINT       NOT NULL COMMENT '会员ID',
    `adjust_point`   INT          NOT NULL COMMENT '调整积分（正增加负减少）',
    `after_balance`  INT          NOT NULL COMMENT '调整后余额',
    `type`           VARCHAR(20)  NOT NULL DEFAULT 'CONSUME' COMMENT '变动类型（CONSUME消费获取/EXCHANGE消费抵扣/REGISTER注册赠送/BIRTHDAY生日赠送/ADJUST手动调整/COUPON优惠券赠送）',
    `related_type`   VARCHAR(20)  NULL DEFAULT NULL COMMENT '关联业务类型（ORDER订单/COUPON优惠券）',
    `related_id`     BIGINT       NULL DEFAULT NULL COMMENT '关联业务ID',
    `reason`         VARCHAR(200) NOT NULL COMMENT '变动原因',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '积分变动记录表';

-- 6.7 储值变动记录表
CREATE TABLE `tqy_member_stored_log`
(
    `id`             BIGINT          NOT NULL AUTO_INCREMENT COMMENT '储值变动ID',
    `member_id`      BIGINT          NOT NULL COMMENT '会员ID',
    `adjust_amount`  DECIMAL(10, 2)  NOT NULL COMMENT '调整金额（正增加负减少）',
    `gift_amount`    DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '赠送金额',
    `gift_point`     INT             NOT NULL DEFAULT 0 COMMENT '赠送积分',
    `after_balance`  DECIMAL(10, 2)  NOT NULL COMMENT '调整后余额',
    `type`           VARCHAR(20)     NOT NULL DEFAULT 'RECHARGE' COMMENT '变动类型（RECHARGE充值/CONSUME消费抵扣/ADJUST手动调整/REFUND退款返还）',
    `related_type`   VARCHAR(20)     NULL DEFAULT NULL COMMENT '关联业务类型（ORDER订单/STORED_ACTIVITY储值活动）',
    `related_id`     BIGINT          NULL DEFAULT NULL COMMENT '关联业务ID',
    `pay_type`       VARCHAR(20)     NULL DEFAULT NULL COMMENT '支付方式（仅充值需要）',
    `reason`         VARCHAR(200)    NULL DEFAULT NULL COMMENT '变动原因',
    `operator_id`    BIGINT          NULL DEFAULT NULL COMMENT '操作人ID（仅线下/手动调整需要）',
    `operator_name`  VARCHAR(20)     NULL DEFAULT NULL COMMENT '操作人姓名',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '储值变动记录表';

-- 6.8 会员优惠券表
CREATE TABLE `tqy_member_coupon`
(
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '优惠券实例ID',
    `template_id`    BIGINT      NOT NULL COMMENT '优惠券模板ID',
    `member_id`      BIGINT      NOT NULL COMMENT '会员ID',
    `status`         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '优惠券状态（0未使用1已使用2已过期3已退回）',
    `receive_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time`       DATETIME    NULL DEFAULT NULL COMMENT '使用时间',
    `expire_time`    DATETIME    NOT NULL COMMENT '过期时间',
    `order_id`       BIGINT      NULL DEFAULT NULL COMMENT '使用订单ID',
    `grant_type`     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '领取方式（同模板）',
    `operator_id`    BIGINT      NULL DEFAULT NULL COMMENT '操作人ID（仅定向发放需要）',
    `operator_name`  VARCHAR(20) NULL DEFAULT NULL COMMENT '操作人姓名',
    `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_status_expire` (`member_id`, `status`, `expire_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '会员优惠券表';

-- 6.9 会员收藏表
CREATE TABLE `tqy_member_favorite`
(
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `member_id`   BIGINT   NOT NULL COMMENT '会员ID',
    `product_id`  BIGINT   NOT NULL COMMENT '商品ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_product` (`member_id`, `product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '会员收藏表';

-- 6.10 会员购物车表
CREATE TABLE `tqy_member_cart`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `member_id`   BIGINT      NOT NULL COMMENT '会员ID',
    `product_id`  BIGINT      NOT NULL COMMENT '商品ID',
    `sku_id`      BIGINT      NOT NULL COMMENT 'SKU ID',
    `quantity`    INT         NOT NULL DEFAULT 1 COMMENT '数量',
    `is_selected` TINYINT(1)  NOT NULL DEFAULT 1 COMMENT '是否选中（0否1是）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_sku` (`member_id`, `sku_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '会员购物车表';

-- 6.11 会员收货地址表
CREATE TABLE `tqy_member_address`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '收货地址ID',
    `member_id`       BIGINT       NOT NULL COMMENT '会员ID',
    `consignee`       VARCHAR(20)  NOT NULL COMMENT '收货人',
    `phone`           VARCHAR(11)  NOT NULL COMMENT '联系电话',
    `province`        VARCHAR(20)  NOT NULL COMMENT '省份',
    `city`            VARCHAR(20)  NOT NULL COMMENT '城市',
    `district`        VARCHAR(20)  NOT NULL COMMENT '区县',
    `detail_address`  VARCHAR(100) NOT NULL COMMENT '详细地址',
    `is_default`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否默认地址（0否1是）',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '会员收货地址表';

-- =============================================
-- 7. 订单管理表
-- =============================================

-- 7.1 订单表
CREATE TABLE `tqy_order`
(
    `id`                       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`                 VARCHAR(20)     NOT NULL COMMENT '订单编号（DD-线上/XD-线下+YYYYMMDD-XXX）',
    `channel`                  VARCHAR(20)     NOT NULL DEFAULT 'ONLINE' COMMENT '订单渠道（ONLINE线上/OFFLINE线下）',
    `member_id`                BIGINT          NULL DEFAULT NULL COMMENT '会员ID（非会员线下订单为空）',
    `total_product_amount`     DECIMAL(10, 2)  NOT NULL COMMENT '商品小计总额',
    `member_discount_amount`   DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '会员折扣金额',
    `coupon_discount_amount`   DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '优惠券折扣金额',
    `point_deduct_amount`      DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
    `point_deduct_count`       INT             NOT NULL DEFAULT 0 COMMENT '积分抵扣数量',
    `actual_pay_amount`        DECIMAL(10, 2)  NOT NULL COMMENT '实付金额',
    `stored_pay_amount`        DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '储值支付金额',
    `third_pay_amount`         DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '第三方支付金额',
    `pay_type`                 VARCHAR(20)     NULL DEFAULT NULL COMMENT '支付方式（CASH现金/WECHAT微信/ALIPAY支付宝/BANK_CARD银行卡/STORED储值余额/OTHER其他）',
    `pay_time`                 DATETIME        NULL DEFAULT NULL COMMENT '支付时间',
    `status`                   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '订单状态（0待付款1待发货2已发货3已完成4已取消5退款中6已退款）',
    `cancel_reason`            VARCHAR(200)    NULL DEFAULT NULL COMMENT '取消原因',
    `cancel_time`              DATETIME        NULL DEFAULT NULL COMMENT '取消时间',
    `consignee`                VARCHAR(20)     NULL DEFAULT NULL COMMENT '收货人（仅线上订单需要）',
    `consignee_phone`          VARCHAR(11)     NULL DEFAULT NULL COMMENT '联系电话（仅线上订单需要）',
    `full_address`             VARCHAR(200)    NULL DEFAULT NULL COMMENT '完整收货地址（仅线上订单需要）',
    `logistics_name`           VARCHAR(30)     NULL DEFAULT NULL COMMENT '物流名称（仅已发货/已完成线上订单需要）',
    `tracking_no`              VARCHAR(50)     NULL DEFAULT NULL COMMENT '运单号（仅已发货/已完成线上订单需要）',
    `ship_time`                DATETIME        NULL DEFAULT NULL COMMENT '发货时间',
    `confirm_receive_time`     DATETIME        NULL DEFAULT NULL COMMENT '确认收货时间',
    `auto_confirm_time`        DATETIME        NULL DEFAULT NULL COMMENT '自动确认收货时间',
    `remark`                   VARCHAR(200)    NULL DEFAULT NULL COMMENT '订单备注',
    `operator_id`              BIGINT          NULL DEFAULT NULL COMMENT '操作人ID（仅线下订单/后台操作需要）',
    `operator_name`            VARCHAR(20)     NULL DEFAULT NULL COMMENT '操作人姓名',
    `create_time`              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_member_status_create` (`member_id`, `status`, `create_time` DESC),
    KEY `idx_channel_status_create` (`channel`, `status`, `create_time` DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '订单表';

-- 7.2 订单明细表
CREATE TABLE `tqy_order_item`
(
    `id`               BIGINT          NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
    `order_id`         BIGINT          NOT NULL COMMENT '订单ID',
    `product_id`       BIGINT          NOT NULL COMMENT '商品ID',
    `sku_id`           BIGINT          NOT NULL COMMENT 'SKU ID',
    `product_name`     VARCHAR(50)     NOT NULL COMMENT '商品名称（快照）',
    `main_image`       VARCHAR(255)    NOT NULL COMMENT '商品主图（快照）',
    `sku_attr1_name`   VARCHAR(30)     NULL DEFAULT NULL COMMENT 'SKU属性1名称（快照）',
    `sku_attr1_value`  VARCHAR(30)     NULL DEFAULT NULL COMMENT 'SKU属性1值（快照）',
    `sku_attr2_name`   VARCHAR(30)     NULL DEFAULT NULL COMMENT 'SKU属性2名称（快照）',
    `sku_attr2_value`  VARCHAR(30)     NULL DEFAULT NULL COMMENT 'SKU属性2值（快照）',
    `tag_price`        DECIMAL(10, 2)  NOT NULL COMMENT '吊牌价（快照）',
    `sale_price`       DECIMAL(10, 2)  NOT NULL COMMENT '销售价（快照）',
    `cost_price`       DECIMAL(10, 2)  NOT NULL COMMENT '成本价（快照）',
    `quantity`         INT             NOT NULL DEFAULT 1 COMMENT '数量',
    `subtotal`         DECIMAL(10, 2)  NOT NULL COMMENT '小计',
    `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '订单明细表';

-- 7.3 订单优惠明细表
CREATE TABLE `tqy_order_discount`
(
    `id`               BIGINT          NOT NULL AUTO_INCREMENT COMMENT '订单优惠明细ID',
    `order_id`         BIGINT          NOT NULL COMMENT '订单ID',
    `type`             VARCHAR(20)     NOT NULL DEFAULT 'MEMBER_DISCOUNT' COMMENT '优惠类型（MEMBER_DISCOUNT会员折扣/COUPON优惠券/POINT_DEDUCT积分抵扣）',
    `related_id`       BIGINT          NULL DEFAULT NULL COMMENT '关联业务ID（优惠券ID）',
    `discount_amount`  DECIMAL(10, 2)  NOT NULL COMMENT '优惠金额',
    `discount_point`   INT             NOT NULL DEFAULT 0 COMMENT '优惠积分（仅积分抵扣需要）',
    `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '订单优惠明细表';

-- 7.4 退款单表
CREATE TABLE `tqy_refund`
(
    `id`                   BIGINT          NOT NULL AUTO_INCREMENT COMMENT '退款单ID',
    `refund_no`            VARCHAR(20)     NOT NULL COMMENT '退款单编号（TK-YYYYMMDD-XXX）',
    `order_id`             BIGINT          NOT NULL COMMENT '订单ID',
    `member_id`            BIGINT          NULL DEFAULT NULL COMMENT '会员ID',
    `refund_amount`        DECIMAL(10, 2)  NOT NULL COMMENT '退款总金额',
    `stored_refund_amount` DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '储值退款金额',
    `third_refund_amount`  DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '第三方退款金额',
    `refund_reason`        VARCHAR(200)    NOT NULL COMMENT '退款原因',
    `apply_time`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `audit_result`         TINYINT(1)      NULL DEFAULT NULL COMMENT '审核结果（0拒绝1通过）',
    `audit_opinion`        VARCHAR(200)    NULL DEFAULT NULL COMMENT '审核意见',
    `audit_time`           DATETIME        NULL DEFAULT NULL COMMENT '审核时间',
    `operator_id`          BIGINT          NULL DEFAULT NULL COMMENT '操作人ID',
    `operator_name`        VARCHAR(20)     NULL DEFAULT NULL COMMENT '操作人姓名',
    `status`               TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '退款状态（0待审核1审核通过待退款2退款成功3审核拒绝）',
    `create_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_general_ci` COMMENT = '退款单表';

-- =============================================
-- 8. 启用外键约束检查
-- =============================================
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 9. 插入预设数据
-- =============================================

-- 9.1 插入预设管理员账号（密码：Tqy@2024，BCrypt加密后的结果）
INSERT INTO `tqy_admin` (`username`, `password`, `nickname`, `phone`, `is_initialized`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '童趣店长', '13800138000', 0);

-- 9.2 插入预设门店信息
INSERT INTO `tqy_store_info` (`name`, `address`, `contact_phone`, `business_hours`, `common_logistics`)
VALUES ('童趣优衣童装旗舰店', '北京市朝阳区建国路88号童趣大厦1层', '400-888-8888', '09:00-21:00',
        '["顺丰速运","中通快递","圆通速递","申通快递","韵达快递","中国邮政EMS"]');

-- 9.3 插入预设商品分类
INSERT INTO `tqy_product_category` (`name`, `sort`, `is_preset`)
VALUES ('新生儿装', 1, 1),
       ('婴儿装', 2, 1),
       ('幼童装', 3, 1),
       ('中大童装', 4, 1),
       ('配饰', 5, 1);

-- 9.4 插入预设商品标签
INSERT INTO `tqy_product_tag` (`name`, `is_preset`)
VALUES ('新品', 1),
       ('热销', 1),
       ('清仓', 1),
       ('联名', 1);

-- 9.5 插入预设会员等级
INSERT INTO `tqy_member_level` (`name`, `upgrade_condition_type`, `upgrade_condition_value`, `discount`, `point_rate`, `sort`, `is_preset`)
VALUES ('普通会员', 'AMOUNT', 0.00, 10.0, 1.0, 1, 1),
       ('银卡会员', 'AMOUNT', 500.00, 9.5, 1.2, 2, 1),
       ('金卡会员', 'AMOUNT', 2000.00, 9.0, 1.5, 3, 1),
       ('钻石会员', 'AMOUNT', 5000.00, 8.5, 2.0, 4, 1);

-- 9.6 插入预设积分规则
INSERT INTO `tqy_member_point_rule` (`consume_per_yuan_point`, `register_gift_point`, `point_per_hundred_yuan`, `max_deduct_ratio`, `point_validity_type`)
VALUES (1, 100, 1.00, 0.20, 'PERMANENT');
```