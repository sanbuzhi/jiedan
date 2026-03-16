# 美妆店后台管理系统技术任务书

## 1. 项目技术规格

### 1.1 技术栈
- 前端：Vue3 + Element Plus
- 后端：Spring Boot 2.7+
- 数据库：MySQL 8.0
- 缓存：Redis

## 2. 前端页面开发清单

### 2.1 商品管理模块
- 页面：商品列表页（/product/list）
  - 功能：分页查询、条件筛选、批量操作
  - 组件：搜索表单、数据表格、分页组件
  - 接口：GET /api/products

- 页面：商品编辑页（/product/edit）
  - 功能：商品信息编辑、图片上传、SKU配置
  - 接口：POST /api/products, PUT /api/products/{id}

### 2.2 订单管理模块
- 页面：订单列表页（/order/list）
  - 功能：订单查询、状态筛选、发货处理
  - 接口：GET /api/orders

### 2.3 会员管理模块
- 页面：会员列表页（/member/list）
  - 功能：会员查询、等级管理、积分调整
  - 接口：GET /api/members

## 3. 后端接口开发清单

### 3.1 商品接口
- GET /api/products - 查询商品列表
  - 参数：page, size, keyword, categoryId
  - 响应：{code, data: {list, total}, message}

- POST /api/products - 创建商品
  - 参数：name, price, stock, categoryId, images
  - 响应：{code, data: productId, message}

- PUT /api/products/{id} - 更新商品
  - 参数：name, price, stock, status

### 3.2 订单接口
- GET /api/orders - 查询订单列表
- GET /api/orders/{id} - 查询订单详情
- POST /api/orders/{id}/ship - 订单发货

### 3.3 会员接口
- GET /api/members - 查询会员列表
- PUT /api/members/{id}/level - 修改会员等级

## 4. 数据库表结构设计

### 4.1 商品表（product）
```sql
CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL COMMENT '商品名称',
  price DECIMAL(10,2) NOT NULL COMMENT '价格',
  stock INT DEFAULT 0 COMMENT '库存',
  category_id BIGINT COMMENT '分类ID',
  status TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 4.2 订单表（order）
```sql
CREATE TABLE `order` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
  member_id BIGINT COMMENT '会员ID',
  total_amount DECIMAL(10,2) COMMENT '订单金额',
  status TINYINT DEFAULT 0 COMMENT '状态',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 5. 开发执行顺序

1. 阶段1：数据库表创建
2. 阶段2：后端接口开发（商品→订单→会员）
3. 阶段3：前端页面开发
4. 阶段4：接口联调
5. 阶段5：功能测试

## 6. 代码生成规范

- 命名规范：类名大驼峰，方法名小驼峰
- 接口返回统一格式：Result<T>
- 异常处理：全局异常处理器
