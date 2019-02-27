package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.*;
import ittalents.javaee1.hibernate.PlaylistRepository;
import ittalents.javaee1.hibernate.VideoRepository;
import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.Video;

import ittalents.javaee1.util.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;

import static ittalents.javaee1.controllers.MyResponse.*;


@RestController
public class PlaylistController extends GlobalController {

    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    VideoRepository videoRepository;

    private void validatePlaylist(Playlist playlist) throws InvalidInputException {
        if (playlist.getPlaylistName() == null || playlist.getPlaylistName().isEmpty()) {
            throw new InvalidInputException("Invalid playlist name!");
        }
    }

    @GetMapping(value = "playlist/showPlaylist/{playlistId}")
    @ResponseBody
    public Object showPlaylist(@PathVariable long playlistId) throws PlaylistNotFoundException {
        if (playlistRepository.existsById(playlistId)) {
            return playlistRepository.findById(playlistId).get();
        }
        throw new PlaylistNotFoundException();
    }

    @PostMapping(value = "playlist/createPlaylist")
    public Object addPlaylist(@RequestBody Playlist playlist, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        validatePlaylist(playlist);
        playlist.setOwnerId(SessionManager.getLoggedUserId(session));
        return playlistRepository.save(playlist);
    }

    @GetMapping(value = "playlist/removePlaylist/{playlistId}")
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
        playlistRepository.delete(playlist);
        return new ErrorMessage(SUCCESSFULLY_REMOVED_PLAYLIST, HttpStatus.OK.value(), LocalDateTime.now());
    }

    @GetMapping(value = "playlist/addVideoToPlaylist/{playlistId}/{videoId}")
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
        return playlistRepository.save(playlist);
    }

    @GetMapping(value = "playlist/removeVideoFromPlaylist/{playlistId}/{videoId}")
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
        return playlistRepository.save(playlist);
    }
}



