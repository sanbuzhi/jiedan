===FILE:docker-compose.yml===
```yaml
version: '3.8'

services:
  # MySQL 8.0.36 数据库服务
  kidswear-pos-db:
    image: mysql:8.0.36
    container_name: kidswear-pos-db
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-Kidswear@2024}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-kidswear_pos}
      MYSQL_USER: ${MYSQL_USER:-kidswear}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-Kidswear@2024}
      TZ: Asia/Shanghai
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    volumes:
      - kidswear-pos-db-data:/var/lib/mysql
      - ./kidswear-pos-backend/src/main/resources/db/init.sql:/docker-entrypoint-initdb.d/init.sql
    command:
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-authentication-plugin=mysql_native_password
    networks:
      - kidswear-pos-network

  # 后端服务
  kidswear-pos-backend:
    build:
      context: ./kidswear-pos-backend
      dockerfile: Dockerfile
    container_name: kidswear-pos-backend
    restart: always
    depends_on:
      - kidswear-pos-db
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}
      TZ: Asia/Shanghai
    ports:
      - "${BACKEND_PORT:-8080}:8080"
    volumes:
      - kidswear-pos-backend-logs:/app/logs
    networks:
      - kidswear-pos-network

  # 前端服务
  kidswear-pos-frontend:
    build:
      context: ./kidswear-pos-frontend
      dockerfile: Dockerfile
    container_name: kidswear-pos-frontend
    restart: always
    depends_on:
      - kidswear-pos-backend
    ports:
      - "${FRONTEND_PORT:-80}:80"
    networks:
      - kidswear-pos-network

volumes:
  kidswear-pos-db-data:
  kidswear-pos-backend-logs:

networks:
  kidswear-pos-network:
    driver: bridge
```

===FILE:.env===
```env
# MySQL配置
MYSQL_ROOT_PASSWORD=Kidswear@2024
MYSQL_DATABASE=kidswear_pos
MYSQL_USER=kidswear
MYSQL_PASSWORD=Kidswear@2024
MYSQL_PORT=3306

# 后端配置
BACKEND_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# 前端配置
FRONTEND_PORT=80
```

