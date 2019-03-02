package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.AccessDeniedException;
import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.exceptions.PlaylistNotFoundException;
import ittalents.javaee1.exceptions.VideoNotFoundException;
import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.Video;

import ittalents.javaee1.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;


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
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistNotFoundException();
        }
        return convertToViewPlaylistDTO(playlistRepository.findById(playlistId).get());
    }

    @PostMapping(value = "/add")
    public Object addPlaylist(@RequestParam("name") String name, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        Playlist playlist = new Playlist(name);
        validatePlaylist(playlist);
        playlist.setOwnerId(SessionManager.getLoggedUserId(session));
        return convertToViewPlaylistDTO(playlistRepository.save(playlist));
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

    @PutMapping(value = "/{playlistId}/videos/{videoId}/add")
    public Object addVideoToPlaylist(@PathVariable long playlistId, @PathVariable long videoId, HttpSession session)
            throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistNotFoundException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Playlist playlist = playlistRepository.findById(playlistId).get();
        Video video = videoRepository.findById(videoId).get();
        if (playlist.getOwnerId() != SessionManager.getLoggedUserId(session)) {
            throw new AccessDeniedException();
        }
        if (playlist.getVideosInPlaylist().contains(video)) {
            throw new BadRequestException(VIDEO_ALREADY_ADDED_TO_PLAYLIST);
        }
        playlist.getVideosInPlaylist().add(video);
        video.getPlaylistContainingVideo().add(playlist);
        videoRepository.save(video);
        return convertToViewPlaylistDTO(playlistRepository.save(playlist));
    }

    @PutMapping(value = "/{playlistId}/videos/{videoId}/remove")
    public Object removeVideoFromPlaylist(@PathVariable long playlistId, @PathVariable long videoId,
                                          HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistNotFoundException();
        }
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException();
        }
        Playlist playlist = playlistRepository.findById(playlistId).get();
        Video video = videoRepository.findById(videoId).get();
        if (playlist.getOwnerId() != SessionManager.getLoggedUserId(session)) {
            throw new AccessDeniedException();
        }
        if (!playlist.getVideosInPlaylist().contains(video)) {
            throw new BadRequestException(VIDEO_NOT_IN_PLAYLIST);
        }
        playlist.getVideosInPlaylist().remove(video);
        video.getPlaylistContainingVideo().remove(playlist);
        videoRepository.save(video);
        return convertToViewPlaylistDTO(playlistRepository.save(playlist));
    }
}



