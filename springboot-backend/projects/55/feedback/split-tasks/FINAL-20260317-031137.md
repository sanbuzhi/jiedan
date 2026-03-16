# 社区便利店自提小程序电商技术任务书
## 1. 项目技术规格
### 1.1 技术栈
- 前端：微信小程序（基础库2.32.3+、WXML/WXSS/JS）
- 后端：Spring Boot 2.7.18 + MyBatis Plus 3.5.3.2 + MySQL 8.0.36 + JWT 0.11.5
- 接口：RESTful API、UTF-8编码
### 1.2 项目结构规范
#### 前端（miniprogram）
```
miniprogram/
├── app.js
├── app.json
├── app.wxss
├── config/
│   └── api.js
├── utils/
│   ├── request.js
│   └── storage.js
├── components/
│   ├── tab-bar/
│   └── product-card/
└── pages/
    ├── index/
    ├── product/
    ├── cart/
    ├── order/
    └── user/
```
#### 后端（community-store）
```
community-store/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── store/
│       │           ├── CommunityStoreApplication.java
│       │           ├── config/
│       │           ├── controller/
│       │           ├── service/
│       │           │   └── impl/
│       │           ├── mapper/
│       │           ├── entity/
│       │           ├── dto/
│       │           ├── vo/
│       │           ├── enums/
│       │           └── exception/
│       └── resources/
│           ├── application.yml
│           └── mapper/
└── pom.xml
```
### 1.3 代码生成规则
- 依赖版本严格固定
- 前后端参数双重校验
- 敏感信息（数据库密码、JWT密钥）放application.yml配置
- 所有后端接口统一Result<T>包装返回
- 除登录/注册/公共资源接口外，其余需JWT登录校验
- 可运行代码覆盖所有核心接口与页面

## 2. 前端页面开发清单
### 2.1 首页
- 页面路径：pages/index/index
- 核心功能：轮播图展示、分类导航、热销商品展示、搜索入口
- 必用组件：tab-bar、product-card、swiper
- 接口调用清单：GET /api/v1/banners、GET /api/v1/categories、GET /api/v1/products/hot
- 页面跳转关系：点击分类→pages/product/list?categoryId=xxx；点击商品→pages/product/detail?id=xxx；点击搜索→pages/product/list

### 2.2 商品模块
#### 2.2.1 商品列表页
- 页面路径：pages/product/list
- 核心功能：分类筛选、搜索筛选、商品列表展示、排序
- 必用组件：tab-bar、product-card
- 接口调用清单：GET /api/v1/categories、GET /api/v1/products
- 页面跳转关系：点击商品→pages/product/detail?id=xxx

#### 2.2.2 商品详情页
- 页面路径：pages/product/detail
- 核心功能：商品信息展示、规格选择、加入购物车、立即购买
- 必用组件：tab-bar
- 接口调用清单：GET /api/v1/products/{id}
- 页面跳转关系：点击购物车→pages/cart/cart；点击立即购买→pages/order/submit?id=xxx&spec=xxx&num=xxx

### 2.3 购物车模块
- 页面路径：pages/cart/cart
- 核心功能：购物车商品展示、数量增减、商品删除、全选/反选、结算
- 必用组件：tab-bar
- 接口调用清单：GET /api/v1/carts、POST /api/v1/carts、PUT /api/v1/carts/{id}、DELETE /api/v1/carts/{id}、DELETE /api/v1/carts/batch
- 页面跳转关系：点击结算→pages/order/submit

### 2.4 订单模块
#### 2.4.1 订单提交页
- 页面路径：pages/order/submit
- 核心功能：收货信息（默认自提点）、商品清单展示、订单金额计算、提交订单
- 必用组件：无
- 接口调用清单：GET /api/v1/users/pickup-points、POST /api/v1/orders
- 页面跳转关系：提交成功→pages/order/detail?id=xxx

