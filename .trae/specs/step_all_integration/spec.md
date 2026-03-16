# 需求智能采集与AI报价系统 产品规格文档

## Why
当前的需求采集流程分散在多个离散页面（step1-7），用户需要频繁进行页面上下文切换，导致操作链路冗长、转化率受损。通过构建一体化的**需求智能采集中心**，将"需求智能解析"、"交互视觉定制"、"AI智能报价"三大核心模块整合至统一工作台，可实现：
1. 打造沉浸式的需求采集体验，降低用户认知负荷
2. 压缩操作路径，提升需求提交转化率
3. 支持从项目工作台直接唤起需求编辑，实现需求迭代闭环
4. 深度融合AI能力，基于自然语言描述智能识别业务场景并推荐功能矩阵

## What Changes

### 1. 项目工作台（Index）交互优化
- **BREAKING**: 需求澄清节点（节点1）的点击跳转目标从预算明细页（step6）迁移至需求智能采集中心（step_all）
- 保留节点1处于激活态（active）时的点击交互行为

### 2. 需求智能采集中心（Step_All）
- 构建全新的需求智能采集中心页面
- 沿用既有 step 页面的经典布局架构：顶部固定导航（header-fixed）+ 进度指示器（progress-bar）+ 内容滚动区（content-scroll）
- 内置三大核心模块：需求智能解析、交互视觉定制、AI智能报价

### 3. 模块一：需求智能解析（Progress Step 1）
- **业务场景描述区**：多行文本输入框（必填项），支持用户以自然语言描述业务诉求
- **智能功能识别引擎**：
  - 触发按钮"智能识别功能点"：调用后端语义分析接口，基于NLP技术提取关键词并匹配业务场景
  - 展示业务角色矩阵与功能点清单（支持多选，默认未选中状态）
  - 成本智能提示："功能点规模与开发投入正相关"
- **功能点自定义扩展**：每个业务角色支持追加自定义功能点，满足个性化需求
- **需求物料交付区**：
  - 参考素材库：支持批量上传参考图片（上限5张）
  - 源码资产包：支持上传源码压缩包（.zip格式）
  - 代码仓库链接：GitHub/GitLab仓库地址录入

### 4. 后端智能匹配服务
- **API端点**：`GET /api/v1/system-templates/search?keyword={keyword}`
- **核心能力**：基于语义相似度算法，从业务场景库中智能匹配系统架构模板
- **响应 payload**：返回系统架构模板、业务角色清单、功能点矩阵

### 5. 模块二：交互视觉定制（Progress Step 2）
- 复用既有 step7 的交互视觉配置能力
- 新增**交互视觉资源库**页面，展示海量UI设计范式
- 服务承诺提示："视觉方案支持3轮免费迭代优化"
- 支持从资源库选择后回传至采集中心

### 6. 模块三：AI智能报价（Progress Step 3）
- 复用既有 step6 的预算明细展示能力
- 基于用户录入的全量需求数据（业务描述、功能点矩阵、视觉方案）调用AI大模型进行智能估价
- **费用构成智能拆分**：
  - AI研发成本（核心开发投入）
  - 基础设施成本（云资源、域名、SSL等，仅当用户选择"需要上线部署"时展示）

### 7. 数据模型扩展
- **system_templates**（系统架构模板库）：存储标准化系统架构（酒店PMS、电商平台、CRM等）
- **system_roles**（业务角色库）：存储角色定义与职责描述
- **system_functions**（功能点库）：存储功能点元数据（复杂度分级、预估工时、基准报价）
- **requirement_functions**（需求功能关联表）：记录需求与功能点的多对多关联关系
- **requirements**（需求主表）字段扩展：
  - requirement_description（业务场景描述）
  - selected_functions（已选功能点快照）
  - materials（需求物料包：图片数组、源码包、仓库地址）
  - need_online（上线部署标识）

## Impact
- **受影响的产品能力**：需求采集流程、智能估价算法、数据持久化层
- **受影响的技术资产**：
  - 前端：miniprogram/pages/index/index.js, miniprogram/pages/requirement/step_all/
  - 后端：RequirementController, RequirementService, 新增 Entity/Repository 层

## ADDED Requirements

### Requirement: 项目工作台节点跳转
The system SHALL support clicking the "需求澄清" node to navigate to the requirement intelligence collection center when the node is in active status.

#### Scenario: 需求澄清节点激活态点击
- **GIVEN** 用户位于项目工作台（Index）
- **AND** 需求澄清节点（节点1）状态为 "active"
- **WHEN** 用户点击该节点
- **THEN** 跳转至需求智能采集中心（step_all）并携带项目ID参数

### Requirement: 需求智能采集中心页面架构
The system SHALL provide a unified requirement intelligence collection center with a 3-stage progressive workflow.

