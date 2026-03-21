===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/entity/User.java===
```java
package com.kidswear.pos.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private Integer status;
    private String role;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/mapper/UserMapper.java===
```java
package com.kidswear.pos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kidswear.pos.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/service/UserService.java===
```java
package com.kidswear.pos.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kidswear.pos.entity.User;

public interface UserService extends IService<User> {
    String login(String username, String password);
    User getUserInfo(Long userId);
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/utils/JwtUtil.java===
```java
package com.kidswear.pos.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public Boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/interceptor/JwtInterceptor.java===
```java
package com.kidswear.pos.interceptor;

import com.kidswear.pos.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return false;
        }
        token = token.substring(7);
        try {
            if (jwtUtil.isTokenExpired(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token expired");
                return false;
            }
            request.setAttribute("userId", jwtUtil.getUserId(token));
            request.setAttribute("username", jwtUtil.getUsername(token));
            request.setAttribute("role", jwtUtil.getRole(token));
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return false;
        }
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/config/WebMvcConfig.java===
```java
package com.kidswear.pos.config;

import com.kidswear.pos.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/error");
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/service/impl/UserServiceImpl.java===
```java
package com.kidswear.pos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kidswear.pos.entity.User;
import com.kidswear.pos.mapper.UserMapper;
import com.kidswear.pos.service.UserService;
import com.kidswear.pos.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = getOne(wrapper);
        if (user == null || !passwordEncoder.matches(password, user.getPassword()) || user.getStatus() != 1) {
            throw new RuntimeException("Invalid username or password");
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public User getUserInfo(Long userId) {
        return getById(userId);
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/controller/AuthController.java===
```java
package com.kidswear.pos.controller;

import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.User;
import com.kidswear.pos.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> loginParams) {
        String username = loginParams.get("username");
        String password = loginParams.get("password");
        String token = userService.login(username, password);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    @GetMapping("/userinfo")
    public Result<User> getUserInfo() {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserInfo(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
```

===FILE:kidswear-pos-backend/src/main/resources/db/init.sql===
```sql
-- 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS kidswear_pos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kidswear_pos;

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    role VARCHAR(20) DEFAULT 'cashier' COMMENT '角色 admin cashier',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 1删除 0未删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 1删除 0未删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- SKU表
CREATE TABLE IF NOT EXISTS sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'SKU ID',
    spu_id BIGINT DEFAULT 0 COMMENT 'SPU ID',
    name VARCHAR(100) NOT NULL COMMENT 'SKU名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    image VARCHAR(255) COMMENT '商品图片',
    barcode VARCHAR(50) UNIQUE COMMENT '条形码',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '成本价',
    stock INT DEFAULT 0 COMMENT '库存',
    warn_stock INT DEFAULT 10 COMMENT '预警库存',
    spec VARCHAR(255) COMMENT '规格 JSON格式',
    status TINYINT DEFAULT 1 COMMENT '状态 1上架 0下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记 1删除 0未删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU表';

-- 插入初始管理员账号 密码: admin123
INSERT INTO sys_user (username, password, nickname, role) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt.0yG', '系统管理员', 'admin'),
('cashier', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt.0yG', '收银员', 'cashier');
```

===FILE:kidswear-pos-backend/src/main/resources/application-dev.yml===
```yaml
server:
  port: 8080
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/kidswear_pos?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
jwt:
  secret: kidswear-pos-secret-key-20240601-kidswear-pos-secret-key-20240601
  expiration: 86400
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/config/SecurityConfig.java===
```java
package com.kidswear.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().permitAll();
        return http.build();
    }
}
```

===FILE:kidswear-pos-frontend/src/api/auth.ts===
```typescript
import request from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  role: string
}

export interface AuthApi {
  login: (params: LoginParams) => Promise<{ token: string }>
  getUserInfo: () => Promise<UserInfo>
  logout: () => Promise<void>
}

const authApi: AuthApi = {
  login: (params) => request.post('/api/auth/login', params),
  getUserInfo: () => request.get('/api/auth/userinfo'),
  logout: () => request.post('/api/auth/logout')
}

export default authApi
```

===FILE:kidswear-pos-frontend/src/api/category.ts===
```typescript
import request from '@/utils/request'

export interface Category {
  id: number
  name: string
  parentId: number
  sortOrder: number
  status: number
  createTime: string
  updateTime: string
}

export interface CategoryQuery {
  name?: string
  status?: number
}

export interface CategoryApi {
  list: (query?: CategoryQuery) => Promise<Category[]>
  add: (data: Partial<Category>) => Promise<void>
  update: (data: Partial<Category>) => Promise<void>
  delete: (id: number) => Promise<void>
  updateStatus: (id: number, status: number) => Promise<void>
}

const categoryApi: CategoryApi = {
  list: (query) => request.get('/api/category/list', { params: query }),
  add: (data) => request.post('/api/category/add', data),
  update: (data) => request.put('/api/category/update', data),
  delete: (id) => request.delete(`/api/category/delete/${id}`),
  updateStatus: (id, status) => request.put(`/api/category/status/${id}`, { status })
}

export default categoryApi
```

===FILE:kidswear-pos-frontend/src/store/user.ts===
```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authApi, { UserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password })
    token.value = res.token
    setToken(res.token)
    await getUserInfo()
  }

  const getUserInfo = async () => {
    const res = await authApi.getUserInfo()
    userInfo.value = res
  }

  const logout = async () => {
    await authApi.logout()
    token.value = ''
    userInfo.value = null
    removeToken()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    getUserInfo,
    logout
  }
})
```

===FILE:kidswear-pos-frontend/src/utils/auth.ts===
```typescript
const TOKEN_KEY = 'kidswear_pos_token'

export const getToken = () => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token: string) => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = () => {
  localStorage.removeItem(TOKEN_KEY)
}
```

===FILE:kidswear-pos-frontend/src/utils/request.ts===
```typescript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

===FILE:kidswear-pos-frontend/src/router/index.ts===
```typescript
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/pos',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'pos',
        name: 'Pos',
        component: () => import('@/views/pos-layout/index.vue'),
        meta: { requiresAuth: true, title: 'POS收银' }
      },
      {
        path: 'sku-init',
        name: 'SkuInit',
        component: () => import('@/views/sku-init/index.vue'),
        meta: { requiresAuth: true, title: 'SKU初始化', roles: ['admin'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = getToken()
  const userStore = useUserStore()

  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
      return
    }
    if (!userStore.userInfo) {
      try {
        await userStore.getUserInfo()
      } catch (error) {
        next('/login')
        return
      }
    }
    if (to.meta.roles && !to.meta.roles.includes(userStore.userInfo?.role)) {
      next('/pos')
      return
    }
    next()
  } else {
    if (token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
  }
})

export default router
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

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
```

===FILE:kidswear-pos-frontend/package.json===
```json
{
  "name": "kidswear-pos-frontend",
  "private": true,
  "version": "0.0.0",
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
    "element-plus": "^2.6.3",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.6.8"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.4",
    "typescript": "^5.2.2",
    "vite": "^5.2.0",
    "vue-tsc": "^2.0.6"
  }
}
```

===FILE:kidswear-pos-frontend/vite.config.ts===
```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

===FILE:kidswear-pos-backend/pom.xml===
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
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

    <properties>
        <java.version>11</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jwt.version>0.12.3</jwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
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
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
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