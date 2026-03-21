package com.kidswear.pos.controller;

import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.User;
import com.kidswear.pos.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> loginParams) {
        String username = loginParams.get("username");
        String password = loginParams.get("password");
        String token = userService.login(username, password);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    @GetMapping("/userinfo")
    public Result<User> getUserInfo() {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserInfo(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}