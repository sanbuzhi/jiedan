package com.tongquyouyi.utils;

import com.tongquyouyi.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtUtils {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 获取签名密钥
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token
     *
     * @param userId   用户ID
     * @param userType 用户类型（admin/store）
     * @return Token
     */
    public String generateToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        return generateToken(claims, jwtProperties.getTokenExpireDays() * 24 * 60 * 60 * 1000L);
    }

    /**
     * 生成刷新Token
     *
     * @param userId   用户ID
     * @param userType 用户类型（admin/store）
     * @return 刷新Token
     */
    public String generateRefreshToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        return generateToken(claims, jwtProperties.getRefreshTokenExpireDays() * 24 * 60 * 60 * 1000L);
    }

    /**
     * 生成Token的通用方法
     */
    private String generateToken(Map<String, Object> claims, long expireTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token
     *
     * @param token Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期：{}", token);
            throw e;
        } catch (JwtException e) {
            log.error("Token解析失败：{}", token, e);
            throw e;
        }
    }

    /**
     * 获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 获取用户类型
     */
    public String getUserType(String token) {
        Claims claims = parseToken(token);
        return claims.get("userType").toString();
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

}