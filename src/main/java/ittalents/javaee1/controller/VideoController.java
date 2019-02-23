package ittalents.javaee1.controller;

import ittalents.javaee1.model.Video;

import ittalents.javaee1.model.VideoDao;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;


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


    @Autowired
    VideoDao videoDao;

    @PostMapping(value = "/addVideo")
    public Object addVideo(@RequestBody Video toAdd, HttpSession session) {
        session.setAttribute("logged", true);
        if ((boolean) session.getAttribute("logged")) {
            long userId = 1;    //(Long)session.getAttribute("user_id");
            toAdd.setUploaderId(userId);
            videoDao.addVideo(toAdd);
            return successfullyAddedVideo;
        }
        //todo redirect to login
        return unsuccessfullyAddedVideo;
    }

    @PostMapping(value = "/removeVideo")
    public Object removeVideo(@RequestBody Video toRemove, HttpSession session) {
        session.setAttribute("logged", true);
        session.setAttribute("user_id", 1);
        if ((boolean) session.getAttribute("logged")) {
            if (((Integer) session.getAttribute("user_id")).longValue() == toRemove.getUploaderId()) {
                videoDao.removeVideo(toRemove);
                return successfullyRemovedVideo;
            } else {
                return noRightsToRemoveAVideo;
            }
        }
        //todo redirect to login
        return unsuccessfullyRemovedVideo;
    }

    @PostMapping(value = "/likeVideo")
    public Object likeVideo(@RequestBody Video toLike, HttpSession session) {
        session.setAttribute("logged", true);
        session.setAttribute("user_id", 1);
        int userId = (Integer) session.getAttribute("user_id");
        if ((boolean) session.getAttribute("logged")) {
            if(!videoDao.likeVideo(toLike, userId)){
                return alreadyLikedVideo;
            }
            return successfullyLikedVideo;
        }
        //todo redirect to login
        return unsuccessfullyLikedVideo;
    }

    @PostMapping(value = "/dislikeVideo")
    public Object dislikeVideo(@RequestBody Video toDislike, HttpSession session) {
        session.setAttribute("logged", true);
        session.setAttribute("user_id", 1);
        int userId = (Integer) session.getAttribute("user_id");
        if ((boolean) session.getAttribute("logged")) {
            if(!videoDao.dislikeVideo(toDislike, userId)){
                return alreadyDislikedVideo;
            }
            return successfullyDislikedVideo;
        }
        //todo redirect to login
        return unsuccessfullyDislikedVideo;
    }

}
