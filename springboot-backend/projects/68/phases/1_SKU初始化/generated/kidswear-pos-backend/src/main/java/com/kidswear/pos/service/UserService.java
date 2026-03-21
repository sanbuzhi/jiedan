package com.kidswear.pos.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kidswear.pos.entity.User;

public interface UserService extends IService<User> {
    String login(String username, String password);
    User getUserInfo(Long userId);
}