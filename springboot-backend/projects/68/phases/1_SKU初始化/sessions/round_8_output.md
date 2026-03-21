===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/enums/SkuStatus.java===
```java
package com.kidswear.pos.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * SKU状态枚举
 */
@Getter
public enum SkuStatus {
    NORMAL(1, "正常销售"),
    OFF_SHELF(2, "已下架"),
    OUT_OF_STOCK(3, "缺货"),
    PRE_SALE(4, "预售中");

    @EnumValue
    private final Integer code;
    private final String desc;

    SkuStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static SkuStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SkuStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/enums/SizeGroup.java===
```java
package com.kidswear.pos.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 童装尺码组枚举
 */
@Getter
public enum SizeGroup {
    INFANT(1, "婴儿装", Arrays.asList("59", "66", "73", "80", "90")),
    TODDLER(2, "幼童装", Arrays.asList("90", "100", "110", "120")),
    CHILDREN(3, "儿童装", Arrays.asList("120", "130", "140", "150", "160")),
    TEEN(4, "大童装", Arrays.asList("160", "165", "170", "175"));

    @EnumValue
    private final Integer code;
    private final String desc;
    private final List<String> defaultSizes;

    SizeGroup(Integer code, String desc, List<String> defaultSizes) {
        this.code = code;
        this.desc = desc;
        this.defaultSizes = defaultSizes;
    }

    /**
     * 根据code获取枚举
     */
    public static SizeGroup getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SizeGroup group : values()) {
            if (group.getCode().equals(code)) {
                return group;
            }
        }
        return null;
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/enums/GenderType.java===
```java
package com.kidswear.pos.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 性别适用枚举
 */
@Getter
public enum GenderType {
    UNISEX(0, "男女通用"),
    BOY(1, "男童"),
    GIRL(2, "女童");

    @EnumValue
    private final Integer code;
    private final String desc;

    GenderType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static GenderType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GenderType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
```

===FILE:kidswear-pos-frontend/src/types/enums.ts===
```typescript
// SKU状态
export enum SkuStatus {
  NORMAL = 1,
  OFF_SHELF = 2,
  OUT_OF_STOCK = 3,
  PRE_SALE = 4
}

export const SKU_STATUS_MAP: Record<number, string> = {
  [SkuStatus.NORMAL]: '正常销售',
  [SkuStatus.OFF_SHELF]: '已下架',
  [SkuStatus.OUT_OF_STOCK]: '缺货',
  [SkuStatus.PRE_SALE]: '预售中'
}

// 性别适用
export enum GenderType {
  UNISEX = 0,
  BOY = 1,
  GIRL = 2
}

export const GENDER_TYPE_MAP: Record<number, string> = {
  [GenderType.UNISEX]: '男女通用',
  [GenderType.BOY]: '男童',
  [GenderType.GIRL]: '女童'
}

// 尺码组
export enum SizeGroup {
  INFANT = 1,
  TODDLER = 2,
  CHILDREN = 3,
  TEEN = 4
}

export const SIZE_GROUP_MAP: Record<number, string> = {
  [SizeGroup.INFANT]: '婴儿装',
  [SizeGroup.TODDLER]: '幼童装',
  [SizeGroup.CHILDREN]: '儿童装',
  [SizeGroup.TEEN]: '大童装'
}

export const SIZE_GROUP_SIZES: Record<number, string[]> = {
  [SizeGroup.INFANT]: ['59', '66', '73', '80', '90'],
  [SizeGroup.TODDLER]: ['90', '100', '110', '120'],
  [SizeGroup.CHILDREN]: ['120', '130', '140', '150', '160'],
  [SizeGroup.TEEN]: ['160', '165', '170', '175']
}
```

===FILE:kidswear-pos-frontend/src/api/common.ts===
```typescript
import request from '@/utils/request'

/**
 * 通用上传文件接口
 */
export const uploadFile = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string>('/common/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/controller/CommonController.java===
```java
package com.kidswear.pos.controller;

import com.kidswear.pos.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/common")
@Tag(name = "通用接口", description = "上传、下载等通用功能")
public class CommonController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public Result<Map<String, String>> uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

        // 生成文件名和路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String relativePath = datePath + "/" + fileName;
        String absolutePath = uploadPath + "/" + relativePath;

        // 创建目录
        File destFile = new File(absolutePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        try {
            // 保存文件
            file.transferTo(destFile);
            Map<String, String> result = new HashMap<>();
            result.put("url", urlPrefix + "/" + relativePath);
            result.put("fileName", fileName);
            return Result.success(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }
}
```

===FILE:kidswear-pos-backend/src/main/resources/application-dev.yml===
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/kidswear_pos?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root123456
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

file:
  upload:
    path: /tmp/kidswear-pos/uploads
    url-prefix: http://localhost:8080/uploads

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

jwt:
  secret: kidswear-pos-secret-key-2024-for-development
  expiration: 86400000
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/config/WebMvcConfig.java===
```java
package com.kidswear.pos.config;

import com.kidswear.pos.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/common/upload", "/uploads/**", "/error", "/swagger-ui/**", "/v3/api-docs/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，用于访问上传的文件
        String pathPattern = urlPrefix.substring(urlPrefix.lastIndexOf("/")) + "/**";
        registry.addResourceHandler(pathPattern)
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
```