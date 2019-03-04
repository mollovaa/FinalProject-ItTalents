package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.AccessDeniedException;
import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.exceptions.VideoNotFoundException;
import ittalents.javaee1.models.pojo.Comment;
import ittalents.javaee1.models.pojo.Notification;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.models.pojo.Video;
import ittalents.javaee1.models.pojo.VideoCategory;
import ittalents.javaee1.models.pojo.WatchHistory;
import ittalents.javaee1.models.search.CommentFilter;
import ittalents.javaee1.util.ResponseMessage;
import ittalents.javaee1.util.MailManager;

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

    private void validatePrivateAccessToVideo(HttpSession session, Video video)
            throws NotLoggedException, VideoNotFoundException {
        if (SessionManager.isLogged(session)) {
            User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
            if (user.getUserId() != video.getUploaderId() && video.isPrivate()) {    //private & not user`s
                throw new VideoNotFoundException();
            }
        } else if (video.isPrivate()) {  //private & user not logged
            throw new VideoNotFoundException();
        }
    }

    private void addToWatchHistory(HttpSession session, Video video) throws NotLoggedException {
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
    }

    @GetMapping(value = "/{videoId}/show")
    public Object showVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();

        this.validatePrivateAccessToVideo(session, video);
        this.addToWatchHistory(session, video);

        video.setNumberOfViews(video.getNumberOfViews() + 1);
        videoRepository.save(video);
        return this.convertToViewVideoDTO(video);
    }

    private List<Comment> getCommentsWithoutResponses(Video video) {
        return video.getComments()
                .stream()
                .filter(comment -> comment.getResponseToId() == null)
                .collect(Collectors.toList());
    }


    @GetMapping(value = "/{videoId}/comments/all")
    public Object showVideoComments(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();
        this.validatePrivateAccessToVideo(session, video);

        List<Comment> comments = this.getCommentsWithoutResponses(video);

        if (comments.isEmpty()) {
            return new ResponseMessage(NO_COMMENTS, HttpStatus.OK.value(), LocalDateTime.now());
        }
        return comments.stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());
    }

    private void filterComments(List<Comment> comments, String filter) throws InvalidInputException {
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
    }

    @GetMapping(value = "/{videoId}/comments/all/sort")
    public Object showVideoCommentsSortedBy(@PathVariable long videoId, @RequestParam String filter,
                                            HttpSession session) throws BadRequestException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();

        this.validatePrivateAccessToVideo(session, video);

        List<Comment> comments = this.getCommentsWithoutResponses(video);
        if (comments.isEmpty()) {
            return new ResponseMessage(NO_COMMENTS, HttpStatus.OK.value(), LocalDateTime.now());
        }

        this.filterComments(comments, filter);

        return comments.stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{videoId}/download")
    public byte[] downloadVideo(@PathVariable long videoId, HttpSession session) throws
            BadRequestException, IOException {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Video video = videoRepository.findById(videoId).get();

        this.validatePrivateAccessToVideo(session, video);

        if (video.getURL() == null || video.getURL().isEmpty()) {
            throw new InvalidInputException(EMPTY_VIDEO_STORAGE);
        }
        return storageManager.downloadVideo(video);
    }

    private void notifySubscribers(User user) {  //notify all subscribers of current user:
        for (User u : user.getMySubscribers()) {
            Notification notif = new Notification(ADDED_VIDEO_BY + user.getFullName(), u.getUserId());
            notificationRepository.save(notif);
            MailManager.sendEmail(u.getEmail(), ADDED_VIDEO, ADDED_VIDEO_BY + user.getFullName());
        }
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

        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (!video.isPrivate()) {
            this.notifySubscribers(user);
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
            if (!video.isPrivate()) {
                throw new AccessDeniedException();
            }
            throw new VideoNotFoundException();
        }
        storageManager.deleteVideo(video);
        videoRepository.delete(video);
        return new ResponseMessage(SUCCESSFULLY_REMOVED_VIDEO, HttpStatus.OK.value(), LocalDateTime.now());
    }

    private void saveUserAndVideo(User user, Video video) {
        videoRepository.save(video);
        userRepository.save(user);
    }

    private void removeDislike(User user, Video video) {
        user.getDislikedVideos().remove(video);
        video.getUsersDislikedVideo().remove(user);
        video.setNumberOfDislikes(video.getNumberOfDislikes() - 1);
        this.saveUserAndVideo(user, video);
    }

    private void removeLike(User user, Video video) {
        user.getLikedVideos().remove(video);
        video.getUsersLikedVideo().remove(user);
        video.setNumberOfLikes(video.getNumberOfLikes() - 1);
        this.saveUserAndVideo(user, video);
    }

    private void addLike(User user, Video video) {
        video.getUsersLikedVideo().add(user);
        user.addLikedVideo(video);
        video.setNumberOfLikes(video.getNumberOfLikes() + 1);
        this.saveUserAndVideo(user, video);
    }

    private void addDislike(User user, Video video) {
        video.getUsersDislikedVideo().add(user);
        user.addDislikedVideo(video);
        video.setNumberOfDislikes(video.getNumberOfDislikes() + 1);
        this.saveUserAndVideo(user, video);
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
        if (user.getUserId() != video.getUploaderId() && video.isPrivate()) {    //private & not user`s
            throw new VideoNotFoundException();
        }
        if (user.getLikedVideos().contains(video)) {
            throw new BadRequestException(ALREADY_LIKED_VIDEO);
        }
        if (user.getDislikedVideos().contains(video)) {
            this.removeDislike(user, video);
        }
        this.addLike(user, video);
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
        if (user.getUserId() != video.getUploaderId() && video.isPrivate()) {    //private & not user`s
            throw new VideoNotFoundException();
        }
        if (user.getDislikedVideos().contains(video)) {
            throw new BadRequestException(ALREADY_DISLIKED_VIDEO);
        }
        if (user.getLikedVideos().contains(video)) {
            this.removeLike(user, video);
        }
        this.addDislike(user, video);
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
        if (user.getUserId() != video.getUploaderId() && video.isPrivate()) {    //private & not user`s
            throw new VideoNotFoundException();
        }
        if (!user.getDislikedVideos().contains(video)) {
            throw new BadRequestException(CANNOT_REMOVE_DISLIKE);
        }
        this.removeDislike(user, video);
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
        if (user.getUserId() != video.getUploaderId() && video.isPrivate()) {    //private & not user`s
            throw new VideoNotFoundException();
        }
        if (!user.getLikedVideos().contains(video)) {
            throw new BadRequestException(CANNOT_REMOVE_LIKE);
        }
        this.removeLike(user, video);
        return convertToViewVideoDTO(videoRepository.save(video));
    }


}
