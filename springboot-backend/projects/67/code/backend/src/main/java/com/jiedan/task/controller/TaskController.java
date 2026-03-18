package com.jiedan.task.controller;

import com.jiedan.task.common.Result;
import com.jiedan.task.dto.TaskDTO;
import com.jiedan.task.entity.Task;
import com.jiedan.task.service.TaskService;
import com.jiedan.task.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromHeader(String token) {
        return jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
    }

    @GetMapping("/list")
    public Result<List<Task>> getTaskList(@RequestHeader("Authorization") String token,
                                           @RequestParam(required = false) Integer status) {
        try {
            Long userId = getUserIdFromHeader(token);
            List<Task> list = taskService.getTaskList(userId, status);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> addTask(@RequestHeader("Authorization") String token,
                                 @Validated @RequestBody TaskDTO taskDTO) {
        try {
            Long userId = getUserIdFromHeader(token);
            taskService.addTask(userId, taskDTO);
            return Result.success();
        } catch (Exception e) {
            log.error("添加任务失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> updateTask(@RequestHeader("Authorization") String token,
                                    @Validated @RequestBody TaskDTO taskDTO) {
        try {
            Long userId = getUserIdFromHeader(token);
            taskService.updateTask(userId, taskDTO);
            return Result.success();
        } catch (Exception e) {
            log.error("更新任务失败", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteTask(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id) {
        try {
            Long userId = getUserIdFromHeader(token);
            taskService.deleteTask(userId, id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/status/{id}/{status}")
    public Result<Void> updateTaskStatus(@RequestHeader("Authorization") String token,
                                          @PathVariable Long id,
                                          @PathVariable Integer status) {
        try {
            Long userId = getUserIdFromHeader(token);
            taskService.updateTaskStatus(userId, id, status);
            return Result.success();
        } catch (Exception e) {
            log.error("更新任务状态失败", e);
            return Result.error(e.getMessage());
        }
    }
}