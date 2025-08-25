package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.ac.bristol.exception.SpExceptions;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class SpExceptionHandler {

    @ExceptionHandler({SpExceptions.BadRequestException.class})
    public ResponseBody handleBadRequestException(HttpServletResponse response, SpExceptions.BadRequestException e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ResponseBody(Code.BAD_REQUEST, null, e.getMessage());
    }

    @ExceptionHandler({SpExceptions.DuplicateFieldException.class})
    public ResponseBody handleDuplicateFieldException(HttpServletResponse response, SpExceptions.DuplicateFieldException e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ResponseBody(Code.DUPLICATE_FIELD, null, e.getMessage());
    }

    @ExceptionHandler({SpExceptions.UnauthorisedException.class})
    public ResponseBody handleUnauthorisedException(HttpServletResponse response, SpExceptions.UnauthorisedException e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return new ResponseBody(Code.UNAUTHORISED, null, e.getMessage());
    }

    @ExceptionHandler({SpExceptions.ForbiddenException.class})
    public ResponseBody handleForbiddenException(HttpServletResponse response, SpExceptions.ForbiddenException e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return new ResponseBody(Code.FORBIDDEN, null, e.getMessage());
    }

    @ExceptionHandler({SpExceptions.NotFoundException.class})
    public ResponseBody handleNotFoundException(HttpServletResponse response, SpExceptions.NotFoundException e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return new ResponseBody(Code.NOT_FOUND, null, e.getMessage());
    }

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
