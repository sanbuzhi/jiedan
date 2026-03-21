以下是继续生成的后端项目底座关键文件：

===FILE:back/src/main/java/com/tongquyouyi/constant/RedisConstant.java===
```java
package com.tongquyouyi.constant;

/**
 * Redis常量类
 */
public class RedisConstant {
    
    /**
     * 验证码key前缀
     */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";
    
    /**
     * 短信验证码key前缀
     */
    public static final String SMS_CODE_KEY_PREFIX = "sms:code:";
    
    /**
     * 用户token key前缀
     */
    public static final String USER_TOKEN_KEY_PREFIX = "user:token:";
    
    /**
     * 管理员token key前缀
     */
    public static final String ADMIN_TOKEN_KEY_PREFIX = "admin:token:";
    
    /**
     * 防刷IP key前缀
     */
    public static final String IP_BRUSH_KEY_PREFIX = "ip:brush:";
    
    /**
     * 验证码过期时间（秒）
     */
    public static final long CAPTCHA_EXPIRE_TIME = 300;
    
    /**
     * 短信验证码过期时间（秒）
     */
    public static final long SMS_CODE_EXPIRE_TIME = 300;
    
    /**
     * 用户token过期时间（秒）
     */
    public static final long USER_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60;
    
    /**
     * 管理员token过期时间（秒）
     */
    public static final long ADMIN_TOKEN_EXPIRE_TIME = 24 * 60 * 60;
    
    /**
     * 防刷IP过期时间（秒）
     */
    public static final long IP_BRUSH_EXPIRE_TIME = 60;
}
```

===FILE:back/src/main/java/com/tongquyouyi/constant/SystemConstant.java===
```java
package com.tongquyouyi.constant;

/**
 * 系统常量类
 */
public class SystemConstant {
    
    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;
    
    /**
     * 默认每页条数
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    /**
     * 最大每页条数
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * 逻辑删除：未删除
     */
    public static final int NOT_DELETED = 0;
    
    /**
     * 逻辑删除：已删除
     */
    public static final int DELETED = 1;
    
    /**
     * 状态：启用
     */
    public static final int STATUS_ENABLE = 1;
    
    /**
     * 状态：禁用
     */
    public static final int STATUS_DISABLE = 0;
    
    /**
     * 是
     */
    public static final int YES = 1;
    
    /**
     * 否
     */
    public static final int NO = 0;
    
    /**
     * 超级管理员角色标识
     */
    public static final String SUPER_ADMIN_ROLE = "super_admin";
    
    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";
    
    /**
     * 字符编码
     */
    public static final String UTF8 = "UTF-8";
    
    /**
     * 逗号分隔符
     */
    public static final String COMMA = ",";
    
    /**
     * 冒号分隔符
     */
    public static final String COLON = ":";
    
    /**
     * 下划线分隔符
     */
    public static final String UNDERLINE = "_";
    
    /**
     * 横杠分隔符
     */
    public static final String HYPHEN = "-";
}
```

===FILE:back/src/main/java/com/tongquyouyi/base/BaseEntity.java===
```java
package com.tongquyouyi.base;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 */
@Data
public class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private Long id;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删除：0-未删除，1-已删除")
    private Integer deleted;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;
}
```

===FILE:back/src/main/java/com/tongquyouyi/base/BaseDTO.java===
```java
package com.tongquyouyi.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 基础DTO类
 */
@Data
public class BaseDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    private Long id;
}
```

===FILE:back/src/main/java/com/tongquyouyi/base/BaseVO.java===
```java
package com.tongquyouyi.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础VO类
 */
@Data
public class BaseVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    private Long id;
    
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
```

===FILE:back/src/main/java/com/tongquyouyi/base/PageQueryDTO.java===
```java
package com.tongquyouyi.base;

import com.tongquyouyi.constant.SystemConstant;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基础DTO
 */
@Data
public class PageQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 页码
     */
    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = SystemConstant.DEFAULT_PAGE_NUM;
    
    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数，默认10")
    private Integer pageSize = SystemConstant.DEFAULT_PAGE_SIZE;
    
    /**
     * 排序字段
     */
    @ApiModelProperty(value = "排序字段")
    private String sortField;
    
    /**
     * 排序方式：asc-升序，desc-降序
     */
    @ApiModelProperty(value = "排序方式：asc-升序，desc-降序")
    private String sortOrder;
}
```

