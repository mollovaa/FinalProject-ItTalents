package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.*;
import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.Video;

import ittalents.javaee1.models.dto.VideoInPlaylistDTO;
import ittalents.javaee1.models.dto.ViewPlaylistDTO;
import ittalents.javaee1.models.dto.ViewVideoDTO;
import ittalents.javaee1.util.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@RestController
@RequestMapping(value = "/playlists")
public class PlaylistController extends GlobalController {

    private static final String VIDEO_NOT_IN_PLAYLIST = "You cannot remove a video from playlist as it is not in the playlist!";
    private static final String VIDEO_ALREADY_ADDED_TO_PLAYLIST = "You have already added this video to playlist!";
    private static final String SUCCESSFULLY_REMOVED_PLAYLIST = "You have successfully removed a playlist!";

    private void validatePlaylist(Playlist playlist) throws InvalidInputException {
        if (!isValidString(playlist.getPlaylistName())) {
            throw new InvalidInputException("Invalid playlist name!");
        }
    }

    private ViewPlaylistDTO convertToPlaylistDTO(Playlist playlist) {
        List<Video> videos = playlist.getVideosInPlaylist();
        List<VideoInPlaylistDTO> videosToShow = new ArrayList<>();
        for (Video v : videos) {
            videosToShow.add(convertToVideoInPlaylistDTO(v));
        }
        return new ViewPlaylistDTO(playlist.getPlaylistId(), playlist.getPlaylistName(),
                userRepository.findById(playlist.getOwnerId()).get().getFullName(), videosToShow);
    }

    @GetMapping(value = "/{playlistId}/show")
    @ResponseBody
    public Object showPlaylist(@PathVariable long playlistId) throws PlaylistNotFoundException {
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistNotFoundException();
        }
        return convertToPlaylistDTO(playlistRepository.findById(playlistId).get());
    }

    @PostMapping(value = "/add")
    public Object addPlaylist(@RequestBody Playlist playlist, HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        validatePlaylist(playlist);
        playlist.setOwnerId(SessionManager.getLoggedUserId(session));
        return convertToPlaylistDTO(playlistRepository.save(playlist));
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
        return new ErrorMessage(SUCCESSFULLY_REMOVED_PLAYLIST, HttpStatus.OK.value(), LocalDateTime.now());
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
        return convertToPlaylistDTO(playlistRepository.save(playlist));
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
        return convertToPlaylistDTO(playlistRepository.save(playlist));
    }
}



