===FILE:beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyPurchase.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美妆采购单主表实体
 *
 * @author beauty-shop
 * @since 2024-05-20
 */
@Data
@TableName("beauty_purchase")
@Schema(description = "美妆采购单主表")
public class BeautyPurchase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 采购单号（CG+8位年月日+4位流水号）
     */
    @Schema(description = "采购单号")
    private String purchaseNo;

    /**
     * 供应商ID（关联beauty_supplier）
     */
    @Schema(description = "供应商ID")
    private Long supplierId;

    /**
     * 临时供应商名称（正式供应商为空）
     */
    @Schema(description = "临时供应商名称")
    private String tempSupplierName;

    /**
     * 临时供应商联系方式（正式供应商为空）
     */
    @Schema(description = "临时供应商联系方式")
    private String tempSupplierPhone;

    /**
     * 采购总金额（元）
     */
    @Schema(description = "采购总金额")
    private BigDecimal totalAmount;

    /**
     * 采购单状态（0草稿，1待入库，2部分入库，3已完成）
     */
    @Schema(description = "采购单状态")
    private Integer purchaseStatus;

    /**
     * 应付账款支付方式（0不使用应付，1使用应付）
     */
    @Schema(description = "应付账款支付方式")
    private Integer payablePayType;

    /**
     * 入库员ID（关联beauty_staff）
     */
    @Schema(description = "入库员ID")
    private Long stockInStaffId;

    /**
     * 入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入库时间")
    private LocalDateTime stockInTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 乐观锁版本号
     */
    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人ID（关联beauty_staff）
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人ID")
    private Long createBy;

    /**
     * 更新人ID（关联beauty_staff）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID")
    private Long updateBy;

    /**
     * 逻辑删除（0未删除，1已删除）
     */
    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;

}

===FILE:beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyPurchaseDetail.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美妆采购单明细表实体
 *
 * @author beauty-shop
 * @since 2024-05-20
 */
@Data
@TableName("beauty_purchase_detail")
@Schema(description = "美妆采购单明细表")
public class BeautyPurchaseDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 采购单ID（关联beauty_purchase）
     */
    @Schema(description = "采购单ID")
    private Long purchaseId;

    /**
     * 商品ID（关联beauty_product）
     */
    @Schema(description = "商品ID")
    private Long productId;

    /**
     * 采购数量（件）
     */
    @Schema(description = "采购数量")
    private Integer purchaseQuantity;

    /**
     * 已入库数量（件）
     */
    @Schema(description = "已入库数量")
    private Integer stockInQuantity;

    /**
     * 采购单价（元）
     */
    @Schema(description = "采购单价")
    private BigDecimal purchasePrice;

    /**
     * 采购折扣率（0.01-1.00）
     */
    @Schema(description = "采购折扣率")
    private BigDecimal purchaseDiscount;

    /**
     * 采购小计金额（元）
     */
    @Schema(description = "采购小计金额")
    private BigDecimal subtotalAmount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除（0未删除，1已删除）
     */
    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;

}

===FILE:beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautySale.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美妆销售单主表实体
 *
 * @author beauty-shop
 * @since 2024-05-20
 */
