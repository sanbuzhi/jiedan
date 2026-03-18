# 个人中心功能增强 产品规格文档

## Why
当前个人中心页面存在功能缺失和用户体验不足的问题：
1. 积分记录"查看全部"功能未实现
2. 缺少积分兑换功能，用户无法使用积累的积分
3. "分享赚积分"缺少规则说明弹窗
4. 需要明确推荐码生成时机和分享功能的商用方案

通过本次增强，提升个人中心的完整性和商业化能力。

## What Changes

### 1. 推荐码生成机制
- 用户首次登录小程序时自动生成推荐码
- 推荐码格式：6位字母数字组合（如：A3B7C9）
- 后端在用户注册时自动创建

### 2. 积分记录列表页（新增）
- 创建页面 `pages/points-list/points-list`
- 展示完整积分记录，支持分页加载
- 个人中心"查看全部"跳转至此页面

### 3. 积分兑换功能页（新增）
- 创建页面 `pages/points-exchange/points-exchange`
- 展示可兑换的平台物品/服务列表
- 支持积分+现金混合支付模式
- 个人中心增加"积分兑换"入口

### 4. 分享赚积分规则弹窗
- 点击"分享赚积分"显示规则弹窗
- 弹窗内容：积分获取规则 + 积分使用说明
- 点击弹窗外区域或关闭按钮隐藏

### 5. 微信小程序分享方案（商用）
- 使用微信小程序原生分享能力
- 生成带推荐码参数的小程序页面路径
- 支持分享到好友和朋友圈
- 被分享者打开后自动关联推荐关系

### 6. 推荐码二维码生成（新增）
- 生成带推荐码参数的小程序二维码
- 支持用户保存二维码到相册
- 二维码扫描后直接进入小程序首页并携带推荐码参数
- 适用于线下推广场景

## Impact
- **受影响的产品能力**：个人中心、积分系统、推荐系统
- **受影响的技术资产**：
  - 前端：miniprogram/pages/profile/, miniprogram/pages/points-list/, miniprogram/pages/points-exchange/
  - 后端：用户注册接口（推荐码生成）、积分兑换接口（新增）

## ADDED Requirements

### Requirement: 推荐码自动生成
The system SHALL automatically generate a unique referral code when a user first logs in to the mini program.

#### Scenario: 新用户首次登录
- **GIVEN** 用户首次使用微信授权登录小程序
- **WHEN** 登录成功
- **THEN** 系统自动生成6位推荐码
- **AND** 推荐码与用户账号永久绑定

### Requirement: 积分记录列表页
The system SHALL provide a dedicated page for viewing complete point records.

#### Scenario: 查看全部积分记录
- **GIVEN** 用户位于个人中心页面
- **WHEN** 用户点击"查看全部"积分记录
- **THEN** 跳转至积分记录列表页
- **AND** 显示分页加载的完整积分历史

### Requirement: 积分兑换功能
The system SHALL provide a points exchange page where users can redeem platform items/services.

#### Scenario: 进入积分兑换页
- **GIVEN** 用户位于个人中心页面
- **WHEN** 用户点击"积分兑换"入口
- **THEN** 跳转至积分兑换页面
- **AND** 展示可兑换物品列表

#### Scenario: 积分兑换物品
- **GIVEN** 用户位于积分兑换页面
- **AND** 用户积分充足或选择积分+现金混合支付
- **WHEN** 用户确认兑换
- **THEN** 扣除相应积分
- **AND** 如需要现金补充，跳转支付流程
- **AND** 生成兑换订单

#### Scenario: 积分抵扣开发费用
- **GIVEN** 用户有系统开发或功能改造需求
- **AND** 用户选择使用积分抵扣
- **THEN** 100积分 = 1元人民币抵扣
- **AND** 在订单结算时自动计算抵扣金额

### Requirement: 分享赚积分规则弹窗
The system SHALL display a modal with sharing rules when user clicks "分享赚积分".

#### Scenario: 查看分享规则
- **GIVEN** 用户位于个人中心页面
- **WHEN** 用户点击"分享赚积分"
- **THEN** 显示规则弹窗
- **AND** 展示积分获取规则和使用说明

#### Scenario: 关闭规则弹窗
- **GIVEN** 分享规则弹窗已显示
- **WHEN** 用户点击关闭按钮或弹窗外区域
- **THEN** 弹窗隐藏

### Requirement: 微信小程序分享功能（商用方案）
The system SHALL provide commercial-grade sharing functionality using WeChat Mini Program native capabilities.

#### Scenario: 分享给好友
- **GIVEN** 用户点击"分享"按钮
- **WHEN** 触发微信分享
- **THEN** 生成带推荐码的小程序卡片
- **AND** 被分享者点击后进入首页并自动关联推荐关系

