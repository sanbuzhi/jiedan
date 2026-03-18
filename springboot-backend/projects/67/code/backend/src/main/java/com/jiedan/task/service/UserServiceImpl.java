package com.jiedan.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiedan.task.dto.LoginDTO;
import com.jiedan.task.entity.User;
import com.jiedan.task.mapper.UserMapper;
import com.jiedan.task.service.UserService;
import com.jiedan.task.util.JwtUtil;
import com.jiedan.task.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSONObject;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={}&secret={}&js_code={}&grant_type=authorization_code";

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 调用微信接口获取openid
        String url = String.format(WX_LOGIN_URL, appid, secret, loginDTO.getCode());
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.parseObject(response);
        String openid = jsonObject.getString("openid");
        if (openid == null) {
            throw new RuntimeException("微信登录失败：" + jsonObject.getString("errmsg"));
        }

        // 查询用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = this.getOne(wrapper);

        // 不存在则创建新用户
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(loginDTO.getNickname() != null ? loginDTO.getNickname() : "微信用户");
            user.setAvatarUrl(loginDTO.getAvatarUrl());
            this.save(user);
        } else {
            // 更新用户信息
            if (loginDTO.getNickname() != null || loginDTO.getAvatarUrl() != null) {
                if (loginDTO.getNickname() != null) user.setNickname(loginDTO.getNickname());
                if (loginDTO.getAvatarUrl() != null) user.setAvatarUrl(loginDTO.getAvatarUrl());
                this.updateById(user);
            }
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId());
        return new LoginVO(token, user.getNickname(), user.getAvatarUrl());
    }

    @Override
    public User getUserById(Long userId) {
        return this.getById(userId);
    }
}