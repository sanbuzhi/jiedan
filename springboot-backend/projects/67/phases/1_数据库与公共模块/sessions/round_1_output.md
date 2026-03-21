我将为您生成美妆小店轻量级后端管理系统V1.0的阶段1代码文件，包括数据库初始化脚本、项目配置文件和公共模块代码。

===FILE: beauty-shop-manage/sql/init.sql===
-- 美妆小店轻量级后端管理系统数据库初始化脚本
-- 数据库名：beauty_shop_manage
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `beauty_shop_manage` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `beauty_shop_manage`;

-- 1. 美妆商品类别表（beauty_category）
DROP TABLE IF EXISTS `beauty_category`;
CREATE TABLE `beauty_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '商品类别名称',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父类别ID（0为「美妆全品类」）',
  `general_expiration_warning_days` INT NOT NULL DEFAULT 30 COMMENT '通用过期预警天数',
  `general_safety_stock` INT NOT NULL DEFAULT 10 COMMENT '通用安全库存（件）',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人ID（关联beauty_staff）',
  `update_by` BIGINT NULL COMMENT '更新人ID（关联beauty_staff）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆商品类别表';

-- 插入默认「美妆全品类」商品类别
INSERT INTO `beauty_category` (`id`, `category_name`, `parent_id`, `general_expiration_warning_days`, `general_safety_stock`, `sort_order`, `create_by`, `update_by`) VALUES
(1, '美妆全品类', 0, 30, 10, 0, 1, 1);

-- 2. 美妆商品档案表（beauty_product）
DROP TABLE IF EXISTS `beauty_product`;
CREATE TABLE `beauty_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_barcode` VARCHAR(50) NOT NULL COMMENT '商品条码',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category_id` BIGINT NOT NULL COMMENT '商品类别ID（关联beauty_category）',
  `brand` VARCHAR(50) NULL COMMENT '品牌',
  `specification` VARCHAR(50) NULL COMMENT '规格（如50ml、片装）',
  `production_date` DATE NULL COMMENT '生产日期',
  `shelf_life_months` INT NULL COMMENT '保质期（月）',
  `expiration_warning_days` INT NULL COMMENT '过期预警天数（为空则继承类别）',
  `cost_price` DECIMAL(10,2) NOT NULL COMMENT '成本价（元）',
  `min_retail_price` DECIMAL(10,2) NOT NULL COMMENT '最低零售价（元）',
  `suggested_retail_price` DECIMAL(10,2) NOT NULL COMMENT '建议零售价（元）',
  `member_level_discount` DECIMAL(3,2) NULL COMMENT '会员等级折扣（为空则继承等级）',
  `safety_stock` INT NULL COMMENT '安全库存（件，为空则继承类别）',
  `product_images` TEXT NULL COMMENT '商品图片URL（逗号分隔，最多5张）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人ID（关联beauty_staff）',
  `update_by` BIGINT NULL COMMENT '更新人ID（关联beauty_staff）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_product_barcode` (`product_barcode`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_product_name` (`product_name`(50))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆商品档案表';

