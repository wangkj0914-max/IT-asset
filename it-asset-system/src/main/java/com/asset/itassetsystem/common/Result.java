package com.asset.itassetsystem.common;

import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class Result<T> {
    // 状态码：200成功，500失败
    private Integer code;
    // 返回信息
    private String msg;
    // 返回数据
    private T data;

    // 成功返回
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 失败返回
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    // 业务异常返回（HTTP 200，code非200标识业务错误）
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}