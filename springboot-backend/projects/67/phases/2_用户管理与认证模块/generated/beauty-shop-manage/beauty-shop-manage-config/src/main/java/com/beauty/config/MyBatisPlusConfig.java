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