package ittalents.javaee1.controllers;


import ittalents.javaee1.util.SessionManager;
import ittalents.javaee1.util.exceptions.*;
import ittalents.javaee1.models.pojo.Comment;
import ittalents.javaee1.models.pojo.Notification;
import ittalents.javaee1.models.pojo.User;

import ittalents.javaee1.models.pojo.Video;
import ittalents.javaee1.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/comments")
public class CommentController extends GlobalController {

    private static final String COMMENTED_VIDEO_BY = "Your video has been commented by ";
    private static final String RESPOND_TO_COMMENT = " respond to your comment";
    private static final String NO_RESPONSES = "No responses!";
    private static final String SUCCESSFULLY_REMOVED_COMMENT = "Successfully removed comment!";
    private static final String ALREADY_DISLIKED_COMMENT = "Already disliked this comment!";
    private static final String INVALID_COMMENT_MESSAGE = "Invalid comment message!";
    private static final String ALREADY_LIKED_COMMENT = "Already liked this comment!";
    private static final String COMMENT_NOT_DISLIKED = "Comment not disliked!";
    private static final String COMMENT_NOT_LIKED = "Comment not liked!";

    private void validateComment(Comment comment) throws InvalidInputException {
        if (!isValidString(comment.getMessage())) {
            throw new InvalidInputException(INVALID_COMMENT_MESSAGE);
        }
    }

    private void setInitialCommentValues(Comment comment) {
        comment.setDateOfPublication(LocalDate.now());
        comment.setNumberOfDislikes(0);
        comment.setNumberOfLikes(0);
    }

    @GetMapping(value = "/{commentId}/responses/all")
    public Object showAllResponsesOnComment(@PathVariable long commentId) throws BadRequestException {
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        Comment comment = commentRepository.findById(commentId).get();
        List<Comment> responses = comment.getResponses();
        if (responses.isEmpty()) {
            return new ResponseMessage(NO_RESPONSES, HttpStatus.OK.value(), LocalDateTime.now());
        }
        return responses
                .stream()
                .map(c -> c.convertToViewCommentDTO(userRepository))
                .collect(Collectors.toList());
    }

    private void notifyVideosOwnerForComment(long videoId, HttpSession session) throws NotLoggedException {
        long uploaderId = videoRepository.findById(videoId).get().getUploaderId();
        if (uploaderId != SessionManager.getLoggedUserId(session)) { //if owner comments -> no notification is send
            String writerName = userRepository.findById(SessionManager.getLoggedUserId(session)).get().getFullName();
            Notification notif = new Notification(COMMENTED_VIDEO_BY + writerName, uploaderId);
            notificationRepository.save(notif);
        }
    }

    @PostMapping(value = "/add/toVideo/{videoId}")
    public Object commentVideo(@RequestBody Comment comment, @PathVariable long videoId, HttpSession session)
            throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.getByVideoId(videoId);
        User user = userRepository.getByUserId(SessionManager.getLoggedUserId(session));
        if (video.isPrivate() && video.getUploaderId() != user.getUserId()) {  //private and not logged user`s
            throw new VideoNotFoundException();
        }
        this.validateComment(comment);
        this.setInitialCommentValues(comment);
        comment.setPublisherId(SessionManager.getLoggedUserId(session));
        comment.setVideoId(videoId);
        comment.setResponseToId(null);
        commentRepository.save(comment);
        this.notifyVideosOwnerForComment(videoId, session);

        return comment.convertToViewCommentDTO(userRepository);
    }

    //notify base comment`s owner
    private void notifyBaseCommentsOwner(long commentId, HttpSession session) throws NotLoggedException {
        long commentOwnerId = commentRepository.findById(commentId).get().getPublisherId();
        if (commentOwnerId != SessionManager.getLoggedUserId(session)) {  //if owner responses to owned comment
            String writerName = userRepository.findById(SessionManager.getLoggedUserId(session)).get().getFullName();
            Notification notif = new Notification(writerName + RESPOND_TO_COMMENT, commentOwnerId);
            notificationRepository.save(notif);
        }
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
        this.setInitialCommentValues(comment);
        comment.setPublisherId(SessionManager.getLoggedUserId(session));
        comment.setVideoId(commentRepository.findById(commentId).get().getVideoId());
        comment.setResponseToId(commentId);
        commentRepository.save(comment);
        this.notifyBaseCommentsOwner(commentId, session);

        return comment.convertToViewCommentDTO(userRepository);
    }


    private void saveUserAndComment(User user, Comment comment) {
        commentRepository.save(comment);
        userRepository.save(user);
    }

    private void removeDislike(User user, Comment comment) {
        user.getDislikedComments().remove(comment);
        comment.getUsersDislikedComment().remove(user);
        comment.setNumberOfDislikes(comment.getNumberOfDislikes() - 1);
        this.saveUserAndComment(user, comment);
    }

    private void removeLike(User user, Comment comment) {
        user.getLikedComments().remove(comment);
        comment.getUsersLikedComment().remove(user);
        comment.setNumberOfLikes(comment.getNumberOfLikes() - 1);
        this.saveUserAndComment(user, comment);
    }

    private void addLike(User user, Comment comment) {
        comment.getUsersLikedComment().add(user);
        user.addLikedComment(comment);
        comment.setNumberOfLikes(comment.getNumberOfLikes() + 1);
        this.saveUserAndComment(user, comment);
    }

    private void addDislike(User user, Comment comment) {
        comment.getUsersDislikedComment().add(user);
        user.getDislikedComments().add(comment);
        comment.setNumberOfDislikes(comment.getNumberOfDislikes() + 1);
        this.saveUserAndComment(user, comment);
    }

    @Transactional
    @PutMapping(value = "/{commentId}/like")
    public Object likeComment(@PathVariable long commentId, HttpSession session) throws BadRequestException {
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
        if (user.getDislikedComments().contains(comment)) {  //if disliked -> remove the dislike
            this.removeDislike(user, comment);
        }
        this.addLike(user, comment);
        return comment.convertToViewCommentDTO(userRepository);
    }

    @Transactional
    @PutMapping(value = "/{commentId}/likes/remove")
    public Object removeLike(@PathVariable long commentId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        Comment comment = commentRepository.findById(commentId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (!user.getLikedComments().contains(comment)) {
            throw new BadRequestException(COMMENT_NOT_LIKED);
        }
        this.removeLike(user, comment);
        return comment.convertToViewCommentDTO(userRepository);
    }

    @Transactional
    @PutMapping(value = "/{commentId}/dislikes/remove")
    public Object removeDislike(@PathVariable long commentId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        Comment comment = commentRepository.findById(commentId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (!user.getDislikedComments().contains(comment)) {
            throw new BadRequestException(COMMENT_NOT_DISLIKED);
        }
        this.removeDislike(user, comment);
        return comment.convertToViewCommentDTO(userRepository);
    }

    @Transactional
    @PutMapping(value = "/{commentId}/dislike")
    public Object dislikeComment(@PathVariable long commentId, HttpSession session) throws BadRequestException {
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
            this.removeLike(user, comment);
        }
        this.addDislike(user, comment);
        return comment.convertToViewCommentDTO(userRepository);
    }

    @DeleteMapping(value = "/{commentId}/remove")
    public Object removeComment(@PathVariable long commentId, HttpSession session) throws BadRequestException {
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
        return new ResponseMessage(SUCCESSFULLY_REMOVED_COMMENT, HttpStatus.OK.value(), LocalDateTime.now());
    }
}




