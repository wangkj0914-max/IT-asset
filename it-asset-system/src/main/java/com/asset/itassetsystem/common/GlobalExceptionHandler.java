package com.asset.itassetsystem.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.fail("参数格式错误: " + e.getName());
    }

    @ExceptionHandler(NumberFormatException.class)
    public Result<String> handleNumberFormat(NumberFormatException e) {
        return Result.fail("参数格式错误");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail("缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntime(RuntimeException e) {
        log.warn("Runtime exception: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return Result.error("服务器内部错误");
    }
}
