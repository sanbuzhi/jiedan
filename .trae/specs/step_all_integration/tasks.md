# 需求智能采集与AI报价系统 实施任务清单

## 任务依赖拓扑
```
任务1、2、3 ──┬──► 任务4 ──► 任务5 ──► 任务6 ──┬──► 任务9
             │                              │
             └──► 任务7 ──► 任务8 ───────────┘

任务10（集成测试）依赖所有前置任务完成
```

## 任务详情

### 任务1: 数据持久化层设计 ✅
**任务描述**: 构建系统架构模板库、业务角色库、功能点库的数据持久化层
- [x] 1.1 创建 **system_templates**（系统架构模板库）
  - 字段：id, name, code, description, keywords(JSON), category, complexity_level, is_active, created_at, updated_at
  - 说明：存储标准化系统架构（酒店PMS、电商平台、CRM、ERP等）
- [x] 1.2 创建 **system_roles**（业务角色库）
  - 字段：id, template_id, name, code, description, responsibilities, sort_order, created_at, updated_at
  - 说明：存储业务角色定义与职责边界
- [x] 1.3 创建 **system_functions**（功能点库）
  - 字段：id, role_id, name, code, description, complexity(LOW/MEDIUM/HIGH), estimated_hours, base_price, priority, created_at, updated_at
  - 说明：存储功能点元数据（复杂度分级、预估工时、基准报价）
- [x] 1.4 创建 **requirement_functions**（需求功能关联表）
  - 字段：id, requirement_id, function_id, is_custom, custom_name, custom_description, estimated_hours, created_at
  - 说明：记录需求与功能点的多对多关联关系，支持自定义功能点
- [x] 1.5 扩展 **requirements**（需求主表）
  - 新增字段：
    - requirement_description (TEXT) - 业务场景详细描述
    - selected_functions (JSON) - 已选功能点快照
    - materials (JSON) - 需求物料包：{reference_images: [], source_code_package: '', repository_url: ''}
    - deployment_mode (ENUM: 'cloud', 'on_premise', 'hybrid', 'none') - 部署模式
    - ai_quotation_result (JSON) - AI智能估价结果缓存

### 任务2: 领域模型与数据访问层构建 ✅
**任务描述**: 创建JPA实体与Repository接口，建立领域模型
- [x] 2.1 创建 **SystemTemplate.java** 实体
  - 注解：@Entity, @Table, @Data, @Builder
  - 关联：@OneToMany(mappedBy = "template") 关联 SystemRole
- [x] 2.2 创建 **SystemRole.java** 实体
  - 注解：@Entity, @Table, @Data, @Builder
  - 关联：@ManyToOne 关联 SystemTemplate，@OneToMany 关联 SystemFunction
- [x] 2.3 创建 **SystemFunction.java** 实体
  - 注解：@Entity, @Table, @Data, @Builder
  - 枚举：ComplexityLevel(LOW, MEDIUM, HIGH)
  - 关联：@ManyToOne 关联 SystemRole
- [x] 2.4 创建 **RequirementFunction.java** 实体（关联表）
  - 注解：@Entity, @Table, @Data, @Builder
  - 关联：@ManyToOne 关联 Requirement，@ManyToOne 关联 SystemFunction
- [x] 2.5 创建 **SystemTemplateRepository.java**
  - 继承 JpaRepository<SystemTemplate, Long>
  - 方法：findByKeywordsContaining, findByIsActiveTrue, findByCategory
- [x] 2.6 创建 **SystemRoleRepository.java**
  - 继承 JpaRepository<SystemRole, Long>
  - 方法：findByTemplateIdOrderBySortOrder
- [x] 2.7 创建 **SystemFunctionRepository.java**
  - 继承 JpaRepository<SystemFunction, Long>
  - 方法：findByRoleIdOrderByPriority, findByComplexity
- [x] 2.8 创建 **RequirementFunctionRepository.java**
  - 继承 JpaRepository<RequirementFunction, Long>
  - 方法：findByRequirementId, deleteByRequirementId
- [x] 2.9 更新 **Requirement.java** 实体
  - 添加新字段映射
  - 关联：@OneToMany(mappedBy = "requirement") 关联 RequirementFunction

### 任务3: 智能匹配服务开发 ✅
**任务描述**: 实现基于语义相似度的系统架构模板智能匹配服务
- [x] 3.1 创建 **SystemTemplateService.java**
  - 方法：searchByKeyword(String keyword)
  - 算法：基于关键词的模糊匹配（LIKE %keyword%）+ 相关性排序
  - 方法：getTemplateDetail(Long templateId)
  - 说明：获取模板完整信息（含角色与功能点矩阵）
- [x] 3.2 创建 **SystemTemplateController.java**
  - 端点：GET /api/v1/system-templates/search?keyword={keyword}
  - 响应：SystemTemplateSearchResponse
  - 端点：GET /api/v1/system-templates/{id}
  - 响应：SystemTemplateDetailResponse
- [x] 3.3 创建 **DTO 传输对象**
  - SystemTemplateSearchResponse.java - 搜索结果响应
  - SystemTemplateDetailResponse.java - 模板详情响应
  - SystemRoleDto.java - 业务角色DTO
  - SystemFunctionDto.java - 功能点DTO
