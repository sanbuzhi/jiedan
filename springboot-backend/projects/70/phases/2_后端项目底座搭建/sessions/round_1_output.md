===FILE:back/pom.xml===
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>

    <groupId>com.tongquyouyi</groupId>
    <artifactId>tongquyouyi</artifactId>
    <version>1.0.0</version>
    <name>tongquyouyi</name>
    <description>童趣优衣童装零售单店全栈系统 - 后端</description>

    <properties>
        <java.version>1.8</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <knife4j.version>4.4.0</knife4j.version>
        <jwt.version>0.11.5</jwt.version>
        <aliyun.oss.version>3.17.4</aliyun.oss.version>
        <aliyun.sms.version>2.0.24</aliyun.sms.version>
        <wechatpay.version>0.2.12</wechatpay.version>
        <alipay.sdk.version>4.39.136.ALL</alipay.sdk.version>
        <easyexcel.version>3.3.2</easyexcel.version>
        <pagehelper.version>5.3.2</pagehelper.version>
    </properties>

    <dependencies>
        <!-- Spring Boot 核心依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- MySQL 驱动 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.35</version>
        </dependency>

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
            <scope>test</scope>
        </dependency>

        <!-- PageHelper 分页（兼容MyBatis Plus场景下的复杂SQL分页） -->
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper-spring-boot-starter</artifactId>
            <version>${pagehelper.version}</version>
        </dependency>

        <!-- Knife4j 接口文档 -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>

        <!-- JWT 认证 -->
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

        <!-- 阿里云 OSS -->
        <dependency>
            <groupId>com.aliyun.oss</groupId>
            <artifactId>aliyun-sdk-oss</artifactId>
            <version>${aliyun.oss.version}</version>
        </dependency>

        <!-- 阿里云 短信 -->
        <dependency>
            <groupId>com.aliyun</groupId>
            <artifactId>dysmsapi20170525</artifactId>
            <version>${aliyun.sms.version}</version>
        </dependency>

        <!-- 微信支付 V3 JSAPI -->
        <dependency>
            <groupId>com.github.wechatpay-apiv3</groupId>
            <artifactId>wechatpay-java</artifactId>
            <version>${wechatpay.version}</version>
        </dependency>

        <!-- 支付宝 SDK -->
        <dependency>
            <groupId>com.alipay.sdk</groupId>
            <artifactId>alipay-sdk-java</artifactId>
            <version>${alipay.sdk.version}</version>
        </dependency>

        <!-- EasyExcel 导入导出 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>easyexcel</artifactId>
            <version>${easyexcel.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Hutool 工具类（补充公共工具） -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.23</version>
        </dependency>

        <!-- 测试依赖 -->
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
```

===FILE:back/src/main/resources/application.yml===
```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: tongquyouyi
  profiles:
    active: dev
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    default-property-inclusion: non_null

mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.tongquyouyi.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

knife4j:
  enable: true
  setting:
    language: zh_cn
    enable-swagger-models: true
    enable-document-manage: true
  production: false

# 自定义配置前缀
tongquyouyi:
  jwt:
    # 会被dev/prod覆盖
    secret: default-secret-change-me-in-dev-prod
    token-expire-days: 7
    refresh-token-expire-days: 14
    header: Authorization
    prefix: "Bearer "
  file:
    # 会被dev/prod覆盖
    upload-path: default-path
    max-image-size: 5242880
    max-excel-size: 104857600
  redis:
    prefix: tqy:
    captcha-expire-seconds: 300
    sms-expire-seconds: 300
    token-expire-seconds: 604800
    refresh-token-expire-seconds: 1209600
    ip-brush-expire-seconds: 60
    ip-brush-max-count: 10
```

===FILE:back/src/main/resources/application-dev.yml===
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/tongquyouyi?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0

# 自定义开发环境配置
tongquyouyi:
  jwt:
    secret: tongquyouyi-jwt-secret-hs256-2024-abcdefghijklmnopqrstuvwxyz
  file:
    upload-path: D:/tongquyouyi/upload
  ali:
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: your-dev-oss-access-key-id
      access-key-secret: your-dev-oss-access-key-secret
      bucket-name: tongquyouyi-dev
    sms:
      access-key-id: your-dev-sms-access-key-id
      access-key-secret: your-dev-sms-access-key-secret
      sign-name: 童趣优衣
      template-code: SMS_123456789
  wechat:
    pay:
      app-id: your-dev-mini-program-appid
      mch-id: your-dev-mch-id
      api-v3-key: your-dev-api-v3-key
      private-key-path: classpath:cert/apiclient_key.pem
      merchant-serial-number: your-dev-merchant-serial-number
      notify-url: http://your-dev-domain.com/api/store/pay/wechat/notify
      sandbox: true
  alipay:
    pay:
      app-id: your-dev-alipay-appid
      private-key: your-dev-alipay-private-key
      public-key: your-dev-alipay-public-key
      gateway-url: https://openapi.alipaydev.com/gateway.do
      notify-url: http://your-dev-domain.com/api/store/pay/alipay/notify
      return-url: http://your-dev-domain.com/store/user/order/list
      sandbox: true
```

===FILE:back/src/main/resources/application-prod.yml===
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://your-prod-mysql-host:3306/tongquyouyi?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=GMT%2B8&useSSL=true&allowPublicKeyRetrieval=true
    username: your-prod-mysql-username
    password: your-prod-mysql-password
  redis:
    host: your-prod-redis-host
    port: 6379
    password: your-prod-redis-password
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 32
        max-wait: -1ms
        max-idle: 16
        min-idle: 4

# 自定义生产环境配置
tongquyouyi:
  jwt:
    secret: tongquyouyi-jwt-secret-hs256-2024-prod-abcdefghijklmnopqrstuvwxyz1234567890
  file:
    upload-path: /data/tongquyouyi/upload
  ali:
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: your-prod-oss-access-key-id
      access-key-secret: your-prod-oss-access-key-secret
      bucket-name: tongquyouyi-prod
    sms:
      access-key-id: your-prod-sms-access-key-id
      access-key-secret: your-prod-sms-access-key-secret
      sign-name: 童趣优衣
      template-code: SMS_987654321
  wechat:
    pay:
      app-id: your-prod-mini-program-appid
      mch-id: your-prod-mch-id
      api-v3-key: your-prod-api-v3-key
      private-key-path: /data/tongquyouyi/cert/apiclient_key.pem
      merchant-serial-number: your-prod-merchant-serial-number
      notify-url: https://your-prod-domain.com/api/store/pay/wechat/notify
      sandbox: false
  alipay:
    pay:
      app-id: your-prod-alipay-appid
      private-key: your-prod-alipay-private-key
      public-key: your-prod-alipay-public-key
      gateway-url: https://openapi.alipay.com/gateway.do
      notify-url: https://your-prod-domain.com/api/store/pay/alipay/notify
      return-url: https://your-prod-domain.com/store/user/order/list
      sandbox: false

knife4j:
  production: true
```

===FILE:back/src/main/java/com/tongquyouyi/TongquyouyiApplication.java===
```java
package com.tongquyouyi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tongquyouyi.mapper")
public class TongquyouyiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TongquyouyiApplication.class, args);
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/common/Result.java===
```java
package com.tongquyouyi.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公共响应类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公共响应类")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    @Schema(description = "响应码")
    private Integer code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息")
    private String msg;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    public static <T> Result<T> error() {
        return new Result<>(500, "系统异常，请稍后再试", null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/common/ErrorCode.java===
```java
package com.tongquyouyi.common;

