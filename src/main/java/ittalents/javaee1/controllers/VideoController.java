package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.*;
import ittalents.javaee1.models.*;
import ittalents.javaee1.models.dto.ViewCommentDTO;
import ittalents.javaee1.util.ErrorMessage;
import ittalents.javaee1.util.MailManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "/videos")
public class VideoController extends GlobalController {

    private static final String NO_COMMENTS = "No comments!";
    private static final String ADDED_VIDEO_BY = "Video added by ";
    private static final String ADDED_VIDEO = "New video added!";
    private static final String INVALID_VIDEO_DESCRIPTION = "Invalid description";
    private static final String SUCCESSFULLY_REMOVED_VIDEO = "You have successfully removed a video!";
    private static final String ALREADY_LIKED_VIDEO = "You have already liked this video!";
    private static final String ALREADY_DISLIKED_VIDEO = "You have already disliked this video!";
    private static final String INVALID_VIDEO_TITLE = "Invalid title!";
    private static final String INVALID_VIDEO_DURATION = "Invalid duration!";
    private static final String INVALID_VIDEO_CATEGORY = "Invalid category!";
    private static final String CANNOT_REMOVE_DISLIKE = "You cannot remove the dislike, as you have not disliked the video!";
    private static final String CANNOT_REMOVE_LIKE = "You cannot remove the like, as you have not liked the video!";

    private void validateVideo(Video video) throws InvalidInputException {
        if (!isValidString(video.getTitle())) {
            throw new InvalidInputException(INVALID_VIDEO_TITLE);
        }
        if (!isValidString(video.getDescription())) {
            throw new InvalidInputException(INVALID_VIDEO_DESCRIPTION);
        }
        if (video.getDuration() <= 0) {
            throw new InvalidInputException(INVALID_VIDEO_DURATION);
        }
        if (!isValidString(video.getCategory()) || (!VideoCategory.contains(video.getCategory()))) {
            throw new InvalidInputException(INVALID_VIDEO_CATEGORY);
        }
        video.setUploadDate(LocalDate.now());
        video.setNumberOfDislikes(0);
        video.setNumberOfLikes(0);
        video.setNumberOfViews(0);
    }

    @GetMapping(value = "/{videoId}/show")
    public Object showVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        video.setNumberOfViews(video.getNumberOfViews() + 1);
        videoRepository.save(video);
        if (SessionManager.isLogged(session)) {
            User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
            if (!watchHistoryRepository.existsByVideoAndUser(video, user)) {
                WatchHistory historyRecord = new WatchHistory(user, video);
                watchHistoryRepository.save(historyRecord);
            }
        }
        return this.convertToViewVideoDTO(video);
    }

    @GetMapping(value = "/{videoId}/comments/all")
    public Object[] showVideoComments(@PathVariable long videoId) throws BadRequestException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        List<Comment> comments = video.getComments();   //need only comments , NOT responses
        comments = comments.stream().filter(comment -> comment.getResponseToId() == null).collect(Collectors.toList());
        if (comments.isEmpty()) {
            throw new BadRequestException(NO_COMMENTS);
        }
        List<ViewCommentDTO> commentsToShow = new ArrayList<>();
        for (Comment c : comments) {
            commentsToShow.add(convertToCommentDTO(c));
        }
        return commentsToShow.toArray();
    }

    @PostMapping(value = "/add")
    public Object addVideo(@RequestBody Video video, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        this.validateVideo(video);
        video.setUploaderId(SessionManager.getLoggedUserId(session));
        //notify all subscribers of current user:
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        for (User u : user.getMySubscribers()) {
            Notification notif = new Notification(ADDED_VIDEO_BY + user.getFullName(), u.getUserId());
            notificationRepository.save(notif);
            MailManager.sendEmail(u.getEmail(), ADDED_VIDEO, ADDED_VIDEO_BY + user.getFullName());
        }
        return convertToViewVideoDTO(videoRepository.save(video));
    }

    @DeleteMapping(value = "/{videoId}/remove")
    public Object removeVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        if (SessionManager.getLoggedUserId(session) != video.getUploaderId()) {
            throw new AccessDeniedException();
        }
        videoRepository.delete(video);
        return new ErrorMessage(SUCCESSFULLY_REMOVED_VIDEO, HttpStatus.OK.value(), LocalDateTime.now());
    }

    @PutMapping(value = "/{videoId}/like")
    public Object likeVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getLikedVideos().contains(video)) {
            throw new BadRequestException(ALREADY_LIKED_VIDEO);
        }
        if (user.getDislikedVideos().contains(video)) {//many to many select
            user.getDislikedVideos().remove(video);
            video.getUsersDislikedVideo().remove(user);
            videoRepository.save(video);
            userRepository.save(user);
            video.setNumberOfDislikes(video.getNumberOfDislikes() - 1);
        }
        video.getUsersLikedVideo().add(user);
        user.addLikedVideo(video);
        video.setNumberOfLikes(video.getNumberOfLikes() + 1);
        videoRepository.save(video);
        userRepository.save(user);
        return convertToViewVideoDTO(video);
    }

    @PutMapping(value = "/{videoId}/dislike")
    public Object dislikeVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getDislikedVideos().contains(video)) {
            throw new BadRequestException(ALREADY_DISLIKED_VIDEO);
        }
        if (user.getLikedVideos().contains(video)) {
            user.getLikedVideos().remove(video);
            video.getUsersLikedVideo().remove(user);
            userRepository.save(user);
            videoRepository.save(video);
            video.setNumberOfLikes(video.getNumberOfLikes() - 1);
        }
        video.getUsersDislikedVideo().add(user);
        user.addDislikedVideo(video);
        video.setNumberOfDislikes(video.getNumberOfDislikes() + 1);
        videoRepository.save(video);
        userRepository.save(user);
        return convertToViewVideoDTO(video);
    }

    @PutMapping(value = "/{videoId}/dislikes/remove")
    public Object removeDislike(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (!user.getDislikedVideos().contains(video)) {
            throw new BadRequestException(CANNOT_REMOVE_DISLIKE);
        }
        user.getDislikedVideos().remove(video);
        video.getUsersDislikedVideo().remove(user);
        video.setNumberOfDislikes(video.getNumberOfDislikes() - 1);
        userRepository.save(user);
        return convertToViewVideoDTO(videoRepository.save(video));
    }

    @PutMapping(value = "/{videoId}/likes/remove")
    public Object removeLike(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (!user.getLikedVideos().contains(video)) {
            throw new BadRequestException(CANNOT_REMOVE_LIKE);
        }
        user.getLikedVideos().remove(video);
        video.getUsersLikedVideo().remove(user);
        video.setNumberOfLikes(video.getNumberOfLikes() - 1);
        userRepository.save(user);
        return convertToViewVideoDTO(videoRepository.save(video));
    }


}
