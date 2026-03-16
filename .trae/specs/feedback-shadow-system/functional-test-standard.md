# AI功能测试标准说明书模板

## 1. 文档概述

### 1.1 目的
本文档定义了AI生成功能代码的测试标准，确保AI输出满足质量要求，为Feedback Shadow系统提供验证依据。

### 1.2 适用范围
适用于所有AI开发接口(generate-code)生成的代码的功能测试验证。

### 1.3 术语定义

| 术语 | 定义 |
|------|------|
| P0 | 最高优先级，核心功能，必须100%通过 |
| P1 | 高优先级，重要功能，通过率≥95% |
| P2 | 中优先级，一般功能，通过率≥80% |
| 行覆盖率 | 被执行的代码行数/总代码行数 |
| 分支覆盖率 | 被执行的分支数/总分支数 |

---

## 2. 测试范围

### 2.1 必须测试的内容

#### 2.1.1 单元测试 (Unit Test)
- 每个public方法至少一个测试用例
- 边界条件测试
- 异常场景测试
- 空值处理测试

#### 2.1.2 集成测试 (Integration Test)
- 模块间接口调用
- 数据库操作
- 第三方服务调用（Mock）
- 事务处理

#### 2.1.3 端到端测试 (E2E Test) - 可选
- 完整业务流程
- 用户场景模拟

### 2.2 测试类型优先级

| 测试类型 | 优先级 | 说明 |
|----------|--------|------|
| 单元测试 | P0 | 必须覆盖所有核心业务逻辑 |
| 集成测试 | P1 | 必须覆盖数据库和外部依赖 |
| E2E测试 | P2 | 关键业务流程建议覆盖 |

---

## 3. 测试用例编写规范

### 3.1 用例结构

每个测试用例必须包含以下字段：

```json
{
  "id": "TC-001",
  "name": "测试用例名称",
  "description": "详细描述测试场景",
  "priority": "P0|P1|P2",
  "type": "unit|integration|e2e",
  "preconditions": ["前置条件1", "前置条件2"],
  "steps": ["步骤1", "步骤2", "步骤3"],
  "expectedResult": "预期结果描述",
  "relatedCode": "关联的代码文件或方法"
}
```

### 3.2 用例命名规范

- **格式**: `[模块]_[功能]_[场景]`
- **示例**: 
  - `User_Register_Success` - 用户注册成功
  - `User_Register_DuplicatePhone` - 用户注册-手机号重复
  - `Order_Create_InsufficientBalance` - 订单创建-余额不足

### 3.3 用例优先级定义

#### P0 - 核心功能用例
- 主流程正向场景
- 关键异常场景
- 安全相关场景
- 数据一致性场景

#### P1 - 重要功能用例
- 次要流程场景
- 常见异常场景
- 边界条件场景

#### P2 - 一般功能用例
- 边缘场景
- 性能相关场景
- 用户体验场景

---

## 4. 覆盖率要求

### 4.1 最低覆盖率标准

| 项目类型 | 行覆盖率 | 分支覆盖率 | 方法覆盖率 |
|----------|----------|------------|------------|
| SpringBoot | ≥80% | ≥70% | ≥90% |
| Python | ≥80% | ≥70% | ≥90% |
| Node.js | ≥80% | ≥70% | ≥90% |
| 小程序 | ≥70% | ≥60% | ≥80% |

### 4.2 覆盖率豁免规则

以下情况可申请豁免覆盖率要求：
- 第三方生成的代码（如ORM生成的实体类）
- 纯数据类（DTO/VO）
- 配置类
- 框架要求的接口实现（无业务逻辑）

### 4.3 未覆盖代码说明

对于未覆盖的代码，必须在测试报告中说明原因：

```json
{
  "uncoveredLines": [
    {
      "file": "UserService.java",
      "lines": [45, 46, 89],
      "reason": "异常分支，需要特定外部服务异常才能触发",
      "planned": true,
      "ticket": "TEST-001"
    }
  ]
}
```

---

## 5. 测试执行标准

### 5.1 执行环境要求

- 使用独立的测试数据库（H2/SQLite/TestContainers）
- 外部服务必须Mock
- 测试数据相互隔离

### 5.2 执行结果判定

| 结果 | 判定标准 | 处理措施 |
|------|----------|----------|
| Passed | 实际结果与预期一致 | 无 |
| Failed | 实际结果与预期不一致 | 必须修复 |
| Skipped | 前置条件不满足或暂时跳过 | 需说明原因 |