#### 2.4.2 订单列表页
- 页面路径：pages/order/list
- 核心功能：订单状态筛选、订单列表展示、查看详情、取消待付款订单
- 必用组件：tab-bar
- 接口调用清单：GET /api/v1/orders
- 页面跳转关系：点击订单→pages/order/detail?id=xxx

#### 2.4.3 订单详情页
- 页面路径：pages/order/detail
- 核心功能：订单信息展示、自提码展示、取消待付款订单、确认收货
- 必用组件：无
- 接口调用清单：GET /api/v1/orders/{id}、PUT /api/v1/orders/{id}/cancel、PUT /api/v1/orders/{id}/confirm
- 页面跳转关系：无

### 2.5 用户模块
#### 2.5.1 用户中心页
- 页面路径：pages/user/user
- 核心功能：用户信息展示、订单入口、自提点管理、登录入口
- 必用组件：tab-bar
- 接口调用清单：GET /api/v1/users/info
- 页面跳转关系：未登录→pages/user/login；点击订单入口→pages/order/list；点击自提点→pages/user/pickup-points

#### 2.5.2 用户登录页
- 页面路径：pages/user/login
- 核心功能：微信授权登录
- 必用组件：无
- 接口调用清单：POST /api/v1/users/login
- 页面跳转关系：登录成功→pages/index/index

#### 2.5.3 自提点管理页
- 页面路径：pages/user/pickup-points
- 核心功能：自提点列表展示、设置默认自提点
- 必用组件：无
- 接口调用清单：GET /api/v1/pickup-points、PUT /api/v1/users/pickup-points/{id}
- 页面跳转关系：无

## 3. 后端接口开发清单
### 3.1 基础通用模块
#### 3.1.1 轮播图列表
- 接口URL+请求方式：GET /api/v1/banners
- 请求参数：无
- 响应数据格式：Result<List<BannerVO>>
- 核心业务逻辑：查询status=1的轮播图，按sort升序排列
- 关联数据库表名：banner

#### 3.1.2 分类列表
- 接口URL+请求方式：GET /api/v1/categories
- 请求参数：无
- 响应数据格式：Result<List<CategoryVO>>
- 核心业务逻辑：查询status=1的分类，按sort升序排列
- 关联数据库表名：category

#### 3.1.3 自提点列表
- 接口URL+请求方式：GET /api/v1/pickup-points
- 请求参数：无
- 响应数据格式：Result<List<PickupPointVO>>
- 核心业务逻辑：查询status=1的自提点
- 关联数据库表名：pickup_point

### 3.2 商品模块
#### 3.2.1 热销商品列表
- 接口URL+请求方式：GET /api/v1/products/hot
- 请求参数：无
- 响应数据格式：Result<List<ProductVO>>
- 核心业务逻辑：查询status=1、is_hot=1的商品，取前10条，按sort升序排列
- 关联数据库表名：product

#### 3.2.2 商品列表
- 接口URL+请求方式：GET /api/v1/products
- 请求参数：categoryId（Long，非必填）、keyword（String，非必填）、sortType（Integer，非必填，0销量1价格降2价格升）
- 响应数据格式：Result<List<ProductVO>>
- 核心业务逻辑：按参数筛选status=1的商品，按sortType排序
- 关联数据库表名：product

#### 3.2.3 商品详情
- 接口URL+请求方式：GET /api/v1/products/{id}
- 请求参数：id（Long，必填，≥1）
- 响应数据格式：Result<ProductDetailVO>
- 核心业务逻辑：根据id查询status=1的商品详情
- 关联数据库表名：product

### 3.3 购物车模块
#### 3.3.1 购物车列表
- 接口URL+请求方式：GET /api/v1/carts
- 请求参数：无（需JWT）
- 响应数据格式：Result<List<CartVO>>
- 核心业务逻辑：根据JWT获取用户id，查询该用户的购物车，关联商品信息
- 关联数据库表名：cart、product

