package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.models.Comment;
import ittalents.javaee1.models.dao.CommentDao;
import ittalents.javaee1.models.dao.VideoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.time.LocalDate;


import static ittalents.javaee1.controllers.ResponseMessages.*;

@RestController
public class CommentController extends GlobalController {

    @Autowired
    private CommentDao commentDao;
    @Autowired
    private VideoDao videoDao;

    private void validateComment(Comment comment) throws InvalidInputException {
        if (comment.getMessage() == null || comment.getMessage().isEmpty()) {
            throw new InvalidInputException(INVALID_COMMENT_MESSAGE);
        }
        comment.setDateOfPublication(LocalDate.now());
        comment.setNumberOfDislikes(0);
        comment.setNumberOfLikes(0);
    }

    @PostMapping(value = "comments/commentVideo/{videoId}")
    public Object commentVideo(@RequestBody Comment comment, @PathVariable long videoId,
                               HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            return redirectingToLogin(response);
        } else {
            if (!videoDao.checkIfVideoExists(videoId)) {
                return responseForBadRequest(response, NOT_FOUND);
            } else {
                this.validateComment(comment);
                comment.setPublisherId(SessionManager.getLoggedUserId(session));
                comment.setVideoId(videoId);
                commentDao.addCommentToVideo(comment);
                return comment;
            }
        }
    }

    @PostMapping(value = "comments/responseToComment/{commentId}")
    public Object responseComment(@RequestBody Comment comment, @PathVariable long commentId,
                                  HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            return redirectingToLogin(response);
        } else {
            if (!commentDao.checkIfCommentExists(commentId)) {
                return responseForBadRequest(response, NOT_FOUND);
            } else {
                this.validateComment(comment);
                comment.setPublisherId(SessionManager.getLoggedUserId(session));
                comment.setVideoId(commentDao.getVideoIdByComment(commentId));
                comment.setResponseToId(commentId);
                commentDao.addResponseToComment(comment);
                return comment;
            }
        }
    }

    @GetMapping(value = "comments/likeComment/{commentId}")
    public Object likeVideo(@PathVariable long commentId, HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!commentDao.checkIfCommentExists(commentId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                if (!commentDao.likeComment(commentId, SessionManager.getLoggedUserId(session))) {
                    return responseForBadRequest(response, ALREADY_LIKED_COMMENT);
                }
                return SUCCESSFULLY_LIKED_COMMENT;
            }
        }
    }

    @GetMapping(value = "comments/dislikeComment/{commentId}")
    public Object dislikeVideo(@PathVariable long commentId, HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!commentDao.checkIfCommentExists(commentId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                if (!commentDao.dislikeComment(commentId, SessionManager.getLoggedUserId(session))) {
                    return responseForBadRequest(response, ALREADY_DISLIKED_COMMENT);
                }
                return SUCCESSFULLY_DISLIKED_COMMENT;
            }
        }
    }

    @GetMapping(value = "comments/removeComment/{commentId}")
    public Object removeVideo(@PathVariable long commentId, HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!commentDao.checkIfCommentExists(commentId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (SessionManager.isLogged(session)) {
                if (SessionManager.getLoggedUserId(session) == commentDao.getPublisherIdByComment(commentId)) {
                    commentDao.removeComment(commentId);
                    return SUCCESSFULLY_REMOVED_COMMENT;
                } else {
                    return responseForBadRequest(response, ACCESS_DENIED);
                }
            } else {
                return redirectingToLogin(response);
            }
        }
    }


}
