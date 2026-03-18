package com.jiedan.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiedan.task.dto.TaskDTO;
import com.jiedan.task.entity.Task;
import java.util.List;

public interface TaskService extends IService<Task> {
    List<Task> getTaskList(Long userId, Integer status);
    void addTask(Long userId, TaskDTO taskDTO);
    void updateTask(Long userId, TaskDTO taskDTO);
    void deleteTask(Long userId, Long taskId);
    void updateTaskStatus(Long userId, Long taskId, Integer status);
}