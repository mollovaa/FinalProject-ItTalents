package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;

import ittalents.javaee1.exceptions.NotFoundException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.hibernate.*;
import ittalents.javaee1.models.Comment;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.dto.VideoInPlaylistDTO;
import ittalents.javaee1.models.dto.ViewCommentDTO;
import ittalents.javaee1.models.dto.ViewVideoDTO;
import ittalents.javaee1.util.ErrorMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import java.io.IOException;
import java.time.LocalDateTime;

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

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorMessage handleNotFound(NotFoundException e) {
        logger.error(e.getMessage(), e);
        return new ErrorMessage(e.getMessage(), HttpStatus.NOT_FOUND.value(), LocalDateTime.now());
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

    ViewCommentDTO convertToCommentDTO(Comment comment) {
        return new ViewCommentDTO(comment.getCommentId(), comment.getMessage(), comment.getDateOfPublication(),
                comment.getNumberOfLikes(), comment.getNumberOfDislikes(),
                userRepository.findById(comment.getPublisherId()).get().getFullName(), comment.getResponses().size());
    }


    ViewVideoDTO convertToViewVideoDTO(Video video) {
        return new ViewVideoDTO(video.getVideoId(), video.getTitle(), video.getCategory(), video.getDescription(),
                video.getUploadDate(), video.getDuration(), video.getNumberOfLikes(), video.getNumberOfDislikes(),
                video.getNumberOfViews(), userRepository.findById(video.getUploaderId()).get().getFullName(),
                video.getComments().size());
    }

    VideoInPlaylistDTO convertToVideoInPlaylistDTO(Video video) {
        return new VideoInPlaylistDTO(video.getVideoId(), video.getTitle(),
                userRepository.findById(video.getUploaderId()).get().getFullName());
    }


}
