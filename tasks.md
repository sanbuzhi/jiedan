# AI自助开发系统 - 任务分解

## 任务总览

| 阶段 | 任务数 | 优先级 |
|------|--------|--------|
| 后端重构 | 4 | 高 |
| 前端重构 | 3 | 高 |
| 测试验证 | 2 | 高 |

---

## 阶段一：后端重构

### 任务1.1：修改AiService集成Feedback Shadow验证
**优先级：** 高  
**预计工时：** 4小时  
**负责人：** 后端开发

**详细步骤：**

1. **修改 `ClarifyRequirementResponse` DTO**
   - 文件：`springboot-backend/src/main/java/com/jiedan/dto/ai/ClarifyRequirementResponse.java`
   - 添加字段：
     ```java
     private boolean success;
     private String errorMessage;
     private String validationDecision;
     private List<String> validationIssues;
     ```

2. **修改 `AiService.clarifyRequirement` 方法**
   - 文件：`springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java`
   - 在AI调用完成后增加Feedback Shadow验证：
     ```java
     // 注入FeedbackShadowService
     private final FeedbackShadowService feedbackShadowService;
     
     public ClarifyRequirementResponse clarifyRequirement(ClarifyRequirementRequest request) {
         // ... 原有AI调用逻辑 ...
         
         // 【新增】Feedback Shadow验证
         FeedbackShadowValidateRequest validateRequest = FeedbackShadowValidateRequest.builder()
             .projectId(request.getProjectId())
             .apiType("clarify-requirement")
             .documentContent(chatResponse.getContent())
             .build();
         
         FeedbackShadowValidateResponse validationResult = feedbackShadowService.validateWithAI(validateRequest);
         
         // 构建响应
         boolean success = validationResult.isSuccess() && 
                          validationResult.getDecision() != ValidationDecision.REJECT;
         
         return ClarifyRequirementResponse.builder()
             .success(success)
             .errorMessage(success ? null : validationResult.getIssues().toString())
             .fullRequirementDoc(chatResponse.getContent())
             .validationDecision(validationResult.getDecision().name())
             .validationIssues(validationResult.getIssues())
             .build();
     }
     ```

3. **修改 `SplitTasksResponse` DTO**
   - 添加success、errorMessage、validationDecision、validationIssues字段

4. **修改 `AiService.splitTasks` 方法**
   - 增加Feedback Shadow验证（apiType="split-tasks"）

5. **修改 `GenerateCodeResponse` DTO**
   - 添加success、errorMessage、validationDecision、validationIssues字段

6. **修改 `AiService.generateCode` 方法**
   - 增加Feedback Shadow验证（apiType="generate-code"）

7. **修改 `FunctionalTestResponse` DTO**
   - 添加success、errorMessage、validationDecision、validationIssues字段

8. **修改 `AiService.functionalTest` 方法**
   - 增加Feedback Shadow验证（apiType="functional-test"）

9. **修改 `SecurityTestResponse` DTO**
   - 添加success、errorMessage、validationDecision、validationIssues字段

10. **修改 `AiService.securityTest` 方法**
    - 增加Feedback Shadow验证（apiType="security-test"）

**验收标准：**
- [ ] 所有5个AI方法都集成了Feedback Shadow验证
- [ ] 响应DTO包含success字段
- [ ] 验证失败时success=false并返回错误信息

---

### 任务1.2：删除FeedbackShadowController
**优先级：** 高  
**预计工时：** 1小时  
**负责人：** 后端开发

**详细步骤：**

1. **删除文件**
   - 文件：`springboot-backend/src/main/java/com/jiedan/controller/FeedbackShadowController.java`
   - 操作：直接删除

2. **验证保留的Service**
   - 确认 `FeedbackShadowService` 仍然存在
   - 确认相关DTO（FeedbackShadowValidateRequest/Response等）仍然存在

**验收标准：**
- [ ] FeedbackShadowController.java已删除
- [ ] 项目可以正常编译
- [ ] FeedbackShadowService仍然可用

---

### 任务1.3：增加AI接口重试机制
**优先级：** 高  
**预计工时：** 3小时  
**负责人：** 后端开发

**详细步骤：**

