package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.ac.bristol.exception.SpExceptions;

@RestControllerAdvice
public class SpExceptionHandler {

    @ExceptionHandler(SpExceptions.GetMethodException.class)
    public ResponseBody handleGetException(SpExceptions.GetMethodException e) {
        e.printStackTrace();
        return new ResponseBody(Code.SELECT_ERR, null, e.toString());
    }

    @ExceptionHandler(SpExceptions.PostMethodException.class)
    public ResponseBody handlePostException(SpExceptions.PostMethodException e) {
        e.printStackTrace();
        return new ResponseBody(Code.INSERT_ERR, null, e.toString());
    }

    @ExceptionHandler(SpExceptions.PutMethodException.class)
    public ResponseBody handlePutException(SpExceptions.PutMethodException e) {
        e.printStackTrace();
        return new ResponseBody(Code.UPDATE_ERR, null, e.toString());
    }

    @ExceptionHandler(SpExceptions.DeleteMethodException.class)
    public ResponseBody handleDeleteException(SpExceptions.DeleteMethodException e) {
        e.printStackTrace();
        return new ResponseBody(Code.DELETE_ERR, null, e.toString());
    }

    @ExceptionHandler(SpExceptions.SystemException.class)
    public ResponseBody handleSystemException(SpExceptions.SystemException e) {
        e.printStackTrace();
        return new ResponseBody(Code.SYSTEM_ERR, null, e.toString());
    }

    @ExceptionHandler(SpExceptions.BusinessException.class)
    public ResponseBody handleBusinessException(SpExceptions.BusinessException e) {
        e.printStackTrace();
        return new ResponseBody(Code.BUSINESS_ERR, null, e.toString());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseBody handleException(Throwable e) {
        e.printStackTrace();
        return new ResponseBody(Code.SYSTEM_ERR, null,
                "Untracked error threw as system exception, please contact for admin support."
                + System.lineSeparator()
                + e.getMessage());
    }
}
