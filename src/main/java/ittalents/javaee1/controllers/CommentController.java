package ittalents.javaee1.controllers;


import ittalents.javaee1.models.dto.ViewCommentDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/comments")
public class CommentController extends GlobalController {

    private static final String COMMENTED_VIDEO_BY = "Your video has been commented by ";
    private static final String RESPOND_TO_COMMENT = " respond to your comment";
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
        Comment baseComment = commentRepository.getByCommentId(commentId);
        List<ViewCommentDTO> responses = new ArrayList<>();
        for (Comment response : baseComment.getResponses()) {
            responses.add(response.convertToViewCommentDTO(userRepository.getById(response.getPublisherId()).getFullName()));
        }
        return responses;
    }

    private void notifyVideosOwnerForComment(Video video, User loggedUser) {
        long uploaderId = video.getUploaderId();
        if (uploaderId != loggedUser.getUserId()) { //if owner comments -> no notification is send
            String writerName = loggedUser.getFullName();
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
        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        if (video.isPrivate() && video.getUploaderId() != user.getUserId()) {  //private and not logged user`s
            throw new VideoNotFoundException();
        }
        this.validateComment(comment);
        this.setInitialCommentValues(comment);
        comment.setPublisherId(user.getUserId());
        comment.setVideoId(videoId);
        comment.setResponseToId(null);
        commentRepository.save(comment);
        this.notifyVideosOwnerForComment(video, user);

        return comment.convertToViewCommentDTO(user.getFullName());
    }

    //notify base comment`s owner
    private void notifyBaseCommentsOwner(Comment comment, User loggedUser) {
        long commentOwnerId = comment.getPublisherId();
        if (commentOwnerId != loggedUser.getUserId()) {  //if owner responses to owned comment
            String writerName = loggedUser.getFullName();
            Notification notif = new Notification(writerName + RESPOND_TO_COMMENT, commentOwnerId);
            notificationRepository.save(notif);
        }
    }

    @PostMapping(value = "/{commentId}/response")
    public Object responseComment(@RequestBody Comment response, @PathVariable long commentId, HttpSession session)
            throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User loggedUser = userRepository.getById(SessionManager.getLoggedUserId(session));

        Comment baseComment = commentRepository.getByCommentId(commentId);
        this.validateComment(response);
        this.setInitialCommentValues(response);
        response.setPublisherId(loggedUser.getUserId());
        response.setVideoId(baseComment.getVideoId());
        response.setResponseToId(commentId);
        commentRepository.save(response);

        this.notifyBaseCommentsOwner(baseComment, loggedUser);
        String publisherName = loggedUser.getFullName();
        return response.convertToViewCommentDTO(publisherName);
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
        Comment comment = commentRepository.getByCommentId(commentId);
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        if (user.getLikedComments().contains(comment)) {
            throw new BadRequestException(ALREADY_LIKED_COMMENT);
        }
        if (user.getDislikedComments().contains(comment)) {  //if disliked -> remove the dislike
            this.removeDislike(user, comment);
        }
        this.addLike(user, comment);
        String publisherName = userRepository.getById(comment.getPublisherId()).getFullName();
        return comment.convertToViewCommentDTO(publisherName);
    }

    @Transactional
    @PutMapping(value = "/{commentId}/likes/remove")
    public Object removeLike(@PathVariable long commentId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Comment comment = commentRepository.getByCommentId(commentId);
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        if (!user.getLikedComments().contains(comment)) {
            throw new BadRequestException(COMMENT_NOT_LIKED);
        }
        this.removeLike(user, comment);
        String publisherName = userRepository.getById(comment.getPublisherId()).getFullName();
        return comment.convertToViewCommentDTO(publisherName);
    }

    @Transactional
    @PutMapping(value = "/{commentId}/dislikes/remove")
    public Object removeDislike(@PathVariable long commentId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Comment comment = commentRepository.getByCommentId(commentId);
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        if (!user.getDislikedComments().contains(comment)) {
            throw new BadRequestException(COMMENT_NOT_DISLIKED);
        }
        this.removeDislike(user, comment);
        String publisherName = userRepository.getById(comment.getPublisherId()).getFullName();
        return comment.convertToViewCommentDTO(publisherName);
    }

    @Transactional
    @PutMapping(value = "/{commentId}/dislike")
    public Object dislikeComment(@PathVariable long commentId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Comment comment = commentRepository.getByCommentId(commentId);
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        if (user.getDislikedComments().contains(comment)) {
            throw new BadRequestException(ALREADY_DISLIKED_COMMENT);
        }
        if (user.getLikedComments().contains(comment)) {   //if liked -> remove the like
            this.removeLike(user, comment);
        }
        this.addDislike(user, comment);
        String publisherName = userRepository.getById(comment.getPublisherId()).getFullName();

        return comment.convertToViewCommentDTO(publisherName);
    }

    @Transactional
    @DeleteMapping(value = "/{commentId}/remove")
    public Object removeComment(@PathVariable long commentId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Comment comment = commentRepository.getByCommentId(commentId);
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        if (user.getUserId() != comment.getPublisherId()) {
            throw new AccessDeniedException();
        }
        commentRepository.delete(comment);
        return new ResponseMessage(SUCCESSFULLY_REMOVED_COMMENT, HttpStatus.OK.value(), LocalDateTime.now());
    }
}




