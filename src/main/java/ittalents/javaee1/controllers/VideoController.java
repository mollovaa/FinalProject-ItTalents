package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.models.Video;

import ittalents.javaee1.models.dao.VideoDao;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


@RestController
public class VideoController {

    //todo: json format:
    private String successfullyAddedVideo = "You have successfully added a video!";
    private String unsuccessfullyAddedVideo = "Please, login to add a video!";
    private String successfullyRemovedVideo = "You have successfully removed a video!";
    private String unsuccessfullyRemovedVideo = "Please, login to remove a video!";
    private String noRightsToRemoveAVideo = "Sorry, you cannot delete a video that is not yours!";
    private String successfullyLikedVideo = "You have successfully liked a video!";
    private String successfullyDislikedVideo = "You have successfully disliked a video!";
    private String unsuccessfullyLikedVideo = "Please, login to like a video!";
    private String unsuccessfullyDislikedVideo = "Please, login to dislike a video!";
    private String alreadyLikedVideo = "You have already liked this video!";
    private String alreadyDislikedVideo = "You have already disliked this video!";
    private String invalidVideoTitle = "Invalid title!";
    private String videoNotFound = "Sorry, video not found!";

    @Autowired
    VideoDao videoDao;

    private void validateVideo(Video video) throws InvalidInputException {
        if (video.getTitle() == null || video.getTitle().isEmpty()) {
            throw new InvalidInputException(invalidVideoTitle);
        }
    }

    @PostMapping(value = "/addVideo")
    public Object addVideo(@RequestBody Video toAdd, HttpSession session, HttpServletResponse response) {
        if (SessionManager.isLogged(session)) {
            try {
                toAdd.setUploaderId(SessionManager.getLoggedUserId(session));
                this.validateVideo(toAdd);
                videoDao.addVideo(toAdd);
                return successfullyAddedVideo;
            } catch (SessionManager.ExpiredSessionException | InvalidInputException e) {
                return e.getMessage();
            }
        }
        try {
            response.sendRedirect("/login");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return unsuccessfullyAddedVideo;
    }

    @PostMapping(value = "/removeVideo")
    public Object removeVideo(@RequestBody Video toRemove, HttpSession session, HttpServletResponse response) {
        if (videoDao.getVideoById(toRemove.getVideoId())) {
            if (SessionManager.isLogged(session)) {
                try {
                    if (SessionManager.getLoggedUserId(session) == toRemove.getUploaderId()) {
                        videoDao.removeVideo(toRemove);
                        return successfullyRemovedVideo;
                    } else {
                        return noRightsToRemoveAVideo;
                    }
                } catch (SessionManager.ExpiredSessionException e) {
                    return e.getMessage();
                }
            }
            try {
                response.sendRedirect("/login");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return unsuccessfullyRemovedVideo;
        }
        return videoNotFound;
    }

    @PostMapping(value = "/likeVideo")
    public Object likeVideo(@RequestBody Video toLike, HttpSession session, HttpServletResponse response) {
        if (videoDao.getVideoById(toLike.getVideoId())) {
            if (SessionManager.isLogged(session)) {
                try {
                    if (!videoDao.likeVideo(toLike, SessionManager.getLoggedUserId(session))) {
                        return alreadyLikedVideo;
                    }
                } catch (SessionManager.ExpiredSessionException e) {
                    return e.getMessage();
                }
                return successfullyLikedVideo;
            }
            try {
                response.sendRedirect("/login");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return unsuccessfullyLikedVideo;
        }
        return videoNotFound;
    }

    @PostMapping(value = "/dislikeVideo")
    public Object dislikeVideo(@RequestBody Video toDislike, HttpSession session, HttpServletResponse response) {
        if(videoDao.getVideoById(toDislike.getVideoId())) {
            if (SessionManager.isLogged(session)) {
                try {
                    if (!videoDao.dislikeVideo(toDislike, SessionManager.getLoggedUserId(session))) {
                        return alreadyDislikedVideo;
                    }
                } catch (SessionManager.ExpiredSessionException e) {
                    return e.getMessage();
                }
                return successfullyDislikedVideo;
            }
            try {
                response.sendRedirect("/login");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return unsuccessfullyDislikedVideo;
        }
        return videoNotFound;
    }

}
