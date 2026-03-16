package com.jiedan.controller;

import com.jiedan.dto.Token;
import com.jiedan.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-code")
    public ResponseEntity<Map<String, String>> sendCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendVerificationCode(request.getPhone());
        return ResponseEntity.ok(Map.of("message", "验证码已发送"));
    }

    @PostMapping("/register")
    public ResponseEntity<Token> register(@Valid @RequestBody RegisterRequest request) {
        Token token = authService.register(
                request.getPhone(),
                request.getCode(),
                request.getNickname(),
                request.getAvatar(),
                request.getReferralCode()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@Valid @RequestBody LoginRequest request) {
        Token token = authService.login(request.getPhone(), request.getCode());
        return ResponseEntity.ok(token);
    }

    // 网页版登录接口 - 开发测试用，固定验证码
    @PostMapping("/web-login")
    public ResponseEntity<Token> webLogin(@Valid @RequestBody WebLoginRequest request) {
        // 固定验证码为 123456
        if (!"123456".equals(request.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Token token = authService.webLogin(request.getPhone());
        return ResponseEntity.ok(token);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendCodeRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;

        @NotBlank(message = "验证码不能为空")
        private String code;

        private String nickname;

        private String avatar;

        private String referralCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;

        @NotBlank(message = "验证码不能为空")
        private String code;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebLoginRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;

        @NotBlank(message = "验证码不能为空")
        private String code;
    }
}
