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