package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;

import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.hibernate.*;
import ittalents.javaee1.models.Notification;
import ittalents.javaee1.models.User;
import ittalents.javaee1.util.ErrorMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping(produces = "application/json")
public abstract class GlobalController {

    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VideoRepository videoRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    WatchHistoryRepository watchHistoryRepository;

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
    
    @ExceptionHandler({IOException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorMessage handleNotFoundFiles(Exception e) {
        logger.error(e.getMessage(), e);
        return new ErrorMessage(e.getMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
    }
    
    @ExceptionHandler({BadRequestException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorMessage handleOwnException(Exception e) {
        logger.error(e.getMessage(), e);
        return new ErrorMessage(e.getMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
    }

    boolean isValidString(String text) {
        return text != null && !text.isEmpty();
    }


}
