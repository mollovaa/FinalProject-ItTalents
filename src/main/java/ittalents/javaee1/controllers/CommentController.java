package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.models.Comment;
import ittalents.javaee1.models.dao.CommentDao;
import ittalents.javaee1.models.dao.VideoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.time.LocalDate;

import static ittalents.javaee1.controllers.PlaylistController.redirectingToLogin;
import static ittalents.javaee1.controllers.ResponseMessages.*;

@RestController
public class CommentController {

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

    @PostMapping(value = "/commentVideo/{videoId}")
    public Object commentVideo(@RequestBody Comment comment, @PathVariable long videoId,
                               HttpSession session, HttpServletResponse response) {
        try {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                if (!videoDao.checkIfVideoExists(videoId)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return NOT_FOUND;
                } else {
                    try {
                        this.validateComment(comment);
                        comment.setPublisherId(SessionManager.getLoggedUserId(session));
                        comment.setVideoId(videoId);
                        commentDao.addCommentToVideo(comment);
                        return SUCCESSFULLY_COMMENTED_VIDEO;
                    } catch (SessionManager.ExpiredSessionException | InvalidInputException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return e.getMessage();
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
    }

    @PostMapping(value = "/responseComment/{commentId}")
    public Object responseComment(@RequestBody Comment comment, @PathVariable long commentId,
                                  HttpSession session, HttpServletResponse response) {
        try {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                if (!commentDao.checkIfCommentExists(commentId)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return NOT_FOUND;
                } else {
                    try {
                        this.validateComment(comment);
                        comment.setPublisherId(SessionManager.getLoggedUserId(session));
                        comment.setVideoId(commentDao.getVideoIdByComment(commentId));
                        comment.setResponseToId(commentId);
                        commentDao.addResponseToComment(comment);
                        return SUCCESSFULLY_COMMENTED_VIDEO;
                    } catch (SessionManager.ExpiredSessionException | InvalidInputException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return e.getMessage();
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
    }

    @GetMapping(value = "/likeComment/{commentId}")
    public Object likeVideo(@PathVariable long commentId, HttpSession session, HttpServletResponse response) {
        try {
            if (commentDao.checkIfCommentExists(commentId)) {
                // System.out.println(videoDao.checkIfVideoExists(videoId));  //
                if (SessionManager.isLogged(session)) {
                    try {
                        if (!commentDao.likeComment(commentId, SessionManager.getLoggedUserId(session))) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            return ALREADY_LIKED_COMMENT;
                        }
                    } catch (SessionManager.ExpiredSessionException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return EXPIRED_SESSION;
                    }
                    return SUCCESSFULLY_LIKED_COMMENT;
                } else {
                    return redirectingToLogin(response);
                }
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return NOT_FOUND;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
    }

    @GetMapping(value = "/dislikeComment/{commentId}")
    public Object dislikeVideo(@PathVariable long commentId, HttpSession session, HttpServletResponse response) {
        try {
            if (commentDao.checkIfCommentExists(commentId)) {
                if (SessionManager.isLogged(session)) {
                    try {
                        if (!commentDao.dislikeComment(commentId, SessionManager.getLoggedUserId(session))) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            return ALREADY_DISLIKED_COMMENT;
                        }
                    } catch (SessionManager.ExpiredSessionException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return EXPIRED_SESSION;
                    }
                    return SUCCESSFULLY_DISLIKED_COMMENT;
                } else {
                    return redirectingToLogin(response);
                }
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return NOT_FOUND;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
    }

    @GetMapping(value = "/removeComment/{commentId}")
    public Object removeVideo(@PathVariable long commentId, HttpSession session, HttpServletResponse response) {
        try {
            if (commentDao.checkIfCommentExists(commentId)) {
                if (SessionManager.isLogged(session)) {
                    try {
                        if (SessionManager.getLoggedUserId(session) == commentDao.getPublisherIdByComment(commentId)) {
                            commentDao.removeComment(commentId);
                            return SUCCESSFULLY_REMOVED_COMMENT;
                        } else {
                            return ACCESS_DENIED;
                        }
                    } catch (SessionManager.ExpiredSessionException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return EXPIRED_SESSION;
                    }
                } else {
                    return redirectingToLogin(response);
                }
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return NOT_FOUND;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return SERVER_ERROR;
        }
    }


}
