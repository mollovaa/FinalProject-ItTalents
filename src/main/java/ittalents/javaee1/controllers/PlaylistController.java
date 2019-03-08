package ittalents.javaee1.controllers;

import ittalents.javaee1.util.SessionManager;
import ittalents.javaee1.util.exceptions.AccessDeniedException;
import ittalents.javaee1.util.exceptions.BadRequestException;
import ittalents.javaee1.util.exceptions.InvalidInputException;
import ittalents.javaee1.util.exceptions.NotLoggedException;
import ittalents.javaee1.util.exceptions.PlaylistNotFoundException;
import ittalents.javaee1.util.exceptions.VideoNotFoundException;
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

    private void validatePlaylist(Playlist playlist) throws InvalidInputException {
        if (!isValidString(playlist.getPlaylistName())) {
            throw new InvalidInputException(INVALID_PLAYLIST_NAME);
        }
    }

    @GetMapping(value = "/{playlistId}/show")
    @ResponseBody
    public Object showPlaylist(@PathVariable long playlistId) throws PlaylistNotFoundException {
        return playlistRepository.getByPlaylistId(playlistId).convertToSearchablePlaylistDTO(userRepository);
    }

    @PostMapping(value = "/add")
    public Object addPlaylist(@RequestParam("name") String name, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Playlist playlist = new Playlist(name);
        validatePlaylist(playlist);
        playlist.setOwnerId(SessionManager.getLoggedUserId(session));
        return playlistRepository.save(playlist).convertToViewPlaylistDTO(userRepository);
    }

    @GetMapping(value = "/{playlistId}/videos/all")
    public Object showVideos(@PathVariable long playlistId) throws PlaylistNotFoundException {
        Playlist playlist = playlistRepository.getByPlaylistId(playlistId);
        List<Video> videos = playlist.getVideosInPlaylist();
        return videos
                .stream()
                .map(video -> video.convertToSearchableVideoDTO(userRepository))
                .collect(Collectors.toList());
    }

    @Transactional
    @DeleteMapping(value = "/{playlistId}/remove")
    public Object removePlaylist(@PathVariable long playlistId, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Playlist playlist = playlistRepository.getByPlaylistId(playlistId);
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
        Playlist playlist = playlistRepository.getByPlaylistId(playlistId);
        Video video = videoRepository.getByVideoId(videoId);
        //only public videos can be added to playlist:
        if (video.isPrivate()) {
            throw new VideoNotFoundException();
        }
        if (playlist.getOwnerId() != SessionManager.getLoggedUserId(session)) {
            throw new AccessDeniedException();
        }
        if (playlist.getVideosInPlaylist().contains(video)) {
            throw new BadRequestException(VIDEO_ALREADY_ADDED_TO_PLAYLIST);
        }
        playlist.getVideosInPlaylist().add(video);
        video.getPlaylistContainingVideo().add(playlist);
        videoRepository.save(video);
        return playlistRepository.save(playlist).convertToViewPlaylistDTO(userRepository);
    }

    @Transactional
    @PutMapping(value = "/{playlistId}/videos/{videoId}/remove")
    public Object removeVideoFromPlaylist(@PathVariable long playlistId, @PathVariable long videoId,
                                          HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Playlist playlist = playlistRepository.getByPlaylistId(playlistId);
        Video video = videoRepository.getByVideoId(videoId);
        if (video.isPrivate()) {
            throw new VideoNotFoundException();
        }
        if (playlist.getOwnerId() != SessionManager.getLoggedUserId(session)) {
            throw new AccessDeniedException();
        }
        if (!playlist.getVideosInPlaylist().contains(video)) {
            throw new BadRequestException(VIDEO_NOT_IN_PLAYLIST);
        }
        playlist.getVideosInPlaylist().remove(video);
        video.getPlaylistContainingVideo().remove(playlist);
        videoRepository.save(video);
        return playlistRepository.save(playlist).convertToViewPlaylistDTO(userRepository);
    }
}



