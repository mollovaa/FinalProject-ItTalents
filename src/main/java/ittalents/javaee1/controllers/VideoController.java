package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.models.Video;

import ittalents.javaee1.models.VideoCategory;
import ittalents.javaee1.models.dao.VideoDao;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;

import static ittalents.javaee1.controllers.ResponseMessages.*;

@RestController
public class VideoController implements GlobalController {

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

    @PostMapping(value = "videos/addVideo")
    public Object addVideo(@RequestBody Video toAdd, HttpSession session, HttpServletResponse response)
            throws BadRequestException {
        if (SessionManager.isLogged(session)) {
            this.validateVideo(toAdd);
            toAdd.setUploaderId(SessionManager.getLoggedUserId(session));
            videoDao.addVideo(toAdd);
            return toAdd;
        } else {
            return redirectingToLogin(response);
        }
    }

    @GetMapping(value = "videos/removeVideo/{videoId}")
    public Object removeVideo(@PathVariable long videoId, HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            return redirectingToLogin(response);
        } else {
            if (SessionManager.getLoggedUserId(session) == videoDao.getVideoById(videoId).getUploaderId()) {
                videoDao.removeVideo(videoId);
                return SUCCESSFULLY_REMOVED_VIDEO;
            } else {
                return responseForBadRequest(response, ACCESS_DENIED);
            }
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


    @GetMapping(value = "videos/likeVideo/{videoId}")
    public Object likeVideo(@PathVariable long videoId, HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (!videoDao.checkIfVideoExists(videoId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                if (!videoDao.likeVideo(videoId, SessionManager.getLoggedUserId(session))) {
                    return responseForBadRequest(response, ALREADY_LIKED_VIDEO);
                }
                return SUCCESSFULLY_LIKED_VIDEO;
            }
        }
    }

    @GetMapping(value = "videos/dislikeVideo/{videoId}")
    public Object dislikeVideo(@PathVariable long videoId, HttpSession session, HttpServletResponse response) throws SessionManager.ExpiredSessionException {
git         if (!videoDao.checkIfVideoExists(videoId)) {
            return responseForBadRequest(response, NOT_FOUND);
        } else {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                if (!videoDao.dislikeVideo(videoId, SessionManager.getLoggedUserId(session))) {
                    return responseForBadRequest(response, ALREADY_DISLIKED_VIDEO);
                }
                return SUCCESSFULLY_DISLIKED_VIDEO;
            }
        }

    }

}
