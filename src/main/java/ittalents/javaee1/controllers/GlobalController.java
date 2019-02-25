package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;



import static ittalents.javaee1.controllers.ResponseMessages.EXPIRED_SESSION;
import static ittalents.javaee1.controllers.ResponseMessages.SERVER_ERROR;

public interface GlobalController {

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    default Object handleGlobalException(Exception e) {
        System.out.println(e.getMessage());
        return SERVER_ERROR;
    }

    @ExceptionHandler({BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    default Object handleOwnException(Exception e) {
        return e.getMessage();
    }

    @ResponseBody
    default Object redirectingToLogin(HttpServletResponse response) {
        try {
            response.sendRedirect("/login");
            return EXPIRED_SESSION;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
    }

    default Object responseForBadRequest(HttpServletResponse response, String message) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return message;
    }

}