===FILE:back/src/main/java/com/tongquyouyi/enums/CommonStatusEnum.java===
```java
package com.tongquyouyi.enums;

import lombok.Getter;

/**
 * 通用状态枚举
 */
@Getter
public enum CommonStatusEnum {
    
    /**
     * 禁用
     */
    DISABLE(0, "禁用"),
    
    /**
     * 启用
     */
    ENABLE(1, "启用");
    
    private final Integer code;
    private final String desc;
    
    CommonStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据code获取枚举
     */
    public static CommonStatusEnum getByCode(Integer code) {
        for (CommonStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
    
    /**
     * 判断是否存在该code
     */
    public static boolean exists(Integer code) {
        return getByCode(code) != null;
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/enums/DeleteFlagEnum.java===
```java
package com.tongquyouyi.enums;

import lombok.Getter;

/**
 * 逻辑删除枚举
 */
@Getter
public enum DeleteFlagEnum {
    
    /**
     * 未删除
     */
    NOT_DELETED(0, "未删除"),
    
    /**
     * 已删除
     */
    DELETED(1, "已删除");
    
    private final Integer code;
    private final String desc;
    
    DeleteFlagEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据code获取枚举
     */
    public static DeleteFlagEnum getByCode(Integer code) {
        for (DeleteFlagEnum deleteFlagEnum : values()) {
            if (deleteFlagEnum.getCode().equals(code)) {
                return deleteFlagEnum;
            }
        }
        return null;
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/utils/DateUtils.java===
```java
package com.tongquyouyi.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtils {
    
    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    
    /**
     * DateTimeFormatter：yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
    
    /**
     * DateTimeFormatter：yyyy-MM-dd
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    
    /**
     * DateTimeFormatter：HH:mm:ss
     */
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);
    
    /**
     * 获取当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * 获取当前日期
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
    
    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * LocalDateTime转Date
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 格式化日期时间
     */
    public static String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(DATE_TIME_FORMATTER);
    }
    
    /**
     * 格式化日期
     */
    public static String formatDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.format(DATE_FORMATTER);
    }
    
    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (StringUtils.isEmpty(dateTimeStr)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }
    
    /**
     * 解析日期字符串
     */
    public static LocalDate parseDate(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
    
    /**
     * 计算两个日期时间的差值（秒）
     */
    public static long betweenSeconds(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(start, end);
    }
    
    /**
     * 计算两个日期的差值（天）
     */
    public static long betweenDays(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }
    
    /**
     * 添加天数
     */
    public static LocalDateTime plusDays(LocalDateTime localDateTime, long days) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.plusDays(days);
    }
    
    /**
     * 添加小时
     */
    public static LocalDateTime plusHours(LocalDateTime localDateTime, long hours) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.plusHours(hours);
    }
    
    /**
     * 添加分钟
     */
    public static LocalDateTime plusMinutes(LocalDateTime localDateTime, long minutes) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.plusMinutes(minutes);
    }
    
    /**
     * 获取当天开始时间
     */
    public static LocalDateTime getDayStart(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.atStartOfDay();
    }
    
    /**
     * 获取当天结束时间
     */
    public static LocalDateTime getDayEnd(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.atTime(LocalTime.MAX);
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/utils/IdWorker.java===
```java
package com.tongquyouyi.utils;

import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 */
@Component
public class IdWorker {
    
    /**
     * 开始时间戳（2023-01-01 00:00:00）
     */
    private static final long START_TIMESTAMP = 1672531200000L;
    
    /**
     * 机器ID所占的位数
     */
    private static final long WORKER_ID_BITS = 5L;
    
    /**
     * 数据中心ID所占的位数
     */
    private static final long DATACENTER_ID_BITS = 5L;
    
    /**
     * 支持的最大机器ID
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    
    /**
     * 支持的最大数据中心ID
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    
    /**
     * 序列所占的位数
     */
    private static final long SEQUENCE_BITS = 12L;
    
    /**
     * 机器ID向左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    
    /**
     * 数据中心ID向左移位数
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    
    /**
     * 时间戳向左移位数
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    
    /**
     * 生成序列的掩码
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    
    /**
     * 工作机器ID(0~31)
     */
    private long workerId;
    
    /**
     * 数据中心ID(0~31)
     */
    private long datacenterId;
    
    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;
    
    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;
    
    public IdWorker() {
        this(0, 0);
    }
    
    /**
     * 构造函数
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public IdWorker(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }
    
    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }
        
        // 上次生成ID的时间戳
        lastTimestamp = timestamp;
        
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;
    }
    
    /**
     * 返回以毫秒为单位的当前时间
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
    
    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/utils/StringUtils.java===
```java
package com.tongquyouyi.utils;

import java.util.Collection;
import java.util.Map;

/**
 * 字符串工具类
 */
public class StringUtils {
    
    /**
     * 空字符串
     */
    public static final String EMPTY = "";
    
    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 判断集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * 判断集合是否不为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
    
    /**
     * 判断Map是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
    
    /**
     * 判断Map是否不为空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
    
    /**
     * 判断数组是否为空
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }
    
    /**
     * 判断数组是否不为空
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }
    
    /**
     * 去除字符串首尾空格
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }
    
    /**
     * 去除字符串首尾空格，如果为null则返回空字符串
     */
    public static String trimToEmpty(String str) {
        return str == null ? EMPTY : str.trim();
    }
    
    /**
     * 将字符串首字母大写
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * 将字符串首字母小写
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
    
    /**
     * 驼峰转下划线
     */
    public static String camelToUnderline(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append("_");
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * 下划线转驼峰
     */
    public static String underlineToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    sb.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * 拼接数组元素
     */
    public static String join(Object[] array, String separator) {
        if (isEmpty(array)) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }
    
    /**
     * 拼接集合元素
     */
    public static String join(Collection<?> collection, String separator) {
        if (isEmpty(collection)) {
            return EMPTY;
        }
        return join(collection.toArray(), separator);
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/utils/BeanCopyUtils.java===
```java
package com.tongquyouyi.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Bean复制工具类
 */
public class BeanCopyUtils {
    
    /**
     * 复制对象属性
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copy(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }
    
    /**
     * 复制对象属性并返回新对象
     * @param source 源对象
     * @param targetSupplier 目标对象供应商
     * @param <T> 目标对象类型
     * @return 目标对象
     */
    public static <T> T copy(Object source, Supplier<T> targetSupplier) {
        if (source == null) {
            return null;
        }
        T target = targetSupplier.get();
        copy(source, target);
        return target;
    }
    
    /**
     * 复制集合对象属性
     * @param sourceList 源对象集合
     * @param targetSupplier 目标对象供应商
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @return 目标对象集合
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Supplier<T> targetSupplier) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            targetList.add(copy(source, targetSupplier));
        }
        return targetList;
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/config/ThreadPoolConfig.java===
```java
package com.tongquyouyi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    
    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 10;
    
    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 50;
    
    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 100;
    
    /**
     * 线程存活时间（秒）
     */
    private static final int KEEP_ALIVE_SECONDS = 60;
    
    /**
     * 线程名称前缀
     */
    private static final String THREAD_NAME_PREFIX = "tqy-exec-";
    
    /**
     * 通用线程池
     */
    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    /**
     * 邮件发送线程池
     */
    @Bean("emailExecutor")
    public ThreadPoolTaskExecutor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("tqy-email-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    /**
     * 短信发送线程池
     */
    @Bean("smsExecutor")
    public ThreadPoolTaskExecutor smsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("tqy-sms-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/handler/MyMetaObjectHandler.java===
```java
package com.tongquyouyi.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ....");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "status", Integer.class, 1);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ....");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/listener/ApplicationStartedListener.java===
```java
package com.tongquyouyi.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器
 */
@Slf4j
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {
    
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.info("==============================================");
        log.info("应用启动成功！");
        log.info("应用名称：{}", event.getApplicationContext().getApplicationName());
        log.info("应用版本：{}", event.getApplicationContext().getEnvironment().getProperty("spring.application.version", "1.0.0"));
        log.info("当前环境：{}", event.getApplicationContext().getEnvironment().getActiveProfiles());
        log.info("==============================================");
    }
}
```