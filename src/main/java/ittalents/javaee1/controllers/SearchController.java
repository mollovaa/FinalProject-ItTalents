package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.dto.SearchablePlaylistDTO;
import ittalents.javaee1.models.dto.SearchableUserDTO;
import ittalents.javaee1.models.dto.SearchableVideoDTO;
import ittalents.javaee1.models.search.Filter;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;

import ittalents.javaee1.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SearchController extends GlobalController {
    private static final String EMPTY_FILTER = "Invalid filter option!";
    private static final String EMPTY_SEARCH = "No matches found!";
    private static final int TWENTY_MINUTES_DURATION = 60 * 20;
    private static final int FOUR_MINUTES_DURATION = 4 * 60;

    @GetMapping(value = "/search")
    public Object search(@RequestParam("q") String search_query) throws InvalidInputException {
        if (isValidString(search_query)) {
            List<Searchable> result = new ArrayList<>();
            result.addAll(getSearchedUsers(search_query));
            result.addAll(getSearchedVideos(search_query));
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
    public Object search(@RequestParam("q") String search_query, @RequestParam("filter") String filter)
            throws InvalidInputException {
        if (isValidString(search_query)) {
            if (isValidString(filter)) {
                Filter myFilter = getFilter(filter);
                if (myFilter == null) {
                    throw new InvalidInputException(EMPTY_FILTER);
                }
                List<Searchable> result = new ArrayList<>();

                if (myFilter.getSearchType() == SearchType.USER) { // Sort Only Users
                    result.addAll(getSearchedUsers(search_query));
                } else if (myFilter.getSearchType() == SearchType.PLAYLIST) { //Sort Only Playlists
                    result.addAll(getSearchedPlaylists(search_query));
                } else {
                    //Video Filters
                    result.addAll(getFilteredVideos(search_query, myFilter));
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
                .map(playlist -> convertToSearchablePlaylistDTO(playlist))
                .collect(Collectors.toList());
    }

    private List<SearchableVideoDTO> getSearchedVideos(String search_query) {
        return videoRepository.findAllByTitleContaining(search_query)
                .stream()
                .map(video -> convertToSearchableVideoDTO(video))
                .collect(Collectors.toList());
    }

    private List<SearchableVideoDTO> getFilteredVideos(String search_query, Filter filter) throws InvalidInputException {
        if (filter == Filter.DATE_OF_UPLOAD) {
            List<Video> videos = videoRepository.findAllByTitleContaining(search_query);
            Collections.sort(videos, Comparator.comparing(Video::getUploadDate).reversed());
            return videos.stream().map(video -> convertToSearchableVideoDTO(video)).collect(Collectors.toList());
        }
        if (filter == Filter.VIEWS) {
            List<Video> videos = videoRepository.findAllByTitleContaining(search_query);
            Collections.sort(videos, Comparator.comparing(Video::getNumberOfViews).reversed());
            return videos.stream().map(video -> convertToSearchableVideoDTO(video)).collect(Collectors.toList());
        }
        if (filter == Filter.MOST_LIKES) {
            List<Video> videos = videoRepository.findAllByTitleContaining(search_query);
            Collections.sort(videos, Comparator.comparing(Video::getNumberOfLikes).reversed());
            return videos.stream().map(video -> convertToSearchableVideoDTO(video)).collect(Collectors.toList());
        }
        if (filter == Filter.VIDEOS) { // only videos without order
            return getSearchedVideos(search_query);
        }
        if (filter == Filter.LENGTH_LONG) { // only videos without order
            return videoRepository.findAllByTitleContainingAndDurationGreaterThan(search_query, TWENTY_MINUTES_DURATION)
                    .stream()
                    .map(video -> convertToSearchableVideoDTO(video))
                    .collect(Collectors.toList());
        }
        if (filter == Filter.LENGTH_SHORT) { // only videos without order
            return videoRepository.findAllByTitleContainingAndDurationLessThanEqual(search_query, FOUR_MINUTES_DURATION)
                    .stream()
                    .map(video -> convertToSearchableVideoDTO(video))
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
