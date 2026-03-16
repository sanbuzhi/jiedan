# 通用本地生活类微信小程序 + Spring Boot 后端技术任务书
（注：本任务书基于“本地便民果蔬自提点”的完整通用业务场景设计，覆盖全流程可落地功能，供AI自动生成100%可运行代码）

---

## 1. 项目技术规格
### 1.1 技术栈确定
- **前端**：微信小程序（原生框架：WXML/WXSS/JS + 官方开发工具v1.06+稳定版）
- **后端**：
  * 核心框架：Spring Boot 2.7.18（LTS版本，兼容性好）
  * 数据访问：MyBatis-Plus 3.5.5（简化CRUD开发）
  * 数据库：MySQL 8.0.36（LTS版本，支持json等类型）
  * 工具库：Hutool 5.8.28（通用工具封装）、Lombok 1.18.30（简化实体类）、Swagger 3.0.0（接口文档自动生成，供AI测试验证）
  * 安全：JWT 0.11.5（用户身份认证）
- **接口**：RESTful API（统一JSON格式，UTF-8编码）

### 1.2 项目结构规范
#### 1.2.1 前端项目结构
```
fruit-pickup-miniprogram/
├── app.js                  // 全局逻辑
├── app.json                // 全局配置（页面路由、tabBar、window等）
├── app.wxss                // 全局样式
├── project.config.json     // 项目配置（AppID、编译设置等，需先占位AppID为测试号）
├── sitemap.json            // 索引配置
├── pages/                  // 页面目录（按业务分模块）
│   ├── index/              // 首页
│   │   ├── index.wxml
│   │   ├── index.wxss
│   │   ├── index.js
│   │   └── index.json
│   ├── merchandise/        // 商品模块
│   │   ├── list/           // 商品列表页（分类筛选）
│   │   ├── detail/         // 商品详情页
│   │   └── search/         // 商品搜索页
│   ├── cart/               // 购物车模块
│   │   ├── cart.wxml
│   │   ├── cart.wxss
│   │   ├── cart.js
│   │   └── cart.json
│   ├── order/              // 订单模块
│   │   ├── confirm/        // 订单确认页
│   │   ├── list/           // 订单列表页（全部/待付款/待自提/已完成）
│   │   └── detail/         // 订单详情页
│   ├── user/               // 用户模块
│   │   ├── login/          // 微信授权登录页
│   │   ├── center/         // 个人中心页
│   │   └── address/        // 自提点地址管理页（新增/编辑/设为默认）
│   └── common/             // 公共跳转路由？废弃，统一用pages里的绝对路径
├── utils/                  // 通用工具目录
│   ├── request.js          // 统一请求封装（加JWT、错误处理）
│   ├── storage.js          // 本地存储封装（wx.setStorageSync/wx.getStorageSync的安全封装）
│   ├── format.js           // 格式化工具（金额、日期、时间、手机号）
│   └── auth.js             // 登录状态校验
├── components/             // 公共组件目录
│   ├── nav-bar/            // 自定义顶部导航栏
│   ├── tab-bar/            // 自定义底部导航栏（可选，增强tabBar体验）
│   ├── loading/            // 全局加载组件
│   ├── empty-list/         // 空数据列表组件
│   ├── goods-card/         // 商品卡片组件（列表页/首页/搜索页共用）
│   ├── order-card/         // 订单卡片组件（订单列表页共用）
│   └── quantity-picker/    // 商品数量加减组件（购物车/商品详情/订单确认共用）
└── assets/                 // 静态资源目录
    ├── images/             // 图片资源（占位图、分类图、logo等）
    └── styles/             // 公共样式片段（颜色变量、按钮样式、间距样式等）
```

#### 1.2.2 后端项目结构
```
fruit-pickup-backend/
├── pom.xml                 // Maven依赖配置
├── src/
│   ├── main/
│   │   ├── java/com/fruit/pickup/  // 包名统一
│   │   │   ├── FruitPickupApplication.java  // 启动类
│   │   │   ├── common/       // 公共模块
│   │   │   │   ├── result/   // 统一响应结果类
│   │   │   │   │   ├── Result.java
│   │   │   │   │   ├── ResultCode.java  // 错误码枚举
│   │   │   │   ├── util/     // 通用工具类
│   │   │   │   │   ├── JwtUtil.java  // JWT生成/解析工具
│   │   │   │   │   ├── PasswordUtil.java  // 密码加密工具（BCrypt，备用）
│   │   │   │   │   └── WechatUtil.java  // 微信登录、手机号解密工具
│   │   │   │   ├── config/   // 配置类
│   │   │   │   │   ├── CorsConfig.java  // 跨域配置
│   │   │   │   │   ├── SwaggerConfig.java  // Swagger接口文档配置
│   │   │   │   │   ├── MybatisPlusConfig.java  // MyBatis-Plus分页、乐观锁配置
│   │   │   │   │   └── InterceptorConfig.java  // 登录拦截器配置
│   │   │   │   └── interceptor/  // 拦截器
│   │   │   │   │   └── LoginInterceptor.java  // JWT登录状态校验拦截器
│   │   │   ├── entity/       // 数据库实体类（对应表名，驼峰转下划线）
│   │   │   ├── dto/          // 前端请求数据传输对象（Data Transfer Object）
│   │   │   ├── vo/           // 后端响应视图对象（View Object）
│   │   │   ├── mapper/       // MyBatis-Plus Mapper接口
│   │   │   ├── service/      // Service接口
│   │   │   └── service/impl/ // Service实现类
│   │   └── resources/
│   │       ├── application.yml  // 主配置文件
│   │       ├── application-dev.yml  // 开发环境配置（数据库、微信小程序信息）
│   │       └── mapper/        // MyBatis-Plus XML映射文件（仅复杂查询用）
│   └── test/
│       └── java/com/fruit/pickup/  // 测试类
└── README.md
```

