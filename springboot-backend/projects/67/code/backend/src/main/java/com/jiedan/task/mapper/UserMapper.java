package com.jiedan.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiedan.task.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}