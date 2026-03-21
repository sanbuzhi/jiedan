package com.beauty.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 美妆小店JWT工具类
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Slf4j
public class JwtUtil {

    private JwtUtil() {
    }

    /**
     * 生成JWT令牌
     *
     * @param staffId     员工ID
     * @param loginAccount 员工登录账号
     * @param secretKey   JWT密钥
     * @param expireDays  有效期（天）
     * @return JWT令牌
     */
    public static String generateToken(Long staffId, String loginAccount, String secretKey, Integer expireDays) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("staffId", staffId);
        claims.put("loginAccount", loginAccount);
        claims.put("jti", IdUtil.fastSimpleUUID());
        Date now = new Date();
        Date expireDate = DateUtil.offsetDay(now, expireDays);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param token     JWT令牌
     * @param secretKey JWT密钥
     * @return Claims对象
     */
    public static Claims parseToken(String token, String secretKey) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证JWT令牌是否过期
     *
     * @param claims Claims对象
     * @return 是否过期
     */
    public static boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从JWT令牌中获取员工ID
     *
     * @param claims Claims对象
     * @return 员工ID
     */
    public static Long getStaffId(Claims claims) {
        return claims.get("staffId", Long.class);
    }

    /**
     * 从JWT令牌中获取登录账号
     *
     * @param claims Claims对象
     * @return 登录账号
     */
    public static String getLoginAccount(Claims claims) {
        return claims.get("loginAccount", String.class);
    }

    /**
     * 从JWT令牌中获取JTI
     *
     * @param claims Claims对象
     * @return JTI
     */
    public static String getJti(Claims claims) {
        return claims.getId();
    }

}