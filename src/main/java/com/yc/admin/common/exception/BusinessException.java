package com.yc.admin.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 业务异常类
 * 
 * @author YC
 * @since 2024
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 默认构造方法
     */
    public BusinessException() {
        super();
    }

    /**
     * 构造方法
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 构造方法
     * 
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造方法
     * 
     * @param message 错误消息
     * @param cause   原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }

    /**
     * 构造方法
     * 
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 静态方法：创建业务异常
     * 
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    /**
     * 静态方法：创建业务异常
     * 
     * @param code    错误码
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }

    /**
     * 静态方法：创建业务异常
     * 
     * @param message 错误消息
     * @param cause   原因
     * @return 业务异常
     */
    public static BusinessException of(String message, Throwable cause) {
        return new BusinessException(message, cause);
    }

    /**
     * 静态方法：创建业务异常
     * 
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原因
     * @return 业务异常
     */
    public static BusinessException of(Integer code, String message, Throwable cause) {
        return new BusinessException(code, message, cause);
    }
}