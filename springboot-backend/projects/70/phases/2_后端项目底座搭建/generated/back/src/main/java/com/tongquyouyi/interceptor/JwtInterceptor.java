package com.tongquyouyi.interceptor;

import com.tongquyouyi.common.ErrorCode;
import com.tongquyouyi.common.TqyException;
import com.tongquyouyi.config.JwtProperties;
import com.tongquyouyi.config.RedisProperties;
import com.tongquyouyi.utils.JwtUtils;
import com.tongquyouyi.utils.RedisUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT身份认证拦截器
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisProperties redisProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取Token
        String token = request.getHeader(jwtProperties.getHeader());
        if (token == null || !token.startsWith(jwtProperties.getPrefix())) {
            throw new TqyException(ErrorCode.UNAUTHORIZED);
        }

        // 去除Token前缀
        token = token.substring(jwtProperties.getPrefix().length());

        try {
            // 解析Token
            Claims claims = jwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            String userType = claims.get("userType").toString();

            // 验证Token是否在Redis中存在
            String redisTokenKey = "token:" + userType + ":" + userId;
            Object redisToken = redisUtils.get(redisTokenKey);
            if (redisToken == null || !redisToken.toString().equals(token)) {
                throw new TqyException(ErrorCode.TOKEN_INVALID);
            }

            // 将用户信息存入Request域，方便后续使用
            request.setAttribute("userId", userId);
            request.setAttribute("userType", userType);

            return true;
        } catch (ExpiredJwtException e) {
            throw new TqyException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new TqyException(ErrorCode.TOKEN_INVALID);
        }
    }

}