/**
 * 错误码枚举
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
public enum ErrorCode {

    // 公共模块
    PARAM_ERROR(400001, "参数校验失败"),
    SYSTEM_ERROR(500001, "系统异常"),
    UNAUTHORIZED(401001, "未授权访问"),
    TOKEN_EXPIRED(401002, "Token已过期"),
    TOKEN_INVALID(401003, "Token无效"),
    IP_BRUSH(429001, "请求过于频繁，请稍后再试"),

    // 管理后台登录/权限
    ADMIN_USER_NOT_FOUND(4010101, "账号不存在"),
    ADMIN_PASSWORD_ERROR(4010102, "密码错误"),
    ADMIN_CAPTCHA_EXPIRED(4010103, "验证码失效"),
    ADMIN_CAPTCHA_ERROR(4010104, "验证码错误"),

    // 商品管理
    PRODUCT_NO_STOCK_ON(402001, "商品库存不足，不可上架"),
    PRODUCT_ON_CANNOT_MODIFY_CATEGORY(402002, "已上架商品不可修改分类/SKU结构"),
    PRODUCT_CANNOT_DELETE(402003, "仅允许删除已下架且无库存、无订单记录的商品"),

    // 库存管理
    SKU_INSUFFICIENT_STOCK(403001, "SKU库存不足"),
    CHECK_ONLY_PENDING(403002, "仅允许修改「待盘点」状态的盘点单"),
    CHECK_ONLY_CONFIRM_PENDING(403003, "仅允许确认「待盘点」状态的盘点单"),

    // 会员管理
    MEMBER_PHONE_EXIST(404001, "手机号已注册"),
    MEMBER_STORED_INSUFFICIENT(404002, "储值余额不足"),
    MEMBER_LEVEL_PRESET_CANNOT_MODIFY(404003, "普通会员不可删除/修改升级条件"),

    // 订单管理
    ORDER_STATUS_NOT_ALLOW(405001, "订单状态不允许操作"),
    COUPON_USED_OR_EXPIRED(405002, "优惠券已使用/已过期"),
    POINT_DEDUCT_OVER_RATIO(405003, "积分抵扣比例超过系统设置的最高比例"),

    // 支付模块
    PAY_FAILED(406001, "支付失败"),
    REFUND_FAILED(406002, "退款失败");

    private final Integer code;
    private final String msg;

    ErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/common/TqyException.java===
```java
package com.tongquyouyi.common;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Getter
public class TqyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    public TqyException(String msg) {
        super(msg);
        this.code = 500;
    }

    public TqyException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public TqyException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/common/GlobalExceptionHandler.java===
```java
package com.tongquyouyi.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(TqyException.class)
    public Result<Void> handleTqyException(TqyException e) {
        log.warn("业务异常：code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常（@RequestBody）：{}", errorMsg);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * 参数校验异常处理（@RequestParam/@PathVariable）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常（@RequestParam/@PathVariable）：{}", errorMsg);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * 参数绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定异常：{}", errorMsg);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * 未知异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(ErrorCode.SYSTEM_ERROR);
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/common/PageResult.java===
```java
package com.tongquyouyi.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应类")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总条数
     */
    @Schema(description = "总条数")
    private Long total;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> list;

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/JwtProperties.java===
```java
package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.jwt")
public class JwtProperties {

    /**
     * 签名密钥
     */
    private String secret;

    /**
     * Token有效期（天）
     */
    private Integer tokenExpireDays;

    /**
     * 刷新Token有效期（天）
     */
    private Integer refreshTokenExpireDays;

    /**
     * Token请求头
     */
    private String header;

    /**
     * Token前缀
     */
    private String prefix;

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/FileProperties.java===
```java
package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.file")
public class FileProperties {

