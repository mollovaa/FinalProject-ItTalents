package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.AccessDeniedException;
import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.exceptions.PlaylistNotFoundException;
import ittalents.javaee1.exceptions.VideoNotFoundException;
import ittalents.javaee1.models.pojo.Playlist;
import ittalents.javaee1.models.pojo.Video;

import ittalents.javaee1.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "/playlists")
public class PlaylistController extends GlobalController {

    private static final String VIDEO_NOT_IN_PLAYLIST = "Video not in playlist!";
    private static final String VIDEO_ALREADY_ADDED_TO_PLAYLIST = "Video already added to playlist!";
    private static final String SUCCESSFULLY_REMOVED_PLAYLIST = "Successfully removed a playlist!";
    private static final String INVALID_PLAYLIST_NAME = "Invalid playlist name!";
    private static final String CANNOT_ADD_PRIVATE_VIDEO_TO_PLAYLIST = "Cannot add private video to playlist!";
    private static final String EMPTY_PLAYLIST = "Empty playlist!";

    private void validatePlaylist(Playlist playlist) throws InvalidInputException {
        if (!isValidString(playlist.getPlaylistName())) {
            throw new InvalidInputException(INVALID_PLAYLIST_NAME);
        }
    }

    @GetMapping(value = "/{playlistId}/show")
    @ResponseBody
    public Object showPlaylist(@PathVariable long playlistId) throws PlaylistNotFoundException {
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistNotFoundException();
        }
        return playlistRepository.findById(playlistId).get().convertToViewPlaylistDTO();
    }

    @PostMapping(value = "/add")
    public Object addPlaylist(@RequestParam("name") String name, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Playlist playlist = new Playlist(name);
        validatePlaylist(playlist);
        playlist.setOwnerId(SessionManager.getLoggedUserId(session));
        return playlistRepository.save(playlist).convertToViewPlaylistDTO();
    }

    @GetMapping(value = "/{playlistId}/videos/all")
    public Object showVideos(@PathVariable long playlistId) {
        List<Video> videos = playlistRepository.getByPlaylistId(playlistId).getVideosInPlaylist();
        if (videos.isEmpty()) {
            return new ResponseMessage(EMPTY_PLAYLIST, HttpStatus.OK.value(), LocalDateTime.now());
        }
        return videos
                .stream()
                .map(Video::convertToSearchableVideoDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping(value = "/{playlistId}/remove")
    public Object removePlaylist(@PathVariable long playlistId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistNotFoundException();
        }
        Playlist playlist = playlistRepository.findById(playlistId).get();
        if (SessionManager.getLoggedUserId(session) != playlist.getOwnerId()) {
            throw new AccessDeniedException();
        }
        playlistRepository.deleteById(playlistId);
        return new ResponseMessage(SUCCESSFULLY_REMOVED_PLAYLIST, HttpStatus.OK.value(), LocalDateTime.now());
    }

    @Transactional
    @PutMapping(value = "/{playlistId}/videos/{videoId}/add")
    public Object addVideoToPlaylist(@PathVariable long playlistId, @PathVariable long videoId, HttpSession session)
            throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistNotFoundException();
        }                                               //only public videos can be added to playlist:
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Playlist playlist = playlistRepository.getByPlaylistId(playlistId);
        Video video = videoRepository.getByVideoId(videoId);
        if (playlist.getOwnerId() != SessionManager.getLoggedUserId(session)) {
            throw new AccessDeniedException();
        }
        if (video.isPrivate()) {
            throw new BadRequestException(CANNOT_ADD_PRIVATE_VIDEO_TO_PLAYLIST);
        }
        if (playlist.getVideosInPlaylist().contains(video)) {
            throw new BadRequestException(VIDEO_ALREADY_ADDED_TO_PLAYLIST);
        }
        playlist.getVideosInPlaylist().add(video);
        video.getPlaylistContainingVideo().add(playlist);
        videoRepository.save(video);
        return playlistRepository.save(playlist).convertToViewPlaylistDTO();
    }

    @Transactional
    @PutMapping(value = "/{playlistId}/videos/{videoId}/remove")
    public Object removeVideoFromPlaylist(@PathVariable long playlistId, @PathVariable long videoId,
                                          HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistNotFoundException();
        }
        if (!videoRepository.existsById(videoId) || videoRepository.getByVideoId(videoId).isPrivate()) {
            throw new VideoNotFoundException();
        }
        Playlist playlist = playlistRepository.getByPlaylistId(playlistId);
        Video video = videoRepository.getByVideoId(videoId);
        if (playlist.getOwnerId() != SessionManager.getLoggedUserId(session)) {
            throw new AccessDeniedException();
        }
        if (!playlist.getVideosInPlaylist().contains(video)) {
            throw new BadRequestException(VIDEO_NOT_IN_PLAYLIST);
        }
        playlist.getVideosInPlaylist().remove(video);
        video.getPlaylistContainingVideo().remove(playlist);
        videoRepository.save(video);
        return playlistRepository.save(playlist).convertToViewPlaylistDTO();
    }
}



