# 美妆小店轻量级后端管理系统V1.0 阶段2后端代码
---

## 1. 公共模块补充（从阶段1延续）
### 1.1 常量定义
---
===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/constant/BeautyAuthConstant.java===
```java
package com.beauty.common.constant;

/**
 * 美妆小店认证相关常量
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
public interface BeautyAuthConstant {

    /**
     * JWT令牌前缀
     */
    String BEAUTY_JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * JWT请求头名称
     */
    String BEAUTY_JWT_HEADER_NAME = "Authorization";

    /**
     * JWT有效期（天）
     */
    Integer BEAUTY_JWT_EXPIRE_DAYS = 7;

    /**
     * Refresh JWT有效期（天）
     */
    Integer BEAUTY_REFRESH_JWT_EXPIRE_DAYS = 30;

    /**
     * JWT黑名单Redis前缀
     */
    String BEAUTY_JWT_BLACKLIST_REDIS_PREFIX = "beauty:jwt:blacklist:";

    /**
     * Refresh JWT Redis前缀
     */
    String BEAUTY_REFRESH_JWT_REDIS_PREFIX = "beauty:refresh:jwt:";

    /**
     * AES256加密初始向量
     */
    String BEAUTY_AES256_IV = "BEAUTY_SHOP_IV_16";

    /**
     * 员工初始密码AES256加密前值
     */
    String BEAUTY_STAFF_INITIAL_PASSWORD = "Admin@123";

    /**
     * 员工首次登录强制修改密码标记Redis前缀
     */
    String BEAUTY_STAFF_FIRST_LOGIN_REDIS_PREFIX = "beauty:staff:first:login:";

    /**
     * 员工登录验证码Redis前缀
     */
    String BEAUTY_STAFF_LOGIN_CAPTCHA_REDIS_PREFIX = "beauty:staff:login:captcha:";

    /**
     * 员工登录验证码有效期（秒）
     */
    Integer BEAUTY_STAFF_LOGIN_CAPTCHA_EXPIRE_SECONDS = 300;

}
```

### 1.2 枚举定义
---
===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/enums/BeautyStaffStatus.java===
```java
package com.beauty.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 美妆小店员工状态枚举
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Getter
@AllArgsConstructor
public enum BeautyStaffStatus {

    /**
     * 正常
     */
    NORMAL(1, "正常"),

    /**
     * 停用
     */
    DISABLED(0, "停用");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举对象
     */
    public static BeautyStaffStatus getByCode(Integer code) {
        for (BeautyStaffStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/enums/BeautyMemberStatus.java===
```java
package com.beauty.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 美妆小店会员状态枚举
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Getter
@AllArgsConstructor
public enum BeautyMemberStatus {

    /**
     * 正常
     */
    NORMAL(1, "正常"),

