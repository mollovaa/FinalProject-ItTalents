
package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.WatchHistory;
import ittalents.javaee1.models.dto.SearchableVideoDTO;
import ittalents.javaee1.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/watchHistory")
public class WatchHistoryController extends GlobalController {

    private static final String NOT_VALID_SORT_DIRECTION = "Not valid sort direction!";
    private final String EMPTY_HISTORY = "Empty watch history!";
    private final String SUCCESSFULLY_CLEARED_HISTORY = "Successfully cleared watched history!";
    private final String ASC = "ASC";
    private final String DESC = "DESC";

    @GetMapping(value = "/all")
    public Object getAllVideosFromHistory(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        ArrayList<SearchableVideoDTO> videos = new ArrayList<>();
        for (WatchHistory w : watchHistoryRepository.getAllByUser(user)) {
            videos.add(convertToSearchableVideoDTO(w.getVideo()));
        }
        if (videos.isEmpty()) {
            return new ResponseMessage(EMPTY_HISTORY, HttpStatus.OK.value(), LocalDateTime.now());
        }
        return videos;
    }

    @PutMapping(value = "/clear")
    public Object clearWatchHistory(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        List<WatchHistory> watchHistories = watchHistoryRepository.getAllByUser(user);
        if (watchHistoryRepository.getAllByUser(user).isEmpty()) {
            return new ResponseMessage(EMPTY_HISTORY, HttpStatus.OK.value(), LocalDateTime.now());
        }
        for (WatchHistory w : watchHistories) {
            watchHistoryRepository.delete(w);
        }
        return new ResponseMessage(SUCCESSFULLY_CLEARED_HISTORY, HttpStatus.OK.value(), LocalDateTime.now());
    }

    @GetMapping(value = "/all/sortByDate")
    public Object sortByDate(@RequestParam(value = "direction") String direction, HttpSession session)
            throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!direction.equalsIgnoreCase(ASC) && !direction.equalsIgnoreCase(DESC)) {
            throw new BadRequestException(NOT_VALID_SORT_DIRECTION);
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        List<WatchHistory> watchHistories = new ArrayList<>();
        switch (direction.toUpperCase()) {
            case ASC:
                watchHistories = watchHistoryRepository.getAllByUserOrderByDateAsc(user);
                break;
            case DESC:
                watchHistories = watchHistoryRepository.getAllByUserOrderByDateDesc(user);
                break;
        }
        if (watchHistories.isEmpty()) {
            return new ResponseMessage(EMPTY_HISTORY, HttpStatus.OK.value(), LocalDateTime.now());
        }
        return watchHistories;
    }

}