package com.leung.exception;

import com.leung.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description: 全局异常处理
 * @author: leung
 * @date: 2022-03-27 14:34
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 抛出ServiceException 调用该方法
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result handleServiceException(ServiceException se) {
        return Result.error(se.getCode(), se.getMessage());
    }
}
