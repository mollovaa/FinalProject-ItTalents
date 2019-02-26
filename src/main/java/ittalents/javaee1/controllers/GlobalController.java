package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;

import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.util.ErrorMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;


import java.time.LocalDateTime;

import static ittalents.javaee1.controllers.MyResponse.EXPIRED_SESSION;
import static ittalents.javaee1.controllers.MyResponse.SERVER_ERROR;

public abstract class GlobalController {
    private static Logger logger = LogManager.getLogger(GlobalController.class);

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleServerError(Exception e) {
        logger.error(e.getMessage(), e);
        return new ErrorMessage(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());
    }

    @ExceptionHandler({NotLoggedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorMessage handleNotLogged(Exception e) {
        logger.error(e.getMessage());
        return new ErrorMessage(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorMessage handleEmptyJsonBody(Exception e) {
        logger.error(e.getMessage(), e);
        return new ErrorMessage(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());
    }

    @ExceptionHandler({BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorMessage handleOwnException(Exception e) {
        logger.error(e.getMessage());
        return new ErrorMessage(e.getMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
    }


}
