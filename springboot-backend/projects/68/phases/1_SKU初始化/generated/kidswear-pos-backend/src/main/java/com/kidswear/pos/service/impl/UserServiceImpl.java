package com.kidswear.pos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kidswear.pos.entity.User;
import com.kidswear.pos.mapper.UserMapper;
import com.kidswear.pos.service.UserService;
import com.kidswear.pos.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = getOne(wrapper);
        if (user == null || !passwordEncoder.matches(password, user.getPassword()) || user.getStatus() != 1) {
            throw new RuntimeException("Invalid username or password");
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public User getUserInfo(Long userId) {
        return getById(userId);
    }
}