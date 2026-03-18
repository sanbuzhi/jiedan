# AI开发机制完善计划

## 一、现状分析

### 1.1 当前问题

**后端问题：**
- 模块化生成功能设计完成，但未充分测试
- 32k maxTokens场景下上下文可能超限
- 文件合并逻辑过于简单，可能产生语法错误
- 续传机制未经大规模验证

**前端问题：**
- 未实现模块化调用逻辑
- 只传递requirementDoc和taskDoc，未使用moduleName、fileList等新参数
- 无法处理大型项目（50万token）

**整体问题：**
- AI生成代码质量不稳定（语法错误、伪代码、接口不一致）
- 缺乏代码验证和编译检查机制
- 任务书解析依赖AI，可能无法正确提取模块信息

### 1.2 可行方案评估

| 方案 | 可行性 | 适用场景 |
|------|--------|----------|
| 一次性生成50万token | ❌ 不可行 | 超出模型限制 |
| 模块化生成（当前设计） | ⚠️ 需完善 | 中型项目（10-20万token） |
| 限制项目规模 | ✅ 最可行 | 小型项目（5-10万token） |
| 人工介入+AI辅助 | ✅ 可行 | 复杂项目 |

## 二、目标设定

### 2.1 短期目标（1-2周）
1. 修复前端调用，支持模块化生成参数
2. 限制项目规模，确保单次生成成功
3. 添加代码质量检查（基础语法验证）

### 2.2 中期目标（3-4周）
1. 完善模块化调用逻辑，支持多模块顺序生成
2. 添加任务书解析服务，自动提取模块信息
3. 实现代码编译验证

### 2.3 长期目标（1-2月）
1. 支持大型项目（50万token）的自动化生成
2. 实现模块间依赖分析和自动更新
3. 建立完整的代码质量保障体系

## 三、详细实施步骤

### 阶段一：基础修复（优先级：高）

#### 步骤1：修复前端调用
**文件：** `miniprogram/pages/index/index.js`

**修改内容：**
```javascript
case 4: // AI开发
  requestData.projectId = requirement.id ? String(requirement.id) : '';
  requestData.requirementDoc = requirement.requirementDoc || '暂无需求文档';
  requestData.taskDoc = requirement.taskDoc || '暂无任务文档';
  
  // 添加模块化参数（可选，用于大型项目）
  if (requirement.moduleName) {
    requestData.moduleName = requirement.moduleName;
    requestData.fileList = requirement.fileList;
    requestData.moduleOrder = requirement.moduleOrder;
    requestData.isLastModule = requirement.isLastModule;
    requestData.generatedModulesSummary = requirement.generatedModulesSummary;
  }
  break;
```

**验收标准：**
- 前端能正确传递所有模块化参数
- 向后兼容（不传模块参数也能正常工作）

#### 步骤2：限制项目规模
**文件：** `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java`

**修改内容：**
- 将maxTokens从32000调整为16000（保守值）
- 将续传次数从3次增加到5次
- 添加输入长度检查，超过限制则报错

**验收标准：**
- 单次生成不超过16000 token
- 能处理10万字符以内的项目

#### 步骤3：添加基础代码验证
**文件：** 新增 `springboot-backend/src/main/java/com/jiedan/service/ai/CodeValidator.java`

**功能：**
- 检查Java文件基本语法（括号匹配、关键字检查）
- 检查文件路径合法性
- 检查必需文件是否存在（pom.xml、application.yml等）

**验收标准：**
- 能检测出80%的明显语法错误
- 验证结果记录到日志

### 阶段二：模块化完善（优先级：中）

#### 步骤4：实现任务书解析服务
**文件：** 新增 `springboot-backend/src/main/java/com/jiedan/service/ai/TaskDocumentParser.java`

**功能：**
- 解析Markdown格式的任务书
- 提取模块列表（前端页面清单、后端接口清单）
- 生成模块依赖关系图
- 输出模块生成顺序

**输入示例：**
```markdown
## 2. 前端页面开发清单
### 2.1 用户管理模块
- 页面：用户列表页
- 组件：用户表单、用户详情
- 接口：GET /api/users

### 2.2 订单管理模块
...
```

**输出示例：**
```json
{
  "modules": [
    {
      "name": "用户管理模块",
      "order": 1,
      "files": ["UserList.vue", "UserForm.vue"],
      "dependencies": []
    }
  ]
}
```

**验收标准：**
- 能正确解析80%的标准任务书格式
- 模块划分合理，依赖关系正确

#### 步骤5：实现模块化顺序生成
**文件：** `springboot-backend/src/main/java/com/jiedan/service/ai/ModularCodeGenerator.java`

