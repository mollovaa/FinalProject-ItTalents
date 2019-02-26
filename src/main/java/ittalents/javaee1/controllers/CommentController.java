package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.*;
import ittalents.javaee1.hibernate.CommentRepository;
import ittalents.javaee1.hibernate.UserRepository;
import ittalents.javaee1.hibernate.VideoRepository;
import ittalents.javaee1.models.Comment;
import ittalents.javaee1.models.User;

import ittalents.javaee1.util.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static ittalents.javaee1.controllers.MyResponse.*;

@RestController
public class CommentController extends GlobalController {

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
    public Object commentVideo(@RequestBody Comment comment, @PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        this.validateComment(comment);
        comment.setPublisherId(SessionManager.getLoggedUserId(session));
        comment.setVideoId(videoId);
        comment.setResponseToId(null);
        return commentRepository.save(comment);
    }

    @PostMapping(value = "comments/responseToComment/{commentId}")
    public Object responseComment(@RequestBody Comment comment, @PathVariable long commentId, HttpSession session)
            throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        this.validateComment(comment);
        comment.setPublisherId(SessionManager.getLoggedUserId(session));
        comment.setVideoId(commentRepository.findById(commentId).get().getVideoId());
        comment.setResponseToId(commentId);
        return commentRepository.save(comment);
    }

    @GetMapping(value = "comments/likeComment/{commentId}")
    public Object likeVideo(@PathVariable long commentId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        Comment comment = commentRepository.findById(commentId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getLikedComments().contains(comment)) {
            throw new BadRequestException(ALREADY_LIKED_COMMENT);
        }
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
        return comment;
    }

    @GetMapping(value = "comments/dislikeComment/{commentId}")
    public Object dislikeVideo(@PathVariable long commentId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        Comment comment = commentRepository.findById(commentId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getDislikedComments().contains(comment)) {
            throw new BadRequestException(ALREADY_DISLIKED_COMMENT);
        }
        if (user.getLikedComments().contains(comment)) {   //if liked -> remove the like
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
        return comment;
    }

    @GetMapping(value = "comments/removeComment/{commentId}")
    public Object removeVideo(@PathVariable long commentId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        Comment comment = commentRepository.findById(commentId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getUserId() != comment.getPublisherId()) {
            throw new AccessDeniedException();
        }
        commentRepository.deleteById(commentId);
        return new ErrorMessage(SUCCESSFULLY_REMOVED_COMMENT, HttpStatus.OK.value(), LocalDateTime.now());
    }
}




