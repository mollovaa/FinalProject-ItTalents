package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.hibernate.UserRepository;
import ittalents.javaee1.hibernate.VideoRepository;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.VideoCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import static ittalents.javaee1.controllers.ResponseMessages.*;

@RestController
public class VideoController implements GlobalController {

    @Autowired
    VideoRepository videoRepository;
    @Autowired
    UserRepository userRepository;

    private void validateVideo(Video video) throws InvalidInputException {
        if (video.getTitle() == null || video.getTitle().isEmpty()) {  //isempty
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
    public Object showVideo(@PathVariable long videoId, HttpServletResponse response) {
        if (videoRepository.existsById(videoId)) {
            return videoRepository.findById(videoId);
        }
        return responseForBadRequest(response, NOT_FOUND);
    }

    @PostMapping(value = "videos/addVideo")
    public Object addVideo(@RequestBody Video video, HttpSession session, HttpServletResponse response)
            throws BadRequestException {
        if (SessionManager.isLogged(session)) {
            this.validateVideo(video);
            video.setUploaderId(SessionManager.getLoggedUserId(session));
            return videoRepository.save(video);
        } else {
            return redirectingToLogin(response);
        }
    }

    @GetMapping(value = "videos/removeVideo/{videoId}")
    public Object removeVideo(@PathVariable long videoId, HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            return redirectingToLogin(response);
        } else {
            if (videoRepository.existsById(videoId)) {
                Video video = videoRepository.findById(videoId).get();
                if (SessionManager.getLoggedUserId(session) == video.getUploaderId()) {
                    videoRepository.deleteById(videoId);
                    return SUCCESSFULLY_REMOVED_VIDEO;
                } else {
                    return responseForBadRequest(response, ACCESS_DENIED);
                }
            } else {
                return responseForBadRequest(response, NOT_FOUND);
            }
        }
    }

    @GetMapping(value = "videos/likeVideo/{videoId}")
    public Object likeVideo(@PathVariable long videoId, HttpSession session, HttpServletResponse response) throws SessionManager.ExpiredSessionException {
        if (!videoRepository.existsById(videoId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                Video video = videoRepository.findById(videoId).get();
                User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();

                if (user.getLikedVideos().contains(video)) {

                    return responseForBadRequest(response, ALREADY_LIKED_VIDEO);
                } else {
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
                    return SUCCESSFULLY_LIKED_VIDEO;
                }
            }
        }
    }

    @GetMapping(value = "videos/dislikeVideo/{videoId}")
    public Object dislikeVideo(@PathVariable long videoId, HttpSession session, HttpServletResponse response) throws SessionManager.ExpiredSessionException {
        if (!videoRepository.existsById(videoId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                Video video = videoRepository.findById(videoId).get();
                User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
                if (user.getDislikedVideos().contains(video)) {
                    return responseForBadRequest(response, ALREADY_DISLIKED_VIDEO);
                } else {
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
                    return SUCCESSFULLY_DISLIKED_VIDEO;
                }
            }
        }
    }


}
