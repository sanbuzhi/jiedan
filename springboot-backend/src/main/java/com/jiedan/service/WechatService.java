package com.jiedan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiedan.dto.Token;
import com.jiedan.entity.User;
import com.jiedan.repository.UserRepository;
import com.jiedan.security.JwtTokenProvider;
import com.jiedan.util.ReferralCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WechatService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final RuleService ruleService;
    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wechat.appid:}")
    private String appid;

    @Value("${wechat.secret:}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    // 缓存access_token，避免频繁调用微信接口
    private final ConcurrentHashMap<String, String> accessTokenCache = new ConcurrentHashMap<>();
    private volatile long accessTokenExpireTime = 0;

    public String getWechatOpenid(String code) {
        // In development mode, return mock openid
        if (appid == null || appid.isEmpty() || secret == null || secret.isEmpty()) {
            return "mock_openid_" + code;
        }

        // Call WeChat API to get openid
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code
        );

        try {
            // Get response as String first
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            String responseBody = responseEntity.getBody();

            if (responseBody == null || responseBody.isEmpty()) {
                throw new RuntimeException("微信API返回空响应");
            }

            // Parse JSON manually
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            if (response.containsKey("openid")) {
                return (String) response.get("openid");
            }

            // Check for error
            if (response.containsKey("errcode")) {
                String errMsg = (String) response.getOrDefault("errmsg", "未知错误");
                throw new RuntimeException("微信API错误: " + errMsg);
            }

            throw new RuntimeException("获取微信openid失败: 响应中无openid");
        } catch (Exception e) {
            throw new RuntimeException("微信登录失败: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Token wechatLogin(String code, String nickname, String avatar, String referralCode) {
        String openid = getWechatOpenid(code);

        // Check if user exists by wx_openid
        User user = userRepository.findByWxOpenid(openid).orElse(null);

        boolean isNewUser = false;
        if (user == null) {
            // Create new user
            isNewUser = true;
            user = new User();
            user.setWxOpenid(openid);

            // Generate referral code first
            String newReferralCode = referralCodeGenerator.generate();
            user.setReferralCode(newReferralCode);

            // Use default nickname: 智搭小掌柜-{推荐码}
            user.setNickname(userProfileService.generateDefaultNickname(newReferralCode));

            // Use default avatar
            user.setAvatar(userProfileService.getDefaultAvatarUrl());

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
        }

        String token = jwtTokenProvider.generateToken(user.getId().toString());

        Token tokenResponse = new Token();
        tokenResponse.setAccessToken(token);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setNewUser(isNewUser);

        return tokenResponse;
    }

    /**
     * 获取微信小程序 access_token
     */
    public String getAccessToken() {
        // 检查缓存的token是否有效
        long now = System.currentTimeMillis();
        if (accessTokenCache.containsKey("access_token") && now < accessTokenExpireTime) {
            return accessTokenCache.get("access_token");
        }

        // 开发模式下返回mock token
        if (appid == null || appid.isEmpty() || secret == null || secret.isEmpty()) {
            return "mock_access_token";
        }

        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appid, secret
        );

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            String responseBody = responseEntity.getBody();

            if (responseBody == null || responseBody.isEmpty()) {
                throw new RuntimeException("获取access_token失败: 空响应");
            }

            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            if (response.containsKey("access_token")) {
                String accessToken = (String) response.get("access_token");
                Integer expiresIn = (Integer) response.getOrDefault("expires_in", 7200);
                // 提前5分钟过期，避免边界问题
                accessTokenExpireTime = now + (expiresIn - 300) * 1000L;
                accessTokenCache.put("access_token", accessToken);
                return accessToken;
            }

            if (response.containsKey("errcode")) {
                String errMsg = (String) response.getOrDefault("errmsg", "未知错误");
                throw new RuntimeException("获取access_token失败: " + errMsg);
            }

            throw new RuntimeException("获取access_token失败: 响应中无access_token");
        } catch (Exception e) {
            throw new RuntimeException("获取access_token异常: " + e.getMessage(), e);
        }
    }

    /**
     * 生成小程序二维码（无限制版）
     * @param scene 场景参数，格式: referral_code=ABC123
     * @param page 页面路径
     * @return 二维码图片字节数组
     */
    public byte[] generateUnlimitedQRCode(String scene, String page) {
        // 开发模式下返回模拟数据
        if (appid == null || appid.isEmpty() || secret == null || secret.isEmpty()) {
            // 返回一个1x1像素的透明PNG作为mock
            return new byte[]{
                    (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                    0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                    (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44,
                    0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x60, 0x00, 0x00,
                    0x00, 0x02, 0x00, 0x01, (byte) 0xE2, 0x21, (byte) 0xBC,
                    0x33, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E,
                    0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
            };
        }

        String accessToken = getAccessToken();
        String url = String.format(
                "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=%s",
                accessToken
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("scene", scene);
        requestBody.put("page", page);
        requestBody.put("width", 280);
        requestBody.put("check_path", false);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, byte[].class
            );

            byte[] responseBody = response.getBody();
            if (responseBody == null || responseBody.length == 0) {
                throw new RuntimeException("生成二维码失败: 空响应");
            }

            // 检查是否是错误响应（JSON格式）
            if (responseBody[0] == '{' && responseBody[responseBody.length - 1] == '}') {
                String errorJson = new String(responseBody);
                Map<String, Object> error = objectMapper.readValue(errorJson, Map.class);
                Integer errcode = (Integer) error.get("errcode");
                if (errcode != null && errcode != 0) {
                    String errmsg = (String) error.getOrDefault("errmsg", "未知错误");
                    throw new RuntimeException("生成二维码失败: " + errmsg);
                }
            }

            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException("生成二维码异常: " + e.getMessage(), e);
        }
    }

    /**
     * 生成推荐码二维码
     * @param referralCode 推荐码
     * @return 二维码图片字节数组
     */
    public byte[] generateReferralQRCode(String referralCode) {
        String scene = "referral_code=" + referralCode;
        String page = "pages/index/index";
        return generateUnlimitedQRCode(scene, page);
    }
}
