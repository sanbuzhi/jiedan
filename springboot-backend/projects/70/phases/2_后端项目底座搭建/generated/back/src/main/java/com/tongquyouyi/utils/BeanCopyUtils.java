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