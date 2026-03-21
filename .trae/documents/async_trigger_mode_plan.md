# 触发器模式改造计划

## 问题分析

当前 `@Async` 可能未生效，因为缺少 `@EnableAsync` 注解。

## 最小改动方案

### Step 1: 添加 @EnableAsync 注解

**文件**: `JiedanApplication.java`

```java
@EnableAsync
@SpringBootApplication
public class JiedanApplication {
    // ...
}
```

**作用**: 启用 Spring 的异步注解支持，使 `@Async` 生效。

### Step 2: 验证异步执行

确保 `startProjectAsync` 在独立线程池中执行，不阻塞控制器返回。

### Step 3: 可选 - 配置异步线程池

如果默认线程池不满足需求，可创建自定义线程池配置：

**文件**: `AsyncConfig.java` (新建)

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-task-");
        executor.initialize();
        return executor;
    }
}
```

然后在 `startProjectAsync` 上指定使用:

```java
@Async("aiTaskExecutor")
public void startProjectAsync(...) { ... }
```

## 架构说明

改造后的调用链:

```
POST /v1/ai/code/project/start
    ↓
projectManager.startProjectAsync(...)  [@Async注解]
    ↓ 立即返回（不等待）
return ApiResponse.success(projectId)  [控制器立即返回200]
    ↓
后台线程池执行:
  startProject()
    → saveTaskDoc()
    → projectRepository.save()
    → tasksAnalysisService.analyzeAndGetPhaseConfigs() [AI调用]
    → createProjectDirectoryStructure()
    → executeAllPhases()  [所有阶段同步执行]
    → updateProjectStatus()
```

## 改动范围

| 文件 | 改动 |
|------|------|
| `JiedanApplication.java` | 添加 `@EnableAsync` |
| `ProjectDevelopmentManager.java` | 无改动 |
| `ProjectDevelopmentController.java` | 无改动 |
| `AsyncConfig.java` | 可选新建 |

## 预期效果

- `/project/start` 接口在 **1秒内** 返回 200
- 后台异步执行所有阶段（可能需要几小时）
- 前端通过轮询 `/project/{id}/status` 查询进度