1. **创建重试工具类**
   - 文件：`springboot-backend/src/main/java/com/jiedan/service/ai/AiRetryService.java`
   ```java
   @Service
   @RequiredArgsConstructor
   public class AiRetryService {
       private final FeedbackShadowService feedbackShadowService;
       private static final int MAX_RETRIES = 3;
       
       public <T> T executeWithRetry(AiTask<T> task, String projectId, String apiType) {
           int attempts = 0;
           Exception lastException = null;
           
           while (attempts < MAX_RETRIES) {
               try {
                   T result = task.execute();
                   
                   // Feedback Shadow验证
                   FeedbackShadowValidateRequest validateRequest = 
                       FeedbackShadowValidateRequest.builder()
                           .projectId(projectId)
                           .apiType(apiType)
                           .documentContent(extractContent(result))
                           .build();
                   
                   FeedbackShadowValidateResponse validation = 
                       feedbackShadowService.validateWithAI(validateRequest);
                   
                   if (validation.getDecision() == ValidationDecision.ALLOW) {
                       return result;
                   } else if (validation.getDecision() == ValidationDecision.REPAIR) {
                       // 尝试修复
                       result = attemptRepair(result, validation, projectId, apiType);
                       if (result != null) {
                           return result;
                       }
                   }
                   // REJECT时继续重试
                   
               } catch (Exception e) {
                   lastException = e;
                   log.warn("AI任务执行失败，准备重试 {}/{}: {}", attempts + 1, MAX_RETRIES, e.getMessage());
               }
               
               attempts++;
               if (attempts < MAX_RETRIES) {
                   try {
                       Thread.sleep(1000 * attempts); // 指数退避
                   } catch (InterruptedException ignored) {}
               }
           }
           
           throw new RuntimeException("AI任务执行失败，已达到最大重试次数: " + MAX_RETRIES, lastException);
       }
   }
   ```

2. **修改AiService使用重试机制**
   - 在每个AI方法中使用AiRetryService

**验收标准：**
- [ ] 重试机制正常工作
- [ ] 最大重试3次
- [ ] 超过重试次数后抛出异常

---

### 任务1.4：后端API测试
**优先级：** 高  
**预计工时：** 2小时  
**负责人：** 后端开发

**详细步骤：**

1. **单元测试**
   - 测试AiService的各个方法
   - 测试Feedback Shadow集成

2. **接口测试**
   - 使用Postman测试5个AI接口
   - 验证响应格式

**验收标准：**
- [ ] 单元测试通过
- [ ] 接口测试通过
- [ ] 响应格式符合预期

---

## 阶段二：前端重构

### 任务2.1：移除api.js中的Feedback Shadow接口
**优先级：** 高  
**预计工时：** 1小时  
**负责人：** 前端开发

**详细步骤：**

1. **修改 `miniprogram/utils/api.js`**
   - 删除以下接口：
     ```javascript
     // 删除第284-299行
     feedbackShadowValidate: (data) => post('/feedback-shadow/validate', data, { timeout: 120000 }),
     feedbackShadowRepair: (projectId, apiType, data) => post(`/feedback-shadow/repair?projectId=${projectId}&apiType=${apiType}`, data, { timeout: 120000 }),
     getFeedbackShadowSpec: (apiType) => get(`/feedback-shadow/spec/${apiType}`),
     feedbackShadowValidateBatch: (data) => post('/feedback-shadow/validate-batch', data, { timeout: 180000 }),
     validateWithFeedbackShadow: (data) => post('/feedback-shadow/validate', data, { timeout: 120000 })
     ```

2. **简化aiApi对象**
   - 只保留5个核心AI接口

**验收标准：**
- [ ] api.js中不再包含feedback-shadow相关接口
- [ ] 项目可以正常编译

---

### 任务2.2：修改index.js移除Feedback Shadow调用
**优先级：** 高  
**预计工时：** 3小时  
**负责人：** 前端开发

**详细步骤：**

1. **删除 `performFeedbackShadowValidation` 方法**
   - 删除index.js第449-483行