- [x] 3.4 初始化种子数据
  - 酒店PMS系统模板（含管理员、前台、客房服务、财务等角色及功能点）
  - 电商平台模板（含买家、卖家、平台运营等角色及功能点）
  - CRM系统模板（含销售、客服、管理层等角色及功能点）

### 任务4: AI智能估价引擎开发 ✅
**任务描述**: 构建AI大模型驱动的智能估价引擎
- [x] 4.1 创建 **AiQuotationService.java**
  - 方法：generateQuotation(Long requirementId, QuotationRequest request)
  - 功能：
    - 构建AI提示词模板（包含业务场景、功能点矩阵、视觉方案、部署模式）
    - 调用AI大模型API（预留接口，支持OpenAI/文心一言/通义千问等）
    - 解析AI返回的JSON格式估价结果
    - 缓存估价结果至 requirements.ai_quotation_result
  - 方法：calculateBasePrice(List<SelectedFunction> functions)
  - 说明：基于功能点复杂度计算基础研发成本
- [x] 4.2 创建 **AiQuotationController.java**
  - 端点：POST /api/v1/requirements/{id}/ai-quotation
  - 请求：AiQuotationRequest（功能点ID列表、视觉方案、部署模式等）
  - 响应：AiQuotationResponse（费用构成明细、总预算、交付周期预估）
  - 端点：GET /api/v1/requirements/{id}/ai-quotation
  - 说明：获取缓存的估价结果
- [x] 4.3 创建 **DTO 传输对象**
  - AiQuotationRequest.java - 估价请求
  - AiQuotationResponse.java - 估价响应
  - QuotationBreakdown.java - 费用构成明细（AI研发成本、平台服务费、基础设施成本）
  - DevelopmentPhaseCost.java - 各研发阶段成本（需求分析、架构设计、程序开发、测试验收、性能优化、安全加固）
- [x] 4.4 实现智能估价算法
  - 基础费用 = Σ(功能点基准报价 × 复杂度系数)
  - 视觉定制费 = 基础费用 × 视觉复杂度系数(0.05-0.15)
  - 加急费用 = 基础费用 × 加急系数(1.0-1.5)
  - 基础设施费 = 云资源配置费（根据部署模式动态计算）
  - 平台服务费 = (基础费用 + 视觉定制费) × 0.2

### 任务5: 需求智能采集中心前端框架 ✅
**任务描述**: 构建需求智能采集中心的基础页面架构
- [x] 5.1 创建页面目录结构
  ```
  miniprogram/pages/requirement/step_all/
  ├── step_all.js
  ├── step_all.json
  ├── step_all.wxml
  └── step_all.wxss
  ```
- [x] 5.2 实现 **顶部固定导航（header-fixed）**
  - 三阶段进度指示器：需求智能解析 → 交互视觉定制 → AI智能报价
  - 阶段状态管理：pending / active / completed
  - 点击已完成阶段可回退
- [x] 5.3 实现 **内容滚动区（content-scroll）**
  - scroll-view 组件，支持垂直滚动
  - 根据 currentStage 动态渲染不同阶段内容
  - 阶段数据本地缓存与恢复机制
- [x] 5.4 实现 **底部固定操作栏（footer-fixed）**
  - 上一步按钮（非首阶段显示）
  - 下一步/完成按钮（根据阶段动态文案）
  - 按钮状态管理（禁用/启用）
- [x] 5.5 实现 **阶段数据管理**
  - 数据结构：stageData: { stage1: {}, stage2: {}, stage3: {} }
  - 阶段切换时自动保存当前阶段数据
  - 页面加载时恢复已保存数据

### 任务6: 需求智能解析模块前端实现 ✅
**任务描述**: 实现需求智能解析阶段的完整交互功能
- [x] 6.1 **业务场景描述区**
  - textarea 多行文本输入组件
  - 必填验证与实时字数统计
  - 占位提示文案："请详细描述您的业务场景，例如：需要一套酒店管理系统，支持客房预订、入住登记、账单结算等功能"
- [x] 6.2 **智能识别功能点触发器**
  - "智能识别功能点"按钮（业务场景描述非空时启用）
  - 点击后显示加载状态（loading spinner + "AI正在分析业务场景..."）
  - 调用后端 /api/v1/system-templates/search 接口
  - 错误处理与重试机制
- [x] 6.3 **业务角色矩阵与功能点清单**
  - 手风琴（accordion）组件展示业务角色
  - 每个角色展开后显示功能点清单（含复选框）
  - 功能点项显示：名称、复杂度标识（低/中/高）、预估工时
  - 全选/取消全选功能
- [x] 6.4 **成本智能提示**
  - 提示文案："功能点规模与开发投入正相关，建议优先选择核心功能"
  - 实时预估价格显示（基于已选功能点动态计算）
- [x] 6.5 **功能点自定义扩展**
  - 每个角色底部"追加自定义功能"按钮
  - 弹出输入对话框（功能名称、功能描述、复杂度预估）
  - 自定义功能标记并追加至清单