    /**
     * 冻结
     */
    FROZEN(0, "冻结");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举对象
     */
    public static BeautyMemberStatus getByCode(Integer code) {
        for (BeautyMemberStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/enums/BeautyPermissionCode.java===
```java
package com.beauty.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 美妆小店权限码枚举（简化版，仅包含阶段2+基础权限）
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Getter
@AllArgsConstructor
public enum BeautyPermissionCode {

    // -------------- 员工管理权限 --------------
    STAFF_LIST("staff:list", "员工列表查询"),
    STAFF_SAVE("staff:save", "员工新增"),
    STAFF_UPDATE("staff:update", "员工修改"),
    STAFF_DELETE("staff:delete", "员工删除"),
    STAFF_RESET_PASSWORD("staff:reset:password", "员工重置密码"),
    STAFF_BATCH_IMPORT("staff:batch:import", "员工批量导入"),
    STAFF_TEMPLATE_DOWNLOAD("staff:template:download", "员工模板下载"),
    STAFF_EXPORT("staff:export", "员工批量导出"),

    // -------------- 角色管理权限 --------------
    ROLE_LIST("role:list", "角色列表查询"),
    ROLE_SAVE("role:save", "角色新增"),
    ROLE_UPDATE("role:update", "角色修改"),
    ROLE_DELETE("role:delete", "角色删除"),

    // -------------- 会员管理权限 --------------
    MEMBER_LIST("member:list", "会员列表查询"),
    MEMBER_SAVE("member:save", "会员新增"),
    MEMBER_UPDATE("member:update", "会员修改"),
    MEMBER_DELETE("member:delete", "会员删除"),
    MEMBER_BATCH_IMPORT("member:batch:import", "会员批量导入"),
    MEMBER_TEMPLATE_DOWNLOAD("member:template:download", "会员模板下载"),
    MEMBER_EXPORT("member:export", "会员批量导出"),
    MEMBER_RECHARGE("member:recharge", "会员储值充值"),
    MEMBER_POINT_ADJUST("member:point:adjust", "会员积分调整"),
    MEMBER_DETAIL("member:detail", "会员详情查询"),
    MEMBER_FLOW_LIST("member:flow:list", "会员流水查询"),
    MEMBER_FLOW_EXPORT("member:flow:export", "会员流水导出"),

    // -------------- 会员等级管理权限 --------------
    LEVEL_LIST("level:list", "会员等级列表查询"),
    LEVEL_SAVE("level:save", "会员等级新增"),
    LEVEL_UPDATE("level:update", "会员等级修改"),
    LEVEL_DELETE("level:delete", "会员等级删除"),

    // -------------- 基础权限 --------------
    DASHBOARD("dashboard", "系统首页"),
    LOGOUT("logout", "登出"),
    FIRST_PASSWORD_CHANGE("first:password:change", "首次登录强制修改密码"),
    REFRESH_TOKEN("refresh:token", "刷新Token");

    private final String code;
    private final String desc;

}
```

### 1.3 自定义注解
---
===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/annotation/BeautyPermission.java===
```java
package com.beauty.common.annotation;

import com.beauty.common.enums.BeautyPermissionCode;

import java.lang.annotation.*;

/**
 * 美妆小店权限校验注解
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BeautyPermission {

    /**
     * 需要的权限码
     */
    BeautyPermissionCode[] value();

    /**
     * 是否需要所有权限，默认false（只要一个权限满足即可）
     */
    boolean requireAll() default false;

}
```

### 1.4 统一响应结果
---
===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/result/BeautyResult.java===
```java
package com.beauty.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 美妆小店统一响应结果
 *
 * @param <T> 响应数据类型
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeautyResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（无数据）
     *
     * @param <T> 响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> success() {
        return new BeautyResult<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> success(T data) {
        return new BeautyResult<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> success(String message) {
        return new BeautyResult<>(200, message, null);
    }

    /**
     * 失败响应（默认状态码）
     *
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> fail(String message) {
        return new BeautyResult<>(500, message, null);
    }

    /**
     * 失败响应（自定义状态码）
     *
     * @param code    响应状态码
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> fail(Integer code, String message) {
        return new BeautyResult<>(code, message, null);
    }

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/result/BeautyPageResult.java===
```java
package com.beauty.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 美妆小店统一分页响应结果
 *
 * @param <T> 分页数据类型
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeautyPageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 分页数据列表
     */
    private List<T> records;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

}
```

### 1.5 工具类
---
===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/util/AES256Util.java===
```java
package com.beauty.common.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.beauty.common.constant.BeautyAuthConstant;

import java.nio.charset.StandardCharsets;

/**
 * 美妆小店AES256加密工具类
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
public class AES256Util {

    private AES256Util() {
    }

    /**
     * 加密
     *
     * @param plainText 明文
     * @param secretKey 密钥（32位字符串）
     * @return 密文
     */
    public static String encrypt(String plainText, String secretKey) {
        AES aes = SecureUtil.aes(secretKey.getBytes(StandardCharsets.UTF_8), BeautyAuthConstant.BEAUTY_AES256_IV.getBytes(StandardCharsets.UTF_8));
        return aes.encryptHex(plainText);
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param secretKey  密钥（32位字符串）
     * @return 明文
     */
    public static String decrypt(String cipherText, String secretKey) {
        AES aes = SecureUtil.aes(secretKey.getBytes(StandardCharsets.UTF_8), BeautyAuthConstant.BEAUTY_AES256_IV.getBytes(StandardCharsets.UTF_8));
        return aes.decryptStr(cipherText, CharsetUtil.CHARSET_UTF_8);
    }

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/util/SHA256Util.java===
```java
package com.beauty.common.util;

import cn.hutool.crypto.SecureUtil;

/**
 * 美妆小店SHA256加密工具类
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
public class SHA256Util {

    private SHA256Util() {
    }

    /**
     * SHA256加密
     *
     * @param plainText 明文
     * @return 密文
     */
    public static String encrypt(String plainText) {
        return SecureUtil.sha256(plainText);
    }

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/util/JwtUtil.java===
```java
package com.beauty.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 美妆小店JWT工具类
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Slf4j
public class JwtUtil {

    private JwtUtil() {
    }

    /**
     * 生成JWT令牌
     *
     * @param staffId     员工ID
     * @param loginAccount 员工登录账号
     * @param secretKey   JWT密钥
     * @param expireDays  有效期（天）
     * @return JWT令牌
     */
    public static String generateToken(Long staffId, String loginAccount, String secretKey, Integer expireDays) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("staffId", staffId);
        claims.put("loginAccount", loginAccount);
        claims.put("jti", IdUtil.fastSimpleUUID());
        Date now = new Date();
        Date expireDate = DateUtil.offsetDay(now, expireDays);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param token     JWT令牌
     * @param secretKey JWT密钥
     * @return Claims对象
     */
    public static Claims parseToken(String token, String secretKey) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证JWT令牌是否过期
     *
     * @param claims Claims对象
     * @return 是否过期
     */
    public static boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从JWT令牌中获取员工ID
     *
     * @param claims Claims对象
     * @return 员工ID
     */
    public static Long getStaffId(Claims claims) {
        return claims.get("staffId", Long.class);
    }

    /**
     * 从JWT令牌中获取登录账号
     *
     * @param claims Claims对象
     * @return 登录账号
     */
    public static String getLoginAccount(Claims claims) {
        return claims.get("loginAccount", String.class);
    }

    /**
     * 从JWT令牌中获取JTI
     *
     * @param claims Claims对象
     * @return JTI
     */
    public static String getJti(Claims claims) {
        return claims.getId();
    }

}
```

---

## 2. 领域模块（Domain）
### 2.1 数据库实体
---
===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyStaff.java===
```java
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.beauty.common.enums.BeautyStaffStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美妆小店员工档案表实体
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@TableName("beauty_staff")
public class BeautyStaff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工工号
     */
    @TableField("staff_no")
    private String staffNo;

    /**
     * 登录账号
     */
    @TableField("login_account")
    private String loginAccount;

    /**
     * 登录密码（AES256加密）
     */
    @TableField("password")
    private String password;

    /**
     * 员工姓名
     */
    @TableField("staff_name")
    private String staffName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 角色ID（关联beauty_role）
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 员工状态（1正常，0停用）
     */
    @TableField("staff_status")
    private BeautyStaffStatus staffStatus;

    /**
     * 是否首次登录（1是，0否）
     */
    @TableField("is_first_login")
    private Integer isFirstLogin;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 逻辑删除（0未删除，1已删除）
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyRole.java===
```java
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美妆小店员工角色表实体
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@TableName("beauty_role")
public class BeautyRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色描述
     */
    @TableField("role_desc")
    private String roleDesc;

    /**
     * 权限码列表（逗号分隔）
     */
    @TableField("permission_codes")
    private String permissionCodes;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 逻辑删除（0未删除，1已删除）
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyMember.java===
```java
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.beauty.common.enums.BeautyMemberStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 美妆小店会员档案表实体
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@TableName("beauty_member")
public class BeautyMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员条码
     */
    @TableField("member_barcode")
    private String memberBarcode;

    /**
     * 会员卡号
     */
    @TableField("member_card_no")
    private String memberCardNo;

    /**
     * 会员姓名
     */
    @TableField("member_name")
    private String memberName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 性别（1男，0女，2未知）
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 等级ID（关联beauty_member_level）
     */
    @TableField("level_id")
    private Long levelId;

    /**
     * 累计消费金额（元）
     */
    @TableField("total_consume_amount")
    private BigDecimal totalConsumeAmount;

    /**
     * 消费次数
     */
    @TableField("consume_count")
    private Integer consumeCount;

    /**
     * 可用储值余额（元）
     */
    @TableField("available_balance")
    private BigDecimal availableBalance;

    /**
     * 可用积分
     */
    @TableField("available_points")
    private Integer availablePoints;

    /**
     * 会员状态（1正常，0冻结）
     */
    @TableField("member_status")
    private BeautyMemberStatus memberStatus;

    /**
     * 会员头像URL
     */
    @TableField("member_avatar")
    private String memberAvatar;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 版本号（乐观锁）
     */
    @TableField("version")
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 逻辑删除（0未删除，1已删除）
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/entity/BeautyMemberLevel.java===
```java
package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美妆小店会员等级表实体
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@TableName("beauty_member_level")
public class BeautyMemberLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称
     */
    @TableField("level_name")
    private String levelName;

    /**
     * 等级图标URL
     */
    @TableField("level_icon")
    private String levelIcon;

    /**
     * 升级条件（累计消费金额≥X元）
     */
    @TableField("level_condition")
    private BigDecimal levelCondition;

    /**
     * 普通折扣率（0.01-1.00）
     */
    @TableField("general_discount")
    private BigDecimal generalDiscount;

    /**
     * 生日折扣率（0.01-1.00）
     */
    @TableField("birthday_discount")
    private BigDecimal birthdayDiscount;

    /**
     * 积分兑换比例（1元=X积分）
     */
    @TableField("point_ratio")
    private BigDecimal pointRatio;

    /**
     * 积分抵现比例（X积分=1元）
     */
    @TableField("point_cash_ratio")
    private Integer pointCashRatio;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 逻辑删除（0未删除，1已删除）
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

}
```

### 2.2 Mapper接口
---
===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyStaffMapper.java===
```java
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyStaff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆小店员工档案表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Mapper
public interface BeautyStaffMapper extends BaseMapper<BeautyStaff> {

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyRoleMapper.java===
```java
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆小店员工角色表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Mapper
public interface BeautyRoleMapper extends BaseMapper<BeautyRole> {

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyMemberMapper.java===
```java
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆小店会员档案表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Mapper
public interface BeautyMemberMapper extends BaseMapper<BeautyMember> {

}
```

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/mapper/BeautyMemberLevelMapper.java===
```java
package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyMemberLevel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆小店会员等级表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Mapper
public interface BeautyMemberLevelMapper extends BaseMapper<BeautyMemberLevel> {

}
```

---

## 3. 配置模块（Config）
### 3.1 MyBatis Plus自动填充配置
---
===FILE:beauty-shop-manage/beauty-shop-manage-config/src/main/java/com/beauty/config/MyMetaObjectHandler.java===
```java
package com.beauty.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 美妆小店MyBatis Plus自动填充处理器
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始执行插入时自动填充...");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // TODO: 从Security上下文或Redis中获取当前登录员工ID
        this.strictInsertFill(metaObject, "createBy", Long.class, 1L);
        this.strictInsertFill(metaObject, "updateBy", Long.class, 1L);
    }

    /**
     * 更新时自动填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始执行更新时自动填充...");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // TODO: 从Security上下文或Redis中获取当前登录员工ID
        this.strictUpdateFill(metaObject, "updateBy", Long.class, 1L);
    }

}
```

### 3.2 MyBatis Plus主配置
---
===FILE:beauty-shop-manage/beauty-shop-manage-config/src/main/java/com/beauty/config/MyBatisPlusConfig.java===
```java
package com.beauty.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 美妆小店MyBatis Plus主配置类
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Configuration
@MapperScan("com.beauty.domain.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus拦截器配置
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件（MySQL8.0）
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 最大单页500条记录
        paginationInnerInterceptor.setMaxLimit(500L);
        // 默认单页20条
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防止全表更新/删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

}
```

---

## 4. 启动模块（App）
### 4.1 启动类
---
===FILE:beauty-shop-manage/beauty-shop-manage-app/src/main/java/com/beauty/BeautyShopManageApplication.java===
```java
package com.beauty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 美妆小店轻量级后端管理系统启动类
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@SpringBootApplication(scanBasePackages = "com.beauty")
public class BeautyShopManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeautyShopManageApplication.class, args);
    }

}
```

### 4.2 配置文件
---
===FILE:beauty-shop-manage/beauty-shop-manage-app/src/main/resources/application.yml===
```yaml
# 通用配置
server:
  port: 8080
  servlet:
    context-path: /api/beauty

spring:
  application:
    name: beauty-shop-manage
  profiles:
    active: dev
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
  # Redis配置
  data:
    redis:
      database: 0
      port: 6379
      host: localhost
      password:
      timeout: 30000ms
  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.beauty.domain.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 美妆小店自定义配置
beauty:
  jwt:
    # TODO: 生产环境配置在环境变量中
    secret-key: BEAUTY_SHOP_SECRET_KEY_32_CHARS_2024
  aes256:
    # TODO: 生产环境配置在环境变量中
    secret-key: BEAUTY_SHOP_AES256_KEY_32_CHARS
```

===FILE:beauty-shop-manage/beauty-shop-manage-app/src/main/resources/application-dev.yml===
```yaml
# 开发环境配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/beauty_shop_manage?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  data:
    redis:
      host: localhost
      port: 6379
      password:
```

---

## 5. 数据库初始化脚本（简化版，阶段2需要的表）
---
===FILE:beauty-shop-manage/beauty-shop-manage-app/src/main/resources/db/schema.sql===
```sql
-- 美妆小店轻量级后端管理系统V1.0 阶段2数据库初始化脚本
-- 数据库名：beauty_shop_manage
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `beauty_shop_manage` DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;

USE `beauty_shop_manage`;

-- 1. 美妆商品类别表（阶段1遗留，必须有默认值）
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
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆商品类别表';

-- 2. 美妆员工角色表
DROP TABLE IF EXISTS `beauty_role`;
CREATE TABLE `beauty_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_desc` VARCHAR(200) NULL COMMENT '角色描述',
    `permission_codes` TEXT NULL COMMENT '权限码列表（逗号分隔）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆员工角色表';

-- 3. 美妆员工档案表
DROP TABLE IF EXISTS `beauty_staff`;
CREATE TABLE `beauty_staff` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `staff_no` VARCHAR(20) NOT NULL COMMENT '员工工号',
    `login_account` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(255) NOT NULL COMMENT '登录密码（AES256加密）',
    `staff_name` VARCHAR(50) NOT NULL COMMENT '员工姓名',
    `contact_phone` VARCHAR(20) NULL COMMENT '联系电话',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `staff_status` TINYINT NOT NULL DEFAULT 1 COMMENT '员工状态（1正常，0停用）',
    `is_first_login` TINYINT NOT NULL DEFAULT 1 COMMENT '是否首次登录（1是，0否）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_login_account` (`login_account`),
    UNIQUE INDEX `idx_staff_no` (`staff_no`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆员工档案表';

-- 4. 美妆会员等级表
DROP TABLE IF EXISTS `beauty_member_level`;
CREATE TABLE `beauty_member_level` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_name` VARCHAR(50) NOT NULL COMMENT '等级名称',
    `level_icon` VARCHAR(255) NULL COMMENT '等级图标URL',
    `level_condition` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件（累计消费金额≥X元）',
    `general_discount` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '普通折扣率（0.01-1.00）',
    `birthday_discount` DECIMAL(3,2) NOT NULL DEFAULT 0.90 COMMENT '生日折扣率（0.01-1.00）',
    `point_ratio` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '积分兑换比例（1元=X积分）',
    `point_cash_ratio` INT NOT NULL DEFAULT 100 COMMENT '积分抵现比例（X积分=1元）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆会员等级表';

-- 5. 美妆会员档案表
DROP TABLE IF EXISTS `beauty_member`;
CREATE TABLE `beauty_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_barcode` VARCHAR(50) NOT NULL COMMENT '会员条码',
    `member_card_no` VARCHAR(20) NULL COMMENT '会员卡号',
    `member_name` VARCHAR(50) NOT NULL COMMENT '会员姓名',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `gender` TINYINT NOT NULL DEFAULT 2 COMMENT '性别（1男，0女，2未知）',
    `birthday` DATE NULL COMMENT '生日',
    `level_id` BIGINT NOT NULL COMMENT '等级ID',
    `total_consume_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额（元）',
    `consume_count` INT NOT NULL DEFAULT 0 COMMENT '消费次数',
    `available_balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '可用储值余额（元）',
    `available_points` INT NOT NULL DEFAULT 0 COMMENT '可用积分',
    `member_status` TINYINT NOT NULL DEFAULT 1 COMMENT '会员状态（1正常，0冻结）',
    `member_avatar` VARCHAR(255) NULL COMMENT '会员头像URL',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人ID',
    `update_by` BIGINT NULL COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0未删除，1已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_member_barcode` (`member_barcode`),
    UNIQUE INDEX `idx_contact_phone` (`contact_phone`),
    INDEX `idx_level_id` (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='美妆会员档案表';

-- ------------------------------
-- 初始化数据
-- ------------------------------
-- 1. 美妆商品类别初始化数据
INSERT INTO `beauty_category` (`id`, `category_name`, `parent_id`, `general_expiration_warning_days`, `general_safety_stock`, `sort_order`, `create_by`, `update_by`) VALUES
(1, '美妆全品类', 0, 30, 10, 0, 1, 1);

-- 2. 美妆员工角色初始化数据（默认店长，拥有所有阶段2+基础权限）
INSERT INTO `beauty_role` (`id`, `role_name`, `role_desc`, `permission_codes`, `sort_order`, `create_by`, `update_by`) VALUES
(1, '店长', '美妆小店店长，拥有所有权限', 'staff:list,staff:save,staff:update,staff:delete,staff:reset:password,staff:batch:import,staff:template:download,staff:export,role:list,role:save,role:update,role:delete,member:list,member:save,member:update,member:delete,member:batch:import,member:template:download,member:export,member:recharge,member:point:adjust,member:detail,member:flow:list,member:flow:export,level:list,level:save,level:update,level:delete,dashboard,logout,first:password:change,refresh:token', 0, 1, 1);

-- 3. 美妆员工档案初始化数据（默认管理员，密码为AES256加密后的Admin@123，密钥BEAUTY_SHOP_AES256_KEY_32_CHARS）
-- 加密前：Admin@123
-- 密钥：BEAUTY_SHOP_AES256_KEY_32_CHARS
-- 加密后（使用Hutool AES256，IV=BEAUTY_SHOP_IV_16）：需替换为实际加密值
-- 这里暂时使用占位符，请使用实际工具加密
INSERT INTO `beauty_staff` (`id`, `staff_no`, `login_account`, `password`, `staff_name`, `contact_phone`, `role_id`, `staff_status`, `is_first_login`, `create_by`, `update_by`) VALUES
(1, 'YG0001', 'admin', 'REPLACE_WITH_ACTUAL_AES256_ENCRYPTED_PASSWORD', '默认管理员', '13800138000', 1, 1, 1, 1, 1);

-- 4. 美妆会员等级初始化数据（默认普通会员）
INSERT INTO `beauty_member_level` (`id`, `level_name`, `level_icon`, `level_condition`, `general_discount`, `birthday_discount`, `point_ratio`, `point_cash_ratio`, `sort_order`, `create_by`, `update_by`) VALUES
(1, '普通会员', NULL, 0.00, 1.00, 0.90, 1.00, 100, 0, 1, 1);
```