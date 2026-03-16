package com.jiedan.service.ai.code;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiedan.dto.ai.code.CodeSummary;
import com.jiedan.dto.ai.code.FileSummary;
import com.jiedan.entity.CodeContext;
import com.jiedan.entity.TaskStatus;
import com.jiedan.repository.CodeContextRepository;
import com.jiedan.repository.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务调度器
 * 负责任务的拓扑排序和状态管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskScheduler {

    private final TaskStatusRepository taskStatusRepository;
    private final CodeContextRepository codeContextRepository;
    private final ObjectMapper objectMapper;

    /**
     * 获取待执行任务（按依赖拓扑排序）
     */
    public List<TaskStatus> getPendingTasksOrdered(String projectId) {
        List<TaskStatus> allTasks = taskStatusRepository.findByProjectId(projectId);
        
        // 过滤出未完成的任务
        List<TaskStatus> pendingTasks = allTasks.stream()
                .filter(task -> !"COMPLETED".equals(task.getState()))
                .collect(Collectors.toList());
        
        // 拓扑排序
        return topologicalSort(pendingTasks);
    }

    /**
     * 获取下一个可执行任务
     */
    public TaskStatus getNextExecutableTask(String projectId) {
        List<TaskStatus> pendingTasks = getPendingTasksOrdered(projectId);
        
        for (TaskStatus task : pendingTasks) {
            // 检查依赖是否都已完成
            if (areDependenciesCompleted(task)) {
                return task;
            }
        }
        
        return null;
    }

    /**
     * 拓扑排序
     */
    private List<TaskStatus> topologicalSort(List<TaskStatus> tasks) {
        // 构建任务映射
        Map<String, TaskStatus> taskMap = tasks.stream()
                .collect(Collectors.toMap(TaskStatus::getTaskId, task -> task));
        
        // 构建依赖图
        Map<String, List<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        
        for (TaskStatus task : tasks) {
            String taskId = task.getTaskId();
            graph.put(taskId, new ArrayList<>());
            inDegree.put(taskId, 0);
        }
        
        for (TaskStatus task : tasks) {
            List<String> dependencies = parseDependencies(task.getDependencies());
            for (String dep : dependencies) {
                if (taskMap.containsKey(dep)) {
                    graph.get(dep).add(task.getTaskId());
                    inDegree.put(task.getTaskId(), inDegree.get(task.getTaskId()) + 1);
                }
            }
        }
        
        // Kahn算法
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        List<TaskStatus> sortedTasks = new ArrayList<>();
        while (!queue.isEmpty()) {
            String taskId = queue.poll();
            sortedTasks.add(taskMap.get(taskId));
            
            for (String neighbor : graph.get(taskId)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        // 检查是否有环
        if (sortedTasks.size() != tasks.size()) {
            log.error("任务依赖存在循环依赖");
            throw new RuntimeException("任务依赖存在循环依赖");
        }
        
        return sortedTasks;
    }

    /**
     * 检查依赖是否都已完成
     */
    private boolean areDependenciesCompleted(TaskStatus task) {
        List<String> dependencies = parseDependencies(task.getDependencies());
        
        for (String depTaskId : dependencies) {
            Optional<TaskStatus> depTask = taskStatusRepository
                    .findByProjectIdAndTaskId(task.getProjectId(), depTaskId);
            
            if (depTask.isEmpty() || !"COMPLETED".equals(depTask.get().getState())) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 解析依赖列表
     */
    private List<String> parseDependencies(String dependenciesJson) {
        if (dependenciesJson == null || dependenciesJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(dependenciesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("解析依赖列表失败: {}", dependenciesJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 标记任务完成
     */
    public void markTaskCompleted(String projectId, String taskId) {
        TaskStatus task = taskStatusRepository
                .findByProjectIdAndTaskId(projectId, taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        task.setState("COMPLETED");
        taskStatusRepository.save(task);
        
        log.info("任务标记完成, projectId: {}, taskId: {}", projectId, taskId);
    }

    /**
     * 标记任务失败
     */
    public void markTaskFailed(String projectId, String taskId, String errorMessage) {
        TaskStatus task = taskStatusRepository
                .findByProjectIdAndTaskId(projectId, taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        task.setState("FAILED");
        task.setErrorMessage(errorMessage);
        taskStatusRepository.save(task);
        
        log.error("任务标记失败, projectId: {}, taskId: {}, error: {}", 
                projectId, taskId, errorMessage);
    }

    /**
     * 标记任务进行中
     */
    public void markTaskInProgress(String projectId, String taskId) {
        TaskStatus task = taskStatusRepository
                .findByProjectIdAndTaskId(projectId, taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        task.setState("IN_PROGRESS");
        taskStatusRepository.save(task);
        
        log.info("任务标记进行中, projectId: {}, taskId: {}", projectId, taskId);
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount(String projectId, String taskId) {
        TaskStatus task = taskStatusRepository
                .findByProjectIdAndTaskId(projectId, taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        task.setRetryCount(task.getRetryCount() + 1);
        taskStatusRepository.save(task);
    }

    /**
     * 检查是否所有任务完成
     */
    public boolean isAllTasksCompleted(String projectId) {
        List<TaskStatus> allTasks = taskStatusRepository.findByProjectId(projectId);
        
        if (allTasks.isEmpty()) {
            return false;
        }
        
        return allTasks.stream()
                .allMatch(task -> "COMPLETED".equals(task.getState()));
    }

    /**
     * 获取已完成任务的代码摘要
     */
    public List<CodeSummary> getCompletedTaskSummaries(String projectId) {
        List<TaskStatus> completedTasks = taskStatusRepository.findByProjectIdAndState(projectId, "COMPLETED");
        
        List<CodeSummary> summaries = new ArrayList<>();
        for (TaskStatus task : completedTasks) {
            Optional<CodeContext> context = codeContextRepository
                    .findByProjectIdAndTaskId(projectId, task.getTaskId());
            
            if (context.isPresent()) {
                CodeContext ctx = context.get();
                CodeSummary summary = CodeSummary.builder()
                        .taskId(ctx.getTaskId())
                        .taskName(ctx.getTaskName())
                        .files(List.of(FileSummary.builder()
                                .filePath(ctx.getClassName())
                                .build()))
                        .build();
                summaries.add(summary);
            }
        }
        
        return summaries;
    }

    /**
     * 初始化任务状态
     */
    public void initializeTasks(String projectId, List<Map<String, Object>> taskList) {
        for (Map<String, Object> taskInfo : taskList) {
            String taskId = (String) taskInfo.get("id");
            String taskName = (String) taskInfo.get("name");
            String taskType = (String) taskInfo.get("type");
            String priority = (String) taskInfo.get("priority");
            
            @SuppressWarnings("unchecked")
            List<String> dependencies = (List<String>) taskInfo.get("dependencies");
            
            TaskStatus task = TaskStatus.builder()
                    .projectId(projectId)
                    .taskId(taskId)
                    .taskName(taskName)
                    .taskType(taskType)
                    .priority(priority)
                    .state("PENDING")
                    .dependencies(toJson(dependencies))
                    .retryCount(0)
                    .build();
            
            taskStatusRepository.save(task);
        }
        
        log.info("任务状态初始化完成, projectId: {}, 任务数: {}", projectId, taskList.size());
    }

    /**
     * 转换为JSON
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON转换失败", e);
            return "[]";
        }
    }
}
