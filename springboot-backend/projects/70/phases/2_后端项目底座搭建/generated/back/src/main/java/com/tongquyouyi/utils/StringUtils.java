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