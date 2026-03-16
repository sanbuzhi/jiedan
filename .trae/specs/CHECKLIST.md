# AI自助开发系统 - 验证清单

## 用户模块

- [ ] 微信登录成功
- [ ] Token 正确存储
- [ ] 未登录访问受限页面跳转到登录页
- [ ] 登录页跳转到首页（已登录）

## 需求引导模块

- [ ] Step 1: 选择项目角色
  - [ ] 所有选项可选择
  - [ ] 其他选项显示输入框
  - [ ] 数据正确保存

- [ ] Step 2: 选择项目类型
  - [ ] 所有选项可选择
  - [ ] 其他选项显示输入框
  - [ ] 数据正确保存

- [ ] Step 3: 是否需要上线
  - [ ] 开关功能正常
  - [ ] 显示费用明细

- [ ] Step 4: 预估系统流量
  - [ ] 输入框功能正常
  - [ ] 服务器配置建议显示

- [ ] Step 5: 选择交付周期
  - [ ] 紧急程度选择正常
  - [ ] 交付周期选择正常
  - [ ] 加急费用显示

- [ ] Step 5: 点击完成
  - [ ] 调用后端API创建项目
  - [ ] 创建成功后跳转到首页
  - [ ] 失败显示错误提示

## 项目管理模块

- [ ] 首页显示项目列表
- [ ] 项目卡片显示流程节点
- [ ] 流程节点水平滚动
- [ ] active节点居中显示
- [ ] 节点状态正确显示（completed/active/pending）
- [ ] AI节点active时显示动态🤖图标
- [ ] 特定节点显示点击提示
- [ ] 节点1为active且status为draft时显示删除按钮
- [ ] 删除功能正常（弹出确认、调用API、刷新列表）

## 预算计算模块

- [ ] 预算计算接口正常
- [ ] 费用明细显示正确

## 订单模块

- [ ] 订单创建正常
- [ ] 订单状态更新正常
- [ ] 支付回调处理正常

## 样式验证

- [ ] 项目卡片使用科技风深色主题
- [ ] completed节点绿色
- [ ] active节点蓝色
- [ ] pending节点灰色
- [ ] 删除按钮红色

## API 验证

- [ ] POST /api/v1/auth/wechat-login
- [ ] POST /api/v1/requirements
- [ ] GET /api/v1/requirements
- [ ] GET /api/v1/requirements/{id}
- [ ] PUT /api/v1/requirements/{id}
- [ ] DELETE /api/v1/requirements/{id}
- [ ] GET /api/v1/requirements/{id}/budget
- [ ] POST /api/v1/orders
- [ ] GET /api/v1/orders
- [ ] GET /api/v1/orders/{id}
- [ ] PUT /api/v1/orders/{id}/pay
- [ ] GET /api/v1/analytics/dashboard
