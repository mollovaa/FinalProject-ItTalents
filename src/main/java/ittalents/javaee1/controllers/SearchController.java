package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.hibernate.PlaylistRepository;
import ittalents.javaee1.hibernate.VideoRepository;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.search.Filter;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;

import ittalents.javaee1.models.dao.UserDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
public class SearchController {
    private static final String EMPTY_FILTER = "{\"error\" : \"Invalid filter option!\"}";
    private static final String EMPTY_SEARCH = "{\"error\" : \"No matches found!\"}";
    private static final long TWENTY_MINUTES_DURATION = 60 * 20;
    public static final long FOUR_MINUTES_DURATION = 4 * 60;
    @Autowired
    private UserDao userDao;
    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    private VideoRepository videoRepository;

    @GetMapping(value = "/search")
    public Object search(@RequestParam("q") String search_query, HttpServletResponse response) {
        try {
            if (search_query != null && !search_query.isEmpty()) {
                List<Searchable> result = new ArrayList<>();
                result.addAll(userDao.getByFullNameLike(search_query));
                result.addAll(videoRepository.findAllByTitleContaining(search_query));
                result.addAll(playlistRepository.findAllByPlaylistName(search_query));
                if (result.isEmpty()) {
                    return EMPTY_SEARCH;
                } else {
                    return result;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return EMPTY_SEARCH;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return UserController.SERVER_ERROR;
        }
    }

    @GetMapping(value = "/search/filterBy")
    public Object search(@RequestParam("q") String search_query, @RequestParam("filter") String filter,
                         HttpServletResponse response) {
        //TODO validate filted and search query then validate myFilter
        try {
            if (search_query != null && !search_query.isEmpty()) {
                if (filter != null && !filter.isEmpty()) {
                    Filter myFilter = getFilter(filter);
                    if (myFilter == null) {
                        throw new InvalidInputException(EMPTY_FILTER);
                    }
                    List<Searchable> result = new ArrayList<>();

                    if (myFilter.getSearchType() == SearchType.USER) { // Sort Only Users
                        result.addAll(userDao.getByFullNameLike(search_query));
                    } else if (myFilter.getSearchType() == SearchType.PLAYLIST) { //Sort Only Playlists
                        result.addAll(playlistRepository.findAllByPlaylistName(search_query));
                    } else {
                        //Video Filters
                        result.addAll(getFilteredVideos(search_query, myFilter));
                    }
                    if (result.isEmpty()) {
                        return EMPTY_SEARCH;
                    }
                    return result;
                } else {
                    throw new InvalidInputException(EMPTY_FILTER);
                }
            } else {
                throw new InvalidInputException(EMPTY_SEARCH);
            }
        } catch (InvalidInputException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return e.getMessage();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return UserController.SERVER_ERROR;
        }

    }

    private List<Video> getFilteredVideos(String search_query, Filter filter) throws Exception {
        if (filter == Filter.DATE_OF_UPLOAD) {
            List<Video> videos = videoRepository.findAllByTitleContaining(search_query);
            Collections.sort(videos, Comparator.comparing(Video::getUploadDate).reversed());
            return videos;
        }
        if (filter == Filter.VIEWS) {
            List<Video> videos = videoRepository.findAllByTitleContaining(search_query);
            Collections.sort(videos, Comparator.comparing(Video::getNumberOfViews).reversed());
            return videos;
        }
        if (filter == Filter.MOST_LIKES) {
            List<Video> videos = videoRepository.findAllByTitleContaining(search_query);
            Collections.sort(videos, Comparator.comparing(Video::getNumberOfLikes).reversed());
            return videos;
        }
        if (filter == Filter.VIDEOS) { // only videos without order
            return videoRepository.findAllByTitleContaining(search_query);
        }
        if (filter == Filter.LENGTH_LONG) { // only videos without order
            return videoRepository.findAllByTitleAndDurationGreaterThan(search_query, TWENTY_MINUTES_DURATION);
        }
        if (filter == Filter.LENGTH_SHORT) { // only videos without order
            return videoRepository.findAllByTitleAndDurationLessThanEqual(search_query, FOUR_MINUTES_DURATION);
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