### 1.3 代码生成规则
#### 1.3.1 通用规则
1. 所有代码必须**完整可运行**，无语法错误
2. 所有依赖必须**明确指定版本号**，无SNAPSHOT版本
3. 所有用户输入必须**做双重校验**（前端表单校验+后端业务/数据校验）
4. 所有接口必须**返回统一的Result格式**
5. 所有敏感信息（微信AppID/AppSecret、数据库密码、JWT密钥）**必须放在配置文件中，占位但明确填写位置**
6. 所有静态资源（前端占位图）**必须使用稳定的CDN链接**或本地资源占位（后端可生成一个临时图片生成接口）

---

## 2. 前端页面开发清单
### 2.1 模块1：首页模块
#### 2.1.1 页面名称：首页
- **路径**：pages/index/index
- **功能描述**：
  1. 展示顶部自定义导航栏
  2. 展示轮播图（3-5张本地果蔬自提活动图）
  3. 展示分类导航栏（快捷跳转到分类商品列表）
  4. 展示「今日推荐」「限时秒杀」2个商品横向滚动区
  5. 展示底部自定义tabBar（选中首页）
- **包含的组件**：
  1. custom-nav-bar（自定义顶部导航栏）
  2. custom-tab-bar（自定义底部tabBar）
  3. swiper（微信原生轮播图）
  4. goods-card（商品卡片组件，横向布局）
- **需要调用的后端接口**：
  1. GET /api/v1/banners（获取首页轮播图列表）
  2. GET /api/v1/categories（获取分类列表）
  3. GET /api/v1/merchandises/hot（获取今日推荐商品列表）
  4. GET /api/v1/merchandises/flash-sale（获取限时秒杀商品列表）
- **页面跳转关系**：
  1. 点击分类导航 → pages/merchandise/list（带分类ID参数）
  2. 点击商品卡片 → pages/merchandise/detail（带商品ID参数）
  3. 点击搜索框（自定义导航栏的一部分） → pages/merchandise/search
  4. 点击tabBar购物车 → pages/cart/cart
  5. 点击tabBar我的 → pages/user/center（需登录，未登录跳登录）

---

### 2.2 模块2：商品模块
#### 2.2.1 页面名称：商品列表页
- **路径**：pages/merchandise/list
- **功能描述**：
  1. 展示顶部自定义导航栏（带返回按钮、分类名称/搜索关键词）
  2. 展示侧边全部分类栏（支持切换分类）
  3. 展示主区域商品列表（支持排序：默认/销量/价格升序/价格降序）
  4. 支持下拉刷新、上拉加载更多
  5. 展示底部自定义tabBar
- **包含的组件**：
  1. custom-nav-bar
  2. custom-tab-bar
  3. goods-card（商品卡片组件，纵向网格布局，每行2个）
  4. empty-list（无数据时展示）
- **需要调用的后端接口**：
  1. GET /api/v1/categories（获取分类列表）
  2. GET /api/v1/merchandises（分页获取商品列表，带分类ID、排序参数、搜索关键词（可选））
- **页面跳转关系**：
  1. 点击返回按钮 → 上一页（首页或搜索页）
  2. 点击商品卡片 → pages/merchandise/detail（带商品ID参数）
  3. 点击tabBar购物车 → pages/cart/cart
  4. 点击tabBar我的 → pages/user/center（需登录）

#### 2.2.2 页面名称：商品详情页
- **路径**：pages/merchandise/detail
- **功能描述**：
  1. 展示顶部自定义导航栏（带返回按钮、分享按钮、客服按钮）
  2. 展示商品主图轮播图
  3. 展示商品基本信息（名称、销量、原价、现价、库存、单位）
  4. 展示商品详情图（HTML富文本渲染）
  5. 展示底部操作栏（加入购物车、立即购买）
  6. 展示数量加减组件（用于调整购买数量）
