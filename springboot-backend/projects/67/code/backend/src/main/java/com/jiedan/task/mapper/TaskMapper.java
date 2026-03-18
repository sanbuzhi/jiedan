package com.jiedan.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiedan.task.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}