**功能：**
- 接收任务书解析结果
- 按依赖顺序逐个生成模块
- 维护模块间接口一致性
- 汇总生成结果

**流程：**
```
1. 解析任务书 -> 模块列表
2. 按order排序模块
3. for each module:
   a. 构建请求（包含已生成模块摘要）
   b. 调用AI生成
   c. 解析并保存文件
   d. 更新模块摘要
4. 返回完整项目
```

**验收标准：**
- 能顺序生成5-8个模块
- 模块间接口一致性达到90%

#### 步骤6：前端模块化调用UI
**文件：** `miniprogram/pages/index/index.js` + 新增模块选择页面

**功能：**
- 显示任务书解析后的模块列表
- 允许用户选择要生成的模块
- 显示生成进度
- 支持暂停和继续

**验收标准：**
- 用户能选择单个或多个模块生成
- 进度显示准确

### 阶段三：质量保障（优先级：中）

#### 步骤7：代码编译验证
**文件：** 新增 `springboot-backend/src/main/java/com/jiedan/service/ai/CodeCompiler.java`

**功能：**
- 调用Maven编译生成的Java代码
- 检查编译错误
- 返回错误列表和位置

**验收标准：**
- 能检测出编译错误
- 错误定位准确

#### 步骤8：AI反馈优化
**文件：** `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java`

**修改内容：**
- 将编译错误反馈给AI进行修复
- 实现自动重试机制
- 限制重试次数（最多3次）

**验收标准：**
- 编译错误能正确反馈
- 修复成功率达到60%

#### 步骤9：代码质量评分
**文件：** 新增 `springboot-backend/src/main/java/com/jiedan/service/ai/CodeQualityScorer.java`

**评分维度：**
- 语法正确性（30%）
- 接口完整性（25%）
- 注释覆盖率（15%）
- 代码规范（15%）
- 可运行性（15%）

**验收标准：**
- 评分准确，与人工评分误差<20%
- 低质量代码（<60分）自动标记

### 阶段四：大型项目支持（优先级：低）

#### 步骤10：分批次生成策略
**文件：** `springboot-backend/src/main/java/com/jiedan/service/ai/LargeProjectGenerator.java`

**策略：**
- 将大型项目拆分为多个批次
- 每批次包含2-3个模块
- 批次间保留接口契约
- 支持断点续传

**验收标准：**
- 能处理50万token项目
- 分10-15个批次完成

#### 步骤11：依赖分析和自动更新
**文件：** 新增 `springboot-backend/src/main/java/com/jiedan/service/ai/DependencyAnalyzer.java`

**功能：**
- 分析模块间依赖关系
- 检测接口变更影响范围
- 自动更新受影响的模块

**验收标准：**
- 依赖分析准确率>90%
- 自动更新成功率>70%

#### 步骤12：完整测试和文档
**文件：** 测试文件 + 使用文档

**内容：**
- 单元测试覆盖率>80%
- 集成测试覆盖主要场景
- 用户使用文档
- 开发者文档

## 四、风险评估

| 风险 | 可能性 | 影响 | 应对措施 |
|------|--------|------|----------|
| AI生成质量不稳定 | 高 | 高 | 添加多重验证和人工审核 |
| 模型API限制 | 中 | 高 | 实现降级方案（简化项目） |
| 前端实现复杂 | 中 | 中 | 分阶段实现，先基础后高级 |
| 性能问题 | 中 | 中 | 添加缓存和异步处理 |

## 五、验收标准

### 5.1 基础功能（必须完成）
- [ ] 前端能正确调用AI开发接口
- [ ] 单次生成成功率>80%
- [ ] 代码文件正确保存
- [ ] 基础语法检查通过

### 5.2 模块化功能（重要）
- [ ] 支持3-5个模块顺序生成
- [ ] 模块间接口一致性>80%
- [ ] 任务书解析准确率>70%

### 5.3 质量保障（加分项）
- [ ] 编译验证通过率>70%
- [ ] 代码质量评分准确
- [ ] 自动修复成功率>50%

## 六、时间计划

| 阶段 | 时间 | 关键里程碑 |
|------|------|-----------|
| 阶段一 | 第1-2周 | 前端修复完成，基础生成可用 |
| 阶段二 | 第3-4周 | 模块化生成可用，支持中型项目 |
| 阶段三 | 第5-6周 | 质量保障体系建立 |
| 阶段四 | 第7-8周 | 大型项目支持，完整测试 |

## 七、下一步行动

1. **立即开始**：步骤1（修复前端调用）
2. **本周完成**：步骤1-3（基础修复）
3. **下周开始**：步骤4-6（模块化完善）

---

**计划创建时间：** 2026-03-17  
**计划版本：** v1.0  
**负责人：** AI Assistant
