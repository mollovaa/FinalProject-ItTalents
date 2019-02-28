
package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.WatchHistory;
import ittalents.javaee1.util.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/watchHistory")
public class WatchHistoryController extends GlobalController {

    private String EMPTY_HISTORY = "Empty watch history!";
    private String SUCCESSFULLY_CLEARED_HISTORY = "Successfully cleared watched history!";

    @GetMapping(value = "/all")
    public Object[] getAllVideosFromHistory(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        ArrayList<Video> videos = new ArrayList<>();
        for (WatchHistory w : watchHistoryRepository.getAllByUser(user)) {
            videos.add(w.getVideo());
        }
        if (videos.isEmpty()) {
            throw new BadRequestException(EMPTY_HISTORY);
        }
        return videos.toArray();
    }

    @PutMapping(value = "/clear")
    public Object clearWatchHistory(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        List<WatchHistory> watchHistories = watchHistoryRepository.getAllByUser(user);
        if (watchHistoryRepository.getAllByUser(user).isEmpty()) {
            throw new BadRequestException(EMPTY_HISTORY);
        }
        for (WatchHistory w : watchHistories) {
            watchHistoryRepository.delete(w);
        }
        return new ErrorMessage(SUCCESSFULLY_CLEARED_HISTORY, HttpStatus.OK.value(), LocalDateTime.now());
    }
}