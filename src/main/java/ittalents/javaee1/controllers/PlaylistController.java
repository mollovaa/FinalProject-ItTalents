package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.PlaylistNotFoundException;
import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.dao.PlaylistDao;
import ittalents.javaee1.models.dao.VideoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static ittalents.javaee1.controllers.ResponseMessages.*;

@RestController
public class PlaylistController {

    @Autowired
    PlaylistDao playlistDao;
    @Autowired
    VideoDao videoDao;

    @GetMapping(value = "/showPlaylist/{id}")
    @ResponseBody
    public Object showPlaylist(@PathVariable long id, HttpServletResponse response) {
        try {
            Playlist getById = playlistDao.getPlaylistById(id);
            return getById;
        } catch (PlaylistNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return e.getMessage();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
    }

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

    @PostMapping(value = "/createPlaylist")
    public Object addPlaylist(@RequestBody Playlist toAdd, HttpSession session, HttpServletResponse response) {
        try {
            if (SessionManager.isLogged(session)) {
                try {
                    toAdd.setOwnerId(SessionManager.getLoggedUserId(session));
                    playlistDao.addPlaylist(toAdd);
                    return SUCCESSFULLY_CREATED_PLAYLIST;
                } catch (SessionManager.ExpiredSessionException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return EXPIRED_SESSION;
                }
            } else {
                try {
                    response.sendRedirect("/login");
                } catch (IOException e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return SERVER_ERROR;
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return SERVER_ERROR;
    }

    @PostMapping(value = "/removePlaylist")  //ako v bodyto ownera e greshen-trie
    public Object removePlaylist(@RequestBody Playlist toRemove, HttpSession session, HttpServletResponse response) {
        try {
            if (playlistDao.checkIfPlaylistExists(toRemove.getId())) {
                if (SessionManager.isLogged(session)) {
                    try {
                        if (SessionManager.getLoggedUserId(session) == toRemove.getOwnerId()) {
                            playlistDao.removePlaylist(toRemove);
                            return SUCCESSFULLY_REMOVED_PLAYLIST;
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            return ACCESS_DENIED;
                        }
                    } catch (SessionManager.ExpiredSessionException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return EXPIRED_SESSION;
                    }
                } else {
                    try {
                        response.sendRedirect("/login");
                    } catch (IOException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return SERVER_ERROR;
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return NOT_FOUND;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return SERVER_ERROR;
    }

    @GetMapping(value = "addVideoToPlaylist/{playlistId}/{videoId}")
    public Object addVideoToPlaylist(@PathVariable long playlistId, @PathVariable long videoId,
                                     HttpSession session, HttpServletResponse response) {
        try {
            if (playlistDao.checkIfPlaylistExists(playlistId) && videoDao.checkIfVideoExists(videoId)) {
                if (SessionManager.isLogged(session)) {
                    try {
                        if (playlistDao.getPlaylistById(playlistId).getOwnerId() == SessionManager.getLoggedUserId(session)) {
                            playlistDao.addVideoToPlaylist(playlistId, videoId);
                            return SUCCESSFULLY_ADDED_VIDEO_TO_PLAYLIST;
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            return ACCESS_DENIED;
                        }
                    } catch (SessionManager.ExpiredSessionException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return EXPIRED_SESSION;
                    }
                } else {
                    try {
                        response.sendRedirect("/login");
                    } catch (IOException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return SERVER_ERROR;
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return NOT_FOUND;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return SERVER_ERROR;
        }
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return SERVER_ERROR;
    }

    //todo remove video from playlist

}