- **包含的组件**：
  1. custom-nav-bar
  2. swiper（主图轮播）
  3. quantity-picker（数量加减）
- **需要调用的后端接口**：
  1. GET /api/v1/merchandises/{id}（获取商品详情）
  2. POST /api/v1/cart（加入购物车，需登录）
- **页面跳转关系**：
  1. 点击返回按钮 → 上一页（列表页或首页或搜索页）
  2. 点击加入购物车 → 提示成功，停留当前页
  3. 点击立即购买 → 校验登录，跳转到pages/order/confirm（带商品ID、数量参数）
  4. 点击客服按钮 → 调用微信客服接口
  5. 点击分享按钮 → 调用微信分享接口（需设置分享权限）

#### 2.2.3 页面名称：商品搜索页
- **路径**：pages/merchandise/search
- **功能描述**：
  1. 展示顶部搜索栏（带返回按钮、输入框、搜索按钮）
  2. 展示搜索历史记录（本地存储，最多10条）
  3. 展示热门搜索关键词（后端返回）
  4. 展示搜索结果列表（支持下拉刷新、上拉加载更多）
- **包含的组件**：
  1. goods-card（纵向网格布局）
  2. empty-list（无搜索历史/无热门搜索/无搜索结果时展示）
- **需要调用的后端接口**：
  1. GET /api/v1/merchandises/hot-keywords（获取热门搜索关键词）
  2. GET /api/v1/merchandises（分页获取搜索结果，带关键词参数）
- **页面跳转关系**：
  1. 点击返回按钮 → 上一页（首页或列表页）
  2. 点击搜索历史/热门搜索关键词 → 触发搜索，跳转到pages/merchandise/list（带关键词参数）
  3. 点击搜索结果商品卡片 → pages/merchandise/detail（带商品ID参数）

---

### 2.3 模块3：购物车模块
#### 2.3.1 页面名称：购物车页
- **路径**：pages/cart/cart
- **功能描述**：
  1. 展示顶部自定义导航栏（带「编辑」/「完成」按钮）
  2. 展示购物车商品列表（支持单选/全选、数量加减、删除）
  3. 展示底部结算栏（选中商品总数、选中商品总金额、结算按钮）
  4. 支持编辑模式（编辑时隐藏结算栏，显示全选删除按钮）
  5. 展示底部自定义tabBar（选中购物车）
- **包含的组件**：
  1. custom-nav-bar
  2. custom-tab-bar
  3. goods-card（购物车专用布局，带复选框）
  4. quantity-picker（数量加减）
  5. empty-list（购物车为空时展示）
- **需要调用的后端接口**：
  1. GET /api/v1/cart（获取购物车列表，需登录）
  2. PUT /api/v1/cart/{id}（更新购物车商品数量，需登录）
  3. DELETE /api/v1/cart/{ids}（批量删除购物车商品，需登录）
- **页面跳转关系**：
  1. 点击商品卡片（未编辑模式下） → pages/merchandise/detail（带商品ID参数）
  2. 点击结算按钮 → 校验选中商品数量，跳转到pages/order/confirm（带选中购物车商品ID数组参数）
  3. 点击tabBar首页 → pages/index/index
  4. 点击tabBar我的 → pages/user/center

---

### 2.4 模块4：订单模块
#### 2.4.1 页面名称：订单确认页
- **路径**：pages/order/confirm
- **功能描述**：
  1. 展示顶部自定义导航栏（带返回按钮）
  2. 展示默认自提点地址（可切换到地址管理页选择）
  3. 展示订单商品列表（不可修改数量）
  4. 展示订单金额明细（商品总金额、配送费/自提优惠、实付金额）
  5. 展示订单备注输入框
  6. 展示底部提交订单/去支付栏
- **包含的组件**：
  1. custom-nav-bar
  2. goods-card（订单确认专用布局，不可选中）
- **需要调用的后端接口**：
  1. GET /api/v1/addresses/default（获取默认自提点地址，需登录）
  2. GET /api/v1/cart/selected（获取选中的购物车商品信息，需登录）
  3. GET /api/v1/merchandises/batch（批量获取商品信息，当从商品详情页跳转时用）
  4. POST /api/v1/orders（创建订单，需登录）
- **页面跳转关系**：
  1. 点击返回按钮 → 上一页（购物车或商品详情页）
  2. 点击自提点地址 → 跳转到pages/user/address（带返回确认页的参数）
  3. 点击创建订单 → 调用微信支付接口（测试时可模拟支付成功），跳转到pages/order/detail（带订单ID参数）

#### 2.4.2 页面名称：订单列表页
- **路径**：pages/order/list
- **功能描述**：
  1. 展示顶部自定义导航栏