-- 3. 美妆库位档案表（beauty_location）
DROP TABLE IF EXISTS `beauty_location`;
CREATE TABLE `beauty_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `location_name` VARCHAR(100) NOT NULL COMMENT '库位名称（如主仓库-口红区-第一层）',
  `capacity` INT NULL COMMENT '库位容量（件）',
  `location_status` TINYINT NOT NULL DEFAULT 1 COMMENT '库位状态（1正常，0停用）',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '库位优先级（用于线上订单库存分配，数字越大优先级越高）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人ID（关联beauty_staff）',
  `update_by` BIGINT NULL COMMENT '更新人ID（关联beauty_staff）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_location_status` (`location_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆库位档案表';

-- 4. 美妆商品库存表（beauty_product_inventory）
DROP TABLE IF EXISTS `beauty_product_inventory`;
CREATE TABLE `beauty_product_inventory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID（关联beauty_product）',
  `location_id` BIGINT NOT NULL COMMENT '库位ID（关联beauty_location）',
  `stock_quantity` INT NOT NULL DEFAULT 0 COMMENT '库存数量（件）',
  `locked_quantity` INT NOT NULL DEFAULT 0 COMMENT '锁定库存数量（件，用于盘点/线上订单）',
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '库存总金额（元）',
  `batch_production_date` DATE NULL COMMENT '库存批次生产日期（可选，用于过期预警）',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_product_location` (`product_id`, `location_id`),
  INDEX `idx_location_id` (`location_id`),
  INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆商品库存表';

-- 5. 美妆供应商档案表（beauty_supplier）
DROP TABLE IF EXISTS `beauty_supplier`;
CREATE TABLE `beauty_supplier` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
  `contact_phone` VARCHAR(20) NULL COMMENT '联系方式（手机号/座机号）',
  `address` VARCHAR(200) NULL COMMENT '地址',
  `supply_category_ids` TEXT NULL COMMENT '供应商品类别ID（逗号分隔）',
  `cooperation_discount_rate` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '合作折扣率',
  `payable_balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '应付账款余额（元）',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人ID（关联beauty_staff）',
  `update_by` BIGINT NULL COMMENT '更新人ID（关联beauty_staff）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_supplier_name` (`supplier_name`(50)),
  INDEX `idx_contact_phone` (`contact_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆供应商档案表';

-- 6. 美妆员工档案表（beauty_staff）
DROP TABLE IF EXISTS `beauty_staff`;
CREATE TABLE `beauty_staff` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `staff_no` VARCHAR(20) NOT NULL COMMENT '员工工号',
  `login_account` VARCHAR(50) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(255) NOT NULL COMMENT '登录密码（AES256加密）',
  `staff_name` VARCHAR(50) NOT NULL COMMENT '员工姓名',
  `phone` VARCHAR(20) NULL COMMENT '手机号',
  `role_id` BIGINT NOT NULL COMMENT '角色ID（关联beauty_role）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '员工状态（1正常，0禁用）',
  `is_first_login` TINYINT NOT NULL DEFAULT 1 COMMENT '是否首次登录（1是，0否）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人ID（关联beauty_staff）',
  `update_by` BIGINT NULL COMMENT '更新人ID（关联beauty_staff）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_staff_no` (`staff_no`),
  UNIQUE INDEX `idx_login_account` (`login_account`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆员工档案表';

-- 7. 美妆员工角色表（beauty_role）
DROP TABLE IF EXISTS `beauty_role`;
CREATE TABLE `beauty_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_description` VARCHAR(200) NULL COMMENT '角色描述',
  `permission_codes` TEXT NULL COMMENT '权限编码列表（逗号分隔）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人ID（关联beauty_staff）',
  `update_by` BIGINT NULL COMMENT '更新人ID（关联beauty_staff）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆员工角色表';

-- 插入默认「店长」员工角色
INSERT INTO `beauty_role` (`id`, `role_name`, `role_description`, `create_by`, `update_by`) VALUES
(1, '店长', '拥有所有权限', 1, 1);

-- 插入默认管理员账号（密码：Admin@123，AES256加密后的示例值）
INSERT INTO `beauty_staff` (`id`, `staff_no`, `login_account`, `password`, `staff_name`, `phone`, `role_id`, `status`, `is_first_login`, `create_by`, `update_by`) VALUES
(1, 'YG0001', 'admin', 'U2FsdGVkX1+Qe6x7Qe6x7Qe6x7Qe6x7Qe6x7Qe6x7Qe6x7Qe6x7Qe6x7Qe6x7Q=', '系统管理员', '13800138000', 1, 1, 1, 1, 1);

-- 8. 美妆会员等级表（beauty_member_level）
DROP TABLE IF EXISTS `beauty_member_level`;
CREATE TABLE `beauty_member_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `level_condition` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '等级条件（累计消费金额≥X元）',
  `general_discount` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '普通折扣',
  `point_ratio` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '积分比例（消费1元得X积分）',
  `birthday_discount` DECIMAL(3,2) NOT NULL DEFAULT 0.90 COMMENT '生日折扣',
  `level_icon` VARCHAR(255) NULL COMMENT '等级图标URL',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人ID（关联beauty_staff）',
  `update_by` BIGINT NULL COMMENT '更新人ID（关联beauty_staff）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆会员等级表';

-- 插入默认「普通会员」会员等级
INSERT INTO `beauty_member_level` (`id`, `level_name`, `level_condition`, `general_discount`, `point_ratio`, `birthday_discount`, `sort_order`, `create_by`, `update_by`) VALUES
(1, '普通会员', 0.00, 1.00, 1.00, 0.90, 0, 1, 1);

-- 9. 美妆会员档案表（beauty_member）
DROP TABLE IF EXISTS `beauty_member`;
CREATE TABLE `beauty_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_card_no` VARCHAR(20) NOT NULL COMMENT '会员卡号',
  `member_name` VARCHAR(50) NOT NULL COMMENT '会员姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `level_id` BIGINT NOT NULL DEFAULT 1 COMMENT '等级ID（关联beauty_member_level）',
  `stored_balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '储值余额（元）',
  `available_points` INT NOT NULL DEFAULT 0 COMMENT '可用积分',
  `total_consumption_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额（元）',
  `consumption_count` INT NOT NULL DEFAULT 0 COMMENT '消费次数',
  `birthday` DATE NULL COMMENT '生日',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人ID（关联beauty_staff）',
  `update_by` BIGINT NULL COMMENT '更新人ID（关联beauty_staff）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_member_card_no` (`member_card_no`),
  UNIQUE INDEX `idx_phone` (`phone`),
  INDEX `idx_level_id` (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆会员档案表';

-- 10. 美妆系统参数表（beauty_system_param）
DROP TABLE IF EXISTS `beauty_system_param`;
CREATE TABLE `beauty_system_param` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `param_key` VARCHAR(100) NOT NULL COMMENT '参数键',
  `param_value` TEXT NULL COMMENT '参数值',
  `param_description` VARCHAR(200) NULL COMMENT '参数描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT NULL COMMENT '创建人ID（关联beauty_staff）',
  `update_by` BIGINT NULL COMMENT '更新人ID（关联beauty_staff）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_param_key` (`param_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆系统参数表';

-- 插入默认系统参数
INSERT INTO `beauty_system_param` (`param_key`, `param_value`, `param_description`, `create_by`, `update_by`) VALUES
('shop_name', '美妆小店', '店铺名称', 1, 1),
('shop_logo', '', '店铺logo URL', 1, 1),
('shop_address', '', '店铺地址', 1, 1),
('shop_phone', '', '店铺联系电话', 1, 1),
('zero_rule', 'none', '抹零规则：none不抹零，jiao抹角，yuan抹元', 1, 1),
('point_ratio', '1.00', '默认积分比例（消费1元得X积分）', 1, 1),
('api_timestamp_tolerance_minutes', '5', 'API签名时间戳误差容忍度（分钟）', 1, 1),
('online_callback_url', '', '线上订单回调地址', 1, 1),
('inventory_lock_hours', '24', '库存锁定时长（小时）', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;

===FILE: beauty-shop-manage/pom.xml===
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>
    <groupId>com.beauty</groupId>
    <artifactId>beauty-shop-manage</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <name>beauty-shop-manage</name>
    <description>美妆小店轻量级后端管理系统</description>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <swagger.version>3.0.0</swagger.version>
        <jwt.version>0.11.5</jwt.version>
        <hutool.version>5.8.26</hutool.version>
        <redisson.version>3.25.2</redisson.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <modules>
        <module>beauty-shop-manage-common</module>
        <module>beauty-shop-manage-domain</module>
        <module>beauty-shop-manage-service</module>
        <module>beauty-shop-manage-controller</module>
        <module>beauty-shop-manage-config</module>
        <module>beauty-shop-manage-app</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <!-- MyBatis Plus -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-generator</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>

            <!-- Swagger -->
            <dependency>
                <groupId>io.springfox</groupId>
                <artifactId>springfox-boot-starter</artifactId>
                <version>${swagger.version}</version>
            </dependency>

            <!-- JWT -->
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>${jwt.version}</version>
                <scope>runtime</scope>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>${jwt.version}</version>
                <scope>runtime</scope>
            </dependency>

            <!-- Hutool -->
            <dependency>
                <groupId>cn.hutool</groupId>
                <artifactId>hutool-all</artifactId>
                <version>${hutool.version}</version>
            </dependency>

            <!-- Redisson -->
            <dependency>
                <groupId>org.redisson</groupId>
                <artifactId>redisson-spring-boot-starter</artifactId>
                <version>${redisson.version}</version>
            </dependency>

            <!-- 内部模块依赖 -->
            <dependency>
                <groupId>com.beauty</groupId>
                <artifactId>beauty-shop-manage-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.beauty</groupId>
                <artifactId>beauty-shop-manage-domain</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.beauty</groupId>
                <artifactId>beauty-shop-manage-service</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.beauty</groupId>
                <artifactId>beauty-shop-manage-controller</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.beauty</groupId>
                <artifactId>beauty-shop-manage-config</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <encoding>${project.build.sourceEncoding}</encoding>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

===FILE: beauty-shop-manage/beauty-shop-manage-common/pom.xml===
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.beauty</groupId>
        <artifactId>beauty-shop-manage</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>beauty-shop-manage-common</artifactId>
    <description>美妆小店公共模块</description>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Hutool -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>

        <!-- Swagger -->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-boot-starter</artifactId>
        </dependency>
    </dependencies>
</project>

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/constant/BeautyConstants.java===
package com.beauty.common.constant;

/**
 * 美妆小店常量类
 *
 * @author beauty
 * @since 2024-05-20
 */
public class BeautyConstants {

    private BeautyConstants() {
    }

    /**
     * 通用常量
     */
    public static final String BEAUTY_YES = "1";
    public static final String BEAUTY_NO = "0";
    public static final Integer BEAUTY_INT_YES = 1;
    public static final Integer BEAUTY_INT_NO = 0;
    public static final String BEAUTY_DEFAULT_CHARSET = "UTF-8";
    public static final String BEAUTY_COMMA = ",";
    public static final String BEAUTY_UNDERLINE = "_";
    public static final String BEAUTY_HYPHEN = "-";
    public static final String BEAUTY_COLON = ":";

    /**
     * 流水号前缀
     */
    public static final String BEAUTY_PURCHASE_NO_PREFIX = "CG";
    public static final String BEAUTY_SALE_NO_PREFIX = "XS";
    public static final String BEAUTY_RETURN_NO_PREFIX = "TH";
    public static final String BEAUTY_TRANSFER_NO_PREFIX = "DB";
    public static final String BEAUTY_CHECK_NO_PREFIX = "PD";

    /**
     * 默认值
     */
    public static final Integer BEAUTY_DEFAULT_SAFETY_STOCK = 10;
    public static final Integer BEAUTY_DEFAULT_EXPIRATION_WARNING_DAYS = 30;
    public static final Integer BEAUTY_DEFAULT_PAGE_SIZE = 20;
    public static final Integer BEAUTY_MAX_PAGE_SIZE = 500;
    public static final Integer BEAUTY_MAX_EXPORT_SIZE = 100000;

    /**
     * 文件上传
     */
    public static final String[] BEAUTY_ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png"};
    public static final Long BEAUTY_PRODUCT_IMAGE_MAX_SIZE = 2 * 1024 * 1024L;
    public static final Long BEAUTY_OTHER_IMAGE_MAX_SIZE = 1 * 1024 * 1024L;
    public static final Integer BEAUTY_PRODUCT_IMAGE_MAX_COUNT = 5;
    public static final Integer BEAUTY_OTHER_IMAGE_MAX_COUNT = 1;

    /**
     * JWT
     */
    public static final Long BEAUTY_JWT_ACCESS_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L;
    public static final Long BEAUTY_JWT_REFRESH_TOKEN_VALIDITY = 30 * 24 * 60 * 60 * 1000L;
    public static final String BEAUTY_JWT_BLACKLIST_PREFIX = "beauty:jwt:blacklist:";
    public static final String BEAUTY_JWT_HEADER = "Authorization";
    public static final String BEAUTY_JWT_PREFIX = "Bearer ";

    /**
     * Redis
     */
    public static final String BEAUTY_REDISSON_LOCK_PREFIX = "beauty:lock:";
    public static final String BEAUTY_NO_GENERATOR_KEY_PREFIX = "beauty:no:generator:";
    public static final String BEAUTY_INVENTORY_LOCK_PREFIX = "beauty:inventory:lock:";
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/enums/BeautyPurchaseStatus.java===
package com.beauty.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 美妆采购单状态枚举
 *
 * @author beauty
 * @since 2024-05-20
 */
@Getter
@AllArgsConstructor
public enum BeautyPurchaseStatus {

    DRAFT(0, "草稿"),
    PENDING_STOCK_IN(1, "待入库"),
    PARTIAL_STOCK_IN(2, "部分入库"),
    COMPLETED(3, "已完成"),
    DELETED(4, "已删除");

    private final Integer code;
    private final String desc;

    public static BeautyPurchaseStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BeautyPurchaseStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/enums/BeautyPayType.java===
package com.beauty.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 美妆支付方式枚举
 *
 * @author beauty
 * @since 2024-05-20
 */
@Getter
@AllArgsConstructor
public enum BeautyPayType {

    CASH(1, "现金"),
    WECHAT(2, "微信支付"),
    ALIPAY(3, "支付宝"),
    STORED_VALUE(4, "储值卡"),
    POINTS(5, "积分抵扣"),
    COMBINATION(6, "组合支付");

    private final Integer code;
    private final String desc;

    public static BeautyPayType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BeautyPayType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/enums/BeautyPermissionCode.java===
package com.beauty.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 美妆权限编码枚举
 *
 * @author beauty
 * @since 2024-05-20
 */
@Getter
@AllArgsConstructor
public enum BeautyPermissionCode {

    // 采购管理
    PURCHASE_SAVE("purchase:save", "采购单保存"),
    PURCHASE_DRAFT_SAVE("purchase:draft:save", "采购草稿保存"),
    PURCHASE_LIST("purchase:list", "采购单列表"),
    PURCHASE_DRAFT_LIST("purchase:draft:list", "采购草稿列表"),
    PURCHASE_DETAIL("purchase:detail", "采购单详情"),
    PURCHASE_STOCK_IN("purchase:stock:in", "采购单入库"),
    PURCHASE_EXPORT("purchase:export", "采购单导出"),
    PURCHASE_DRAFT_DELETE("purchase:draft:delete", "采购草稿删除"),
    SUPPLIER_LIST("supplier:list", "供应商列表"),
    SUPPLIER_SAVE("supplier:save", "供应商保存"),
    SUPPLIER_UPDATE("supplier:update", "供应商更新"),
    SUPPLIER_DELETE("supplier:delete", "供应商删除"),

    // 销售管理
    SALE_RETAIL_CREATE("sale:retail:create", "零售单创建"),
    SALE_LIST("sale:list", "销售单列表"),
    SALE_ONLINE_LIST("sale:online:list", "线上订单列表"),
    SALE_ONLINE_DELIVER("sale:online:deliver", "线上订单发货"),
    SALE_ONLINE_CANCEL("sale:online:cancel", "线上订单取消"),
    SALE_RETURN_CREATE("sale:return:create", "销售退货创建"),
    SALE_DETAIL("sale:detail", "销售单详情"),
    SALE_EXPORT("sale:export", "销售单导出"),

    // 库存管理
    PRODUCT_LIST("product:list", "商品列表"),
    PRODUCT_SAVE("product:save", "商品保存"),
    PRODUCT_UPDATE("product:update", "商品更新"),
    PRODUCT_DELETE("product:delete", "商品删除"),
    PRODUCT_BATCH_IMPORT("product:batch:import", "商品批量导入"),
    CATEGORY_LIST("category:list", "商品类别列表"),
    CATEGORY_SAVE("category:save", "商品类别保存"),
    CATEGORY_UPDATE("category:update", "商品类别更新"),
    CATEGORY_DELETE("category:delete", "商品类别删除"),
    LOCATION_LIST("location:list", "库位列表"),
    LOCATION_SAVE("location:save", "库位保存"),
    LOCATION_UPDATE("location:update", "库位更新"),
    LOCATION_DELETE("location:delete", "库位删除"),
    INVENTORY_WARNING_LIST("inventory:warning:list", "库存预警列表"),
    INVENTORY_TRANSFER("inventory:transfer", "库存调拨"),
    INVENTORY_TRANSFER_LIST("inventory:transfer:list", "调拨单列表"),
    INVENTORY_TRANSFER_DETAIL("inventory:transfer:detail", "调拨单详情"),
    INVENTORY_CHECK_CREATE("inventory:check:create", "盘点单创建"),
    INVENTORY_CHECK_LIST("inventory:check:list", "盘点单列表"),
    INVENTORY_CHECK_PHYSICAL_SAVE("inventory:check:physical:save", "实盘录入保存"),
    INVENTORY_CHECK_AUDIT("inventory:check:audit", "盘点单审核"),
    INVENTORY_CHECK_DETAIL("inventory:check:detail", "盘点单详情"),

    // 用户管理
    STAFF_LIST("staff:list", "员工列表"),
    STAFF_SAVE("staff:save", "员工保存"),
    STAFF_UPDATE("staff:update", "员工更新"),
    STAFF_DELETE("staff:delete", "员工删除"),
    STAFF_BATCH_IMPORT("staff:batch:import", "员工批量导入"),
    STAFF_RESET_PASSWORD("staff:reset:password", "员工重置密码"),
    ROLE_LIST("role:list", "角色列表"),
    ROLE_SAVE("role:save", "角色保存"),
    ROLE_UPDATE("role:update", "角色更新"),
    ROLE_DELETE("role:delete", "角色删除"),
    MEMBER_LIST("member:list", "会员列表"),
    MEMBER_SAVE("member:save", "会员保存"),
    MEMBER_UPDATE("member:update", "会员更新"),
    MEMBER_DELETE("member:delete", "会员删除"),
    MEMBER_BATCH_IMPORT("member:batch:import", "会员批量导入"),
    MEMBER_RECHARGE("member:recharge", "会员储值充值"),
    MEMBER_POINT_ADJUST("member:point:adjust", "会员积分调整"),
    MEMBER_DETAIL("member:detail", "会员详情"),
    LEVEL_LIST("level:list", "等级列表"),
    LEVEL_SAVE("level:save", "等级保存"),
    LEVEL_UPDATE("level:update", "等级更新"),
    LEVEL_DELETE("level:delete", "等级删除"),

    // 系统设置
    SYSTEM_BASIC_PARAM_SET("system:basic:param:set", "基础参数设置"),
    SYSTEM_SECOND_VERIFY_SET("system:second:verify:set", "二次验证设置"),
    SYSTEM_API_KEY_MANAGE("system:api:key:manage", "API Key管理"),
    SYSTEM_API_CONFIG_SET("system:api:config:set", "API配置设置"),
    SYSTEM_SMS_CONFIG_SET("system:sms:config:set", "短信配置设置"),
    SYSTEM_LOG_LIST("system:log:list", "系统日志列表"),
    SYSTEM_LOG_EXPORT("system:log:export", "系统日志导出");

    private final String code;
    private final String desc;
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/exception/BeautyBusinessException.java===
package com.beauty.common.exception;

import lombok.Getter;

/**
 * 美妆业务异常类
 *
 * @author beauty
 * @since 2024-05-20
 */
@Getter
public class BeautyBusinessException extends RuntimeException {

    private final String errorCode;

    public BeautyBusinessException(String message) {
        super(message);
        this.errorCode = "BEAUTY_COMMON_003";
    }

    public BeautyBusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BeautyBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BEAUTY_COMMON_003";
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/exception/BeautyParamException.java===
package com.beauty.common.exception;

import lombok.Getter;

/**
 * 美妆参数异常类
 *
 * @author beauty
 * @since 2024-05-20
 */
@Getter
public class BeautyParamException extends RuntimeException {

    private final String errorCode;

    public BeautyParamException(String message) {
        super(message);
        this.errorCode = "BEAUTY_COMMON_001";
    }

    public BeautyParamException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/exception/BeautyPermissionException.java===
package com.beauty.common.exception;

import lombok.Getter;

/**
 * 美妆权限异常类
 *
 * @author beauty
 * @since 2024-05-20
 */
@Getter
public class BeautyPermissionException extends RuntimeException {

    private final String errorCode;

    public BeautyPermissionException(String message) {
        super(message);
        this.errorCode = "BEAUTY_COMMON_002";
    }

    public BeautyPermissionException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/result/BeautyResult.java===
package com.beauty.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 美妆统一响应结果类
 *
 * @author beauty
 * @since 2024-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "统一响应结果")
public class BeautyResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "响应状态码", required = true, example = "200")
    private Integer code;

    @ApiModelProperty(value = "响应消息", required = true, example = "操作成功")
    private String message;

    @ApiModelProperty(value = "响应数据")
    private T data;

    @ApiModelProperty(value = "时间戳", required = true, example = "1716172800000")
    private Long timestamp;

    public static <T> BeautyResult<T> success() {
        return success(null);
    }

    public static <T> BeautyResult<T> success(T data) {
        return BeautyResult.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> BeautyResult<T> success(String message, T data) {
        return BeautyResult.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> BeautyResult<T> error(String message) {
        return BeautyResult.<T>builder()
                .code(500)
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> BeautyResult<T> error(Integer code, String message) {
        return BeautyResult.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/result/BeautyPageResult.java===
package com.beauty.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 美妆统一分页响应结果类
 *
 * @author beauty
 * @since 2024-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "统一分页响应结果")
public class BeautyPageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "数据列表", required = true)
    private List<T> records;

    @ApiModelProperty(value = "总记录数", required = true, example = "100")
    private Long total;

    @ApiModelProperty(value = "当前页", required = true, example = "1")
    private Long current;

    @ApiModelProperty(value = "每页大小", required = true, example = "20")
    private Long size;

    @ApiModelProperty(value = "总页数", required = true, example = "5")
    private Long pages;

    public static <T> BeautyPageResult<T> of(List<T> records, Long total, Long current, Long size) {
        long pages = (total + size - 1) / size;
        return BeautyPageResult.<T>builder()
                .records(records)
                .total(total)
                .current(current)
                .size(size)
                .pages(pages)
                .build();
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/annotation/BeautyPermission.java===
package com.beauty.common.annotation;

import java.lang.annotation.*;

/**
 * 美妆权限校验注解
 *
 * @author beauty
 * @since 2024-05-20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BeautyPermission {

    String value() default "";
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/annotation/BeautyLog.java===
package com.beauty.common.annotation;

import java.lang.annotation.*;

/**
 * 美妆日志记录注解
 *
 * @author beauty
 * @since 2024-05-20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BeautyLog {

    String value() default "";

    String module() default "";
}

===FILE: beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/annotation/BeautySecondVerify.java===
package com.beauty.common.annotation;

import java.lang.annotation.*;

/**
 * 美妆二次验证注解
 *
 * @author beauty
 * @since 2024-05-20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BeautySecondVerify {

    String value() default "";
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/pom.xml===
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.beauty</groupId>
        <artifactId>beauty-shop-manage</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>beauty-shop-manage-domain</artifactId>
    <description>美妆小店领域模块</description>

    <dependencies>
        <dependency>
            <groupId>com.beauty</groupId>
            <artifactId>beauty-shop-manage-common</artifactId>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Swagger -->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-boot-starter</artifactId>
        </dependency>
    </dependencies>
</project>

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyCategory.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美妆商品类别表
 *
 * @author beauty
 * @since 2024-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("beauty_category")
@ApiModel(description = "美妆商品类别表")
public class BeautyCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("category_name")
    @ApiModelProperty(value = "商品类别名称")
    private String categoryName;

    @TableField("parent_id")
    @ApiModelProperty(value = "父类别ID（0为「美妆全品类」）")
    private Long parentId;

    @TableField("general_expiration_warning_days")
    @ApiModelProperty(value = "通用过期预警天数")
    private Integer generalExpirationWarningDays;

    @TableField("general_safety_stock")
    @ApiModelProperty(value = "通用安全库存（件）")
    private Integer generalSafetyStock;

    @TableField("sort_order")
    @ApiModelProperty(value = "排序顺序")
    private Integer sortOrder;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人ID（关联beauty_staff）")
    private Long createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新人ID（关联beauty_staff）")
    private Long updateBy;

    @TableLogic
    @TableField("deleted")
    @ApiModelProperty(value = "逻辑删除（0未删除，1已删除）")
    private Integer deleted;
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyProduct.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 美妆商品档案表
 *
 * @author beauty
 * @since 2024-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("beauty_product")
@ApiModel(description = "美妆商品档案表")
public class BeautyProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("product_barcode")
    @ApiModelProperty(value = "商品条码")
    private String productBarcode;

    @TableField("product_name")
    @ApiModelProperty(value = "商品名称")
    private String productName;

    @TableField("category_id")
    @ApiModelProperty(value = "商品类别ID（关联beauty_category）")
    private Long categoryId;

    @TableField("brand")
    @ApiModelProperty(value = "品牌")
    private String brand;

    @TableField("specification")
    @ApiModelProperty(value = "规格（如50ml、片装）")
    private String specification;

    @TableField("production_date")
    @ApiModelProperty(value = "生产日期")
    private LocalDate productionDate;

    @TableField("shelf_life_months")
    @ApiModelProperty(value = "保质期（月）")
    private Integer shelfLifeMonths;

    @TableField("expiration_warning_days")
    @ApiModelProperty(value = "过期预警天数（为空则继承类别）")
    private Integer expirationWarningDays;

    @TableField("cost_price")
    @ApiModelProperty(value = "成本价（元）")
    private BigDecimal costPrice;

    @TableField("min_retail_price")
    @ApiModelProperty(value = "最低零售价（元）")
    private BigDecimal minRetailPrice;

    @TableField("suggested_retail_price")
    @ApiModelProperty(value = "建议零售价（元）")
    private BigDecimal suggestedRetailPrice;

    @TableField("member_level_discount")
    @ApiModelProperty(value = "会员等级折扣（为空则继承等级）")
    private BigDecimal memberLevelDiscount;

    @TableField("safety_stock")
    @ApiModelProperty(value = "安全库存（件，为空则继承类别）")
    private Integer safetyStock;

    @TableField("product_images")
    @ApiModelProperty(value = "商品图片URL（逗号分隔，最多5张）")
    private String productImages;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人ID（关联beauty_staff）")
    private Long createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新人ID（关联beauty_staff）")
    private Long updateBy;

    @TableLogic
    @TableField("deleted")
    @ApiModelProperty(value = "逻辑删除（0未删除，1已删除）")
    private Integer deleted;
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyLocation.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美妆库位档案表
 *
 * @author beauty
 * @since 2024-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("beauty_location")
@ApiModel(description = "美妆库位档案表")
public class BeautyLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("location_name")
    @ApiModelProperty(value = "库位名称（如主仓库-口红区-第一层）")
    private String locationName;

    @TableField("capacity")
    @ApiModelProperty(value = "库位容量（件）")
    private Integer capacity;

    @TableField("location_status")
    @ApiModelProperty(value = "库位状态（1正常，0停用）")
    private Integer locationStatus;

    @TableField("priority")
    @ApiModelProperty(value = "库位优先级（用于线上订单库存分配，数字越大优先级越高）")
    private Integer priority;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人ID（关联beauty_staff）")
    private Long createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新人ID（关联beauty_staff）")
    private Long updateBy;

    @TableLogic
    @TableField("deleted")
    @ApiModelProperty(value = "逻辑删除（0未删除，1已删除）")
    private Integer deleted;
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyProductInventory.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 美妆商品库存表
 *
 * @author beauty
 * @since 2024-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("beauty_product_inventory")
@ApiModel(description = "美妆商品库存表")
public class BeautyProductInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("product_id")
    @ApiModelProperty(value = "商品ID（关联beauty_product）")
    private Long productId;

    @TableField("location_id")
    @ApiModelProperty(value = "库位ID（关联beauty_location）")
    private Long locationId;

    @TableField("stock_quantity")
    @ApiModelProperty(value = "库存数量（件）")
    private Integer stockQuantity;

    @TableField("locked_quantity")
    @ApiModelProperty(value = "锁定库存数量（件，用于盘点/线上订单）")
    private Integer lockedQuantity;

    @TableField("total_amount")
    @ApiModelProperty(value = "库存总金额（元）")
    private BigDecimal totalAmount;

    @TableField("batch_production_date")
    @ApiModelProperty(value = "库存批次生产日期（可选，用于过期预警）")
    private LocalDate batchProductionDate;

    @Version
    @TableField("version")
    @ApiModelProperty(value = "乐观锁版本号")
    private Integer version;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautySupplier.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美妆供应商档案表
 *
 * @author beauty
 * @since 2024-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("beauty_supplier")
@ApiModel(description = "美妆供应商档案表")
public class BeautySupplier implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("supplier_name")
    @ApiModelProperty(value = "供应商名称")
    private String supplierName;

    @TableField("contact_phone")
    @ApiModelProperty(value = "联系方式（手机号/座机号）")
    private String contactPhone;

    @TableField("address")
    @ApiModelProperty(value = "地址")
    private String address;

    @TableField("supply_category_ids")
    @ApiModelProperty(value = "供应商品类别ID（逗号分隔）")
    private String supplyCategoryIds;

    @TableField("cooperation_discount_rate")
    @ApiModelProperty(value = "合作折扣率")
    private BigDecimal cooperationDiscountRate;

    @TableField("payable_balance")
    @ApiModelProperty(value = "应付账款余额（元）")
    private BigDecimal payableBalance;

    @Version
    @TableField("version")
    @ApiModelProperty(value = "乐观锁版本号")
    private Integer version;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人ID（关联beauty_staff）")
    private Long createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新人ID（关联beauty_staff）")
    private Long updateBy;

    @TableLogic
    @TableField("deleted")
    @ApiModelProperty(value = "逻辑删除（0未删除，1已删除）")
    private Integer deleted;
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyCategoryMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆商品类别Mapper接口
 *
 * @author beauty
 * @since 2024-05-20
 */
@Mapper
public interface BeautyCategoryMapper extends BaseMapper<BeautyCategory> {
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyProductMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆商品档案Mapper接口
 *
 * @author beauty
 * @since 2024-05-20
 */
@Mapper
public interface BeautyProductMapper extends BaseMapper<BeautyProduct> {
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyLocationMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyLocation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆库位档案Mapper接口
 *
 * @author beauty
 * @since 2024-05-20
 */
@Mapper
public interface BeautyLocationMapper extends BaseMapper<BeautyLocation> {
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyProductInventoryMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyProductInventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆商品库存Mapper接口
 *
 * @author beauty
 * @since 2024-05-20
 */
@Mapper
public interface BeautyProductInventoryMapper extends BaseMapper<BeautyProductInventory> {
}

===FILE: beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautySupplierMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautySupplier;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆供应商档案Mapper接口
 *
 * @author beauty
 * @since 2024-05-20
 */
@Mapper
public interface BeautySupplierMapper extends BaseMapper<BeautySupplier> {
}

===FILE: beauty-shop-manage/beauty-shop-manage-config/pom.xml===
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.beauty</groupId>
        <artifactId>beauty-shop-manage</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>beauty-shop-manage-config</artifactId>
    <description>美妆小店配置模块</description>

    <dependencies>
        <dependency>
            <groupId>com.beauty</groupId>
            <artifactId>beauty-shop-manage-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.beauty</groupId>
            <artifactId>beauty-shop-manage-domain</artifactId>
        </dependency>

        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>

        <!-- Redisson -->
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
        </dependency>

        <!-- Swagger -->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-boot-starter</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>

===FILE: beauty-shop-manage/beauty-shop-manage-config/src/main/java/com/beauty/config/MyBatisPlusConfig.java===
package com.beauty.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus配置类
 *
 * @author beauty
 * @since 2024-05-20
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Component
    public static class BeautyMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            // 这里需要从Security上下文或JWT中获取当前登录用户ID
            this.strictInsertFill(metaObject, "createBy", Long.class, 1L);
            this.strictInsertFill(metaObject, "updateBy", Long.class, 1L);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            // 这里需要从Security上下文或JWT中获取当前登录用户ID
            this.strictUpdateFill(metaObject, "updateBy", Long.class, 1L);
        }
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-config/src/main/java/com/beauty/config/SwaggerConfig.java===
package com.beauty.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

/**
 * Swagger配置类
 *
 * @author beauty
 * @since 2024-05-20
 */
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .groupName("管理端接口")
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.ant("/api/beauty/**"))
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    @Bean
    public Docket apiApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .groupName("通用前端对接接口")
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.ant("/api/beauty/api/**"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("美妆小店轻量级后端管理系统API文档")
                .description("美妆小店轻量级后端管理系统V1.0接口文档")
                .contact(new Contact("beauty", "", ""))
                .version("1.0.0")
                .build();
    }

    private List<SecurityScheme> securitySchemes() {
        return Collections.singletonList(
                new HttpAuthenticationScheme("JWT", "Bearer token", "bearer")
        );
    }

    private List<SecurityContext> securityContexts() {
        return Collections.singletonList(
                SecurityContext.builder()
                        .securityReferences(securityReferences())
                        .operationSelector(operationContext -> true)
                        .build()
        );
    }

    private List<SecurityReference> securityReferences() {
        return Collections.singletonList(
                new SecurityReference("JWT", new AuthorizationScope[]{new AuthorizationScope("global", "accessEverything")})
        );
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-app/pom.xml===
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.beauty</groupId>
        <artifactId>beauty-shop-manage</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>beauty-shop-manage-app</artifactId>
    <description>美妆小店启动模块</description>

    <dependencies>
        <dependency>
            <groupId>com.beauty</groupId>
            <artifactId>beauty-shop-manage-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.beauty</groupId>
            <artifactId>beauty-shop-manage-domain</artifactId>
        </dependency>
        <dependency>
            <groupId>com.beauty</groupId>
            <artifactId>beauty-shop-manage-service</artifactId>
        </dependency>
        <dependency>
            <groupId>com.beauty</groupId>
            <artifactId>beauty-shop-manage-controller</artifactId>
        </dependency>
        <dependency>
            <groupId>com.beauty</groupId>
            <artifactId>beauty-shop-manage-config</artifactId>
        </dependency>

        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

===FILE: beauty-shop-manage/beauty-shop-manage-app/src/main/java/com/beauty/BeautyShopManageApplication.java===
package com.beauty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 美妆小店轻量级后端管理系统启动类
 *
 * @author beauty
 * @since 2024-05-20
 */
@SpringBootApplication
@MapperScan("com.beauty.domain.mapper")
public class BeautyShopManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeautyShopManageApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  美妆小店轻量级后端管理系统启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}

===FILE: beauty-shop-manage/beauty-shop-manage-app/src/main/resources/application.yml===
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: beauty-shop-manage
  profiles:
    active: dev
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/beauty_shop_manage?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.beauty.domain.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html

===FILE: beauty-shop-manage/beauty-shop-manage-app/src/main/resources/application-dev.yml===
# 开发环境配置
logging:
  level:
    com.beauty: debug
    org.springframework: info

===FILE: beauty-shop-manage/beauty-shop-manage-app/src/main/resources/application-prod.yml===
# 生产环境配置
logging:
  level:
    com.beauty: info
    org.springframework: warn
  file:
    name: /var/log/beauty-shop-manage/application.log

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

以上是阶段1的完整代码文件，包含了数据库初始化脚本、项目配置文件、公共模块和基础的domain/config/app模块代码。这些代码为后续的业务模块开发提供了坚实的基础。