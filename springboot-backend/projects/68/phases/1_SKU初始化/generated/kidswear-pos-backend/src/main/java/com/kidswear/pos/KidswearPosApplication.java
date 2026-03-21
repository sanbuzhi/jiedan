package com.kidswear.pos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 童趣衣橱3-12岁儿童服装单店进销存POS管理系统主启动类
 *
 * @author kidswear
 * @since 2024-06-01
 */
@SpringBootApplication
@MapperScan("com.kidswear.pos.mapper")
public class KidswearPosApplication {

    public static void main(String[] args) {
        SpringApplication.run(KidswearPosApplication.class, args);
    }

}