package com.jiedan.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiedan.task.dto.TaskDTO;
import com.jiedan.task.entity.Task;
import com.jiedan.task.mapper.TaskMapper;
import com.jiedan.task.service.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
    @Override
    public List<Task> getTaskList(Long userId, Integer status) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId);
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        wrapper.orderByDesc(Task::getPriority)
               .orderByDesc(Task::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public void addTask(Long userId, TaskDTO taskDTO) {
        Task task = new Task();
        BeanUtils.copyProperties(taskDTO, task);
        task.setUserId(userId);
        task.setStatus(0);
        this.save(task);
    }

    @Override
    public void updateTask(Long userId, TaskDTO taskDTO) {
        Task task = this.getById(taskDTO.getId());
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权限操作");
        }
        BeanUtils.copyProperties(taskDTO, task, "id", "userId", "status", "createTime");
        this.updateById(task);
    }

    @Override
    public void deleteTask(Long userId, Long taskId) {
        Task task = this.getById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权限操作");
        }
        this.removeById(taskId);
    }

    @Override
    public void updateTaskStatus(Long userId, Long taskId, Integer status) {
        Task task = this.getById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权限操作");
        }
        task.setStatus(status);
        this.updateById(task);
    }
}