package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.AccessDeniedException;
import ittalents.javaee1.exceptions.BadRequestException;

import ittalents.javaee1.exceptions.NotFoundException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.repository.*;
import ittalents.javaee1.models.pojo.Comment;
import ittalents.javaee1.models.pojo.Playlist;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.models.pojo.Video;
import ittalents.javaee1.models.dto.*;
import ittalents.javaee1.util.ResponseMessage;
import ittalents.javaee1.util.StorageManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(produces = "application/json")
public abstract class GlobalController {

    protected final String EMPTY_HISTORY = "Empty history!";
    protected final String SUCCESSFULLY_CLEARED_HISTORY = "Successfully cleared history!";

    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VideoRepository videoRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    WatchHistoryRepository watchHistoryRepository;
    @Autowired
    StorageManager storageManager;
    @Autowired
    SearchHistoryRepository searchHistoryRepository;


    private static Logger logger = LogManager.getLogger(GlobalController.class);

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleServerError(Exception e) {
        logger.error(e.getMessage(), e);
        return new ResponseMessage(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseMessage handleNotFound(NotFoundException e) {
        logger.error(e.getMessage(), e);
        return new ResponseMessage(e.getMessage(), HttpStatus.NOT_FOUND.value(), LocalDateTime.now());
    }

    @ExceptionHandler({NotLoggedException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ResponseMessage handleNotLogged(Exception e) {
        logger.error(e.getMessage());
        return new ResponseMessage(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());
    }

    @ExceptionHandler({IOException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseMessage handleNotFoundFiles(Exception e) {
        logger.error(e.getMessage(), e);
        return new ResponseMessage(e.getMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
    }

    @ExceptionHandler({BadRequestException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseMessage handleOwnException(Exception e) {
        logger.error(e.getMessage(), e);
        return new ResponseMessage(e.getMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
    }

    boolean isValidString(String text) {
        return text != null && !text.isEmpty();
    }

    ViewCommentDTO convertToCommentDTO(Comment comment) {
        return new ViewCommentDTO(comment.getCommentId(), comment.getMessage(), comment.getDateOfPublication(),
                comment.getNumberOfLikes(), comment.getNumberOfDislikes(),
                userRepository.findById(comment.getPublisherId()).get().getFullName(), comment.getResponses().size());
    }

    ViewVideoDTO convertToViewVideoDTO(Video video) {
        return new ViewVideoDTO(video.getVideoId(), video.getTitle(), video.getCategory(), video.getDescription(),
                video.getURL(), video.getUploadDate(), video.getDuration(), video.getNumberOfLikes(),
                video.getNumberOfDislikes(), video.getNumberOfViews(),
                userRepository.findById(video.getUploaderId()).get().getFullName(), video.getComments().size());
    }

    SearchableVideoDTO convertToSearchableVideoDTO(Video video) {
        return new SearchableVideoDTO(video.getVideoId(), video.getTitle(),
                userRepository.findById(video.getUploaderId()).get().getFullName(),
                video.getUploaderId(),  video.isPrivate());
    }

    SearchablePlaylistDTO convertToSearchablePlaylistDTO(Playlist playlist) {
        return new SearchablePlaylistDTO(playlist.getPlaylistId(), playlist.getPlaylistName(),
                userRepository.findById(playlist.getOwnerId()).get().getFullName(),
                playlist.getVideosInPlaylist().size());
    }

    ViewPlaylistDTO convertToViewPlaylistDTO(Playlist playlist) {
        List<Video> videos = playlist.getVideosInPlaylist();
        List<SearchableVideoDTO> videosToShow = new ArrayList<>();
        for (Video v : videos) {
            videosToShow.add(convertToSearchableVideoDTO(v));
        }
        return new ViewPlaylistDTO(playlist.getPlaylistId(), playlist.getPlaylistName(),
                userRepository.findById(playlist.getOwnerId()).get().getFullName(), videosToShow.size(), videosToShow);
    }

    ViewProfileUserDTO convertToViewProfileUserDTO(User user) {
        List<SearchableVideoDTO> videosToShow = new ArrayList<>();
        videosToShow.addAll(user.getVideos()
                .stream()
                .map(this::convertToSearchableVideoDTO)
                .collect(Collectors.toList()));

        List<SearchablePlaylistDTO> playlistsToShow = new ArrayList<>();
        playlistsToShow.addAll(user.getPlaylists()
                .stream()
                .map(this::convertToSearchablePlaylistDTO)
                .collect(Collectors.toList()));

        return new ViewProfileUserDTO(user.getUserId(), user.getMySubscribers().size(), user.getFullName(),
                videosToShow, playlistsToShow);
    }

    SearchableUserDTO convertToSearchableUserDTO(User user) {
        return new SearchableUserDTO(user.getUserId(), user.getMySubscribers().size(), user.getFullName(),
                user.getVideos().size(), user.getPlaylists().size());
    }


}
