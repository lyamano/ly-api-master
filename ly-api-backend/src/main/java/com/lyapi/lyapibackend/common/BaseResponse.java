package com.lyapi.lyapibackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Liu
 * @Date: 2023/08/31 07:45:24
 * @Version: 1.0
 * @Description: 通用返回类
 */
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = -1693660536490703953L;
    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,  data,"操作成功");
    }

    public static <T> BaseResponse<T> fail(String message) {
        return new BaseResponse<>(-1,  null,message);
    }

}