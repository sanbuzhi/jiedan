===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/exception/BeautyBusinessException.java===
package com.beauty.common.exception;

import lombok.Getter;

/**
 * 美容店管理系统业务异常
 */
@Getter
public class BeautyBusinessException extends RuntimeException {

    private final Integer code;

    public BeautyBusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BeautyBusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BeautyBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }
}

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/exception/GlobalExceptionHandler.java===
package com.beauty.common.exception;

import com.beauty.common.result.BeautyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BeautyBusinessException.class)
    public BeautyResult<Void> handleBeautyBusinessException(BeautyBusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return BeautyResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BeautyResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null ? 
                e.getBindingResult().getFieldError().getDefaultMessage() : "参数校验失败";
        log.error("参数校验异常：{}", message);
        return BeautyResult.error(400, message);
    }

    /**
     * 处理参数绑定异常（@RequestParam/@PathVariable）
     */
    @ExceptionHandler(BindException.class)
    public BeautyResult<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldError() != null ? 
                e.getBindingResult().getFieldError().getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定异常：{}", message);
        return BeautyResult.error(400, message);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public BeautyResult<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return BeautyResult.error("系统异常，请联系管理员");
    }
}

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/dto/auth/StaffLoginDTO.java===
package com.beauty.common.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 员工登录DTO
 */
@Data
@Schema(description = "员工登录DTO")
public class StaffLoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;
}

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/vo/auth/StaffLoginVO.java===
package com.beauty.common.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 员工登录VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工登录VO")
public class StaffLoginVO {

    @Schema(description = "员工ID")
    private Long staffId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

    @Schema(description = "权限码列表")
    private List<String> permissionCodes;

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;
}

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/dto/staff/StaffPageQueryDTO.java===
package com.beauty.common.dto.staff;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工分页查询DTO
 */
@Data
@Schema(description = "员工分页查询DTO")
public class StaffPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "用户名（模糊查询）")
    private String username;

    @Schema(description = "真实姓名（模糊查询）")
    private String realName;

    @Schema(description = "手机号（模糊查询）")
    private String phone;

    @Schema(description = "状态")
    private Integer status;
}

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/vo/staff/StaffVO.java===
package com.beauty.common.vo.staff;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工VO")
public class StaffVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

    @Schema(description = "角色名称列表")
    private List<String> roleNames;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

===FILE:beauty-shop-manage/beauty-shop-manage-common/src/main/java/com/beauty/common/util/CurrentUserUtil.java===
package com.beauty.common.util;

import com.beauty.common.constant.BeautyAuthConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * 当前用户工具类
 */
public class CurrentUserUtil {

    private CurrentUserUtil() {
    }

    /**
     * 获取当前登录员工ID
     */
    public static Long getCurrentStaffId() {
        HttpServletRequest request = getRequest();
        Object staffId = request.getAttribute(BeautyAuthConstant.CURRENT_STAFF_ID);
        return staffId != null ? Long.valueOf(staffId.toString()) : null;
    }

    /**
     * 获取当前登录员工ID（带默认值）
     */
    public static Long getCurrentStaffIdOrDefault(Long defaultValue) {
        return Optional.ofNullable(getCurrentStaffId()).orElse(defaultValue);
    }

    /**
     * 获取当前登录员工用户名
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getRequest();
        Object username = request.getAttribute(BeautyAuthConstant.CURRENT_USERNAME);
        return username != null ? username.toString() : null;
    }

    /**
     * 获取HttpServletRequest
     */
    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("当前上下文不存在HttpServletRequest");
        }
        return attributes.getRequest();
    }

    /**
     * 设置当前用户信息到请求域
     */
    public static void setCurrentUserInfo(Long staffId, String username) {
        HttpServletRequest request = getRequest();
        request.setAttribute(BeautyAuthConstant.CURRENT_STAFF_ID, staffId);
        request.setAttribute(BeautyAuthConstant.CURRENT_USERNAME, username);
    }
}

===FILE:beauty-shop-manage/beauty-shop-manage-config/src/main/java/com/beauty/config/JwtAuthenticationInterceptor.java===
package com.beauty.config;

