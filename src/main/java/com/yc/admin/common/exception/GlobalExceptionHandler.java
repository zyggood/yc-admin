package com.yc.admin.common.exception;

import com.yc.admin.common.core.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author YC
 * @since 2024
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 
     * @param e       业务异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@RequestBody）
     * 
     * @param e       参数校验异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("参数校验异常: {} - {}", request.getRequestURI(), errorMessage);
        return Result.error(400, "参数校验失败: " + errorMessage);
    }

    /**
     * 处理参数绑定异常（@ModelAttribute）
     * 
     * @param e       参数绑定异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("参数绑定异常: {} - {}", request.getRequestURI(), errorMessage);
        return Result.error(400, "参数绑定失败: " + errorMessage);
    }

    /**
     * 处理约束违反异常（@RequestParam）
     * 
     * @param e       约束违反异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("约束违反异常: {} - {}", request.getRequestURI(), errorMessage);
        return Result.error(400, "参数校验失败: " + errorMessage);
    }

    /**
     * 处理非法参数异常
     * 
     * @param e       非法参数异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(400, "参数错误: " + e.getMessage());
    }

    /**
     * 处理空指针异常
     * 
     * @param e       空指针异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统内部错误");
    }

    /**
     * 处理运行时异常
     * 
     * @param e       运行时异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统运行异常: " + e.getMessage());
    }

    /**
     * 处理其他所有异常
     * 
     * @param e       异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统异常，请联系管理员");
    }
}