package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.*;


import ittalents.javaee1.models.Notification;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.VideoCategory;
import ittalents.javaee1.util.ErrorMessage;

import ittalents.javaee1.util.MailManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;


import static ittalents.javaee1.controllers.MyResponse.*;

@RestController
@RequestMapping(value = "/videos")
public class VideoController extends GlobalController {

    private String ADDED_VIDEO_BY = "Video added by ";
    private String ADDED_VIDEO = "New video added!";


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

    @GetMapping(value = "getUser")
    public Object showUser(HttpSession session) throws BadRequestException {
        return userRepository.findById(SessionManager.getLoggedUserId(session));
    }

    @GetMapping(value = "/{videoId}/play")
    public Object showVideo(@PathVariable long videoId) throws VideoNotFoundException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();    //todo add to watch_history
        video.setNumberOfViews(video.getNumberOfViews() + 1);
        videoRepository.save(video);
        return video;
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
        return videoRepository.save(video);
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
        return video;
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
        return video;
    }

}
