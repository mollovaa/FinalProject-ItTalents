package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.hibernate.CommentRepository;
import ittalents.javaee1.hibernate.UserRepository;
import ittalents.javaee1.hibernate.VideoRepository;
import ittalents.javaee1.models.Comment;
import ittalents.javaee1.models.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.time.LocalDate;

import static ittalents.javaee1.controllers.ResponseMessages.*;

@RestController
public class CommentController implements GlobalController {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private UserRepository userRepository;

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
            if (!videoRepository.existsById(videoId)) {
                return responseForBadRequest(response, NOT_FOUND);
            } else {
                this.validateComment(comment);
                comment.setPublisherId(SessionManager.getLoggedUserId(session));
                comment.setVideoId(videoId);
                comment.setResponseToId(null);
                return commentRepository.save(comment);
            }
        }
    }

    @PostMapping(value = "comments/responseToComment/{commentId}")
    public Object responseComment(@RequestBody Comment comment, @PathVariable long commentId,
                                  HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            return redirectingToLogin(response);
        } else {
            if (!commentRepository.existsById(commentId)) {
                return responseForBadRequest(response, NOT_FOUND);
            } else {
                this.validateComment(comment);
                comment.setPublisherId(SessionManager.getLoggedUserId(session));
                comment.setVideoId(commentRepository.findById(commentId).get().getVideoId());
                comment.setResponseToId(commentId);
                return commentRepository.save(comment);
            }
        }
    }

    @GetMapping(value = "comments/likeComment/{commentId}")
    public Object likeVideo(@PathVariable long commentId, HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!commentRepository.existsById(commentId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                Comment comment = commentRepository.findById(commentId).get();
                User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
                if (user.getLikedComments().contains(comment)) {
                    return responseForBadRequest(response, ALREADY_LIKED_COMMENT);
                } else {
                    if (user.getDislikedComments().contains(comment)) {  //many to many select
                        user.getDislikedComments().remove(comment);
                        comment.getUsersDislikedComment().remove(user);
                        commentRepository.save(comment);
                        userRepository.save(user);
                        comment.setNumberOfDislikes(comment.getNumberOfDislikes() - 1);
                    }
                    comment.getUsersLikedComment().add(user);
                    user.addLikedComment(comment);
                    comment.setNumberOfLikes(comment.getNumberOfLikes() + 1);
                    commentRepository.save(comment);
                    userRepository.save(user);
                    return SUCCESSFULLY_LIKED_COMMENT;
                }
            }
        }
    }

    @GetMapping(value = "comments/dislikeComment/{commentId}")
    public Object dislikeVideo(@PathVariable long commentId, HttpSession session, HttpServletResponse response) throws
            BadRequestException {
        if (!commentRepository.existsById(commentId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                Comment comment = commentRepository.findById(commentId).get();
                User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
                if (user.getDislikedComments().contains(comment)) {
                    return responseForBadRequest(response, ALREADY_DISLIKED_COMMENT);
                } else {
                    if (user.getLikedComments().contains(comment)) {
                        user.getLikedComments().remove(comment);
                        comment.getUsersLikedComment().remove(user);
                        userRepository.save(user);
                        commentRepository.save(comment);
                        comment.setNumberOfLikes(comment.getNumberOfLikes() - 1);
                    }
                    comment.getUsersDislikedComment().add(user);
                    user.getDislikedComments().add(comment);
                    comment.setNumberOfDislikes(comment.getNumberOfDislikes() + 1);
                    commentRepository.save(comment);
                    userRepository.save(user);
                    return SUCCESSFULLY_DISLIKED_COMMENT;
                }
            }
        }
    }

    @GetMapping(value = "comments/removeComment/{commentId}")
    public Object removeVideo(@PathVariable long commentId, HttpSession session, HttpServletResponse response) throws
            BadRequestException {
        if (!commentRepository.existsById(commentId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (SessionManager.isLogged(session)) {
                User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
                Comment comment = commentRepository.findById(commentId).get();
                if (user.getUserId() == comment.getPublisherId()) {
                    commentRepository.deleteById(commentId);
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