2. **修改 `executeAINodeWithRetry` 方法**
   - 文件：`miniprogram/pages/index/index.js`
   - 原代码（第221-304行）：
     ```javascript
     executeAINodeWithRetry: function (requirementId, nodeIndex, retryCount = 0) {
       // ...
       requirementApi.getRequirementDetail(requirementId)
         .then(requirement => {
           // ...
           return this.callAIWithTimeout(aiApi[aiNode.api], requestData, aiNode.timeout);
         })
         .then(res => {
           // AI处理完成，进行Feedback Shadow验证 ← 删除这部分
           return this.performFeedbackShadowValidation(requirementId, nodeIndex, res);
         })
         .then(validationResult => {
           // ...
         })
     }
     ```
   
   - 新代码：
     ```javascript
     executeAINodeWithRetry: function (requirementId, nodeIndex, retryCount = 0) {
       const aiNode = AI_NODE_CONFIG[nodeIndex];
       if (!aiNode) {
         console.error(`未找到索引为 ${nodeIndex} 的AI节点配置`);
         return;
       }

       console.log(`执行AI节点: ${aiNode.name}, 需求ID: ${requirementId}, 重试次数: ${retryCount}`);

       // 获取需求详情
       requirementApi.getRequirementDetail(requirementId)
         .then(requirement => {
           // 构建请求数据
           let requestData = this.buildAIRequestData(nodeIndex, requirement);

           // 调用AI接口（内部已完成Feedback Shadow验证）
           return this.callAIWithTimeout(aiApi[aiNode.api], requestData, aiNode.timeout);
         })
         .then(res => {
           console.log(`${aiNode.name}完成:`, res);

           // 检查AI接口返回的success字段
           if (!res.data || !res.data.success) {
             // 验证失败，抛出错误触发重试
             const errorMsg = res.data && res.data.errorMessage ? res.data.errorMessage : 'AI处理失败';
             throw new Error(errorMsg);
           }

           // 【移除】保存AI产物到本地存储 - AI产物由后端保存
           // this.saveAIProduct(requirementId, nodeIndex, res.data);

           // 更新节点状态到下一阶段
           return this.updateFlowNodeStatus(requirementId, aiNode.nextNode);
         })
         .then(() => {
           // 如果需要客户验收，停止自动执行
           if (aiNode.needsApproval) {
             console.log(`${aiNode.name}完成，等待客户验收，节点已更新为: ${aiNode.nextNode}`);
             // 【移除】不需要保存approval_data到本地存储
             return;
           }

           // 不需要验收，自动继续下一个AI节点
           if (AI_NODE_CONFIG[aiNode.nextNode]) {
             setTimeout(() => {
               this.executeAINodeWithRetry(requirementId, aiNode.nextNode, 0);
             }, 500);
           }
         })
         .catch(err => {
           console.error(`${aiNode.name}失败:`, err);

           // 检查是否需要重试
           if (retryCount < aiNode.maxRetries) {
             console.log(`${aiNode.name}将在3秒后重试 (${retryCount + 1}/${aiNode.maxRetries})`);
             setTimeout(() => {
               this.executeAINodeWithRetry(requirementId, nodeIndex, retryCount + 1);
             }, 3000);
           } else {
             console.error(`${aiNode.name}已达到最大重试次数，停止自动执行`);
             this.markNodeAsFailed(requirementId, nodeIndex);
             
             wx.showToast({
               title: `${aiNode.name}失败，请重试`,
               icon: 'none',
               duration: 3000
             });
           }
         });
     }
     ```

3. **删除 `saveAIProduct` 方法**
   - 删除index.js第307-376行
   - AI产物由后端保存，前端不需要保存

4. **简化客户验收流程**
   - 修改 `onFlowNodeTap` 方法
   - 移除对 `approval_data_${req.id}` 本地存储的检查
   - 用户点击验收节点直接跳转到approve页面

**验收标准：**
- [ ] performFeedbackShadowValidation方法已删除
- [ ] saveAIProduct方法已删除
- [ ] executeAINodeWithRetry方法已修改（移除AI产物保存和approval_data保存）
- [ ] onFlowNodeTap方法已简化（移除本地存储检查）
- [ ] 前端不再调用Feedback Shadow接口

---

### 任务2.3：前端页面测试
**优先级：** 高  
**预计工时：** 2小时  
**负责人：** 前端开发

**详细步骤：**

1. **单元测试**
   - 测试executeAINodeWithRetry方法

2. **集成测试**
   - 在开发者工具中测试完整流程
   - 验证AI节点链式执行

**验收标准：**
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] AI流程正常运行
- [ ] 客户验收流程简化后正常

---

## 阶段三：测试验证

### 任务3.1：端到端测试
**优先级：** 高  
**预计工时：** 4小时  
**负责人：** 测试/全栈

**详细步骤：**

1. **测试环境准备**
   - 启动后端服务
   - 启动小程序开发者工具

2. **完整流程测试**
   - 创建新项目
   - 完成step页面（5步）
   - 完成step_all页面（3阶段）
   - 验证AI节点1自动触发
   - 验证AI节点链式执行（1→3→4→5→6）
   - 验证客户验收流程（节点2/7/9）

3. **异常场景测试**
   - AI接口超时
   - Feedback Shadow验证失败
   - 重试机制

**验收标准：**
- [ ] 完整流程测试通过
- [ ] 异常场景处理正常

---

### 任务3.2：性能测试
**优先级：** 高  
**预计工时：** 2小时  
**负责人：** 测试/全栈

**详细步骤：**

1. **响应时间测试**
   - 测量AI接口响应时间
   - 测量Feedback Shadow验证时间

2. **并发测试**
   - 多个项目同时执行AI流程

**验收标准：**
- [ ] AI接口响应时间 < 3分钟
- [ ] Feedback Shadow验证不阻塞主流程

---

## 任务依赖关系

```
任务1.1（修改AiService）
    ↓
任务1.2（删除Controller）
    ↓
任务1.3（增加重试机制）
    ↓
任务1.4（后端测试）
    ↓
任务2.1（移除api.js接口）
    ↓
任务2.2（修改index.js）
    ↓
任务2.3（前端测试）
    ↓
任务3.1（端到端测试）
    ↓
任务3.2（性能测试）
```

---

## 时间计划

| 阶段 | 任务 | 预计工时 | 累计工时 |
|------|------|----------|----------|
| 后端重构 | 1.1-1.4 | 10小时 | 10小时 |
| 前端重构 | 2.1-2.3 | 6小时 | 16小时 |
| 测试验证 | 3.1-3.2 | 6小时 | 22小时 |

**总预计工时：22小时（约3个工作日）**