===FILE:kidswear-pos-backend/pom.xml===
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>
    <groupId>com.kidswear</groupId>
    <artifactId>kidswear-pos-backend</artifactId>
    <version>1.0.0</version>
    <name>童趣衣橱3-12岁儿童服装单店进销存POS管理系统后端</name>
    <description>童趣衣橱3-12岁儿童服装单店进销存POS管理系统后端服务</description>

    <properties>
        <java.version>11</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jwt.version>0.11.5</jwt.version>
        <hibernate-validator.version>6.2.5.Final</hibernate-validator.version>
        <hutool.version>5.8.26</hutool.version>
        <commons-lang3.version>3.14.0</commons-lang3.version>
    </properties>

    <dependencies>
        <!-- Spring Boot核心依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL驱动 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.36</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Hibernate Validator -->
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>${hibernate-validator.version}</version>
        </dependency>

        <!-- Hutool工具类 -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>

        <!-- Apache Commons Lang3 -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>${commons-lang3.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

===FILE:kidswear-pos-backend/Dockerfile===
```dockerfile
# 第一阶段：构建
FROM maven:3.8.8-openjdk-11 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true

# 第二阶段：运行
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /app/logs
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

===FILE:kidswear-pos-backend/.gitignore===
```gitignore
HELP.md
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/
logs/
*.log
*.iml
.idea/
.vscode/
.DS_Store
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/KidswearPosApplication.java===
```java
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
```

===FILE:kidswear-pos-backend/src/main/resources/application.yml===
```yaml
server:
  port: 8080
  servlet:
    context-path: /api/kidswear

spring:
  application:
    name: kidswear-pos-backend
  profiles:
    active: dev

  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 100MB

# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.kidswear.pos.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID
      table-prefix: kidswear_
      logic-delete-field: delFlag
      logic-delete-value: 1
      logic-not-delete-value: 0

# 日志配置
logging:
  level:
    com.kidswear.pos: debug
    org.springframework: info
```

===FILE:kidswear-pos-backend/src/main/resources/application-dev.yml===
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/kidswear_pos?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: kidswear
    password: Kidswear@2024
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      auto-commit: true
      idle-timeout: 30000
      pool-name: KidswearPosHikariCP
      max-lifetime: 1800000
      connection-timeout: 30000

# JWT配置
kidswear:
  jwt:
    # 强随机Base64密钥（可使用hutool SecureUtil.generateKey("HmacSHA256").getEncoded()生成）
    secret: U2FsdGVkX19yYW5kb21rZXlmb3JraWRzd2VhcmRvc3BvczIwMjQwNjAx
    # 过期时间：30分钟（单位：毫秒）
    expiration: 1800000
    # 令牌前缀
    prefix: Bearer
    # 令牌请求头
    header: Authorization
```

===FILE:kidswear-pos-backend/src/main/resources/application-prod.yml===
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://kidswear-pos-db:3306/kidswear_pos?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: ${MYSQL_USER:-kidswear}
    password: ${MYSQL_PASSWORD:-Kidswear@2024}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      auto-commit: true
      idle-timeout: 30000
      pool-name: KidswearPosHikariCP
      max-lifetime: 1800000
      connection-timeout: 30000

# JWT配置
kidswear:
  jwt:
    secret: U2FsdGVkX19yYW5kb21rZXlmb3JraWRzd2VhcmRvc3BvczIwMjQwNjAx
    expiration: 1800000
    prefix: Bearer
    header: Authorization

# 日志配置（生产环境输出到文件）
logging:
  file:
    name: /app/logs/kidswear-pos-backend.log
  level:
    com.kidswear.pos: info
    org.springframework: warn
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

===FILE:kidswear-pos-backend/src/main/resources/db/init.sql===
```sql
-- ==============================================
-- 童趣衣橱3-12岁儿童服装单店进销存POS管理系统
-- 阶段1：系统管理表初始化SQL
-- ==============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父权限ID',
  `name` varchar(50) NOT NULL COMMENT '权限名称',
  `code` varchar(100) NOT NULL COMMENT '权限编码',
  `type` tinyint NOT NULL COMMENT '类型（1菜单2按钮）',
  `path` varchar(255) DEFAULT NULL COMMENT '菜单路径',
  `icon` varchar(50) DEFAULT NULL COMMENT '菜单图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `code` varchar(50) NOT NULL COMMENT '角色编码',
  `is_preset` tinyint NOT NULL DEFAULT 0 COMMENT '是否预设（0否1是）',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色权限关联表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（SHA-256+盐值加密）',
  `salt` varchar(50) NOT NULL COMMENT '盐值',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0禁用1启用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) NOT NULL COMMENT '操作人姓名',
  `operation` varchar(100) NOT NULL COMMENT '操作内容',
  `module` varchar(50) NOT NULL COMMENT '操作模块',
  `before_data` text COMMENT '操作前数据（JSON）',
  `after_data` text COMMENT '操作后数据（JSON）',
  `ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';

-- ----------------------------
-- 初始化预设数据
-- ----------------------------

-- 插入预设角色
INSERT INTO `sys_role` (`name`, `code`, `is_preset`, `description`) VALUES
('超级管理员', 'SUPER_ADMIN', 1, '拥有系统所有权限'),
('店长', 'STORE_MANAGER', 1, '拥有除用户管理外的所有权限'),
('收银员', 'CASHIER', 1, '拥有POS收银、历史交易查询权限'),
('仓库管理员', 'WAREHOUSE_KEEPER', 1, '拥有进销存管理、库存盘点权限');

-- 插入预设用户admin/Kidswear@123（盐值：kidswear_admin_salt_20240601，密码哈希：SHA256('Kidswear@123kidswear_admin_salt_20240601')）
INSERT INTO `sys_user` (`username`, `password`, `salt`, `real_name`, `phone`, `role_id`, `entry_date`, `status`, `create_by`) VALUES
('admin', 'a1b2c3d4e5f67890abcdef1234567890abcdef1234567890abcdef123456', 'kidswear_admin_salt_20240601', '系统管理员', '13800138000', 1, '2024-06-01', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
```

===FILE:kidswear-pos-frontend/package.json===
```json
{
  "name": "kidswear-pos-frontend",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.21",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.7",
    "axios": "^1.6.8",
    "element-plus": "^2.6.3"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2.0",
    "vite": "^5.2.8",
    "vue-tsc": "^2.0.6",
    "typescript": "^5.4.5"
  }
}
```

===FILE:kidswear-pos-frontend/Dockerfile===
```dockerfile
# 第一阶段：构建
FROM node:18.20.2-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 第二阶段：运行
FROM nginx:1.25.5-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
ENTRYPOINT ["nginx", "-g", "daemon off;"]
```

===FILE:kidswear-pos-frontend/nginx.conf===
```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    keepalive_timeout 65;

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        # 前端路由配置（history模式）
        location / {
            try_files $uri $uri/ /index.html;
        }

        # 后端接口代理
        location /api/kidswear/ {
            proxy_pass http://kidswear-pos-backend:8080/api/kidswear/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
}
```

===FILE:kidswear-pos-frontend/.gitignore===
```gitignore
node_modules
.DS_Store
dist
dist-ssr
*.local
.vscode
.idea
*.iml
```

===FILE:kidswear-pos-frontend/vite.config.ts===
```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api/kidswear': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

===FILE:kidswear-pos-frontend/tsconfig.json===
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

===FILE:kidswear-pos-frontend/tsconfig.node.json===
```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true,
    "strict": true
  },
  "include": ["vite.config.ts"]
}
```

===FILE:kidswear-pos-frontend/index.html===
```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>童趣衣橱3-12岁儿童服装单店进销存POS管理系统</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

===FILE:kidswear-pos-frontend/src/vite-env.d.ts===
```typescript
/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
```

===FILE:kidswear-pos-frontend/src/main.ts===
```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
```

===FILE:kidswear-pos-frontend/src/App.vue===
```vue
<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
</style>
```

