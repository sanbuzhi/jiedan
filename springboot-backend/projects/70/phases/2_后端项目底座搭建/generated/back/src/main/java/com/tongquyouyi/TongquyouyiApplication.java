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