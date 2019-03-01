package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.*;
import ittalents.javaee1.models.Comment;
import ittalents.javaee1.models.Notification;
import ittalents.javaee1.models.User;

import ittalents.javaee1.models.dto.ViewCommentDTO;
import ittalents.javaee1.util.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/comments")
public class CommentController extends GlobalController {

    private static final String COMMENTED_VIDEO_BY = "Your video has been commented by ";
    private static final String RESPONSED_TO_COMMENT = " responsed to your comment";
    private static final String NO_RESPONSES = "No responses!";
    private static final String SUCCESSFULLY_REMOVED_COMMENT = "You have successfully removed a comment!";
    private static final String ALREADY_DISLIKED_COMMENT = "You have already disliked this comment!";
    private static final String INVALID_COMMENT_MESSAGE = "Invalid comment message!";
    private static final String ALREADY_LIKED_COMMENT = "You have already liked this comment!";

    private void validateComment(Comment comment) throws InvalidInputException {
        if (!isValidString(comment.getMessage())) {
            throw new InvalidInputException(INVALID_COMMENT_MESSAGE);
        }
        comment.setDateOfPublication(LocalDate.now());
        comment.setNumberOfDislikes(0);
        comment.setNumberOfLikes(0);
    }

    @GetMapping(value = "/{commentId}/responses/all")
    public Object[] showAllResponsesOnComment(@PathVariable long commentId) throws BadRequestException {
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        Comment comment = commentRepository.findById(commentId).get();
        List<Comment> responses = comment.getResponses();
        if (responses.isEmpty()) {
            throw new BadRequestException(NO_RESPONSES);
        }
        List<ViewCommentDTO> responsesToShow = new ArrayList<>();
        for (Comment c : responses) {
            responsesToShow.add(convertToCommentDTO(c));
        }
        return responsesToShow.toArray();
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
        return convertToCommentDTO(commentRepository.save(comment));
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
        return convertToCommentDTO(commentRepository.save(comment));
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
        return convertToCommentDTO(comment);
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
        return convertToCommentDTO(comment);
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