#### 3.3.2 加入购物车
- 接口URL+请求方式：POST /api/v1/carts
- 请求参数：productId（Long，必填，≥1）、spec（String，非必填，默认“默认规格”）、num（Integer，必填，≥1）
- 响应数据格式：Result<Boolean>
- 核心业务逻辑：根据JWT获取用户id，查询该用户购物车是否存在同商品同规格，存在则累加数量，不存在则新增
- 关联数据库表名：cart

#### 3.3.3 修改购物车数量
- 接口URL+请求方式：PUT /api/v1/carts/{id}
- 请求参数：id（Long，必填，≥1）、num（Integer，必填，≥1）
- 响应数据格式：Result<Boolean>
- 核心业务逻辑：根据JWT获取用户id，验证购物车所属用户，修改数量
- 关联数据库表名：cart

#### 3.3.4 删除购物车商品
- 接口URL+请求方式：DELETE /api/v1/carts/{id}
- 请求参数：id（Long，必填，≥1）
- 响应数据格式：Result<Boolean>
- 核心业务逻辑：根据JWT获取用户id，验证购物车所属用户，删除商品
- 关联数据库表名：cart

#### 3.3.5 批量删除购物车商品
- 接口URL+请求方式：DELETE /api/v1/carts/batch
- 请求参数：ids（List<Long>，必填，非空）
- 响应数据格式：Result<Boolean>
- 核心业务逻辑：根据JWT获取用户id，验证所有购物车所属用户，批量删除
- 关联数据库表名：cart

### 3.4 订单模块
#### 3.4.1 提交订单
- 接口URL+请求方式：POST /api/v1/orders
- 请求参数：pickupPointId（Long，必填，≥1）、cartIds（List<Long>，非必填，非空）、productId（Long，非必填，≥1）、spec（String，非必填，默认“默认规格”）、num（Integer，非必填，≥1）
- 响应数据格式：Result<OrderVO>
- 核心业务逻辑：根据JWT获取用户id，校验参数（二选一：cartIds或productId+spec+num），查询商品信息，计算总金额，生成订单号（yyyyMMddHHmmss+6位随机数），生成订单及订单明细，扣减商品库存，若使用cartIds则删除对应购物车
- 关联数据库表名：order_info、order_item、product、cart

#### 3.4.2 订单列表
- 接口URL+请求方式：GET /api/v1/orders
- 请求参数：status（Integer，非必填，0待付款1待自提2已完成3已取消）
- 响应数据格式：Result<List<OrderVO>>
- 核心业务逻辑：根据JWT获取用户id，按参数筛选订单，关联订单明细、自提点信息，按创建时间降序排列
- 关联数据库表名：order_info、order_item、pickup_point

#### 3.4.3 订单详情
- 接口URL+请求方式：GET /api/v1/orders/{id}
- 请求参数：id（Long，必填，≥1）
- 响应数据格式：Result<OrderDetailVO>
- 核心业务逻辑：根据JWT获取用户id，验证订单所属用户，查询订单详情，关联订单明细、自提点信息
- 关联数据库表名：order_info、order_item、pickup_point

#### 3.4.4 取消待付款订单
- 接口URL+请求方式：PUT /api/v1/orders/{id}/cancel
- 请求参数：id（Long，必填，≥1）
- 响应数据格式：Result<Boolean>
- 核心业务逻辑：根据JWT获取用户id，验证订单所属用户，校验订单状态为待付款，修改订单状态为已取消，恢复商品库存
- 关联数据库表名：order_info、order_item、product

#### 3.4.5 确认收货
- 接口URL+请求方式：PUT /api/v1/orders/{id}/confirm
- 请求参数：id（Long，必填，≥1）
- 响应数据格式：Result<Boolean>
- 核心业务逻辑：根据JWT获取用户id，验证订单所属用户，校验订单状态为待自提，修改订单状态为已完成
- 关联数据库表名：order_info

### 3.5 用户模块
#### 3.5.1 微信授权登录
- 接口URL+请求方式：POST /api/v1/users/login
- 请求参数：code（String，必填，非空）
- 响应数据格式：Result<UserLoginVO>
- 核心业务逻辑：调用微信auth.code2Session接口获取openid，查询用户是否存在，不存在则新增，生成JWT token
- 关联数据库表名：user

