===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyCategory.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美妆商品类别表
 *
 * @author beauty-shop
 * @since 2024-05-28
 */
@Data
@TableName("beauty_category")
@Schema(description = "美妆商品类别表")
public class BeautyCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 商品类别名称
     */
    @TableField("category_name")
    @Schema(description = "商品类别名称")
    private String categoryName;

    /**
     * 父类别ID（0为「美妆全品类」）
     */
    @TableField("parent_id")
    @Schema(description = "父类别ID")
    private Long parentId;

    /**
     * 通用过期预警天数
     */
    @TableField("general_expiration_warning_days")
    @Schema(description = "通用过期预警天数")
    private Integer generalExpirationWarningDays;

    /**
     * 通用安全库存（件）
     */
    @TableField("general_safety_stock")
    @Schema(description = "通用安全库存")
    private Integer generalSafetyStock;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    @Schema(description = "排序顺序")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人ID")
    private Long createBy;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID")
    private Long updateBy;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    @Schema(description = "逻辑删除")
    private Integer deleted;
}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyLocation.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美妆库位档案表
 *
 * @author beauty-shop
 * @since 2024-05-28
 */
@Data
@TableName("beauty_location")
@Schema(description = "美妆库位档案表")
public class BeautyLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 库位名称
     */
    @TableField("location_name")
    @Schema(description = "库位名称")
    private String locationName;

    /**
     * 库位容量（件）
     */
    @TableField("capacity")
    @Schema(description = "库位容量")
    private Integer capacity;

    /**
     * 库位状态（1正常，0停用）
     */
    @TableField("location_status")
    @Schema(description = "库位状态")
    private Integer locationStatus;

    /**
     * 库位优先级
     */
    @TableField("priority")
    @Schema(description = "库位优先级")
    private Integer priority;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人ID")
    private Long createBy;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID")
    private Long updateBy;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    @Schema(description = "逻辑删除")
    private Integer deleted;
}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyProduct.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 美妆商品档案表
 *
 * @author beauty-shop
 * @since 2024-05-28
 */
@Data
@TableName("beauty_product")
@Schema(description = "美妆商品档案表")
public class BeautyProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 商品条码
     */
    @TableField("product_barcode")
    @Schema(description = "商品条码")
    private String productBarcode;

    /**
     * 商品名称
     */
    @TableField("product_name")
    @Schema(description = "商品名称")
    private String productName;

    /**
     * 商品类别ID
     */
    @TableField("category_id")
    @Schema(description = "商品类别ID")
    private Long categoryId;

    /**
     * 品牌
     */
    @TableField("brand")
    @Schema(description = "品牌")
    private String brand;

    /**
     * 规格
     */
    @TableField("specification")
    @Schema(description = "规格")
    private String specification;

    /**
     * 生产日期
     */
    @TableField("production_date")
    @Schema(description = "生产日期")
    private LocalDate productionDate;

    /**
     * 保质期（月）
     */
    @TableField("shelf_life_months")
    @Schema(description = "保质期")
    private Integer shelfLifeMonths;

    /**
     * 过期预警天数
     */
    @TableField("expiration_warning_days")
    @Schema(description = "过期预警天数")
    private Integer expirationWarningDays;

    /**
     * 成本价
     */
    @TableField("cost_price")
    @Schema(description = "成本价")
    private BigDecimal costPrice;

    /**
     * 最低零售价
     */
    @TableField("min_retail_price")
    @Schema(description = "最低零售价")
    private BigDecimal minRetailPrice;

    /**
     * 建议零售价
     */
    @TableField("suggested_retail_price")
    @Schema(description = "建议零售价")
    private BigDecimal suggestedRetailPrice;

    /**
     * 会员等级折扣
     */
    @TableField("member_level_discount")
    @Schema(description = "会员等级折扣")
    private BigDecimal memberLevelDiscount;

    /**
     * 安全库存
     */
    @TableField("safety_stock")
    @Schema(description = "安全库存")
    private Integer safetyStock;

    /**
     * 商品图片URL
     */
    @TableField("product_images")
    @Schema(description = "商品图片URL")
    private String productImages;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人ID")
    private Long createBy;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID")
    private Long updateBy;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    @Schema(description = "逻辑删除")
    private Integer deleted;
}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyProductInventory.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 美妆商品库存表
 *
 * @author beauty-shop
 * @since 2024-05-28
 */
@Data
@TableName("beauty_product_inventory")
@Schema(description = "美妆商品库存表")
public class BeautyProductInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 商品ID
     */
    @TableField("product_id")
    @Schema(description = "商品ID")
    private Long productId;

    /**
     * 库位ID
     */
    @TableField("location_id")
    @Schema(description = "库位ID")
    private Long locationId;

    /**
     * 库存数量
     */
    @TableField("stock_quantity")
    @Schema(description = "库存数量")
    private Integer stockQuantity;

    /**
     * 锁定库存数量
     */
    @TableField("locked_quantity")
    @Schema(description = "锁定库存数量")
    private Integer lockedQuantity;

    /**
     * 库存总金额
     */
    @TableField("total_amount")
    @Schema(description = "库存总金额")
    private BigDecimal totalAmount;

    /**
     * 库存批次生产日期
     */
    @TableField("batch_production_date")
    @Schema(description = "库存批次生产日期")
    private LocalDate batchProductionDate;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyCategoryMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆商品类别Mapper接口
 *
 * @author beauty-shop
 * @since 2024-05-28
 */
@Mapper
public interface BeautyCategoryMapper extends BaseMapper<BeautyCategory> {

}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyLocationMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyLocation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆库位档案Mapper接口
 *
 * @author beauty-shop
 * @since 2024-05-28
 */
@Mapper
public interface BeautyLocationMapper extends BaseMapper<BeautyLocation> {

}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyProductMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆商品档案Mapper接口
 *
 * @author beauty-shop
 * @since 2024-05-28
 */
@Mapper
public interface BeautyProductMapper extends BaseMapper<BeautyProduct> {

}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyProductInventoryMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyProductInventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆商品库存Mapper接口
 *
 * @author beauty-shop
 * @since 2024-05-28
 */
@Mapper
public interface BeautyProductInventoryMapper extends BaseMapper<BeautyProductInventory> {

}