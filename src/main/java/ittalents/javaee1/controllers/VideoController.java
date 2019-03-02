package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.AccessDeniedException;
import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.exceptions.VideoNotFoundException;
import ittalents.javaee1.models.Comment;
import ittalents.javaee1.models.Notification;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.VideoCategory;
import ittalents.javaee1.models.WatchHistory;
import ittalents.javaee1.models.search.Filter;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.util.ResponseMessage;
import ittalents.javaee1.util.MailManager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ittalents.javaee1.controllers.SearchController.EMPTY_FILTER;

@RestController
@RequestMapping(value = "/videos")
public class VideoController extends GlobalController {

    private static final String NO_COMMENTS = "No comments!";
    private static final String ADDED_VIDEO_BY = "Video added by ";
    private static final String ADDED_VIDEO = "New video added!";
    private static final String INVALID_VIDEO_DESCRIPTION = "Invalid description";
    private static final String SUCCESSFULLY_REMOVED_VIDEO = "Successfully removed a video!";
    private static final String ALREADY_LIKED_VIDEO = "Already liked this video!";
    private static final String ALREADY_DISLIKED_VIDEO = "Already disliked this video!";
    private static final String INVALID_VIDEO_TITLE = "Invalid title!";
    private static final String INVALID_VIDEO_DURATION = "Invalid duration!";
    private static final String INVALID_VIDEO_CATEGORY = "Invalid category!";
    private static final String CANNOT_REMOVE_DISLIKE = "Video not disliked!";
    private static final String CANNOT_REMOVE_LIKE = "Video not liked!";

    private static final String EMPTY_VIDEO_STORAGE = "Empty video storage";
    private static final String ALREADY_UPLOADED = "Video is already uploaded!";

    private void validateVideo(Video video) throws InvalidInputException {
        if (!isValidString(video.getTitle())) {
            throw new InvalidInputException(INVALID_VIDEO_TITLE);
        }
        if (!isValidString(video.getDescription())) {
            throw new InvalidInputException(INVALID_VIDEO_DESCRIPTION);
        }
        if (video.getDuration() <= 0) {
            throw new InvalidInputException(INVALID_VIDEO_DURATION);
        }
        if (!isValidString(video.getCategory()) || (!VideoCategory.contains(video.getCategory()))) {
            throw new InvalidInputException(INVALID_VIDEO_CATEGORY);
        }
        video.setUploadDate(LocalDate.now());
        video.setNumberOfDislikes(0);
        video.setNumberOfLikes(0);
        video.setNumberOfViews(0);
    }

