package com.jiedan.service;

import com.jiedan.dto.*;
import com.jiedan.entity.User;
import com.jiedan.repository.UserRepository;
import com.jiedan.security.JwtTokenProvider;
import com.jiedan.util.ReferralCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final RuleService ruleService;

    // In-memory storage for verification codes (use Redis in production)
    private final ConcurrentHashMap<String, String> verificationCodes = new ConcurrentHashMap<>();

    public void sendVerificationCode(String phone) {
        // Generate 6-digit code
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        verificationCodes.put(phone, code);
        
        // TODO: Integrate with SMS service
        System.out.println("Verification code for " + phone + ": " + code);
    }

    public boolean verifyCode(String phone, String code) {
        String storedCode = verificationCodes.get(phone);
        return storedCode != null && storedCode.equals(code);
    }

    @Transactional
    public Token register(String phone, String code, String nickname, String avatar, String referralCode) {
        if (!verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误");
        }

        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setReferralCode(referralCodeGenerator.generate());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Handle referrer
        if (referralCode != null && !referralCode.isEmpty()) {
            User referrer = userRepository.findByReferralCode(referralCode).orElse(null);
            if (referrer != null) {
                user.setReferrerId(referrer.getId());
            }
        }

        user = userRepository.save(user);

        // Grant referral points if has referrer
        if (user.getReferrerId() != null) {
            ruleService.grantReferralPoints(user);
        }

        // Clear verification code
        verificationCodes.remove(phone);

        String token = jwtTokenProvider.generateToken(user.getId().toString());
        
        Token tokenResponse = new Token();
        tokenResponse.setAccessToken(token);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setNewUser(true);
        
        return tokenResponse;
    }

    public Token login(String phone, String code) {
        if (!verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误");
        }

        User user = userRepository.findByPhone(phone).orElse(null);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // Clear verification code
        verificationCodes.remove(phone);

        String token = jwtTokenProvider.generateToken(user.getId().toString());
        
        Token tokenResponse = new Token();
        tokenResponse.setAccessToken(token);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setNewUser(false);
        
        return tokenResponse;
    }

    // 网页版登录 - 自动创建用户（如果不存在）
    @Transactional
    public Token webLogin(String phone) {
        User user = userRepository.findByPhone(phone).orElse(null);
        boolean isNewUser = false;
        
        if (user == null) {
            // 自动创建用户
            user = new User();
            user.setPhone(phone);
            user.setNickname("用户" + phone.substring(phone.length() - 4));
            user.setAvatar(null);
            user.setReferralCode(referralCodeGenerator.generate());
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
            isNewUser = true;
        }

        String token = jwtTokenProvider.generateToken(user.getId().toString());
        
        Token tokenResponse = new Token();
        tokenResponse.setAccessToken(token);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setNewUser(isNewUser);
        
        return tokenResponse;
    }
}
