package com.jiedan.controller;

import com.jiedan.dto.Token;
import com.jiedan.service.WechatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class WechatController {

    private final WechatService wechatService;

    @PostMapping("/wechat-login")
    public ResponseEntity<Token> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        Token token = wechatService.wechatLogin(
                request.getCode(),
                request.getNickname(),
                request.getAvatar(),
                request.getReferralCode()
        );
        return ResponseEntity.ok(token);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WechatLoginRequest {
        @NotBlank(message = "微信code不能为空")
        private String code;

        private String nickname;

        private String avatar;

        private String referralCode;
    }
}
