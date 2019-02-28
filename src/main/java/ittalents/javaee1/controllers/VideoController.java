package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.*;

import ittalents.javaee1.hibernate.NotificationRepository;
import ittalents.javaee1.hibernate.UserRepository;
import ittalents.javaee1.hibernate.VideoRepository;
import ittalents.javaee1.models.Notification;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.VideoCategory;
import ittalents.javaee1.util.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;


import static ittalents.javaee1.controllers.MyResponse.*;

@RestController
public class VideoController extends GlobalController {

    private String ADDED_VIDEO_BY = "Video added by ";

    @GetMapping(value = "showNotifications")     //only unread notifications
    public Object[] showNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getNotifications() == null || user.getNotifications().isEmpty()) {
            throw new BadRequestException("No notifications");
        }
        ArrayList<Notification> result = new ArrayList<>();
        for (Notification n : user.getNotifications()) {
            if (!n.isRead()) {
                result.add(n);
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
        if (result.isEmpty()) {
            throw new BadRequestException("No unread notifications");
        }
        return result.toArray();
    }



    private void validateVideo(Video video) throws InvalidInputException {
        if (video.getTitle() == null || video.getTitle().isEmpty()) {
            throw new InvalidInputException(INVALID_VIDEO_TITLE);
        }
        if (video.getDuration() <= 0) {
            throw new InvalidInputException(INVALID_VIDEO_DURATION);
        }
        if (!VideoCategory.contains(video.getCategory())) {
            throw new InvalidInputException(INVALID_VIDEO_CATEGORY);
        }
        video.setUploadDate(LocalDate.now());
        video.setNumberOfDislikes(0);
        video.setNumberOfLikes(0);
        video.setNumberOfViews(0);
    }

    @GetMapping(value = "videos/showVideo/{videoId}")
    public Object showVideo(@PathVariable long videoId) throws VideoNotFoundException {
        if (videoRepository.existsById(videoId)) {
            return videoRepository.findById(videoId);
        }
        throw new VideoNotFoundException();
    }

    @PostMapping(value = "videos/addVideo")
    public Object addVideo(@RequestBody Video video, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        this.validateVideo(video);
        video.setUploaderId(SessionManager.getLoggedUserId(session));
        //notify all subscribers of current user:
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        for (User u : user.getMySubscribers()) {
            Notification notif = new Notification(ADDED_VIDEO_BY + u.getFullName(), u.getUserId());
            notificationRepository.save(notif);
        }
        return videoRepository.save(video);
    }

    @GetMapping(value = "videos/removeVideo/{videoId}")
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
        videoRepository.deleteById(videoId);
        return new ErrorMessage(SUCCESSFULLY_REMOVED_VIDEO, HttpStatus.OK.value(), LocalDateTime.now());
    }


    @GetMapping(value = "videos/likeVideo/{videoId}")
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
        return video;
    }


    @GetMapping(value = "videos/dislikeVideo/{videoId}")
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
        return video;
    }

}
