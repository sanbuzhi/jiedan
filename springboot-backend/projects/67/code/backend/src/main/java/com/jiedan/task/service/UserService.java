package com.jiedan.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiedan.task.dto.LoginDTO;
import com.jiedan.task.entity.User;
import com.jiedan.task.vo.LoginVO;

public interface UserService extends IService<User> {
    LoginVO login(LoginDTO loginDTO);
    User getUserById(Long userId);
}