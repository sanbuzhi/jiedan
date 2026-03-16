# 需求智能采集与AI报价系统 质量检查清单

## 一、数据持久化层检查项

### 1.1 系统架构模板库（system_templates）
- [ ] 表结构已创建，包含完整字段集
  - id（主键）、name（模板名称）、code（模板编码）
  - description（模板描述）、keywords（关键词数组，JSON格式）
  - category（分类：酒店/电商/CRM/ERP等）
  - complexity_level（复杂度等级）、is_active（启用状态）
  - created_at、updated_at（时间戳）
- [ ] 索引已创建：keywords（全文检索）、category、is_active
- [ ] 字符集：utf8mb4，支持emoji与特殊字符

### 1.2 业务角色库（system_roles）
- [ ] 表结构已创建，外键关联正确
  - id（主键）、template_id（关联system_templates）
  - name（角色名称）、code（角色编码）
  - description（角色描述）、responsibilities（职责范围）
  - sort_order（排序权重）、created_at、updated_at
- [ ] 外键约束：ON DELETE CASCADE
- [ ] 索引已创建：template_id、sort_order

### 1.3 功能点库（system_functions）
- [ ] 表结构已创建，complexity字段有约束
  - id（主键）、role_id（关联system_roles）
  - name（功能点名称）、code（功能点编码）
  - description（功能点描述）、complexity（复杂度：LOW/MEDIUM/HIGH）
  - estimated_hours（预估工时）、base_price（基准报价）
  - priority（优先级）、created_at、updated_at
- [ ] CHECK约束：complexity IN ('LOW', 'MEDIUM', 'HIGH')
- [ ] 索引已创建：role_id、complexity、priority

### 1.4 需求功能关联表（requirement_functions）
- [ ] 表结构已创建，支持自定义功能点
  - id（主键）、requirement_id（关联requirements）
  - function_id（关联system_functions，可为NULL）
  - is_custom（是否自定义）、custom_name（自定义名称）
  - custom_description（自定义描述）、estimated_hours（预估工时）
  - created_at
- [ ] 复合索引：requirement_id + function_id

### 1.5 需求主表扩展（requirements）
- [ ] 新字段已添加
  - requirement_description（TEXT，业务场景详细描述）
  - selected_functions（JSON，已选功能点快照）
  - materials（JSON，需求物料包结构）
  - deployment_mode（ENUM: 'cloud'/'on_premise'/'hybrid'/'none'）
  - ai_quotation_result（JSON，AI智能估价结果缓存）
- [ ] 既有数据迁移脚本已执行
- [ ] 默认值设置正确

### 1.6 种子数据
- [ ] 酒店PMS系统模板数据已初始化
  - 角色：系统管理员、前台接待、客房服务、财务核算
  - 每个角色至少5个功能点
- [ ] 电商平台模板数据已初始化
  - 角色：买家、卖家、平台运营、平台客服
- [ ] CRM系统模板数据已初始化
  - 角色：销售代表、客服专员、销售经理、系统管理员

---

## 二、后端服务层检查项

### 2.1 领域模型（Entity）
- [ ] SystemTemplate.java 已创建
  - 注解：@Entity、@Table、@Data、@Builder
  - 关联：@OneToMany(mappedBy = "template") 关联 SystemRole
  - 字段映射正确
- [ ] SystemRole.java 已创建
  - 关联：@ManyToOne 关联 SystemTemplate
  - 关联：@OneToMany(mappedBy = "role") 关联 SystemFunction
- [ ] SystemFunction.java 已创建
  - 枚举：ComplexityLevel（LOW、MEDIUM、HIGH）
  - 关联：@ManyToOne 关联 SystemRole
- [ ] RequirementFunction.java 已创建
  - 关联：@ManyToOne 关联 Requirement
  - 关联：@ManyToOne 关联 SystemFunction（optional = true）

### 2.2 数据访问层（Repository）
- [ ] SystemTemplateRepository.java 已创建
  - 继承 JpaRepository<SystemTemplate, Long>
  - 方法：List<SystemTemplate> findByKeywordsContaining(String keyword)
  - 方法：List<SystemTemplate> findByIsActiveTrue()
- [ ] SystemRoleRepository.java 已创建
  - 方法：List<SystemRole> findByTemplateIdOrderBySortOrderAsc(Long templateId)
- [ ] SystemFunctionRepository.java 已创建
  - 方法：List<SystemFunction> findByRoleIdOrderByPriorityAsc(Long roleId)
