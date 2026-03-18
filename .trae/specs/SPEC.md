# AI自助开发系统 - 产品规格文档

## 项目概述

### 系统目标
将现有的智能获客系统改造为AI自助开发系统，小程序作为用户唯一入口，提供从需求引导到开发交付的完整流程。

### 系统架构
- **后端**: Spring Boot REST API
- **小程序端**: 微信小程序（用户入口）
- **管理后台**: Vue.js 管理后台
- **网页前端**: 客户展示页面

---

## 功能规格

### 1. 用户模块

#### 1.1 微信登录
- 小程序微信授权登录
- JWT Token 认证
- 用户信息存储

#### 1.2 登录状态检查
- 所有页面（除登录页、引导页）需检查登录状态
- 未登录跳转到登录页面

---

### 2. 需求引导模块（Step 1-5）

#### Step 1: 选择项目角色
- 选项：公司/企业、个人经营者、学生（毕设/大作业）、不明确、其他
- 其他选项显示手填输入框

#### Step 2: 选择项目类型
- 选项：微信小程序、抖音小程序、网站系统、爬虫/数据采集、不明确、其他
- 其他选项显示手填输入框

#### Step 3: 是否需要上线
- 开关选择是否需要上线部署
- 显示上线服务费用明细

#### Step 4: 预估系统流量
- 系统总人数输入
- 日活用户数输入
- 系统并发数输入
- 根据输入计算服务器配置建议

#### Step 5: 选择交付周期
- 紧急程度：宽松、正常、加急、特急
- 交付周期：7天、14天、30天、60天、90天
- 加急费用计算

---

### 3. 项目管理模块

#### 3.1 项目流程节点（11个节点）
1. 明确需求
2. AI明确需求
3. 客户验收
4. AI拆分任务
5. AI开发
6. AI代码审查
7. AI测试
8. 客户验收
9. 部署上线
10. AI安全测试
11. 项目完成

#### 3.2 节点状态
- **completed**: 已完成（绿色）
- **active**: 进行中（蓝色，带动画）
- **pending**: 待处理（灰色）

#### 3.3 AI节点动态图标
- 节点4(AI拆分任务)、5(AI开发)、6(AI代码审查)、7(AI测试)、10(AI安全测试)为AI节点
- AI节点处于active状态时显示动态🤖图标

#### 3.4 节点点击提示
- current_flow_node=1: 节点1显示"点击调整"
- current_flow_node=3,8,10: 节点3,8,10显示"点击验收"

#### 3.5 删除项目
- 仅当节点1(active)且status=draft时显示删除按钮
- 删除前弹出确认对话框

---

### 4. 预算计算模块

#### 4.1 AI开发费用
- 根据项目类型计算基础费用
- 根据流量预估计算扩展费用
- 根据紧急程度计算加急费用

#### 4.2 平台服务费
- 固定比例或固定金额

#### 4.3 总预算
- AI开发费用 + 平台服务费

---

### 5. 订单模块

#### 5.1 订单创建
- 根据需求创建订单

#### 5.2 订单状态
- pending（待支付）
- paid（已支付）
- processing（处理中）
- completed（已完成）
- cancelled（已取消）
- refunded（已退款）

---

### 6. 数据统计模块

#### 6.1 仪表盘数据
- 总点击量
- 总注册数
- 总成交量
- 总收入
- 最近需求列表
- 最近订单列表

---

## API 接口

### 认证接口
- `POST /api/v1/auth/wechat-login` - 微信登录

### 需求接口
- `POST /api/v1/requirements` - 创建需求
- `GET /api/v1/requirements` - 获取需求列表（分页）
- `GET /api/v1/requirements/{id}` - 获取需求详情
- `PUT /api/v1/requirements/{id}` - 更新需求
- `DELETE /api/v1/requirements/{id}` - 删除需求
- `GET /api/v1/requirements/{id}/budget` - 计算预算
- `POST /api/v1/requirements/{id}/step-data` - 保存步骤数据
- `GET /api/v1/requirements/{id}/step-data` - 获取步骤数据

### 订单接口
- `POST /api/v1/orders` - 创建订单
- `GET /api/v1/orders` - 获取订单列表
- `GET /api/v1/orders/{id}` - 获取订单详情
- `PUT /api/v1/orders/{id}/pay` - 支付订单
- `PUT /api/v1/orders/{id}/refund` - 退款订单
- `PUT /api/v1/orders/{id}/cancel` - 取消订单

### 统计接口
- `GET /api/v1/analytics/dashboard` - 获取仪表盘数据

---

## 数据库设计

### Requirement 表（需求表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| user_id | Long | 用户ID |
| user_type | String | 用户类型 |
| user_type_other | String | 其他用户类型 |
| project_type | String | 项目类型 |
| project_type_other | String | 其他项目类型 |
| need_online | Boolean | 是否需要上线 |
| urgency | String | 紧急程度 |
| delivery_period | Integer | 交付周期(天) |
| ui_style | String | UI风格 |
| traffic | JSON | 流量预估 |
| current_flow_node | Integer | 当前流程节点 |
| status | String | 状态 |
| created_at | Timestamp | 创建时间 |
| updated_at | Timestamp | 更新时间 |

### Order 表（订单表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| user_id | Long | 用户ID |
| requirement_id | Long | 需求ID |
| amount | BigDecimal | 金额 |
| status | String | 状态 |
| payment_type | String | 支付类型 |
| created_at | Timestamp | 创建时间 |
| updated_at | Timestamp | 更新时间 |

---

## 前端页面结构

### 小程序端
- `pages/guide/guide` - 引导页
- `pages/login/login` - 登录页
- `pages/index/index` - 首页（我的项目）
- `pages/requirement/step/step` - 统一需求引导页（Step 1-5）
- `pages/profile/profile` - 个人中心

### 管理后台
- `/` - 首页/仪表盘
- `/requirements` - 需求管理
- `/orders` - 订单管理

---

## 验收标准

### 核心功能
- [ ] 用户可以通过微信登录
- [ ] 未登录用户访问受限页面会跳转到登录页
- [ ] 用户可以完成5步需求引导
- [ ] 需求提交后创建项目并跳转到首页
- [ ] 首页显示项目流程节点
- [ ] active节点居中显示
- [ ] 节点1为active且status为draft时显示删除按钮
- [ ] 可以删除项目

### 流程节点
- [ ] 节点使用current_flow_node计算状态
- [ ] 前置节点显示completed状态
- [ ] 当前节点显示active状态
- [ ] 后续节点显示pending状态
- [ ] AI节点active时显示动态🤖图标
- [ ] 特定节点显示点击提示

### 样式
- [ ] 项目卡片使用科技风深色主题
- [ ] 流程节点水平滚动
- [ ] completed节点绿色
- [ ] active节点蓝色
- [ ] pending节点灰色
