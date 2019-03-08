
package ittalents.javaee1.controllers;

import ittalents.javaee1.util.SessionManager;
import ittalents.javaee1.util.exceptions.BadRequestException;
import ittalents.javaee1.util.exceptions.NotLoggedException;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.models.pojo.WatchHistory;
import ittalents.javaee1.models.dto.SearchableVideoDTO;
import ittalents.javaee1.util.ResponseMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/watchHistory")
public class WatchHistoryController extends GlobalController {

    private static final String NOT_VALID_SORT_DIRECTION = "Not valid sort direction!";
    private final String ASC = "ASC";
    private final String DESC = "DESC";

    @AllArgsConstructor
    @Getter
    private class viewWatchHistoryDTO {
        private long id;
        private SearchableVideoDTO video;
        private LocalDate date;
    }

    private viewWatchHistoryDTO convertToWatchHistoryToShow(WatchHistory w) {
        return new viewWatchHistoryDTO(w.getId(), w.getVideo().convertToSearchableVideoDTO(userRepository), w.getDate());
    }

    @GetMapping(value = "/all")
    public Object viewWatchHistory(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.getByUserId(SessionManager.getLoggedUserId(session));
        return new ArrayList<>(watchHistoryRepository.getAllByUser(user)
                .stream()
                .map(this::convertToWatchHistoryToShow)
                .collect(Collectors.toList()));
    }

    @DeleteMapping(value = "/clear")
    public Object clearWatchHistory(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        List<WatchHistory> watchHistories = watchHistoryRepository.getAllByUser(user);
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
        List<viewWatchHistoryDTO> watchHistories = new ArrayList<>();
        switch (direction.toUpperCase()) {
            case ASC:
                watchHistories.addAll(watchHistoryRepository.getAllByUserOrderByDateAsc(user)
                        .stream()
                        .map(this::convertToWatchHistoryToShow)
                        .collect(Collectors.toList()));
                break;
            case DESC:
                watchHistories.addAll(watchHistoryRepository.getAllByUserOrderByDateDesc(user)
                        .stream()
                        .map(this::convertToWatchHistoryToShow)
                        .collect(Collectors.toList()));
                break;
        }
        return watchHistories;
    }

}