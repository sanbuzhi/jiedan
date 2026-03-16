# 美妆管理系统 - 产品需求文档 (PRD)

## 1. 项目概述

### 1.1 项目名称
美妆管理系统 (Beauty Management System)

### 1.2 项目描述
一套面向美妆行业的综合管理系统，支持客户端（消费者）和店家端（商家）双端操作。系统涵盖商品展示、订单管理、库存管理、财务统计等核心功能。

### 1.3 项目类型
多端应用：微信小程序（客户端）+ Web管理后台（店家端）+ SpringBoot后端

### 1.4 目标用户
- **客户端用户**：购买美妆产品的消费者
- **店家端用户**：美妆店铺管理员/店员

---

## 2. 功能需求

### 2.1 客户端功能（微信小程序）

#### 2.1.1 用户模块
- 用户注册/登录（微信授权登录）
- 个人信息管理
- 收货地址管理

#### 2.1.2 商品模块
- 商品分类浏览（护肤/彩妆/香水等）
- 商品搜索
- 商品详情查看（图片/价格/描述/库存）
- 商品收藏

#### 2.1.3 购物车模块
- 添加商品到购物车
- 修改购物车商品数量
- 删除购物车商品
- 购物车结算

#### 2.1.4 订单模块
- 创建订单
- 订单支付（微信支付）
- 订单状态查询（待付款/待发货/待收货/已完成）
- 订单取消
- 申请退款

### 2.2 店家端功能（Web管理后台）

#### 2.2.1 商品管理（进销存）
- 商品信息管理（增删改查）
- 商品分类管理
- 商品上下架
- 库存管理（入库/出库/盘点）
- 库存预警（低库存提醒）

#### 2.2.2 订单管理
- 订单列表查看
- 订单状态更新（接单/发货/完成）
- 订单筛选（按状态/时间/用户）
- 订单详情查看
- 退款处理

#### 2.2.3 小票打印
- 销售小票生成
- 小票模板配置
- 连接打印机打印
- 小票历史记录

#### 2.2.4 收支流水
- 收入记录（销售收入）
- 支出记录（采购成本/运营成本）
- 流水分类管理
- 流水查询统计

#### 2.2.5 盈亏报表
- 日报/周报/月报
- 销售收入统计
- 成本支出统计
- 利润计算
- 图表展示（柱状图/折线图/饼图）

#### 2.2.6 系统管理
- 店铺信息管理
- 员工账号管理
- 权限控制

---

## 3. 数据库设计

### 3.1 用户表 (users)
```sql
- id: BIGINT PRIMARY KEY
- openid: VARCHAR(100) UNIQUE  -- 微信openid
- nickname: VARCHAR(100)       -- 昵称
- avatar: VARCHAR(500)         -- 头像URL
- phone: VARCHAR(20)           -- 手机号
- type: VARCHAR(20)            -- 类型: customer/shopkeeper
- created_at: DATETIME
- updated_at: DATETIME
```

### 3.2 商品表 (products)
```sql
- id: BIGINT PRIMARY KEY
- name: VARCHAR(200)           -- 商品名称
- category_id: BIGINT          -- 分类ID
- price: DECIMAL(10,2)         -- 售价
- cost_price: DECIMAL(10,2)    -- 成本价
- stock: INT                   -- 库存数量
- description: TEXT            -- 商品描述
- images: JSON                 -- 图片列表
- status: VARCHAR(20)          -- 状态: on_sale/off_sale
- created_at: DATETIME
- updated_at: DATETIME
```

### 3.3 商品分类表 (categories)
```sql
- id: BIGINT PRIMARY KEY
- name: VARCHAR(100)           -- 分类名称
- parent_id: BIGINT            -- 父分类ID
- sort_order: INT              -- 排序
- created_at: DATETIME
```

### 3.4 订单表 (orders)
```sql
- id: BIGINT PRIMARY KEY
- order_no: VARCHAR(50) UNIQUE -- 订单编号
- user_id: BIGINT              -- 用户ID
- total_amount: DECIMAL(10,2)  -- 订单总金额
- status: VARCHAR(20)          -- 状态: pending/paid/shipped/completed/cancelled
- pay_type: VARCHAR(20)        -- 支付方式: wechat
- pay_time: DATETIME           -- 支付时间
- ship_time: DATETIME          -- 发货时间
- receive_time: DATETIME       -- 收货时间
- address: JSON                -- 收货地址
- remark: VARCHAR(500)         -- 备注
- created_at: DATETIME
- updated_at: DATETIME
```

### 3.5 订单商品表 (order_items)
```sql
- id: BIGINT PRIMARY KEY
- order_id: BIGINT             -- 订单ID
- product_id: BIGINT           -- 商品ID
- product_name: VARCHAR(200)   -- 商品名称
- product_image: VARCHAR(500)  -- 商品图片
- price: DECIMAL(10,2)         -- 单价
- quantity: INT                -- 数量
- subtotal: DECIMAL(10,2)      -- 小计
```

