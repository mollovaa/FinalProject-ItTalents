package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.PlaylistNotFoundException;
import ittalents.javaee1.hibernate.PlaylistRepository;
import ittalents.javaee1.hibernate.VideoRepository;
import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.Video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static ittalents.javaee1.controllers.ResponseMessages.*;


@RestController
public class PlaylistController implements GlobalController {

    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    VideoRepository videoRepository;


    @GetMapping(value = "playlist/showPlaylist/{playlistId}")
    @ResponseBody
    public Object showPlaylist(@PathVariable long playlistId, HttpServletResponse response) {
        if (playlistRepository.existsById(playlistId)) {
            return playlistRepository.findById(playlistId).get();
        }
        return responseForBadRequest(response, NOT_FOUND);
    }

    //validate playlistName
    @PostMapping(value = "playlist/createPlaylist")
    public Object addPlaylist(@RequestBody Playlist playlist, HttpSession session, HttpServletResponse response)
            throws BadRequestException {
        if (SessionManager.isLogged(session)) {
            playlist.setOwnerId(SessionManager.getLoggedUserId(session));
            return playlistRepository.save(playlist);
        }
        return redirectingToLogin(response);
    }

    @GetMapping(value = "playlist/removePlaylist/{playlistId}")
    public Object removePlaylist(@PathVariable long playlistId, HttpSession session, HttpServletResponse response)
            throws BadRequestException {
        if (playlistRepository.existsById(playlistId)) {
            if (SessionManager.isLogged(session)) {
                Playlist playlist = playlistRepository.findById(playlistId).get();
                if (SessionManager.getLoggedUserId(session) == playlist.getOwnerId()) {
                    playlistRepository.delete(playlist);
                    return SUCCESSFULLY_REMOVED_PLAYLIST;
                } else {
                    return responseForBadRequest(response, ACCESS_DENIED);
                }
            } else {
                return redirectingToLogin(response);
            }
        } else {
            return responseForBadRequest(response, NOT_FOUND);
        }

    }

    @GetMapping(value = "playlist/addVideoToPlaylist/{playlistId}/{videoId}")
    public Object addVideoToPlaylist(@PathVariable long playlistId, @PathVariable long videoId, HttpSession session,
                                     HttpServletResponse response) throws BadRequestException {
        if (playlistRepository.existsById(playlistId) && videoRepository.existsById(videoId)) {
            if (SessionManager.isLogged(session)) {
                Playlist playlist = playlistRepository.findById(playlistId).get();
                Video video = videoRepository.findById(videoId).get();
                if (playlist.getOwnerId() == SessionManager.getLoggedUserId(session)) {
                    if (playlist.getVideosInPlaylist().contains(video)) {
                        return responseForBadRequest(response, VIDEO_ALREADY_ADDED_TO_PLAYLIST);
                    }
                    playlist.getVideosInPlaylist().add(video);
                    video.getPlaylistContainingVideo().add(playlist);
                    videoRepository.save(video);
                    playlistRepository.save(playlist);
                    return playlist;
                } else {
                    return responseForBadRequest(response, ACCESS_DENIED);
                }
            } else {
                return redirectingToLogin(response);
            }
        } else {
            return responseForBadRequest(response, NOT_FOUND);
        }
    }

    @GetMapping(value = "playlist/removeVideoFromPlaylist/{playlistId}/{videoId}")
    public Object removeVideoFromPlaylist(@PathVariable long playlistId, @PathVariable long videoId,
                                          HttpSession session, HttpServletResponse response) throws BadRequestException {
        if (playlistRepository.existsById(playlistId) && videoRepository.existsById(videoId)) {
            if (SessionManager.isLogged(session)) {
                Playlist playlist = playlistRepository.findById(playlistId).get();
                Video video = videoRepository.findById(videoId).get();
                if (playlist.getOwnerId() == SessionManager.getLoggedUserId(session)) {
                    if (!playlist.getVideosInPlaylist().contains(video)) {
                        return responseForBadRequest(response, VIDEO_NOT_IN_PLAYLIST);
                    }
                    playlist.getVideosInPlaylist().remove(video);
                    video.getPlaylistContainingVideo().remove(playlist);
                    videoRepository.save(video);
                    playlistRepository.save(playlist);
                    return playlist;
                } else {
                    return responseForBadRequest(response, ACCESS_DENIED);
                }
            } else {
                return redirectingToLogin(response);
            }
        } else {
            return responseForBadRequest(response, NOT_FOUND);
        }
    }


}
