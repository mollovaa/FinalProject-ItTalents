package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.*;
import ittalents.javaee1.hibernate.CommentRepository;
import ittalents.javaee1.hibernate.NotificationRepository;
import ittalents.javaee1.hibernate.UserRepository;
import ittalents.javaee1.hibernate.VideoRepository;
import ittalents.javaee1.models.Comment;
import ittalents.javaee1.models.Notification;
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
@RequestMapping(value = "/comments")
public class CommentController extends GlobalController {

    private String COMMENTED_VIDEO_BY = "Your video has been commented by ";
    private String RESPONSED_TO_COMMENT = " responsed to your comment";

    private void validateComment(Comment comment) throws InvalidInputException {
        if (comment.getMessage() == null || comment.getMessage().isEmpty()) {
            throw new InvalidInputException(INVALID_COMMENT_MESSAGE);
        }
        comment.setDateOfPublication(LocalDate.now());
        comment.setNumberOfDislikes(0);
        comment.setNumberOfLikes(0);
    }

    @PostMapping(value = "/add/toVideo/{videoId}")
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
        //notify video`s owner
        long uploaderId = videoRepository.findById(videoId).get().getUploaderId();
        String writerName = userRepository.findById(SessionManager.getLoggedUserId(session)).get().getFullName();
        Notification notif = new Notification(COMMENTED_VIDEO_BY + writerName, uploaderId);
        notificationRepository.save(notif);
        return commentRepository.save(comment);
    }

    @PostMapping(value = "/{commentId}/response")
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
        //notify base comment`s owner
        long commentOwnerId = commentRepository.findById(commentId).get().getPublisherId();
        String writerName = userRepository.findById(SessionManager.getLoggedUserId(session)).get().getFullName();
        Notification notif = new Notification(writerName + RESPONSED_TO_COMMENT, commentOwnerId);
        notificationRepository.save(notif);
        return commentRepository.save(comment);
    }

    @PutMapping(value = "/{commentId}/like")
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

    @PutMapping(value = "/{commentId}/dislike")
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

    @DeleteMapping(value = "/{commentId}/remove")
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
        commentRepository.delete(comment);
        return new ErrorMessage(SUCCESSFULLY_REMOVED_COMMENT, HttpStatus.OK.value(), LocalDateTime.now());
    }
}