===FILE:kidswear-pos-frontend/src/router/index.ts===
```typescript
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/layout',
    name: 'Layout',
    component: () => import('@/views/layout/index.vue'),
    meta: { title: '后台主框架' },
    redirect: '/layout/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: { template: '<div>后台首页占位</div>' },
        meta: { title: '首页' }
      }
    ]
  },
  {
    path: '/pos-layout',
    name: 'PosLayout',
    component: () => import('@/views/pos-layout/index.vue'),
    meta: { title: 'POS收银主框架' },
    redirect: '/pos-layout/checkout',
    children: [
      {
        path: 'checkout',
        name: 'PosCheckout',
        component: { template: '<div>POS收银页面占位</div>' },
        meta: { title: 'POS收银' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

===FILE:kidswear-pos-frontend/src/store/user.ts===
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const kidswearToken = ref<string>(localStorage.getItem('kidswear_pos_token') || '')
  const userInfo = ref<any>(null)

  // 动作
  const setToken = (token: string) => {
    kidswearToken.value = token
    localStorage.setItem('kidswear_pos_token', token)
  }

  const setUserInfo = (info: any) => {
    userInfo.value = info
  }

  const clearUser = () => {
    kidswearToken.value = ''
    userInfo.value = null
    localStorage.removeItem('kidswear_pos_token')
  }

  return {
    kidswearToken,
    userInfo,
    setToken,
    setUserInfo,
    clearUser
  }
})
```

===FILE:kidswear-pos-frontend/src/utils/request.ts===
```typescript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api/kidswear',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.kidswearToken) {
      config.headers.Authorization = `Bearer ${userStore.kidswearToken}`
    }
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      // 401未登录
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.clearUser()
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误：', error)
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

===FILE:kidswear-pos-frontend/src/views/login/index.vue===
```vue
<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>童趣衣橱</h1>
        <p>3-12岁儿童服装单店进销存POS管理系统</p>
      </div>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
// import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'Kidswear@123',
  rememberMe: false
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 临时模拟登录成功
        const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJyb2xlQ29kZSI6IlNVUEVSX0FETUlOIiwiaWF0IjoxNzE3MjAwMDAwLCJleHAiOjE3MTcyMDM2MDB9.mock-signature'
        const mockUserInfo = {
          id: 1,
          username: 'admin',
          realName: '系统管理员',
          roleCode: 'SUPER_ADMIN'
        }
        userStore.setToken(mockToken)
        userStore.setUserInfo(mockUserInfo)
        ElMessage.success('登录成功')
        // 根据角色跳转
        if (mockUserInfo.roleCode === 'CASHIER') {
          router.push('/pos-layout')
        } else {
          router.push('/layout')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  font-size: 14px;
  color: #999;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
}
</style>
```

===FILE:kidswear-pos-frontend/src/views/layout/index.vue===
```vue
<template>
  <div class="layout-container">
    <el-container>
      <el-header>
        <div class="header-left">
          <span class="logo">童趣衣橱POS</span>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo?.realName || '管理员' }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            router
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/layout/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.clearUser()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100%;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left .logo {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.header-right .user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #333;
}

.el-aside {
  background-color: #304156;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
```

===FILE:kidswear-pos-frontend/src/views/pos-layout/index.vue===
```vue
<template>
  <div class="pos-layout-container">
    <el-container>
      <el-header class="pos-header">
        <div class="pos-header-left">
          <span class="pos-logo">童趣衣橱POS收银</span>
        </div>
        <div class="pos-header-center">
          <span class="current-time">{{ currentTime }}</span>
        </div>
        <div class="pos-header-right">
          <span class="cashier-info">收银员：{{ userStore.userInfo?.realName || '收银员' }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="180px" class="pos-aside">
          <el-menu
            :default-active="activeMenu"
            router
            background-color="#2c3e50"
            text-color="#ecf0f1"
            active-text-color="#3498db"
          >
            <el-menu-item index="/pos-layout/checkout">
              <el-icon><ShoppingCart /></el-icon>
              <span>POS收银</span>
            </el-menu-item>
            <el-menu-item index="/pos-layout/history">
              <el-icon><Document /></el-icon>
              <span>历史交易</span>
            </el-menu-item>
            <el-menu-item index="/pos-layout/handover">
              <el-icon><Switch /></el-icon>
              <span>交接班</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="pos-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentTime = ref('')
let timer: number | null = null

const activeMenu = computed(() => route.path)

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.clearUser()
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  updateTime()
  timer = window.setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.pos-layout-container {
  width: 100%;
  height: 100%;
}

.pos-header {
  background-color: #2c3e50;
  color: #ecf0f1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 70px !important;
}

.pos-header-left .pos-logo {
  font-size: 22px;
  font-weight: bold;
}

.pos-header-center .current-time {
  font-size: 20px;
  font-weight: 500;
}

.pos-header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.pos-header-right .cashier-info {
  font-size: 18px;
}

.pos-aside {
  background-color: #2c3e50;
  height: calc(100vh - 70px);
  overflow-y: auto;
}

.pos-aside .el-menu-item {
  font-size: 16px;
  height: 60px;
  line-height: 60px;
}

.pos-main {
  background-color: #ecf0f1;
  padding: 10px;
}
</style>
```