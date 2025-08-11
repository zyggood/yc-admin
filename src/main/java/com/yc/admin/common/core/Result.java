package com.yc.admin.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果封装类
 * 
 * @param <T> 数据类型
 * @author YC
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功状态码
     */
    public static final Integer SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final Integer ERROR_CODE = 500;

    /**
     * 成功消息
     */
    public static final String SUCCESS_MESSAGE = "操作成功";

    /**
     * 失败消息
     */
    public static final String ERROR_MESSAGE = "操作失败";

    /**
     * 返回成功结果（无数据）
     * 
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
    }

    /**
     * 返回成功结果（带数据）
     * 
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    /**
     * 返回成功结果（自定义消息）
     * 
     * @param message 消息
     * @param <T>     数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(SUCCESS_CODE, message, null);
    }

    /**
     * 返回成功结果（自定义消息和数据）
     * 
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    /**
     * 返回失败结果（默认消息）
     * 
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error() {
        return new Result<>(ERROR_CODE, ERROR_MESSAGE, null);
    }

    /**
     * 返回失败结果（自定义消息）
     * 
     * @param message 消息
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ERROR_CODE, message, null);
    }

    /**
     * 返回失败结果（自定义状态码和消息）
     * 
     * @param code    状态码
     * @param message 消息
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 返回失败结果（自定义状态码、消息和数据）
     * 
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 判断是否成功
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }

    /**
     * 判断是否失败
     * 
     * @return 是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}