- [x] 6.6 **需求物料交付区**
  - 参考素材库：wx.chooseImage 多选（上限5张），支持预览与删除
  - 源码资产包：wx.chooseMessageFile 选择.zip文件
  - 代码仓库链接：input 输入框，正则验证GitHub/GitLab URL格式

### 任务7: 交互视觉定制模块前端实现 ✅
**任务描述**: 实现交互视觉定制阶段的完整交互功能
- [x] 7.1 **视觉方案选择卡片**
  - 5种预设方案卡片：极简现代主义、商务专业风、活力创意派、科技未来感、完全自定义
  - 每种方案展示预览图、风格标签、适用场景描述
  - 选中状态高亮（边框+阴影效果）
- [x] 7.2 **服务承诺提示**
  - 提示文案："视觉方案支持3轮免费迭代优化，确保最终交付符合预期"
  - 提示位置：页面顶部或视觉方案区域下方
- [x] 7.3 **交互视觉资源库入口**
  - "浏览更多设计方案"按钮
  - 点击跳转至 ui_gallery 页面
- [x] 7.4 创建 **交互视觉资源库页面（ui_gallery）**
  - 页面路径：miniprogram/pages/requirement/ui_gallery/
  - 网格布局展示海量UI设计范式（至少12种预设方案）
  - 支持按风格筛选（极简、商务、创意、科技、复古等）
  - 点击方案查看大图预览
  - "采用此方案"按钮，选择后返回 step_all 并带回选择结果

### 任务8: AI智能报价模块前端实现 ✅
**任务描述**: 实现AI智能报价阶段的完整交互功能
- [x] 8.1 **智能估价加载**
  - 进入阶段3自动触发AI估价请求
  - 显示加载状态："AI正在评估项目成本..." + 进度动画
  - 调用后端 POST /api/v1/requirements/{id}/ai-quotation 接口
- [x] 8.2 **费用构成明细展示**
  - **AI研发成本**（可展开/折叠）：
    - 需求分析阶段成本
    - 架构设计阶段成本
    - 程序开发阶段成本
    - 测试验收阶段成本
    - 性能优化阶段成本
    - 安全加固阶段成本
  - **平台服务费**（固定比例20%）
  - **基础设施成本**（根据部署模式条件展示）：
    - 云服务器租用费
    - 域名注册与备案费
    - SSL证书配置费
    - CDN加速服务费
- [x] 8.3 **总预算展示**
  - 大字号突出显示总预算金额
  - 货币单位标识（CNY）
  - 交付周期预估
- [x] 8.4 **服务承诺展示**
  - 四项服务承诺卡片：
    - 100%交付承诺：项目完整交付，不遗漏功能
    - 满意为止承诺：不满意调整到满意为止
    - 一次收费承诺：一次收费，后续调整不收费
    - 全额退款承诺：不满意可全额退款
- [x] 8.5 **确认预算并提交**
  - "确认预算并提交需求"按钮
  - 点击后调用后端接口保存全量需求数据
  - 更新需求状态为 "quoted"（已报价）
  - 提交成功后跳转至项目工作台（Index）
  - 显示成功提示："需求提交成功，请查看项目进度"

### 任务9: 项目工作台（Index）跳转逻辑更新 ✅
**任务描述**: 更新项目工作台的节点点击跳转逻辑
- [x] 9.1 修改 **onFlowNodeTap** 方法
  - 节点1（需求澄清）处于 active 状态时，点击跳转至 step_all
  - 跳转 URL：/pages/requirement/step_all/step_all?id=${req.id}&stage=1
- [x] 9.2 更新跳转参数
  - 携带项目ID（requirementId）
  - 携带初始阶段参数（stage=1，直接进入需求智能解析阶段）

### 任务10: 端到端集成测试
**任务描述**: 全链路功能验证与质量保障
- [ ] 10.1 **后端服务层测试**
  - SystemTemplateService 单元测试（关键词匹配算法、边界条件）
  - AiQuotationService 单元测试（估价计算逻辑、AI调用异常处理）
  - Repository 层集成测试（数据库操作、关联查询）
- [ ] 10.2 **前端页面层测试**
  - step_all 页面三阶段切换测试（数据持久化、状态恢复）
  - 功能点选择交互测试（全选、取消、自定义添加）
  - 视觉方案选择测试（预设方案、资源库选择）
  - AI报价展示测试（费用明细、总预算计算）
- [ ] 10.3 **全链路集成测试**
  - 从项目工作台节点1点击 → step_all 页面加载 → 三阶段完整流程 → 提交需求 → 返回工作台
  - 数据一致性验证（前端提交数据与后端存储数据比对）
  - 流程节点状态同步验证（提交后首页节点状态更新）
- [ ] 10.4 **性能基准测试**
  - 后端API响应时间 < 2秒（P95）
  - 前端页面首屏加载时间 < 3秒
  - AI估价接口响应时间 < 5秒（含AI大模型调用）
- [ ] 10.5 **异常场景测试**
  - 网络异常处理（断网重连、超时重试）
  - AI服务不可用降级方案（使用本地估价算法）
  - 数据校验失败提示（必填项未填、格式错误）
