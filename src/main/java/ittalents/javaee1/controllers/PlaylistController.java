package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.PlaylistNotFoundException;
import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.dao.PlaylistDao;
import ittalents.javaee1.models.dao.VideoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static ittalents.javaee1.controllers.ResponseMessages.*;
import static javax.management.Query.value;

@RestController
public class PlaylistController extends GlobalController {

    @Autowired
    PlaylistDao playlistDao;
    @Autowired
    VideoDao videoDao;


    @GetMapping(value = "searchPlaylistBy/{search}")
    public Object[] searchVideosBy(@PathVariable String search, HttpServletResponse response) {
        try {
            return playlistDao.getPlaylistByTitle(search).toArray();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Object b[] = new Object[1];
            b[0] = SERVER_ERROR;
            return b;
        }
    }


    @GetMapping(value = "playlist/showPlaylist/{id}")
    @ResponseBody
    public Object showPlaylist(@PathVariable long id, HttpServletResponse response) throws PlaylistNotFoundException {
        return playlistDao.getPlaylistById(id);
    }

    //validate name
    @PostMapping(value = "playlist/createPlaylist")
    public Object addPlaylist(@RequestBody Playlist playlist, HttpSession session, HttpServletResponse response)
            throws BadRequestException {
        if (SessionManager.isLogged(session)) {
            playlist.setOwnerId(SessionManager.getLoggedUserId(session));
            playlistDao.addPlaylist(playlist);
            return playlist;
        }
        return redirectingToLogin(response);
    }

    @GetMapping(value = "playlist/removePlaylist/{playlistId}")
    public Object removePlaylist(@PathVariable long playlistId, HttpSession session, HttpServletResponse response)
            throws BadRequestException {
        if (SessionManager.isLogged(session)) {
            if (SessionManager.getLoggedUserId(session) == playlistDao.getPlaylistById(playlistId).getOwnerId()) {
                playlistDao.removePlaylist(playlistId);
                return SUCCESSFULLY_REMOVED_PLAYLIST;
            } else {
                return responseForBadRequest(response, ACCESS_DENIED);
            }
        } else {
            return redirectingToLogin(response);
        }

    }

    @GetMapping(value = "playlist/addVideoToPlaylist/{playlistId}/{videoId}")
    public Object addVideoToPlaylist(@PathVariable long playlistId, @PathVariable long videoId, HttpSession session,
                                     HttpServletResponse response) throws BadRequestException {
        if (playlistDao.checkIfPlaylistExists(playlistId) && videoDao.checkIfVideoExists(videoId)) {
            if (SessionManager.isLogged(session)) {
                if (playlistDao.getPlaylistById(playlistId).getOwnerId() == SessionManager.getLoggedUserId(session)) {
                    if (playlistDao.isVideoAddedToPlaylist(playlistId, videoId)) {
                        return responseForBadRequest(response, VIDEO_ALREADY_ADDED_TO_PLAYLIST);
                    }
                    playlistDao.addVideoToPlaylist(playlistId, videoId);
                    return SUCCESSFULLY_ADDED_VIDEO_TO_PLAYLIST;
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
        if (playlistDao.checkIfPlaylistExists(playlistId) && videoDao.checkIfVideoExists(videoId)) {
            if (SessionManager.isLogged(session)) {
                if (playlistDao.getPlaylistById(playlistId).getOwnerId() == SessionManager.getLoggedUserId(session)) {
                    if (!playlistDao.isVideoAddedToPlaylist(playlistId, videoId)) {
                        return responseForBadRequest(response, VIDEO_NOT_IN_PLAYLIST);
                    }
                    playlistDao.removeVideoFromPlaylist(playlistId, videoId);
                    return SUCCESSFULLY_REMOVED_VIDEO_FROM_PLAYLIST;
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
