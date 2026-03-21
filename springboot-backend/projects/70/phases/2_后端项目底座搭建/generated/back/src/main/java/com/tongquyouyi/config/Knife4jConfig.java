package com.tongquyouyi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j接口文档配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Configuration
public class Knife4jConfig {

    /**
     * 管理后台接口分组
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("童趣优衣-管理后台")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    /**
     * 线上商城接口分组
     */
    @Bean
    public GroupedOpenApi storeApi() {
        return GroupedOpenApi.builder()
                .group("童趣优衣-线上商城")
                .pathsToMatch("/api/store/**")
                .build();
    }

    /**
     * OpenAPI基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("童趣优衣童装零售单店全栈系统")
                        .version("1.0.0")
                        .description("童趣优衣童装零售单店全栈系统接口文档")
                        .contact(new Contact()
                                .name("童趣优衣技术团队")
                                .email("support@tongquyouyi.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

}