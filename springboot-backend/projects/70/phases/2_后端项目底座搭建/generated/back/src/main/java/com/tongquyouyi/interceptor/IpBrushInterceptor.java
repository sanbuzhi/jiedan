package com.tongquyouyi.interceptor;

import com.tongquyouyi.common.ErrorCode;
import com.tongquyouyi.common.TqyException;
import com.tongquyouyi.config.RedisProperties;
import com.tongquyouyi.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * IP防刷拦截器
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Slf4j
@Component
public class IpBrushInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedisProperties redisProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getIpAddress(request);
        String key = "ip:brush:" + ip;

        Long count = redisUtils.increment(key);
        if (count == 1) {
            redisUtils.expire(key, redisProperties.getIpBrushExpireSeconds(), TimeUnit.SECONDS);
        }

        if (count > redisProperties.getIpBrushMaxCount()) {
            log.warn("IP {} 请求过于频繁，当前请求数：{}", ip, count);
            throw new TqyException(ErrorCode.IP_BRUSH);
        }

        return true;
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

}