- [ ] RequirementFunctionRepository.java 已创建
  - 方法：List<RequirementFunction> findByRequirementId(Long requirementId)
  - 方法：void deleteByRequirementId(Long requirementId)

### 2.3 智能匹配服务（SystemTemplateService）
- [ ] searchByKeyword(String keyword) 方法已实现
  - 使用 JPA Criteria API 或原生SQL实现模糊匹配
  - 返回结果按相关性排序
  - 空关键词返回热门模板列表
- [ ] getTemplateDetail(Long templateId) 方法已实现
  - 使用 @Transactional 确保数据一致性
  - 返回模板完整信息（含角色与功能点矩阵）
  - 缓存支持（可选）

### 2.4 智能匹配接口（SystemTemplateController）
- [ ] GET /api/v1/system-templates/search 接口可访问
  - 请求参数：keyword（必填）
  - 响应格式：SystemTemplateSearchResponse
  - HTTP状态码：200（成功）、400（参数错误）
- [ ] GET /api/v1/system-templates/{id} 接口可访问
  - 路径参数：id（模板ID）
  - 响应格式：SystemTemplateDetailResponse
  - HTTP状态码：200（成功）、404（模板不存在）

### 2.5 AI智能估价服务（AiQuotationService）
- [ ] generateQuotation(Long requirementId, QuotationRequest request) 方法已实现
  - 构建AI提示词模板（包含业务场景、功能点矩阵、视觉方案、部署模式）
  - 调用AI大模型API（预留接口，支持多厂商适配）
  - 解析AI返回的JSON格式估价结果
  - 缓存估价结果至数据库
- [ ] calculateBasePrice(List<SelectedFunction> functions) 方法已实现
  - 基础费用 = Σ(功能点基准报价 × 复杂度系数)
  - 复杂度系数：LOW=1.0, MEDIUM=1.5, HIGH=2.5
- [ ] 估价算法完整实现
  - 视觉定制费 = 基础费用 × 视觉复杂度系数(0.05-0.15)
  - 加急费用 = 基础费用 × 加急系数(1.0-1.5)
  - 基础设施费 = 云资源配置费（根据部署模式动态计算）
  - 平台服务费 = (基础费用 + 视觉定制费) × 0.2

### 2.6 AI智能估价接口（AiQuotationController）
- [ ] POST /api/v1/requirements/{id}/ai-quotation 接口可访问
  - 路径参数：id（需求ID）
  - 请求体：AiQuotationRequest
  - 响应格式：AiQuotationResponse
  - HTTP状态码：200（成功）、404（需求不存在）
- [ ] GET /api/v1/requirements/{id}/ai-quotation 接口可访问
  - 返回缓存的估价结果
  - 无缓存时返回404

### 2.7 数据传输对象（DTO）
- [ ] SystemTemplateSearchResponse.java 已创建
- [ ] SystemTemplateDetailResponse.java 已创建
- [ ] SystemRoleDto.java 已创建
- [ ] SystemFunctionDto.java 已创建
- [ ] AiQuotationRequest.java 已创建
- [ ] AiQuotationResponse.java 已创建
- [ ] QuotationBreakdown.java 已创建
- [ ] DevelopmentPhaseCost.java 已创建

---

## 三、前端需求智能采集中心检查项

### 3.1 页面基础架构
- [ ] 页面目录结构正确
  ```
  miniprogram/pages/requirement/step_all/
  ├── step_all.js
  ├── step_all.json
  ├── step_all.wxml
  └── step_all.wxss
  ```
- [ ] step_all.json 配置正确
  - navigationBarTitleText: "需求智能采集"
  - usingComponents 声明（如有自定义组件）

### 3.2 顶部固定导航（header-fixed）
- [ ] 固定在视口顶部（position: fixed; top: 0）
- [ ] 三阶段进度指示器渲染正确
  - 阶段1：需求智能解析
  - 阶段2：交互视觉定制
  - 阶段3：AI智能报价
- [ ] 阶段状态管理正确
  - pending（未开始）：灰色
  - active（进行中）：蓝色高亮
  - completed（已完成）：绿色+勾选图标
- [ ] 点击已完成阶段可回退

### 3.3 内容滚动区（content-scroll）
- [ ] scroll-view 组件配置正确
  - scroll-y="true"
  - enhanced="true"
  - show-scrollbar="false"
- [ ] 根据 currentStage 动态渲染内容
- [ ] 阶段切换时数据自动保存
- [ ] 页面加载时数据自动恢复