@Data
@TableName("beauty_sale")
@Schema(description = "美妆销售单主表")
public class BeautySale implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 销售单号（XS+8位年月日+4位流水号）
     */
    @Schema(description = "销售单号")
    private String saleNo;

    /**
     * 会员ID（关联beauty_member，散客为空）
     */
    @Schema(description = "会员ID")
    private Long memberId;

    /**
     * 收银员ID（关联beauty_staff）
     */
    @Schema(description = "收银员ID")
    private Long cashierStaffId;

    /**
     * 发货员ID（关联beauty_staff，线上单必填）
     */
    @Schema(description = "发货员ID")
    private Long deliverStaffId;

    /**
     * 物流公司ID（关联beauty_logistics，线上单必填）
     */
    @Schema(description = "物流公司ID")
    private Long logisticsId;

    /**
     * 物流单号（线上单必填）
     */
    @Schema(description = "物流单号")
    private String logisticsNo;

    /**
     * 库位ID（关联beauty_location，零售单必填）
     */
    @Schema(description = "库位ID")
    private Long locationId;

    /**
     * 商品总金额（元，未抹零/抹分）
     */
    @Schema(description = "商品总金额")
    private BigDecimal productTotalAmount;

    /**
     * 会员折扣总金额（元）
     */
    @Schema(description = "会员折扣总金额")
    private BigDecimal memberDiscountAmount;

    /**
     * 抹零/抹分金额（元）
     */
    @Schema(description = "抹零/抹分金额")
    private BigDecimal roundingAmount;

    /**
     * 实付总金额（元）
     */
    @Schema(description = "实付总金额")
    private BigDecimal actualPayAmount;

    /**
     * 积分抵扣数量（个）
     */
    @Schema(description = "积分抵扣数量")
    private Integer pointsDeductQuantity;

    /**
     * 积分抵扣金额（元）
     */
    @Schema(description = "积分抵扣金额")
    private BigDecimal pointsDeductAmount;

    /**
     * 储值消费金额（元）
     */
    @Schema(description = "储值消费金额")
    private BigDecimal storedValuePayAmount;

    /**
     * 支付方式（1现金，2微信，3支付宝，4储值卡，5积分，6组合）
     */
    @Schema(description = "支付方式")
    private Integer payType;

    /**
     * 销售单状态（1待发货，2已发货，3已完成，4已部分退货，5已全部退货，6已取消）
     */
    @Schema(description = "销售单状态")
    private Integer saleStatus;

    /**
     * 订单来源（1零售，2线上小程序，3线上公众号，4线上APP）
     */
    @Schema(description = "订单来源")
    private Integer orderSource;

    /**
     * 发货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发货时间")
    private LocalDateTime deliverTime;

    /**
     * 取消原因
     */
    @Schema(description = "取消原因")
    private String cancelReason;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 乐观锁版本号
     */
    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人ID（关联beauty_staff）
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人ID")
    private Long createBy;

    /**
     * 更新人ID（关联beauty_staff）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID")
    private Long updateBy;

    /**
     * 逻辑删除（0未删除，1已删除）
     */
    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;

}

===FILE:beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautySaleDetail.java===
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美妆销售单明细表实体
 *
 * @author beauty-shop
 * @since 2024-05-20
 */
@Data
@TableName("beauty_sale_detail")
@Schema(description = "美妆销售单明细表")
public class BeautySaleDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 销售单ID（关联beauty_sale）
     */
    @Schema(description = "销售单ID")
    private Long saleId;

    /**
     * 商品ID（关联beauty_product）
     */
    @Schema(description = "商品ID")
    private Long productId;

    /**
     * 销售数量（件）
     */
    @Schema(description = "销售数量")
    private Integer saleQuantity;

    /**
     * 已退货数量（件）
     */
    @Schema(description = "已退货数量")
    private Integer returnQuantity;

    /**
     * 销售单价（元，未折扣）
     */
    @Schema(description = "销售单价")
    private BigDecimal salePrice;

    /**
     * 销售折扣率（0.01-1.00）
     */
    @Schema(description = "销售折扣率")
    private BigDecimal saleDiscount;

    /**
     * 成本单价（元，取入库时的加权平均或先进先出）
     */
    @Schema(description = "成本单价")
    private BigDecimal costPrice;

    /**
     * 销售小计金额（元，已折扣）
     */
    @Schema(description = "销售小计金额")
    private BigDecimal subtotalAmount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除（0未删除，1已删除）
     */
    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;

}

===FILE:beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyPurchaseMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyPurchase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆采购单主表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-05-20
 */
@Mapper
public interface BeautyPurchaseMapper extends BaseMapper<BeautyPurchase> {

}

===FILE:beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyPurchaseDetailMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyPurchaseDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆采购单明细表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-05-20
 */
@Mapper
public interface BeautyPurchaseDetailMapper extends BaseMapper<BeautyPurchaseDetail> {

}

===FILE:beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautySaleMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautySale;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆销售单主表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-05-20
 */
@Mapper
public interface BeautySaleMapper extends BaseMapper<BeautySale> {

}

===FILE:beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautySaleDetailMapper.java===
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautySaleDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆销售单明细表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-05-20
 */
@Mapper
public interface BeautySaleDetailMapper extends BaseMapper<BeautySaleDetail> {

}