#### 3.5.2 获取用户信息
- 接口URL+请求方式：GET /api/v1/users/info
- 请求参数：无（需JWT）
- 响应数据格式：Result<UserInfoVO>
- 核心业务逻辑：根据JWT获取用户id，查询用户信息
- 关联数据库表名：user

#### 3.5.3 获取用户自提点
- 接口URL+请求方式：GET /api/v1/users/pickup-points
- 请求参数：无（需JWT）
- 响应数据格式：Result<UserPickupPointsVO>
- 核心业务逻辑：根据JWT获取用户id，查询用户默认自提点，以及所有可用自提点
- 关联数据库表名：user、pickup_point

#### 3.5.4 设置默认自提点
- 接口URL+请求方式：PUT /api/v1/users/pickup-points/{id}
- 请求参数：id（Long，必填，≥1）
- 响应数据格式：Result<Boolean>
- 核心业务逻辑：根据JWT获取用户id，验证自提点存在且status=1，修改用户默认自提点id
- 关联数据库表名：user、pickup_point

## 4. 数据库表结构设计
### 4.1 轮播图表（banner）
- 核心用途：存储首页轮播图信息
- 字段定义：
  | 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
  |--------|----------|------|--------|------|
  | id | bigint | 主键、自增 | - | 轮播图id |
  | image_url | varchar(255) | 非空 | - | 轮播图地址 |
  | link_url | varchar(255) | 允许空 | - | 跳转链接 |
  | sort | int | 非空 | 0 | 排序值 |
  | status | tinyint | 非空 | 1 | 状态（0禁用1启用） |
  | create_time | datetime | 非空 | CURRENT_TIMESTAMP | 创建时间 |
  | update_time | datetime | 非空 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
- 主键/外键/索引：主键id、索引status
- 表间关联关系：无

### 4.2 分类表（category）
- 核心用途：存储商品分类信息
- 字段定义：
  | 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
  |--------|----------|------|--------|------|
  | id | bigint | 主键、自增 | - | 分类id |
  | name | varchar(50) | 非空 | - | 分类名称 |
  | icon_url | varchar(255) | 非空 | - | 分类图标地址 |
  | sort | int | 非空 | 0 | 排序值 |
  | status | tinyint | 非空 | 1 | 状态（0禁用1启用） |
  | create_time | datetime | 非空 | CURRENT_TIMESTAMP | 创建时间 |
  | update_time | datetime | 非空 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
- 主键/外键/索引：主键id、索引status
- 表间关联关系：无

### 4.3 自提点表（pickup_point）
- 核心用途：存储自提点信息
- 字段定义：
  | 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
  |--------|----------|------|--------|------|
  | id | bigint | 主键、自增 | - | 自提点id |
  | name | varchar(100) | 非空 | - | 自提点名称 |
  | address | varchar(255) | 非空 | - | 自提点地址 |
  | phone | varchar(20) | 非空 | - | 自提点电话 |
  | business_hours | varchar(100) | 非空 | - | 营业时间 |
  | status | tinyint | 非空 | 1 | 状态（0禁用1启用） |
  | create_time | datetime | 非空 | CURRENT_TIMESTAMP | 创建时间 |
  | update_time | datetime | 非空 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
- 主键/外键/索引：主键id、索引status
- 表间关联关系：无

### 4.4 商品表（product）
- 核心用途：存储商品信息
- 字段定义：
  | 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
  |--------|----------|------|--------|------|
  | id | bigint | 主键、自增 | - | 商品id |
  | category_id | bigint | 非空 | - | 分类id |
  | name | varchar(100) | 非空 | - | 商品名称 |
  | image_url | varchar(255) | 非空 | - | 商品主图地址 |
  | spec | varchar(100) | 非空 | 默认规格 | 商品规格 |
  | price | decimal(10,2) | 非空 | - | 商品价格 |
  | stock | int | 非空 | 0 | 商品库存 |
  | sales | int | 非空 | 0 | 商品销量 |
  | is_hot | tinyint | 非空 | 0 | 是否热销（0否1是） |
  | description | text | 允许空 | - | 商品描述 |
  | sort | int | 非空 | 0 | 排序值 |
  | status | tinyint | 非空 | 1 | 状态（0下架1上架） |
  | create_time | datetime | 非空 | CURRENT_TIMESTAMP | 创建时间 |
  | update_time | datetime | 非空 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
