package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.models.Comment;
import ittalents.javaee1.models.dao.CommentDao;
import ittalents.javaee1.models.dao.VideoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.time.LocalDate;

import static ittalents.javaee1.controllers.PlaylistController.redirectingToLogin;
import static ittalents.javaee1.controllers.ResponseMessages.*;

@RestController
public class CommentController {

    @Autowired
    private CommentDao commentDao;
    @Autowired
    private VideoDao videoDao;

    private void validateComment(Comment comment) throws InvalidInputException {
        if (comment.getMessage() == null || comment.getMessage().isEmpty()) {
            throw new InvalidInputException(INVALID_COMMENT_MESSAGE);
        }
        comment.setDateOfPublication(LocalDate.now());
        comment.setNumberOfDislikes(0);
        comment.setNumberOfLikes(0);
    }

    @PostMapping(value = "/commentVideo/{videoId}")
    public Object commentVideo(@RequestBody Comment comment, @PathVariable long videoId,
                               HttpSession session, HttpServletResponse response) {
        try {
            if (!SessionManager.isLogged(session)) {
                return redirectingToLogin(response);
            } else {
                if (!videoDao.checkIfVideoExists(videoId)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return NOT_FOUND;
                } else {
                    try {
                        this.validateComment(comment);
                        comment.setPublisherId(SessionManager.getLoggedUserId(session));
                        comment.setVideoId(videoId);
                        commentDao.addCommentToVideo(comment);
                        return SUCCESSFULLY_COMMENTED_VIDEO;
                    } catch (SessionManager.ExpiredSessionException | InvalidInputException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return e.getMessage();
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
    }


}
