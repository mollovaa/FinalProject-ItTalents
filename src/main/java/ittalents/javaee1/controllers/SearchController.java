package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.repository.SearchQueryRepository;
import ittalents.javaee1.models.pojo.SearchHistory;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.models.pojo.Video;
import ittalents.javaee1.models.dto.SearchablePlaylistDTO;
import ittalents.javaee1.models.dto.SearchableUserDTO;
import ittalents.javaee1.models.dto.SearchableVideoDTO;
import ittalents.javaee1.models.search.Filter;
import ittalents.javaee1.models.pojo.SearchQuery;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;

import ittalents.javaee1.util.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SearchController extends GlobalController {
    public static final String EMPTY_FILTER = "Invalid filter option!";
    private static final String EMPTY_SEARCH = "No matches found!";
    private static final int TWENTY_MINUTES_DURATION = 60 * 20;
    private static final int FOUR_MINUTES_DURATION = 4 * 60;

    @Autowired
    SearchQueryRepository searchQueryRepository;

    private SearchQuery addToSearchQueries(String search_query) {
        if (searchQueryRepository.existsBySearchQuery(search_query)) {
            return searchQueryRepository.getBySearchQuery(search_query);
        }
        SearchQuery searchQuery = new SearchQuery(search_query);
        return searchQueryRepository.save(searchQuery);
    }

    private void addToSearchHistory(User user, SearchQuery searchQuery) {
        if (!searchHistoryRepository.existsByUserAndSearchQuery(user, searchQuery)) {
            searchHistoryRepository.save(new SearchHistory(user, searchQuery));
        } else {
            SearchHistory sHistory = searchHistoryRepository.getByUserAndSearchQuery(user, searchQuery);
            sHistory.setDate(LocalDate.now());
            searchHistoryRepository.save(sHistory);
        }
    }

    private List<SearchableVideoDTO> getOnlyPublicVideosWithoutFilter(String search_query, HttpSession session) {
        try {
            long currentUserId = SessionManager.getLoggedUserId(session);
            return getSearchedVideos(search_query)
                    .stream()
                    .filter(video -> video.getUploaderId() == currentUserId     //all that are user`s have to be shown
                            || !video.isIsprivate())                               //OR all that are not private
                    .collect(Collectors.toList());
        } catch (NotLoggedException e) {
            return getSearchedVideos(search_query)
                    .stream()
                    .filter(video -> !video.isIsprivate())                          //all that are not private
                    .collect(Collectors.toList());
        }
    }

    private List<SearchableVideoDTO> getOnlyPublicVideosWithFilter(String search_query, HttpSession session,
                                                                   Filter myFilter) throws InvalidInputException {
        try {
            long currentUserId = SessionManager.getLoggedUserId(session);
            return getFilteredVideos(search_query, myFilter)
                    .stream()
                    .filter(video -> video.getUploaderId() == currentUserId     //all that are user`s have to be shown
                            || !video.isIsprivate())                               //OR all that are not private
                    .collect(Collectors.toList());
        } catch (NotLoggedException e) {
            return getFilteredVideos(search_query, myFilter)
                    .stream()
                    .filter(video -> !video.isIsprivate())                          //all that are not private
                    .collect(Collectors.toList());
        }
    }

    @GetMapping(value = "/search")
    public Object search(@RequestParam("q") String search_query, HttpSession session) throws BadRequestException {
        if (isValidString(search_query)) {
            SearchQuery sQuery = this.addToSearchQueries(search_query);  //add to search queries
            if (SessionManager.isLogged(session)) { //if logged -> add to search history
                this.addToSearchHistory(userRepository.getByUserId(
                        SessionManager.getLoggedUserId(session)), sQuery);
            }
            List<Searchable> result = new ArrayList<>();
            result.addAll(getSearchedUsers(search_query));

            result.addAll(getOnlyPublicVideosWithoutFilter(search_query, session));

            result.addAll(getSearchedPlaylists(search_query));
            if (result.isEmpty()) {
                return new ResponseMessage(EMPTY_SEARCH, HttpStatus.OK.value(), LocalDateTime.now());
            } else {
                return result;
            }
        }
        throw new InvalidInputException(EMPTY_SEARCH);
    }

    @GetMapping(value = "/search/filters")
    public Object search(@RequestParam("q") String search_query, @RequestParam("filter") String filter, HttpSession session)
            throws BadRequestException {
        if (isValidString(search_query)) {
            if (isValidString(filter)) {
                Filter myFilter = getFilter(filter);
                if (myFilter == null) {
                    throw new InvalidInputException(EMPTY_FILTER);
                }
                SearchQuery sQuery = this.addToSearchQueries(search_query);  //add to search queries
                if (SessionManager.isLogged(session)) { //if logged -> add to search history
                    this.addToSearchHistory(userRepository.getByUserId(
                            SessionManager.getLoggedUserId(session)), sQuery);
                }
                List<Searchable> result = new ArrayList<>();

                if (myFilter.getSearchType() == SearchType.USER) { // Sort Only Users
                    result.addAll(getSearchedUsers(search_query));
                } else if (myFilter.getSearchType() == SearchType.PLAYLIST) { //Sort Only Playlists
                    result.addAll(getSearchedPlaylists(search_query));
                } else {
                    //Video Filters
                    result.addAll(getOnlyPublicVideosWithFilter(search_query, session, myFilter));
                }
                if (result.isEmpty()) {
                    return new ResponseMessage(EMPTY_SEARCH, HttpStatus.OK.value(), LocalDateTime.now());
                }
                return result;
            } else {
                throw new InvalidInputException(EMPTY_FILTER);
            }
        } else {
            throw new InvalidInputException(EMPTY_SEARCH);
        }

    }

    private List<SearchableUserDTO> getSearchedUsers(String search_query) {
        return userRepository.findAllByFullNameContaining(search_query)
                .stream()
                .map(user -> convertToSearchableUserDTO(user))
                .collect(Collectors.toList());
    }

    private List<SearchablePlaylistDTO> getSearchedPlaylists(String search_query) {
        return playlistRepository.findAllByPlaylistNameContaining(search_query)
                .stream()
                .map(this::convertToSearchablePlaylistDTO)
                .collect(Collectors.toList());
    }

    private List<SearchableVideoDTO> getSearchedVideos(String search_query) {
        return videoRepository.findAllByTitleContaining(search_query)
                .stream()
                .map(this::convertToSearchableVideoDTO)
                .collect(Collectors.toList());
    }

    private List<SearchableVideoDTO> getFilteredVideos(String search_query, Filter filter) throws InvalidInputException {
        if (filter == Filter.DATE_OF_UPLOAD) {
            List<Video> videos = videoRepository.findAllByTitleContaining(search_query);
            Collections.sort(videos, Comparator.comparing(Video::getUploadDate).reversed());
            return videos.stream().map(this::convertToSearchableVideoDTO).collect(Collectors.toList());
        }
        if (filter == Filter.VIEWS) {
            List<Video> videos = videoRepository.findAllByTitleContaining(search_query);
            Collections.sort(videos, Comparator.comparing(Video::getNumberOfViews).reversed());
            return videos.stream().map(this::convertToSearchableVideoDTO).collect(Collectors.toList());
        }
        if (filter == Filter.MOST_LIKES) {
            List<Video> videos = videoRepository.findAllByTitleContaining(search_query);
            Collections.sort(videos, Comparator.comparing(Video::getNumberOfLikes).reversed());
            return videos.stream().map(this::convertToSearchableVideoDTO).collect(Collectors.toList());
        }
        if (filter == Filter.VIDEOS) { // only videos without order
            return getSearchedVideos(search_query);
        }
        if (filter == Filter.LENGTH_LONG) { // only videos without order
            return videoRepository.findAllByTitleContainingAndDurationGreaterThan(search_query, TWENTY_MINUTES_DURATION)
                    .stream()
                    .map(this::convertToSearchableVideoDTO)
                    .collect(Collectors.toList());
        }
        if (filter == Filter.LENGTH_SHORT) { // only videos without order
            return videoRepository.findAllByTitleContainingAndDurationLessThanEqual(search_query, FOUR_MINUTES_DURATION)
                    .stream()
                    .map(this::convertToSearchableVideoDTO)
                    .collect(Collectors.toList());
        }
        throw new InvalidInputException(EMPTY_FILTER);
    }

    private Filter getFilter(String filterType) {
        Filter[] allFilters = Filter.values();
        for (int i = 0; i < allFilters.length; i++) {
            if (filterType.equals(allFilters[i].getName())) {
                return allFilters[i];
            }
        }
        return null;
    }
}