#### Scenario: 页面初始化加载
- **GIVEN** 用户导航至需求智能采集中心
- **WHEN** 页面完成加载
- **THEN** 渲染顶部固定导航（header-fixed）含三阶段进度指示器
- **AND** 渲染内容滚动区（content-scroll）展示当前阶段内容

### Requirement: 需求智能解析模块
The system SHALL provide an intelligent requirement parsing module with NLP-based function recognition.

#### Scenario: 业务场景描述录入
- **GIVEN** 用户位于需求智能解析阶段
- **WHEN** 用户在业务场景描述区录入文本
- **THEN** 启用"智能识别功能点"触发按钮

#### Scenario: 智能功能点识别
- **GIVEN** 用户已完成业务场景描述
- **WHEN** 用户触发"智能识别功能点"操作
- **THEN** 调用后端语义分析接口进行关键词提取与场景匹配
- **AND** 展示匹配的业务角色矩阵与功能点清单
- **AND** 所有功能点默认处于未选中状态

#### Scenario: 成本智能提示
- **GIVEN** 业务角色与功能点矩阵已展示
- **THEN** 展示成本提示："功能点规模与开发投入正相关，请按需选择"

#### Scenario: 功能点自定义扩展
- **GIVEN** 用户正在浏览某业务角色的功能点清单
- **WHEN** 用户触发"追加功能点"操作
- **THEN** 弹出输入控件接收自定义功能点描述
- **AND** 确认后将自定义功能点追加至该角色清单并标记为自定义类型

#### Scenario: 需求物料交付
- **GIVEN** 用户位于需求智能解析阶段
- **THEN** 展示需求物料交付区：
  - 参考素材库：支持多选图片上传（上限5张）
  - 源码资产包：支持单选.zip格式文件上传
  - 代码仓库链接：文本输入框接收GitHub/GitLab地址

### Requirement: 后端智能匹配服务
The system SHALL provide a semantic matching service for system architecture templates based on user input keywords.

#### Scenario: 关键词语义匹配
- **GIVEN** 用户输入关键词"酒店管理系统"
- **WHEN** 调用智能匹配接口
- **THEN** 返回系统架构模板"酒店PMS系统"
- **AND** 返回业务角色清单：[系统管理员、前台接待、客房服务、财务核算]
- **AND** 返回各角色关联的功能点矩阵

#### Scenario: 无匹配场景处理
- **GIVEN** 用户输入关键词未匹配任何系统架构模板
- **WHEN** 调用智能匹配接口
- **THEN** 返回空角色清单
- **AND** 提供手动录入入口支持用户自主定义

### Requirement: 交互视觉定制模块
The system SHALL provide an interactive visual customization module with an extended design resource library.

#### Scenario: 视觉方案选择
- **GIVEN** 用户位于交互视觉定制阶段
- **WHEN** 用户选择某视觉方案
- **THEN** 高亮展示已选方案
- **AND** 展示服务承诺："视觉方案支持3轮免费迭代优化"

#### Scenario: 视觉资源库浏览
- **GIVEN** 用户位于交互视觉定制阶段
- **WHEN** 用户触发"浏览更多方案"操作
- **THEN** 跳转至交互视觉资源库页面
- **AND** 支持从资源库选择方案并回传至采集中心

### Requirement: AI智能报价模块
The system SHALL provide an AI-powered intelligent quotation module based on comprehensive requirement data.

#### Scenario: 智能估价计算
- **GIVEN** 用户已完成需求智能解析与交互视觉定制
- **WHEN** 用户进入AI智能报价阶段
- **THEN** 调用AI大模型接口进行智能估价
- **AND** 展示详细的费用构成明细

#### Scenario: AI研发成本展示
- **GIVEN** 智能估价已完成
- **THEN** 展示AI研发成本（基于已选功能点规模计算）

#### Scenario: 基础设施成本条件展示
- **GIVEN** 用户在需求数据中标记"需要上线部署"
- **THEN** 展示基础设施成本（云服务器、域名、SSL证书等）
- **ELSE** 隐藏基础设施成本模块

### Requirement: AI大模型集成
The system SHALL integrate with large language models for intelligent requirement analysis and quotation.

#### Scenario: AI智能估价请求
- **GIVEN** 全量需求数据已采集（业务场景描述、功能点矩阵、视觉方案）
- **WHEN** 调用AI智能估价接口
- **THEN** 向AI大模型发送结构化需求数据
- **AND** 接收并解析AI返回的标准化估价结果

## MODIFIED Requirements

### Requirement: 项目工作台节点跳转目标
**Previous**: 点击需求澄清节点跳转至预算明细页（step6）
**Modified**: 点击需求澄清节点跳转至需求智能采集中心（step_all）

## REMOVED Requirements
None - 既有功能能力全部保留并整合升级。