import com.beauty.common.constant.BeautyAuthConstant;
import com.beauty.common.exception.BeautyBusinessException;
import com.beauty.common.util.CurrentUserUtil;
import com.beauty.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的token
        String token = request.getHeader(BeautyAuthConstant.AUTHORIZATION_HEADER);
        if (token == null || !token.startsWith(BeautyAuthConstant.BEARER_PREFIX)) {
            throw new BeautyBusinessException(401, "未登录或登录已过期");
        }

        // 去掉Bearer前缀
        token = token.substring(BeautyAuthConstant.BEARER_PREFIX.length());

        try {
            // 解析token
            Claims claims = jwtUtil.parseToken(token);
            Long staffId = claims.get(BeautyAuthConstant.CLAIM_KEY_STAFF_ID, Long.class);
            String username = claims.get(BeautyAuthConstant.CLAIM_KEY_USERNAME, String.class);

            // 将当前用户信息设置到请求域
            CurrentUserUtil.setCurrentUserInfo(staffId, username);
            log.info("当前登录员工：staffId={}, username={}", staffId, username);
            return true;
        } catch (Exception e) {
            log.error("token解析失败：", e);
            throw new BeautyBusinessException(401, "未登录或登录已过期");
        }
    }
}

===FILE:beauty-shop-manage/beauty-shop-manage-config/src/main/java/com/beauty/config/WebMvcConfig.java===
package com.beauty.config;

import com.beauty.common.constant.BeautyAuthConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 登录相关
                        "/auth/**",
                        // Swagger相关
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        // 健康检查
                        "/actuator/**"
                );
    }
}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/service/AuthService.java===
package com.beauty.domain.service;

import com.beauty.common.dto.auth.StaffLoginDTO;
import com.beauty.common.vo.auth.StaffLoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 员工登录
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    StaffLoginVO staffLogin(StaffLoginDTO loginDTO);

    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshToken(String refreshToken);

    /**
     * 员工登出
     */
    void staffLogout();
}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/service/impl/AuthServiceImpl.java===
package com.beauty.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.beauty.common.constant.BeautyAuthConstant;
import com.beauty.common.dto.auth.StaffLoginDTO;
import com.beauty.common.enums.BeautyStaffStatus;
import com.beauty.common.exception.BeautyBusinessException;
import com.beauty.common.util.JwtUtil;
import com.beauty.common.util.SHA256Util;
import com.beauty.common.vo.auth.StaffLoginVO;
import com.beauty.domain.entity.BeautyRole;
import com.beauty.domain.entity.BeautyStaff;
import com.beauty.domain.mapper.BeautyRoleMapper;
import com.beauty.domain.mapper.BeautyStaffMapper;
import com.beauty.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final BeautyStaffMapper beautyStaffMapper;
    private final BeautyRoleMapper beautyRoleMapper;
    private final JwtUtil jwtUtil;

    @Override
    public StaffLoginVO staffLogin(StaffLoginDTO loginDTO) {
        // 1. 根据用户名查询员工
        LambdaQueryWrapper<BeautyStaff> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BeautyStaff::getUsername, loginDTO.getUsername());
        BeautyStaff staff = beautyStaffMapper.selectOne(queryWrapper);

        // 2. 校验员工是否存在
        if (staff == null) {
            throw new BeautyBusinessException("用户名或密码错误");
        }

        // 3. 校验密码是否正确
        String encryptedPassword = SHA256Util.encrypt(loginDTO.getPassword());
        if (!encryptedPassword.equals(staff.getPassword())) {
            throw new BeautyBusinessException("用户名或密码错误");
        }

        // 4. 校验员工状态
        if (!BeautyStaffStatus.ACTIVE.getCode().equals(staff.getStatus())) {
            throw new BeautyBusinessException("员工已被禁用");
        }

        // 5. 查询员工角色
        List<BeautyRole> roles = beautyRoleMapper.selectByStaffId(staff.getId());
        List<Long> roleIds = roles.stream().map(BeautyRole::getId).collect(Collectors.toList());
        List<String> permissionCodes = roles.stream()
                .map(BeautyRole::getPermissionCodes)
                .filter(codes -> codes != null && !codes.isEmpty())
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        // 6. 生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put(BeautyAuthConstant.CLAIM_KEY_STAFF_ID, staff.getId());
        claims.put(BeautyAuthConstant.CLAIM_KEY_USERNAME, staff.getUsername());
        String accessToken = jwtUtil.generateToken(claims, BeautyAuthConstant.ACCESS_TOKEN_EXPIRE_TIME);
        String refreshToken = jwtUtil.generateToken(claims, BeautyAuthConstant.REFRESH_TOKEN_EXPIRE_TIME);

        // 7. 组装返回结果
        return StaffLoginVO.builder()
                .staffId(staff.getId())
                .username(staff.getUsername())
                .realName(staff.getRealName())
                .avatar(staff.getAvatar())
                .roleIds(roleIds)
                .permissionCodes(permissionCodes)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public String refreshToken(String refreshToken) {
        try {
            // 解析刷新令牌
            Map<String, Object> claims = jwtUtil.parseToken(refreshToken);
            Long staffId = (Long) claims.get(BeautyAuthConstant.CLAIM_KEY_STAFF_ID);
            String username = (String) claims.get(BeautyAuthConstant.CLAIM_KEY_USERNAME);

            // 校验员工是否存在
            BeautyStaff staff = beautyStaffMapper.selectById(staffId);
            if (staff == null || !BeautyStaffStatus.ACTIVE.getCode().equals(staff.getStatus())) {
                throw new BeautyBusinessException(401, "员工状态异常");
            }

            // 生成新的访问令牌
            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put(BeautyAuthConstant.CLAIM_KEY_STAFF_ID, staffId);
            newClaims.put(BeautyAuthConstant.CLAIM_KEY_USERNAME, username);
            return jwtUtil.generateToken(newClaims, BeautyAuthConstant.ACCESS_TOKEN_EXPIRE_TIME);
        } catch (Exception e) {
            throw new BeautyBusinessException(401, "刷新令牌无效或已过期");
        }
    }

    @Override
    public void staffLogout() {
        // 这里可以将token加入黑名单，暂时先不实现
        log.info("员工登出，staffId={}", com.beauty.common.util.CurrentUserUtil.getCurrentStaffId());
    }
}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/service/BeautyStaffService.java===
package com.beauty.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beauty.common.dto.staff.StaffPageQueryDTO;
import com.beauty.common.vo.staff.StaffVO;
import com.beauty.domain.entity.BeautyStaff;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 员工服务接口
 */
