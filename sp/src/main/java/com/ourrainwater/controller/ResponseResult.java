package com.ourrainwater.controller;

public class ResponseResult {
    private Integer code;
    private Object data;
    private String message;

    public ResponseResult() {
    }

    public ResponseResult(Integer code, Object data) {
        this.data = data;
        this.code = code;
    }

    public ResponseResult(Integer code, Object data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