### 3.6 库存记录表 (inventory_records)
```sql
- id: BIGINT PRIMARY KEY
- product_id: BIGINT           -- 商品ID
- type: VARCHAR(20)            -- 类型: in/out/check
- quantity: INT                -- 数量
- before_stock: INT            -- 变动前库存
- after_stock: INT             -- 变动后库存
- remark: VARCHAR(500)         -- 备注
- operator_id: BIGINT          -- 操作人ID
- created_at: DATETIME
```

### 3.7 收支流水表 (transactions)
```sql
- id: BIGINT PRIMARY KEY
- type: VARCHAR(20)            -- 类型: income/expense
- category: VARCHAR(50)        -- 分类
- amount: DECIMAL(10,2)        -- 金额
- related_order_id: BIGINT     -- 关联订单ID
- remark: VARCHAR(500)         -- 备注
- operator_id: BIGINT          -- 操作人ID
- created_at: DATETIME
```

### 3.8 店铺表 (shops)
```sql
- id: BIGINT PRIMARY KEY
- name: VARCHAR(200)           -- 店铺名称
- logo: VARCHAR(500)           -- 店铺Logo
- address: VARCHAR(500)        -- 地址
- phone: VARCHAR(20)           -- 联系电话
- business_hours: VARCHAR(100) -- 营业时间
- status: VARCHAR(20)          -- 状态: active/inactive
- created_at: DATETIME
- updated_at: DATETIME
```

---

## 4. 接口设计

### 4.1 客户端API

#### 用户相关
- POST /api/client/login - 微信登录
- GET /api/client/user/info - 获取用户信息
- PUT /api/client/user/info - 更新用户信息

#### 商品相关
- GET /api/client/products - 商品列表
- GET /api/client/products/{id} - 商品详情
- GET /api/client/categories - 商品分类

#### 购物车相关
- POST /api/client/cart - 添加购物车
- GET /api/client/cart - 获取购物车
- PUT /api/client/cart/{id} - 更新购物车
- DELETE /api/client/cart/{id} - 删除购物车

#### 订单相关
- POST /api/client/orders - 创建订单
- GET /api/client/orders - 订单列表
- GET /api/client/orders/{id} - 订单详情
- POST /api/client/orders/{id}/pay - 支付订单
- POST /api/client/orders/{id}/cancel - 取消订单

### 4.2 店家端API

#### 商品管理
- GET /api/admin/products - 商品列表
- POST /api/admin/products - 创建商品
- PUT /api/admin/products/{id} - 更新商品
- DELETE /api/admin/products/{id} - 删除商品

#### 库存管理
- POST /api/admin/inventory/in - 入库
- POST /api/admin/inventory/out - 出库
- GET /api/admin/inventory/records - 库存记录

#### 订单管理
- GET /api/admin/orders - 订单列表
- GET /api/admin/orders/{id} - 订单详情
- PUT /api/admin/orders/{id}/status - 更新订单状态

#### 财务管理
- GET /api/admin/transactions - 收支流水
- POST /api/admin/transactions - 记录收支
- GET /api/admin/reports/summary - 汇总报表
- GET /api/admin/reports/daily - 日报
- GET /api/admin/reports/monthly - 月报

#### 小票打印
- POST /api/admin/orders/{id}/receipt - 生成小票
- POST /api/admin/receipts/print - 打印小票

---

## 5. 业务流程

### 5.1 客户端下单流程
1. 用户浏览商品 → 选择商品 → 加入购物车
2. 进入购物车 → 确认商品 → 点击结算
3. 选择收货地址 → 确认订单信息 → 提交订单
4. 调起微信支付 → 支付成功 → 订单状态变为"待发货"
5. 店家发货 → 用户收货 → 订单完成

### 5.2 店家端处理流程
1. 接收新订单通知
2. 查看订单详情 → 确认库存 → 接单
3. 打印小票 → 配货 → 发货
4. 更新订单状态为"已发货"
5. 用户确认收货 → 订单完成

### 5.3 库存管理流程
1. 商品入库：采购商品 → 录入入库单 → 库存增加
2. 商品出库：销售商品 → 自动扣减库存
3. 库存盘点：定期盘点 → 调整库存数量
4. 库存预警：库存低于阈值 → 系统提醒 → 补货

---

## 6. 非功能需求

### 6.1 性能要求
- 页面加载时间 < 3秒
- API响应时间 < 500ms
- 支持并发用户 1000+

### 6.2 安全要求
- 用户密码加密存储
- API接口鉴权
- 支付安全（微信支付SDK）
- 敏感数据脱敏

### 6.3 可用性要求
- 系统可用性 99.9%
- 数据备份每日一次
- 故障恢复时间 < 30分钟

---

## 7. 技术栈

### 7.1 后端
- Java 17
- SpringBoot 3.2
- MySQL 8.0
- Redis
- MyBatis-Plus

### 7.2 前端
- 客户端：微信小程序
- 店家端：Vue3 + Element Plus

### 7.3 部署
- Docker
- Nginx
- Jenkins

---

**文档版本**: 1.0  
**创建日期**: 2024-03-14  
**作者**: AI开发助手
