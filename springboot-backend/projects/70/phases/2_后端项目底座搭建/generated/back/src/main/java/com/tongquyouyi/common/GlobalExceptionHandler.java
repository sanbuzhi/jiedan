package com.tongquyouyi.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(TqyException.class)
    public Result<Void> handleTqyException(TqyException e) {
        log.warn("业务异常：code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常（@RequestBody）：{}", errorMsg);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * 参数校验异常处理（@RequestParam/@PathVariable）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常（@RequestParam/@PathVariable）：{}", errorMsg);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * 参数绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定异常：{}", errorMsg);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * 未知异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(ErrorCode.SYSTEM_ERROR);
    }

}