### 3.4 底部固定操作栏（footer-fixed）
- [ ] 固定在视口底部（position: fixed; bottom: 0）
- [ ] 上一步按钮（非首阶段显示）
- [ ] 下一步/完成按钮（文案根据阶段动态变化）
- [ ] 按钮状态管理（禁用态样式、加载态样式）

---

## 四、需求智能解析模块检查项

### 4.1 业务场景描述区
- [ ] textarea 多行文本输入组件
- [ ] 必填验证（空值时提示"请描述您的业务场景"）
- [ ] 实时字数统计（右下角显示：当前字数/最大字数）
- [ ] 占位提示文案正确显示

### 4.2 智能识别功能点触发器
- [ ] "智能识别功能点"按钮默认禁用
- [ ] 业务场景描述非空时按钮启用
- [ ] 点击后显示加载状态
  - loading spinner 动画
  - 提示文案："AI正在分析业务场景..."
- [ ] 调用后端 /api/v1/system-templates/search 接口
- [ ] 网络错误处理（重试按钮、错误提示）

### 4.3 业务角色矩阵与功能点清单
- [ ] 手风琴（accordion）组件展示业务角色
  - 点击角色名称展开/折叠
  - 展开时显示该角色下的功能点清单
- [ ] 功能点项显示内容完整
  - 功能点名称
  - 复杂度标识（低/中/高，带颜色区分）
  - 预估工时
- [ ] 复选框功能正常
  - 点击切换选中/未选中状态
  - 全选/取消全选功能
- [ ] 默认状态：所有功能点未选中

### 4.4 成本智能提示
- [ ] 提示文案显示："功能点规模与开发投入正相关，建议优先选择核心功能"
- [ ] 实时预估价格显示
  - 基于已选功能点动态计算
  - 价格格式化显示（¥X,XXX）

### 4.5 功能点自定义扩展
- [ ] 每个角色底部显示"追加自定义功能"按钮
- [ ] 点击弹出输入对话框
  - 功能名称输入框（必填）
  - 功能描述输入框（选填）
  - 复杂度选择（单选：低/中/高）
- [ ] 确认后将自定义功能点追加至清单
- [ ] 自定义功能点标记（特殊样式或标签）

### 4.6 需求物料交付区
- [ ] 参考素材库
  - 点击唤起 wx.chooseImage（count: 5）
  - 已选图片缩略图预览
  - 点击缩略图可删除
- [ ] 源码资产包
  - 点击唤起 wx.chooseMessageFile（type: file, extension: ['zip']）
  - 显示已选文件名
  - 支持删除重选
- [ ] 代码仓库链接
  - input 输入框
  - 正则验证：^https://(github|gitlab)\.com/.+
  - 格式错误时提示"请输入有效的GitHub或GitLab仓库地址"

---

## 五、交互视觉定制模块检查项

### 5.1 视觉方案选择卡片
- [ ] 5种预设方案卡片渲染正确
  - 极简现代主义
  - 商务专业风
  - 活力创意派
  - 科技未来感
  - 完全自定义
- [ ] 每种方案展示内容完整
  - 预览图（placeholder或实际图片）
  - 风格标签
  - 适用场景描述
- [ ] 选中状态高亮
  - 边框颜色变化（蓝色）
  - 阴影效果
  - 勾选图标显示

### 5.2 服务承诺提示
- [ ] 提示文案显示："视觉方案支持3轮免费迭代优化，确保最终交付符合预期"
- [ ] 提示位置正确（页面顶部或视觉方案区域下方）
- [ ] 提示样式醒目（背景色、图标）

### 5.3 交互视觉资源库入口
- [ ] "浏览更多设计方案"按钮显示
- [ ] 点击跳转至 ui_gallery 页面
- [ ] 跳转参数正确（如需要）

### 5.4 交互视觉资源库页面（ui_gallery）
- [ ] 页面目录结构正确
  ```
  miniprogram/pages/requirement/ui_gallery/
  ├── ui_gallery.js
  ├── ui_gallery.json
  ├── ui_gallery.wxml
  └── ui_gallery.wxss
  ```
- [ ] 网格布局展示UI设计范式（至少12种）
- [ ] 支持按风格筛选
  - 筛选标签：极简、商务、创意、科技、复古
  - 点击筛选后重新渲染列表
- [ ] 点击方案查看大图预览
  - 使用 wx.previewImage