    /**
     * 本地临时上传路径
     */
    private String uploadPath;

    /**
     * 最大图片大小（字节）
     */
    private Long maxImageSize;

    /**
     * 最大Excel大小（字节）
     */
    private Long maxExcelSize;

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/RedisProperties.java===
```java
package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.redis")
public class RedisProperties {

    /**
     * Redis键前缀
     */
    private String prefix;

    /**
     * 图形验证码有效期（秒）
     */
    private Long captchaExpireSeconds;

    /**
     * 短信验证码有效期（秒）
     */
    private Long smsExpireSeconds;

    /**
     * Token有效期（秒）
     */
    private Long tokenExpireSeconds;

    /**
     * 刷新Token有效期（秒）
     */
    private Long refreshTokenExpireSeconds;

    /**
     * IP防刷时间窗口（秒）
     */
    private Long ipBrushExpireSeconds;

    /**
     * IP防刷最大请求数
     */
    private Integer ipBrushMaxCount;

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/AliOssProperties.java===
```java
package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.ali.oss")
public class AliOssProperties {

    /**
     * OSS端点
     */
    private String endpoint;

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥密钥
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/AliSmsProperties.java===
```java
package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.ali.sms")
public class AliSmsProperties {

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥密钥
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 短信模板代码
     */
    private String templateCode;

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/WechatPayProperties.java===
```java
package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付V3配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.wechat.pay")
public class WechatPayProperties {

    /**
     * 小程序AppID
     */
    private String appId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * API V3密钥
     */
    private String apiV3Key;

