package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SpExceptionHandler {

    // TODO: use customized exceptions and handle them here

    @ExceptionHandler(Throwable.class)
    public ResponseBody handleException(Throwable e) {
        e.printStackTrace();
        return new ResponseBody(Code.SYSTEM_ERR, null, e.toString());
    }
}
