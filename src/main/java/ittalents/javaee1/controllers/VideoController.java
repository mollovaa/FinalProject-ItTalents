package ittalents.javaee1.controllers;

import ittalents.javaee1.models.dto.ViewCommentDTO;
import ittalents.javaee1.util.SessionManager;
import ittalents.javaee1.util.AmazonClient;
import ittalents.javaee1.util.exceptions.AccessDeniedException;
import ittalents.javaee1.util.exceptions.BadRequestException;
import ittalents.javaee1.util.exceptions.InvalidInputException;
import ittalents.javaee1.util.exceptions.NotLoggedException;
import ittalents.javaee1.util.exceptions.VideoNotFoundException;
import ittalents.javaee1.models.pojo.Comment;
import ittalents.javaee1.models.pojo.Notification;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.models.pojo.Video;
import ittalents.javaee1.models.pojo.VideoCategory;
import ittalents.javaee1.models.pojo.WatchHistory;
import ittalents.javaee1.models.search.CommentFilter;
import ittalents.javaee1.util.ResponseMessage;
import ittalents.javaee1.util.MailManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ittalents.javaee1.controllers.SearchController.EMPTY_FILTER;

@RestController
@RequestMapping(value = "/videos")
public class VideoController extends GlobalController {

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

    @Autowired
    private AmazonClient amazonClient;

    private void validateVideo(Video video) throws InvalidInputException {
        if (!isValidString(video.getTitle())) {
            throw new InvalidInputException(INVALID_VIDEO_TITLE);
        }
        if (!isValidString(video.getDescription())) {
            throw new InvalidInputException(INVALID_VIDEO_DESCRIPTION);
        }
        if (video.getDuration() < 0) {
            throw new InvalidInputException(INVALID_VIDEO_DURATION);
        }
        if (!isValidString(video.getCategory()) || (!VideoCategory.contains(video.getCategory()))) {
            throw new InvalidInputException(INVALID_VIDEO_CATEGORY);
        }
    }

    private void setInitialVideoValues(Video video) {
        video.setUploadDate(LocalDate.now());
        video.setNumberOfDislikes(0);
        video.setNumberOfLikes(0);
        video.setNumberOfViews(0);
        video.setURL(null);
    }

    private void validatePrivateAccessToVideo(HttpSession session, Video video) throws BadRequestException {
        if (SessionManager.isLogged(session)) {
            User user = userRepository.getById(SessionManager.getLoggedUserId(session));
            if (user.getUserId() != video.getUploaderId() && video.isPrivate()) {    //private & not user`s
                throw new VideoNotFoundException();
            }
        } else if (video.isPrivate()) {  //private & user not logged
            throw new VideoNotFoundException();
        }
    }