### 5.3 测试报告要求

测试报告必须包含：
1. 测试执行摘要（总用例数、通过数、失败数、跳过数）
2. 覆盖率报告
3. 失败用例详情（堆栈跟踪、日志）
4. 未覆盖代码说明
5. 性能指标（执行时间）

---

## 6. 各项目类型测试标准

### 6.1 SpringBoot项目

#### 6.1.1 测试框架
- JUnit 5
- Mockito
- Spring Boot Test
- AssertJ

#### 6.1.2 必测场景

**Controller层**
- 请求参数校验
- 响应格式正确性
- HTTP状态码
- 异常处理

**Service层**
- 业务逻辑正确性
- 事务处理
- 异常转换

**Repository层**
- CRUD操作
- 查询条件
- 分页逻辑

#### 6.1.3 测试代码模板

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("用户注册成功")
    void register_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setPassword("password123");
        
        when(userService.register(any())).thenReturn(new User());

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userId").exists());
    }
}
```

### 6.2 Python项目

#### 6.2.1 测试框架
- pytest
- pytest-cov（覆盖率）
- unittest.mock

#### 6.2.2 必测场景
- API端点测试
- 业务逻辑测试
- 数据库模型测试

### 6.3 微信小程序

#### 6.3.1 测试框架
- miniprogram-simulate
- jest

#### 6.3.2 必测场景
- 页面渲染
- 事件处理
- API调用
- 数据绑定

---

## 7. 测试通过标准

### 7.1 强制通过条件

以下所有条件必须满足，否则测试不通过：

1. **P0用例100%通过** - 不允许有失败的P0用例
2. **行覆盖率达标** - 达到项目类型要求的最低覆盖率
3. **无编译错误** - 测试代码必须可编译
4. **无运行时错误** - 测试执行无异常终止

### 7.2 建议通过条件

以下条件建议满足：

1. P1用例通过率≥95%
2. 分支覆盖率≥70%
3. 测试执行时间合理（单个用例<5秒）

### 7.3 测试不通过处理

当测试不通过时，Feedback Shadow系统将：

1. 记录失败的用例和原因
2. 调度AI开发接口进行修复
3. 提供失败上下文（错误信息、堆栈）
4. 重新执行测试，最多重试3次

---

## 8. 测试数据管理

### 8.1 测试数据原则

- 使用Builder模式创建测试数据
- 避免使用生产数据
- 敏感信息必须脱敏

### 8.2 测试数据示例

```java
public class UserTestDataBuilder {
    
    public static User validUser() {
        User user = new User();
        user.setId(1L);
        user.setPhone("13800138000");
        user.setPassword("encrypted_password");
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
    
    public static User userWithInvalidPhone() {
        User user = validUser();
        user.setPhone("invalid_phone");
        return user;
    }
}
```

---

## 9. 持续集成集成

### 9.1 CI/CD流水线测试步骤

```yaml
test:
  stage: test
  script:
    - mvn clean test
    - mvn jacoco:report
  coverage: '/Total.*?([0-9]{1,3})%/'  
  artifacts:
    reports:
      junit: target/surefire-reports/*.xml
      coverage_report:
        coverage_format: jacoco
        path: target/site/jacoco/index.html
```

### 9.2 质量门禁

在CI/CD中设置质量门禁：
- 测试通过率≥80%
- 行覆盖率≥80%
- 无P0用例失败

---

## 10. 附录

### 10.1 常用断言示例

**JUnit 5**
```java
assertEquals(expected, actual);
assertTrue(condition);
assertThrows(Exception.class, () -> { ... });
assertNull(object);
```

**AssertJ**
```java
assertThat(actual).isEqualTo(expected);
assertThat(list).hasSize(3).contains("a", "b");
assertThatThrownBy(() -> { ... }).isInstanceOf(Exception.class);
```

### 10.2 Mock示例

**Mockito**
```java
when(service.method(any())).thenReturn(result);
when(service.method(any())).thenThrow(new Exception());
verify(service, times(1)).method(any());
```

### 10.3 测试用例ID编码规则

- **格式**: `TC-[三位数字]`
- **范围**: TC-001 到 TC-999
- **分配**: 按模块顺序分配
  - TC-001 ~ TC-100: 用户模块
  - TC-101 ~ TC-200: 订单模块
  - TC-201 ~ TC-300: 商品模块
