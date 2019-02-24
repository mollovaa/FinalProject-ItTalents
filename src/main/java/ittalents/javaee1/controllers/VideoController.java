package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.models.Video;

import ittalents.javaee1.models.VideoCategory;
import ittalents.javaee1.models.dao.VideoDao;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;

import static ittalents.javaee1.controllers.PlaylistController.redirectingToLogin;
import static ittalents.javaee1.controllers.ResponseMessages.*;

@RestController
public class VideoController {

    @Autowired
    VideoDao videoDao;

    private void validateVideo(Video video) throws InvalidInputException {
        if (video.getTitle() == null || video.getTitle().isEmpty()) {
            throw new InvalidInputException(INVALID_VIDEO_TITLE);
        }
        if (video.getDuration().getSeconds() <= 0) {
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

    @PostMapping(value = "/addVideo")
    public Object addVideo(@RequestBody Video toAdd, HttpSession session, HttpServletResponse response) {
        try {
            if (SessionManager.isLogged(session)) {
                try {
                    this.validateVideo(toAdd);
                    toAdd.setUploaderId(SessionManager.getLoggedUserId(session));
                    videoDao.addVideo(toAdd);
                    return SUCCESSFULLY_ADDED_VIDEO;
                } catch (SessionManager.ExpiredSessionException | InvalidInputException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return e.getMessage();
                }
            } else {
                return redirectingToLogin(response);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println(e.getMessage());
            return SERVER_ERROR;
        }
    }

    @GetMapping(value = "/removeVideo/{videoId}")
    public Object removeVideo(@PathVariable long videoId, HttpSession session, HttpServletResponse response) {
        try {
            if (videoDao.checkIfVideoExists(videoId)) {
                if (SessionManager.isLogged(session)) {
                    try {
                        if (SessionManager.getLoggedUserId(session) == videoDao.getVideoById(videoId).getUploaderId()) {
                            videoDao.removeVideo(videoId);
                            return SUCCESSFULLY_REMOVED_VIDEO;
                        } else {
                            return ACCESS_DENIED;
                        }
                    } catch (SessionManager.ExpiredSessionException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return EXPIRED_SESSION;
                    }
                } else {
                    return redirectingToLogin(response);
                }
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return NOT_FOUND;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            return SERVER_ERROR;
        }
    }

    @GetMapping(value = "searchVideoBy/{search}")
    public Object[] searchVideosBy(@PathVariable String search, HttpServletResponse response) {
        try {
            return videoDao.getVideoByTitle(search).toArray();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Object b[] = new Object[1];
            b[0] = SERVER_ERROR;
            return b;
        }
    }

    @GetMapping(value = "/likeVideo/{videoId}")
    public Object likeVideo(@PathVariable long videoId, HttpSession session, HttpServletResponse response) {
        try {
            if (videoDao.checkIfVideoExists(videoId)) {
                // System.out.println(videoDao.checkIfVideoExists(videoId));  //
                if (SessionManager.isLogged(session)) {
                    try {
                        if (!videoDao.likeVideo(videoId, SessionManager.getLoggedUserId(session))) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            return ALREADY_LIKED_VIDEO;
                        }
                    } catch (SessionManager.ExpiredSessionException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return EXPIRED_SESSION;
                    }
                    return SUCCESSFULLY_LIKED_VIDEO;
                } else {
                    return redirectingToLogin(response);
                }
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return NOT_FOUND;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
    }

    @GetMapping(value = "/dislikeVideo/{videoId}")
    public Object dislikeVideo(@PathVariable long videoId, HttpSession session, HttpServletResponse response) {
        try {
            if (videoDao.checkIfVideoExists(videoId)) {
                if (SessionManager.isLogged(session)) {
                    try {
                        if (!videoDao.dislikeVideo(videoId, SessionManager.getLoggedUserId(session))) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            return ALREADY_DISLIKED_VIDEO;
                        }
                    } catch (SessionManager.ExpiredSessionException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return EXPIRED_SESSION;
                    }
                    return SUCCESSFULLY_DISLIKED_VIDEO;
                } else {
                    return redirectingToLogin(response);
                }
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return NOT_FOUND;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
    }

}
