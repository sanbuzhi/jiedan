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