public interface BeautyStaffService extends IService<BeautyStaff> {

    /**
     * 分页查询员工
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<StaffVO> pageQuery(StaffPageQueryDTO queryDTO);

    /**
     * 根据ID查询员工VO
     * @param id 员工ID
     * @return 员工VO
     */
    StaffVO getStaffVOById(Long id);
}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/java/com/beauty/domain/service/impl/BeautyStaffServiceImpl.java===
package com.beauty.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beauty.common.dto.staff.StaffPageQueryDTO;
import com.beauty.common.enums.BeautyStaffStatus;
import com.beauty.common.exception.BeautyBusinessException;
import com.beauty.common.vo.staff.StaffVO;
import com.beauty.domain.entity.BeautyRole;
import com.beauty.domain.entity.BeautyStaff;
import com.beauty.domain.mapper.BeautyRoleMapper;
import com.beauty.domain.mapper.BeautyStaffMapper;
import com.beauty.domain.service.BeautyStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工服务实现
 */
@Service
@RequiredArgsConstructor
public class BeautyStaffServiceImpl extends ServiceImpl<BeautyStaffMapper, BeautyStaff> implements BeautyStaffService {

    private final BeautyRoleMapper beautyRoleMapper;

    @Override
    public Page<StaffVO> pageQuery(StaffPageQueryDTO queryDTO) {
        // 1. 构建分页对象
        Page<BeautyStaff> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<BeautyStaff> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(queryDTO.getUsername()), BeautyStaff::getUsername, queryDTO.getUsername())
                .like(StringUtils.hasText(queryDTO.getRealName()), BeautyStaff::getRealName, queryDTO.getRealName())
                .like(StringUtils.hasText(queryDTO.getPhone()), BeautyStaff::getPhone, queryDTO.getPhone())
                .eq(queryDTO.getStatus() != null, BeautyStaff::getStatus, queryDTO.getStatus())
                .orderByDesc(BeautyStaff::getCreateTime);

        // 3. 执行查询
        Page<BeautyStaff> staffPage = this.page(page, queryWrapper);