    @GetMapping(value = "/{videoId}/show")
    public Object showVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        video.setNumberOfViews(video.getNumberOfViews() + 1);
        videoRepository.save(video);
        if (SessionManager.isLogged(session)) {
            User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
            if (!watchHistoryRepository.existsByVideoAndUser(video, user)) {
                WatchHistory historyRecord = new WatchHistory(user, video);
                watchHistoryRepository.save(historyRecord);
            } else {
                WatchHistory watchHistory = watchHistoryRepository.getByUserAndVideo(user, video);
                watchHistory.setDate(LocalDate.now());
                watchHistoryRepository.save(watchHistory);
            }
        }
        return this.convertToViewVideoDTO(video);
    }

    @GetMapping(value = "/{videoId}/comments/all")
    public Object showVideoComments(@PathVariable long videoId) throws BadRequestException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        List<Comment> comments = video.getComments();   //need only comments , NOT responses
        comments = comments.stream()
                .filter(comment -> comment.getResponseToId() == null)
                .collect(Collectors.toList());
        if (comments.isEmpty()) {
            return new ResponseMessage(NO_COMMENTS, HttpStatus.OK.value(), LocalDateTime.now());
        }
        return comments.stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());
    }

    @Getter
    @AllArgsConstructor
    enum CommentFilter {

        NEWEST("newest"), TOP_COMMENTS("top");
        private final String filter;

        public static CommentFilter getCommentFilter(String filter) {
            CommentFilter[] allFilters = CommentFilter.values();
            for (int i = 0; i < allFilters.length; i++) {
                if (filter.equals(allFilters[i].getFilter())) {
                    return CommentFilter.values()[i];
                }
            }
            return null;
        }
    }

    @GetMapping(value = "/{videoId}/comments/all/sort")
    public Object showVideoCommentsSortedBy(@PathVariable long videoId, @RequestParam String filter) throws BadRequestException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        List<Comment> comments = video.getComments();   //need only comments , NOT responses
        comments = comments.stream()
                .filter(comment -> comment.getResponseToId() == null)
                .collect(Collectors.toList());
        if (comments.isEmpty()) {
            return new ResponseMessage(NO_COMMENTS, HttpStatus.OK.value(), LocalDateTime.now());
        }

        if (!isValidString(filter)) {
            throw new InvalidInputException(EMPTY_FILTER);
        }
        CommentFilter myFilter = CommentFilter.getCommentFilter(filter);
        if (myFilter == null) {
            throw new InvalidInputException(EMPTY_FILTER);
        }
        if (myFilter == CommentFilter.NEWEST) {
            comments.sort(Comparator.comparing(Comment::getDateOfPublication).reversed());
        } else if (myFilter == CommentFilter.TOP_COMMENTS) {
            comments.sort(Comparator.comparing(Comment::getNumberOfLikes).reversed());
        } else {
            throw new InvalidInputException(EMPTY_FILTER);
        }
        return comments.stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{videoId}/download")
    public byte[] downloadVideo(@PathVariable long videoId) throws BadRequestException, IOException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        if (video.getURL() == null || video.getURL().isEmpty()) {
            throw new InvalidInputException(EMPTY_VIDEO_STORAGE);
        }
        return storageManager.downloadVideo(video);
    }

    @PostMapping(value = "/{videoId}/upload")
    public Object uploadVideo(@PathVariable long videoId, @RequestPart(value = "file") MultipartFile file,
                              HttpSession session)
            throws BadRequestException, IOException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        if (SessionManager.getLoggedUserId(session) != video.getUploaderId()) {
            throw new AccessDeniedException();
        }
        if (video.getURL() != null && !video.getURL().isEmpty()) { // video already uploaded
            throw new InvalidInputException(ALREADY_UPLOADED);
        }
        video.setURL(storageManager.uploadVideo(file, video));
        //notify all subscribers of current user:
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        for (User u : user.getMySubscribers()) {
            Notification notif = new Notification(ADDED_VIDEO_BY + user.getFullName(), u.getUserId());
            notificationRepository.save(notif);
            MailManager.sendEmail(u.getEmail(), ADDED_VIDEO, ADDED_VIDEO_BY + user.getFullName());
        }
        return convertToViewVideoDTO(videoRepository.save(video));
    }

    @PostMapping(value = "/add")
    public Object addVideo(@RequestBody Video video, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        this.validateVideo(video);
        video.setUploaderId(SessionManager.getLoggedUserId(session));
        return convertToViewVideoDTO(videoRepository.save(video));
    }

    @DeleteMapping(value = "/{videoId}/remove")
    public Object removeVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        if (SessionManager.getLoggedUserId(session) != video.getUploaderId()) {
            throw new AccessDeniedException();
        }
        storageManager.deleteVideo(video);
        videoRepository.delete(video);
        return new ResponseMessage(SUCCESSFULLY_REMOVED_VIDEO, HttpStatus.OK.value(), LocalDateTime.now());
    }

    @PutMapping(value = "/{videoId}/like")
    public Object likeVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getLikedVideos().contains(video)) {
            throw new BadRequestException(ALREADY_LIKED_VIDEO);
        }
        if (user.getDislikedVideos().contains(video)) {
            user.getDislikedVideos().remove(video);
            video.getUsersDislikedVideo().remove(user);
            videoRepository.save(video);
            userRepository.save(user);
            video.setNumberOfDislikes(video.getNumberOfDislikes() - 1);
        }
        video.getUsersLikedVideo().add(user);
        user.addLikedVideo(video);
        video.setNumberOfLikes(video.getNumberOfLikes() + 1);
        videoRepository.save(video);
        userRepository.save(user);
        return convertToViewVideoDTO(video);
    }

    @PutMapping(value = "/{videoId}/dislike")
    public Object dislikeVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getDislikedVideos().contains(video)) {
            throw new BadRequestException(ALREADY_DISLIKED_VIDEO);
        }
        if (user.getLikedVideos().contains(video)) {
            user.getLikedVideos().remove(video);
            video.getUsersLikedVideo().remove(user);
            userRepository.save(user);
            videoRepository.save(video);
            video.setNumberOfLikes(video.getNumberOfLikes() - 1);
        }
        video.getUsersDislikedVideo().add(user);
        user.addDislikedVideo(video);
        video.setNumberOfDislikes(video.getNumberOfDislikes() + 1);
        videoRepository.save(video);
        userRepository.save(user);
        return convertToViewVideoDTO(video);
    }

    @PutMapping(value = "/{videoId}/dislikes/remove")
    public Object removeDislike(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (!user.getDislikedVideos().contains(video)) {
            throw new BadRequestException(CANNOT_REMOVE_DISLIKE);
        }
        user.getDislikedVideos().remove(video);
        video.getUsersDislikedVideo().remove(user);
        video.setNumberOfDislikes(video.getNumberOfDislikes() - 1);
        userRepository.save(user);
        return convertToViewVideoDTO(videoRepository.save(video));
    }

    @PutMapping(value = "/{videoId}/likes/remove")
    public Object removeLike(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (!user.getLikedVideos().contains(video)) {
            throw new BadRequestException(CANNOT_REMOVE_LIKE);
        }
        user.getLikedVideos().remove(video);
        video.getUsersLikedVideo().remove(user);
        video.setNumberOfLikes(video.getNumberOfLikes() - 1);
        userRepository.save(user);
        return convertToViewVideoDTO(videoRepository.save(video));
    }


}