- 主键/外键/索引：主键id、索引category_id、索引status、索引is_hot
- 表间关联关系：category_id关联category.id

### 4.5 用户表（user）
- 核心用途：存储用户信息
- 字段定义：
  | 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
  |--------|----------|------|--------|------|
  | id | bigint | 主键、自增 | - | 用户id |
  | openid | varchar(100) | 非空、唯一 | - | 微信openid |
  | nickname | varchar(50) | 允许空 | 微信用户 | 昵称 |
  | avatar_url | varchar(255) | 允许空 | - | 头像地址 |
  | default_pickup_point_id | bigint | 允许空 | - | 默认自提点id |
  | create_time | datetime | 非空 | CURRENT_TIMESTAMP | 创建时间 |
  | update_time | datetime | 非空 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
- 主键/外键/索引：主键id、唯一索引openid、索引default_pickup_point_id
- 表间关联关系：default_pickup_point_id关联pickup_point.id

### 4.6 购物车表（cart）
- 核心用途：存储用户购物车信息
- 字段定义：
  | 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
  |--------|----------|------|--------|------|
  | id | bigint | 主键、自增 | - | 购物车id |
  | user_id | bigint | 非空 | - | 用户id |
  | product_id | bigint | 非空 | - | 商品id |
  | spec | varchar(100) | 非空 | 默认规格 | 商品规格 |
  | num | int | 非空 | 1 | 商品数量 |
  | create_time | datetime | 非空 | CURRENT_TIMESTAMP | 创建时间 |
  | update_time | datetime | 非空 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
- 主键/外键/索引：主键id、索引user_id、索引product_id、唯一索引uk_user_product_spec
- 表间关联关系：user_id关联user.id、product_id关联product.id

### 4.7 订单表（order_info）
- 核心用途：存储订单信息
- 字段定义：
  | 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
  |--------|----------|------|--------|------|
  | id | bigint | 主键、自增 | - | 订单id |
  | order_no | varchar(30) | 非空、唯一 | - | 订单号 |
  | user_id | bigint | 非空 | - | 用户id |
  | pickup_point_id | bigint | 非空 | - | 自提点id |
  | total_amount | decimal(10,2) | 非空 | - | 订单总金额 |
  | pickup_code | varchar(10) | 非空 | - | 自提码 |
  | status | tinyint | 非空 | 0 | 状态（0待付款1待自提2已完成3已取消） |
  | create_time | datetime | 非空 | CURRENT_TIMESTAMP | 创建时间 |
  | update_time | datetime | 非空 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
- 主键/外键/索引：主键id、唯一索引order_no、索引user_id、索引pickup_point_id、索引status
- 表间关联关系：user_id关联user.id、pickup_point_id关联pickup_point.id

### 4.8 订单明细表（order_item）
- 核心用途：存储订单商品明细
- 字段定义：
  | 字段名 | 数据类型 | 约束 | 默认值 | 注释 |
  |--------|----------|------|--------|------|
  | id | bigint | 主键、自增 | - | 订单明细id |
  | order_id | bigint | 非空 | - | 订单id |
  | product_id | bigint | 非空 | - | 商品id |
  | product_name | varchar(100) | 非空 | - | 商品名称 |
  | product_image_url | varchar(255) | 非空 | - | 商品主图地址 |
  | spec | varchar(100) | 非空 | 默认规格 | 商品规格 |
  | price | decimal(10,2) | 非空 | - | 商品单价 |
  | num | int | 非空 | 1 | 商品数量 |
  | create_time | datetime | 非空 | CURRENT_TIMESTAMP | 创建时间 |