        // 4. 转换为VO
        Page<StaffVO> voPage = new Page<>(staffPage.getCurrent(), staffPage.getSize(), staffPage.getTotal());
        List<StaffVO> voList = staffPage.getRecords().stream().map(this::convertToStaffVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public StaffVO getStaffVOById(Long id) {
        BeautyStaff staff = this.getById(id);
        if (staff == null) {
            throw new BeautyBusinessException("员工不存在");
        }
        return convertToStaffVO(staff);
    }

    /**
     * 员工实体转换为VO
     */
    private StaffVO convertToStaffVO(BeautyStaff staff) {
        StaffVO vo = new StaffVO();
        BeanUtils.copyProperties(staff, vo);

        // 设置状态描述
        vo.setStatusDesc(BeautyStaffStatus.getDescByCode(staff.getStatus()));

        // 查询并设置角色信息
        List<BeautyRole> roles = beautyRoleMapper.selectByStaffId(staff.getId());
        vo.setRoleIds(roles.stream().map(BeautyRole::getId).collect(Collectors.toList()));
        vo.setRoleNames(roles.stream().map(BeautyRole::getRoleName).collect(Collectors.toList()));

        return vo;
    }
}

===FILE:beauty-shop-manage/beauty-shop-manage-app/src/main/java/com/beauty/controller/AuthController.java===
package com.beauty.controller;

import com.beauty.common.dto.auth.StaffLoginDTO;
import com.beauty.common.result.BeautyResult;
import com.beauty.common.vo.auth.StaffLoginVO;
import com.beauty.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "员工登录")
    @PostMapping("/staff/login")
    public BeautyResult<StaffLoginVO> staffLogin(@Valid @RequestBody StaffLoginDTO loginDTO) {
        StaffLoginVO loginVO = authService.staffLogin(loginDTO);
        return BeautyResult.success(loginVO);
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public BeautyResult<String> refreshToken(@RequestParam String refreshToken) {
        String accessToken = authService.refreshToken(refreshToken);
        return BeautyResult.success(accessToken);
    }

    @Operation(summary = "员工登出")
    @PostMapping("/staff/logout")
    public BeautyResult<Void> staffLogout() {
        authService.staffLogout();
        return BeautyResult.success();
    }
}

===FILE:beauty-shop-manage/beauty-shop-manage-app/src/main/java/com/beauty/controller/BeautyStaffController.java===
package com.beauty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beauty.common.dto.staff.StaffPageQueryDTO;
import com.beauty.common.result.BeautyPageResult;
import com.beauty.common.result.BeautyResult;
import com.beauty.common.vo.staff.StaffVO;
import com.beauty.domain.entity.BeautyStaff;
import com.beauty.domain.service.BeautyStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理控制器
 */
@Tag(name = "员工管理", description = "员工管理相关接口")
@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class BeautyStaffController {

    private final BeautyStaffService beautyStaffService;

    @Operation(summary = "分页查询员工")
    @GetMapping("/page")
    public BeautyPageResult<StaffVO> pageQuery(StaffPageQueryDTO queryDTO) {
        Page<StaffVO> page = beautyStaffService.pageQuery(queryDTO);
        return BeautyPageResult.success(page);
    }

    @Operation(summary = "根据ID查询员工")
    @GetMapping("/{id}")
    public BeautyResult<StaffVO> getById(@PathVariable Long id) {
        StaffVO staffVO = beautyStaffService.getStaffVOById(id);
        return BeautyResult.success(staffVO);
    }

    @Operation(summary = "新增员工")
    @PostMapping
    public BeautyResult<Void> save(@RequestBody BeautyStaff staff) {
        beautyStaffService.save(staff);
        return BeautyResult.success();
    }

    @Operation(summary = "修改员工")
    @PutMapping
    public BeautyResult<Void> updateById(@RequestBody BeautyStaff staff) {
        beautyStaffService.updateById(staff);
        return BeautyResult.success();
    }

    @Operation(summary = "删除员工")
    @DeleteMapping("/{id}")
    public BeautyResult<Void> removeById(@PathVariable Long id) {
        beautyStaffService.removeById(id);
        return BeautyResult.success();
    }
}

===FILE:beauty-shop-manage/beauty-shop-manage-domain/src/main/resources/mapper/BeautyRoleMapper.xml===
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.beauty.domain.mapper.BeautyRoleMapper">

    <!-- 根据员工ID查询角色列表 -->
    <select id="selectByStaffId" resultType="com.beauty.domain.entity.BeautyRole">
        SELECT br.* FROM beauty_role br
        INNER JOIN beauty_staff_role bsr ON br.id = bsr.role_id
        WHERE bsr.staff_id = #{staffId}
        AND br.deleted = 0
    </select>

</mapper>