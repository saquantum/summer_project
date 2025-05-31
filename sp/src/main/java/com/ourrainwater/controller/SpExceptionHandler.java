package com.ourrainwater.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SpExceptionHandler {
    @ExceptionHandler(Throwable.class)
    public ResponseResult handleException(Throwable e) {
        return new ResponseResult(Code.SYSTEM_ERR, null, e.getMessage());
    }
}