- 主键/外键/索引：主键id、索引order_id、索引product_id
- 表间关联关系：order_id关联order_info.id、product_id关联product.id

## 5. 业务逻辑规则
### 5.1 核心业务流程
微信授权登录→浏览/搜索商品→加入购物车/立即购买→提交订单（自动生成自提码）→待付款（超时未付自动取消）→待自提→确认收货→已完成

### 5.2 数据校验规则
#### 前端表单校验
- 加入购物车/立即购买：商品数量≥1
- 提交订单：必须选择自提点
#### 后端参数校验
- 商品id、分类id、自提点id、用户id、购物车id、订单id≥1
- 商品数量≥1
- 订单总金额≥0.01
- 微信code非空
- 轮播图/分类/自提点/商品状态为1时才可展示/使用

### 5.3 状态流转规则
- 订单状态：
  - 待付款→已取消：用户主动取消或创建后30分钟未付款
  - 待付款→待自提：支付成功（本任务书默认模拟支付成功）
  - 待自提→已完成：用户主动确认收货

### 5.4 异常处理规则
- 接口异常：统一返回Result<ErrorVO>，错误码500，错误信息“系统异常，请稍后重试”
- 登录失效：返回Result<ErrorVO>，错误码401，错误信息“登录已失效，请重新登录”
- 参数错误：返回Result<ErrorVO>，错误码400，错误信息为具体的参数校验失败信息
- 库存不足：返回Result<ErrorVO>，错误码400，错误信息“商品库存不足”

## 6. 开发执行顺序
### 阶段1：数据库表创建
按依赖顺序执行：banner→category→pickup_point→product→user→cart→order_info→order_item

### 阶段2：后端接口开发
按依赖顺序执行：基础通用→商品→用户→购物车→订单

### 阶段3：前端页面开发
按依赖顺序执行：登录→首页→商品列表→商品详情→购物车→订单提交→订单列表→订单详情→用户中心→自提点管理

### 阶段4：接口联调
1. 登录接口联调，验证JWT生成与校验
2. 公共资源接口联调，验证轮播图、分类、自提点展示
3. 商品模块接口联调，验证商品列表、详情展示
4. 购物车模块接口联调，验证加购、修改、删除、批量删除
5. 订单模块接口联调，验证提交订单、订单列表、详情、取消、确认收货
6. 用户模块接口联调，验证用户信息、自提点管理

## 7. 代码生成规范
### 7.1 命名规范
- 类名：大驼峰（如BannerController、ProductService）
- 方法名：小驼峰（如getBanners、addCart）
- 变量名：小驼峰（如orderNo、productId）
- 表名：下划线+小写（如banner、order_info）
- 字段名：下划线+小写（如image_url、user_id）

### 7.2 代码结构
#### 前端
- 请求封装：utils/request.js统一封装wx.request，携带JWT token，统一处理登录失效
- 存储封装：utils/storage.js统一封装wx.setStorageSync、wx.getStorageSync、wx.removeStorageSync
#### 后端
- Controller：接收请求参数，校验参数，调用Service，返回Result
- Service：处理核心业务逻辑
- Mapper：使用MyBatis Plus，继承BaseMapper，按需自定义SQL
- Entity：对应数据库表
- DTO：接收前端请求参数
- VO：返回给前端的数据
- Enums：统一错误码、订单状态枚举

### 7.3 注释要求
- 接口：必须加@ApiOperation注解（Swagger可选，但注释必须包含参数、返回值、业务说明）
- 核心方法：必须加单行/多行注释，包含参数、返回值、业务说明
- 数据库表/字段：必须加注释

### 7.4 错误码定义
统一错误码枚举：
- SUCCESS=200，成功
- PARAM_ERROR=400，参数错误
- LOGIN_EXPIRE=401，登录已失效
- PERMISSION_DENIED=403，权限不足
- SYSTEM_ERROR=500，系统异常