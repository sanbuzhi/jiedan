package com.kidswear.pos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kidswear.pos.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}