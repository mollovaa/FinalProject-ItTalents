package ittalents.javaee1.controllers;

import ittalents.javaee1.util.SessionManager;
import ittalents.javaee1.util.exceptions.AccessDeniedException;
import ittalents.javaee1.util.exceptions.BadRequestException;
import ittalents.javaee1.util.exceptions.NotLoggedException;
import ittalents.javaee1.models.pojo.SearchHistory;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.util.ResponseMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/searchHistory")
public class SearchHistoryController extends GlobalController {

    private static final String REMOVED_SEARCH = "Removed search!";

    @Getter
    @AllArgsConstructor
    private class viewSearchHistoryDTO {
        private long id;
        private String searched;
        private LocalDate date;
    }

    private viewSearchHistoryDTO convertToViewSearchHistoryDTO(SearchHistory searchHistory) {
        return new viewSearchHistoryDTO(searchHistory.getId(), searchHistory.getSearchQuery().getSearchQuery(),
                searchHistory.getDate());
    }

    @GetMapping(value = "/all")
    public Object viewSearchHistory(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.getByUserId(SessionManager.getLoggedUserId(session));
        return new ArrayList<>(searchHistoryRepository.getAllByUser(user)
                .stream()
                .map(this::convertToViewSearchHistoryDTO)
                .collect(Collectors.toList()));
    }

    @Transactional
    @PutMapping(value = "/{searchHistoryId}/remove")
    public Object removeSearchedById(@PathVariable long searchHistoryId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.getByUserId(SessionManager.getLoggedUserId(session));
        SearchHistory searchHistory = searchHistoryRepository.getById(searchHistoryId);
        if (searchHistory.getUser() != user) {
            throw new AccessDeniedException();
        }
        searchHistoryRepository.deleteById(searchHistoryId);
        return new ResponseMessage(REMOVED_SEARCH, HttpStatus.OK.value(), LocalDateTime.now());
    }

    @Transactional
    @DeleteMapping(value = "/clear")
    public Object clearSearchHistory(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        List<SearchHistory> searchHistories = searchHistoryRepository.getAllByUser(user);
        for (SearchHistory s : searchHistories) {
            searchHistoryRepository.delete(s);
        }
        return new ResponseMessage(SUCCESSFULLY_CLEARED_HISTORY, HttpStatus.OK.value(), LocalDateTime.now());
    }

}
