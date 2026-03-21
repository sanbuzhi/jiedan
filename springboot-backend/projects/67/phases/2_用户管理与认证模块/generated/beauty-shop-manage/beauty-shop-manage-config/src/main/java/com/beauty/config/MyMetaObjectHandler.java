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