- [ ] "采用此方案"按钮
  - 点击后返回 step_all
  - 带回选择结果（方案ID或名称）
  - step_all 页面选中状态更新

---

## 六、AI智能报价模块检查项

### 6.1 智能估价加载
- [ ] 进入阶段3自动触发AI估价请求
- [ ] 加载状态显示
  - 进度动画（spinner或进度条）
  - 提示文案："AI正在评估项目成本..."
- [ ] 调用后端 POST /api/v1/requirements/{id}/ai-quotation 接口
- [ ] 请求参数正确（功能点ID列表、视觉方案、部署模式）

### 6.2 费用构成明细展示
- [ ] AI研发成本（可展开/折叠）
  - 需求分析阶段成本
  - 架构设计阶段成本
  - 程序开发阶段成本
  - 测试验收阶段成本
  - 性能优化阶段成本
  - 安全加固阶段成本
- [ ] 平台服务费（固定比例20%）
- [ ] 基础设施成本（根据部署模式条件展示）
  - 云服务器租用费
  - 域名注册与备案费
  - SSL证书配置费
  - CDN加速服务费
- [ ] 每项费用显示金额（¥X,XXX）

### 6.3 总预算展示
- [ ] 大字号突出显示总预算金额
- [ ] 货币单位标识（CNY）
- [ ] 交付周期预估显示（如：预计交付周期：14天）

### 6.4 服务承诺展示
- [ ] 四项服务承诺卡片渲染
  - 100%交付承诺：项目完整交付，不遗漏功能
  - 满意为止承诺：不满意调整到满意为止
  - 一次收费承诺：一次收费，后续调整不收费
  - 全额退款承诺：不满意可全额退款
- [ ] 每项承诺带图标

### 6.5 确认预算并提交
- [ ] "确认预算并提交需求"按钮显示
- [ ] 点击后调用后端接口保存全量需求数据
- [ ] 更新需求状态为 "quoted"
- [ ] 提交成功后跳转至项目工作台（Index）
- [ ] 显示成功提示："需求提交成功，请查看项目进度"

---

## 七、项目工作台（Index）检查项

### 7.1 节点点击跳转
- [ ] 需求澄清节点（节点1）处于 active 状态时可点击
- [ ] 点击后跳转至 step_all 页面
- [ ] 跳转 URL 正确：/pages/requirement/step_all/step_all?id=${req.id}&stage=1
- [ ] URL 参数正确
  - id：项目ID
  - stage=1：直接进入需求智能解析阶段

---

## 八、端到端集成测试检查项

### 8.1 完整业务流程
- [ ] 从项目工作台节点1点击 → step_all 页面加载
- [ ] step_all 三阶段完整流程
  - 阶段1：录入业务场景 → 智能识别功能点 → 选择功能点 → 上传物料
  - 阶段2：选择视觉方案（或从资源库选择）
  - 阶段3：查看AI报价 → 确认提交
- [ ] 提交需求 → 返回工作台
- [ ] 首页项目卡片状态更新

### 8.2 数据一致性
- [ ] 前端提交数据与后端存储数据一致
- [ ] 需求详情页展示数据与提交数据一致
- [ ] 流程节点状态同步正确

### 8.3 性能基准
- [ ] 后端API响应时间 < 2秒（P95）
- [ ] 前端页面首屏加载时间 < 3秒
- [ ] AI估价接口响应时间 < 5秒（含AI大模型调用）

### 8.4 异常处理
- [ ] 网络异常处理
  - 断网提示
  - 超时重试机制
- [ ] AI服务不可用降级
  - 使用本地估价算法
  - 提示用户"当前使用离线估价模式"
- [ ] 数据校验失败
  - 必填项未填提示
  - 格式错误提示
  - 字段级错误提示

### 8.5 兼容性
- [ ] 微信小程序基础库版本兼容（2.19.4+）
- [ ] 不同屏幕尺寸适配（iPhone SE ~ iPhone 14 Pro Max）
- [ ] 安卓与iOS表现一致

---

## 九、文档与交付物检查项

### 9.1 技术文档
- [ ] API接口文档已更新
- [ ] 数据库ER图已更新
- [ ] 前端组件说明文档已更新

### 9.2 测试报告
- [ ] 单元测试报告（覆盖率 > 80%）
- [ ] 集成测试报告
- [ ] 性能测试报告

### 9.3 部署文档
- [ ] 数据库迁移脚本
- [ ] 环境变量配置说明
- [ ] 部署步骤文档
