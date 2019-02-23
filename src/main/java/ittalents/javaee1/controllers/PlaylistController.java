package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.PlaylistNotFoundException;
import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.dao.PlaylistDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
public class PlaylistController {

    private String successfullyCreatedPlaylist = "You have successfully created a playlist!";
    private String successfullyAddedVideoToPlaylist = "You have successfully added a video to playlist!";
    private String successfullyRemovedPlaylist = "You have successfully removed a playlist!";
    private String unsuccessfullyCreatedPlaylist = "Please, login to create a playlist!";
    private String unsuccessfullyRemovedPlaylist = "Please, login to remove a playlist!";
    private String login = "Please, login to continue!";
    private String noRightsToRemoveAPlaylist = "Sorry, you cannot delete a playlist that is not yours!";

    @Autowired
    PlaylistDao playlistDao;

    @GetMapping(value = "/showPlaylist/{id}")
    @ResponseBody
    public Object showPlaylist(@PathVariable long id) {
        try {
            Playlist getById = playlistDao.getPlaylistById(id);
            return getById;
        } catch (PlaylistNotFoundException e) {
            return e.getMessage();
        }
    }

    @PostMapping(value = "/createPlaylist")
    public Object addPlaylist(@RequestBody Playlist toAdd, HttpSession session, HttpServletResponse response) {
        if (SessionManager.isLogged(session)) {
            try {
                toAdd.setOwnerId(SessionManager.getLoggedUserId(session));
                playlistDao.addPlaylist(toAdd);
                return successfullyCreatedPlaylist;
            } catch (SessionManager.ExpiredSessionException e) {
                return e.getMessage();
            }
        }
        try {
            response.sendRedirect("/login");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return unsuccessfullyCreatedPlaylist;
    }

    @PostMapping(value = "/removePlaylist")
    public Object removePlaylist(@RequestBody Playlist toRemove, HttpSession session, HttpServletResponse response) {
        try {
            if (playlistDao.getPlaylistById(toRemove.getId()) != null) {
                if (SessionManager.isLogged(session)) {
                    try {
                        if (SessionManager.getLoggedUserId(session) == toRemove.getOwnerId()) {
                            playlistDao.removePlaylist(toRemove);
                            return successfullyRemovedPlaylist;
                        } else {
                            return noRightsToRemoveAPlaylist;
                        }
                    } catch (SessionManager.ExpiredSessionException e) {
                        return e.getMessage();
                    }
                }
            }
            try {
                response.sendRedirect("/login");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return unsuccessfullyRemovedPlaylist;
        } catch (PlaylistNotFoundException e) {
            return e.getMessage();
        }
    }

    @GetMapping(value = "addVideoToPlaylist/{playlistId}/{videoId}")
    public Object addVideoToPlaylist(@PathVariable long playlistId, @PathVariable long videoId,
                                     HttpSession session, HttpServletResponse response) {
        if (SessionManager.isLogged(session)) {
            try {
                if (playlistDao.getPlaylistById(playlistId).getOwnerId() == SessionManager.getLoggedUserId(session)) {
                    playlistDao.addVideoToPlaylist(playlistId, videoId);
                    return successfullyAddedVideoToPlaylist;
                }
            } catch (PlaylistNotFoundException | SessionManager.ExpiredSessionException e) {
                return e.getMessage();
            }
        }
        try {
            response.sendRedirect("/login");
        } catch (IOException e) {
            return e.getMessage();
        }
        return login;
    }

}