#### Scenario: 分享到朋友圈
- **GIVEN** 用户选择分享到朋友圈
- **WHEN** 分享成功
- **THEN** 朋友圈显示小程序卡片
- **AND** 点击后进入小程序并关联推荐关系

### Requirement: 推荐码二维码生成
The system SHALL generate a mini program QR code with referral code parameter for offline promotion.

#### Scenario: 生成推荐码二维码
- **GIVEN** 用户位于个人中心页面
- **WHEN** 用户点击"生成二维码"或"分享赚积分"弹窗中的二维码选项
- **THEN** 调用微信接口生成小程序二维码
- **AND** 二维码包含推荐码参数
- **AND** 显示二维码预览弹窗

#### Scenario: 保存二维码到相册
- **GIVEN** 二维码预览弹窗已显示
- **WHEN** 用户点击"保存到相册"
- **THEN** 调用微信API保存图片
- **AND** 显示保存成功提示

#### Scenario: 扫描二维码进入小程序
- **GIVEN** 用户扫描推荐码二维码
- **WHEN** 二维码识别成功
- **THEN** 进入小程序首页
- **AND** 自动携带推荐码参数
- **AND** 新用户注册时关联推荐关系

## MODIFIED Requirements

### Requirement: 个人中心页面结构
**Previous**: 个人中心仅展示基本信息、推荐码、推荐树、积分记录
**Modified**: 新增"积分兑换"入口，更新"分享赚积分"交互

### Requirement: 查看全部积分跳转
**Previous**: 点击"查看全部"显示"功能开发中..."提示
**Modified**: 点击"查看全部"跳转至积分记录列表页

## REMOVED Requirements
None

## 技术架构说明

### 推荐码生成算法
- 格式：6位字母数字组合（大写字母A-Z + 数字0-9）
- 生成方式：随机生成 + 唯一性校验
- 存储：与用户表关联，字段 `referral_code`

### 积分兑换支付流程（简化版）
```
用户选择物品 → 计算所需积分/现金 → 确认兑换
    ↓
积分充足？ → 是 → 扣除积分 → 生成订单
    ↓ 否
计算现金差额 → 调起微信支付 → 支付成功 → 扣除积分 → 生成订单
```
**注意**: 积分兑换功能依赖微信支付，需配置商户号和支付密钥

### 微信小程序分享方案
1. **页面路径**: `pages/index/index?referral_code={推荐码}`
2. **关联机制**: 
   - 新用户打开带推荐码链接
   - 自动记录推荐关系到 `user_referrals` 表
   - 注册成功后给推荐人发放积分
3. **分享卡片**: 
   - 标题：推荐你使用接单助手，智能获客轻松接单！
   - 图片：自定义分享封面 `/images/share-cover.png`
   - 路径：带推荐码参数

### 推荐码二维码方案
1. **生成方式**: 调用微信 `wxacode.getUnlimited` 接口（后端调用）
2. **页面路径**: `pages/index/index`
3. **场景参数**: `scene=referral_code={推荐码}`
4. **前端实现**: 
   - 后端提供 `GET /api/v1/users/referral-qrcode` 接口
   - 返回二维码图片URL或Base64数据
   - 前端展示并支持保存到相册
5. **扫码处理**:
   - 首页 `onLoad` 解析 `scene` 参数
   - 提取推荐码并记录

## 潜在技术风险与解决方案

### 风险1: 微信二维码生成接口限制
**问题**: 微信 `wxacode.getUnlimited` 接口有调用频率限制（5000次/分钟）
**解决方案**: 
- 后端缓存用户二维码，避免重复生成
- 设置合理的缓存时间（如7天）
- 超限后降级提示用户稍后重试

### 风险2: 积分兑换支付流程复杂
**问题**: 积分+现金混合支付涉及微信支付集成，开发复杂度高
**解决方案**:
- **分阶段实施**: 第一阶段仅支持纯积分兑换，第二阶段再添加现金支付
- 或者使用简化方案：积分仅用于抵扣开发费用（在订单结算时使用），不单独做积分商城

### 风险3: 小程序码scene参数长度限制
**问题**: 微信scene参数最大32个字符，`referral_code=ABCDEF` 格式可能超长
**解决方案**:
- 使用短格式：`r=ABC123`（6字符）
- 或使用纯推荐码格式：`ABC123`（直接就是推荐码）

### 风险4: 推荐关系循环依赖
**问题**: A推荐B，B推荐C，C推荐A，形成循环
**解决方案**:
- 后端校验：新用户的推荐人不能是自己的下级
- 查询推荐树，检查是否形成闭环

### 建议的分阶段实施计划
**第一阶段（核心功能）**:
1. 推荐码自动生成
2. 积分记录列表页
3. 分享规则弹窗
4. 小程序分享功能

**第二阶段（增强功能）**:
1. 推荐码二维码生成
2. 积分兑换功能（纯积分兑换）

**第三阶段（高级功能）**:
1. 积分+现金混合支付
2. 更多兑换商品类型