    private void addToWatchHistory(HttpSession session, Video video) throws BadRequestException {
        if (SessionManager.isLogged(session)) {
            User user = userRepository.getById(SessionManager.getLoggedUserId(session));
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

        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        this.validatePrivateAccessToVideo(session, video);
        this.addToWatchHistory(session, video);

        video.setNumberOfViews(video.getNumberOfViews() + 1);
        videoRepository.save(video);
        return video.convertToViewVideoDTO(userRepository.getById(video.getUploaderId()).getFullName());
    }

    private List<Comment> getCommentsWithoutResponses(Video video) {
        return video.getComments()
                .stream()
                .filter(comment -> comment.getResponseToId() == null)
                .collect(Collectors.toList());
    }


    @GetMapping(value = "/{videoId}/comments/all")
    public Object showVideoComments(@PathVariable long videoId, HttpSession session) throws BadRequestException {

        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        this.validatePrivateAccessToVideo(session, video);
        List<ViewCommentDTO> comments = new ArrayList<>();
        for (Comment response : getCommentsWithoutResponses(video)) {
            comments.add(response.convertToViewCommentDTO(userRepository.getById(response.getPublisherId()).getFullName()));
        }
        return comments;
    }

    private List<Comment> filterComments(List<Comment> comments, String filter) throws InvalidInputException {
        if (!isValidString(filter)) {
            throw new InvalidInputException(EMPTY_FILTER);
        }
        CommentFilter myFilter = CommentFilter.getCommentFilter(filter);
        if (myFilter == null) {
            throw new InvalidInputException(EMPTY_FILTER);
        }
        switch (myFilter) {
            case NEWEST:
                comments.sort(Comparator.comparing(Comment::getDateOfPublication).reversed());
                return comments;
            case TOP_COMMENTS:
                comments.sort(Comparator.comparing(Comment::getNumberOfLikes).reversed());
                return comments;
            default:
                throw new InvalidInputException(EMPTY_FILTER);
        }
    }

    @GetMapping(value = "/{videoId}/comments/all/sort")
    public Object showVideoCommentsSortedBy(@PathVariable long videoId, @RequestParam String filter,
                                            HttpSession session) throws BadRequestException {

        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        this.validatePrivateAccessToVideo(session, video);

        List<ViewCommentDTO> comments = new ArrayList<>();
        for (Comment response : filterComments(getCommentsWithoutResponses(video), filter)) {
            comments.add(response.convertToViewCommentDTO(userRepository.getById(response.getPublisherId()).getFullName()));
        }
        return comments;
    }

    @GetMapping(value = "/{videoId}/play")
    public Object playVideo(@PathVariable long videoId, HttpSession session) throws
            BadRequestException {
        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found

        this.validatePrivateAccessToVideo(session, video);

        if (video.getURL() == null || video.getURL().isEmpty()) {
            throw new InvalidInputException(EMPTY_VIDEO_STORAGE);
        }
        return video.getURL();
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
            throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        if (SessionManager.getLoggedUserId(session) != video.getUploaderId()) {
            throw new AccessDeniedException();
        }
        if (video.getURL() != null && !video.getURL().isEmpty()) { // video already uploaded
            throw new InvalidInputException(ALREADY_UPLOADED);
        }
        video.setURL(this.amazonClient.uploadFile(file, video));

        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        if (!video.isPrivate()) {
            this.notifySubscribers(user);
        }
        return videoRepository.save(video).convertToViewVideoDTO(user.getFullName());
    }

    @PostMapping(value = "/add")
    public Object addVideo(@RequestBody Video video, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User loggedUser = userRepository.getById(SessionManager.getLoggedUserId(session));
        this.validateVideo(video);
        this.setInitialVideoValues(video);
        video.setUploaderId(loggedUser.getUserId());
        return videoRepository.save(video).convertToViewVideoDTO(loggedUser.getFullName());
    }

    @Transactional
    @DeleteMapping(value = "/{videoId}/remove")
    public Object removeVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        if (SessionManager.getLoggedUserId(session) != video.getUploaderId()) {
            if (!video.isPrivate()) {        //public video
                throw new AccessDeniedException();
            }
            throw new VideoNotFoundException();  //if video is private -> it cannot be seen by user
        }
        amazonClient.deleteFileFromS3Bucket(video.getURL());
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

    @Transactional
    @PutMapping(value = "/{videoId}/like")
    public Object likeVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
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
        String uploaderName = userRepository.getById(video.getUploaderId()).getFullName();
        return video.convertToViewVideoDTO(uploaderName);
    }

    @Transactional
    @PutMapping(value = "/{videoId}/dislike")
    public Object dislikeVideo(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
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
        String uploaderName = userRepository.getById(video.getUploaderId()).getFullName();
        return video.convertToViewVideoDTO(uploaderName);
    }

    @Transactional
    @PutMapping(value = "/{videoId}/dislikes/remove")
    public Object removeDislike(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        if (user.getUserId() != video.getUploaderId() && video.isPrivate()) {    //private & not user`s
            throw new VideoNotFoundException();
        }
        if (!user.getDislikedVideos().contains(video)) {
            throw new BadRequestException(CANNOT_REMOVE_DISLIKE);
        }
        this.removeDislike(user, video);
        String uploaderName = userRepository.getById(video.getUploaderId()).getFullName();
        return videoRepository.save(video).convertToViewVideoDTO(uploaderName);
    }

    @Transactional
    @PutMapping(value = "/{videoId}/likes/remove")
    public Object removeLike(@PathVariable long videoId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Video video = videoRepository.getByVideoId(videoId); // throws Video Not Found
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        if (user.getUserId() != video.getUploaderId() && video.isPrivate()) {    //private & not user`s
            throw new VideoNotFoundException();
        }
        if (!user.getLikedVideos().contains(video)) {
            throw new BadRequestException(CANNOT_REMOVE_LIKE);
        }
        this.removeLike(user, video);
        String uploaderName = userRepository.getById(video.getUploaderId()).getFullName();
        return videoRepository.save(video).convertToViewVideoDTO(uploaderName);
    }


}