    /**
     * 商户私钥路径
     */
    private String privateKeyPath;

    /**
     * 商户证书序列号
     */
    private String merchantSerialNumber;

    /**
     * 支付结果通知地址
     */
    private String notifyUrl;

    /**
     * 是否沙箱环境
     */
    private Boolean sandbox;

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/AlipayProperties.java===
```java
package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.alipay.pay")
public class AlipayProperties {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 应用私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String publicKey;

    /**
     * 支付宝网关
     */
    private String gatewayUrl;

    /**
     * 支付结果异步通知地址
     */
    private String notifyUrl;

    /**
     * 支付结果同步跳转地址
     */
    private String returnUrl;

    /**
     * 是否沙箱环境
     */
    private Boolean sandbox;

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/MybatisPlusConfig.java===
```java
package com.tongquyouyi.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Configuration
@MapperScan("com.tongquyouyi.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis Plus拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防止全表更新/删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/Knife4jConfig.java===
```java
package com.tongquyouyi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j接口文档配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Configuration
public class Knife4jConfig {

    /**
     * 管理后台接口分组
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("童趣优衣-管理后台")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    /**
     * 线上商城接口分组
     */
    @Bean
    public GroupedOpenApi storeApi() {
        return GroupedOpenApi.builder()
                .group("童趣优衣-线上商城")
                .pathsToMatch("/api/store/**")
                .build();
    }

    /**
     * OpenAPI基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("童趣优衣童装零售单店全栈系统")
                        .version("1.0.0")
                        .description("童趣优衣童装零售单店全栈系统接口文档")
                        .contact(new Contact()
                                .name("童趣优衣技术团队")
                                .email("support@tongquyouyi.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/CorsConfig.java===
```java
package com.tongquyouyi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/config/RedisConfig.java===
```java
package com.tongquyouyi.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // JSON序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        // 解决Java8时间类型序列化问题
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // String序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Key和HashKey使用String序列化
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        // Value和HashValue使用JSON序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/utils/JwtUtils.java===
```java
package com.tongquyouyi.utils;

import com.tongquyouyi.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtUtils {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 获取签名密钥
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token
     *
     * @param userId   用户ID
     * @param userType 用户类型（admin/store）
     * @return Token
     */
    public String generateToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        return generateToken(claims, jwtProperties.getTokenExpireDays() * 24 * 60 * 60 * 1000L);
    }

    /**
     * 生成刷新Token
     *
     * @param userId   用户ID
     * @param userType 用户类型（admin/store）
     * @return 刷新Token
     */
    public String generateRefreshToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        return generateToken(claims, jwtProperties.getRefreshTokenExpireDays() * 24 * 60 * 60 * 1000L);
    }

    /**
     * 生成Token的通用方法
     */
    private String generateToken(Map<String, Object> claims, long expireTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token
     *
     * @param token Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期：{}", token);
            throw e;
        } catch (JwtException e) {
            log.error("Token解析失败：{}", token, e);
            throw e;
        }
    }

    /**
     * 获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 获取用户类型
     */
    public String getUserType(String token) {
        Claims claims = parseToken(token);
        return claims.get("userType").toString();
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/utils/BCryptUtils.java===
```java
package com.tongquyouyi.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt加密工具类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Component
public class BCryptUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/utils/CaptchaUtils.java===
```java
package com.tongquyouyi.utils;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 图形验证码工具类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Component
public class CaptchaUtils {

    /**
     * 生成图形验证码
     *
     * @return 包含captchaKey和captchaImg的Map
     */
    public static Map<String, String> generateCaptcha() {
        // 生成4位数字验证码，宽度120，高度40
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        Map<String, String> result = new HashMap<>();
        // 使用UUID作为captchaKey
        result.put("captchaKey", cn.hutool.core.lang.UUID.fastUUID().toString(true));
        // 获取Base64格式的验证码图片
        result.put("captchaImg", lineCaptcha.getImageBase64Data());
        // 存储验证码值到Redis的key（这里只返回验证码值，实际存储在Redis由业务层处理）
        result.put("captchaValue", lineCaptcha.getCode());
        return result;
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/utils/RedisUtils.java===
```java
package com.tongquyouyi.utils;

import com.tongquyouyi.config.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisProperties redisProperties;

    /**
     * 拼接前缀
     */
    private String buildKey(String key) {
        return redisProperties.getPrefix() + key;
    }

    // ====================== 基本操作 ======================

    /**
     * 设置键值对
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(buildKey(key), value);
    }

    /**
     * 设置键值对及过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(buildKey(key), value, timeout, unit);
    }

    /**
     * 获取键值对
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(buildKey(key));
    }

    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(buildKey(key));
    }

    /**
     * 批量删除键
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys.stream().map(this::buildKey).toList());
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(buildKey(key), timeout, unit);
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(buildKey(key));
    }

    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(buildKey(key));
    }

    // ====================== 计数器操作 ======================

    /**
     * 递增
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(buildKey(key));
    }

    /**
     * 递增指定值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(buildKey(key), delta);
    }

    /**
     * 递减
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(buildKey(key));
    }

    /**
     * 递减指定值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(buildKey(key), delta);
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/interceptor/IpBrushInterceptor.java===
```java
package com.tongquyouyi.interceptor;

import com.tongquyouyi.common.ErrorCode;
import com.tongquyouyi.common.TqyException;
import com.tongquyouyi.config.RedisProperties;
import com.tongquyouyi.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * IP防刷拦截器
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Slf4j
@Component
public class IpBrushInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedisProperties redisProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getIpAddress(request);
        String key = "ip:brush:" + ip;

        Long count = redisUtils.increment(key);
        if (count == 1) {
            redisUtils.expire(key, redisProperties.getIpBrushExpireSeconds(), TimeUnit.SECONDS);
        }

        if (count > redisProperties.getIpBrushMaxCount()) {
            log.warn("IP {} 请求过于频繁，当前请求数：{}", ip, count);
            throw new TqyException(ErrorCode.IP_BRUSH);
        }

        return true;
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/interceptor/JwtInterceptor.java===
```java
package com.tongquyouyi.interceptor;

import com.tongquyouyi.common.ErrorCode;
import com.tongquyouyi.common.TqyException;
import com.tongquyouyi.config.JwtProperties;
import com.tongquyouyi.config.RedisProperties;
import com.tongquyouyi.utils.JwtUtils;
import com.tongquyouyi.utils.RedisUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT身份认证拦截器
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisProperties redisProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取Token
        String token = request.getHeader(jwtProperties.getHeader());
        if (token == null || !token.startsWith(jwtProperties.getPrefix())) {
            throw new TqyException(ErrorCode.UNAUTHORIZED);
        }

        // 去除Token前缀
        token = token.substring(jwtProperties.getPrefix().length());

        try {
            // 解析Token
            Claims claims = jwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            String userType = claims.get("userType").toString();

            // 验证Token是否在Redis中存在
            String redisTokenKey = "token:" + userType + ":" + userId;
            Object redisToken = redisUtils.get(redisTokenKey);
            if (redisToken == null || !redisToken.toString().equals(token)) {
                throw new TqyException(ErrorCode.TOKEN_INVALID);
            }

            // 将用户信息存入Request域，方便后续使用
            request.setAttribute("userId", userId);
            request.setAttribute("userType", userType);

            return true;
        } catch (ExpiredJwtException e) {
            throw new TqyException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new TqyException(ErrorCode.TOKEN_INVALID);
        }
    }

}
```

===FILE:back/src/main/java/com/tongquyouyi/interceptor/WebMvcConfig.java===
```java
package com.tongquyouyi.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类（拦截器注册）
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private IpBrushInterceptor ipBrushInterceptor;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // IP防刷拦截器（拦截所有请求）
        registry.addInterceptor(ipBrushInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**");

        // JWT身份认证拦截器（拦截管理后台和线上商城的业务接口）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/admin/**", "/api/store/**")
                .excludePathPatterns(
                        // 管理后台登录相关
                        "/api/admin/auth/captcha",
                        "/api/admin/auth/login",
                        "/api/admin/auth/refresh",
                        // 线上商城登录相关
                        "/api/store/auth/send-sms",
                        "/api/store/auth/login",
                        "/api/store/auth/refresh",
                        // 线上商城首页相关
                        "/api/store/index/**",
                        "/api/store/product/list",
                        "/api/store/product/search",
                        "/api/store/product/get",
                        // 支付回调
                        "/api/store/pay/**/notify"
                );
    }

}
```