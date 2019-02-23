package ittalents.javaee1.controllers;

import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.dao.PlaylistDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;


@RestController
public class PlaylistController {

    private String successfullyCreatedPlaylist = "You have successfully created a playlist!";
    private String successfullyAddedVideoToPlaylist = "You have successfully aded a video to playlist!";
    private String successfullyRemovedPlaylist = "You have successfully removed a playlist!";
    private String unsuccessfullyCreatedPlaylist = "Please, login to create a playlist!";
    private String unsuccessfullyRemovedPlaylist = "Please, login to remove a playlist!";
    private String noRightsToRemoveAPlaylist = "Sorry, you cannot delete a playlist that is not yours!";

    @Autowired
    PlaylistDao playlistDao;

    @GetMapping(value = "/showPlaylist/{id}")
    @ResponseBody
    public Object showPlaylist(@PathVariable long id) {
        try {
            Playlist getById = playlistDao.getPlaylistById(id);
            return getById;
        } catch (PlaylistDao.PlaylistNotFoundException e) {
            return e.getMessage();
        }
    }

    @PostMapping(value = "/createPlaylist")
    public Object addPlaylist(@RequestBody Playlist toAdd, HttpSession session) {
        //todo check session
        if(true) {
            int userId = 14;
            toAdd.setOwnerId(userId);
            playlistDao.addPlaylist(toAdd);
            return successfullyCreatedPlaylist;
        }
        return unsuccessfullyCreatedPlaylist;
    }

    @PostMapping(value = "/removePlaylist")
    public Object removePlaylist(@RequestBody Playlist toRemove, HttpSession session){
        session.setAttribute("logged", true);
        session.setAttribute("user_id", 1);
        if ((boolean) session.getAttribute("logged")) {
            if (((Integer) session.getAttribute("user_id")).longValue() == toRemove.getOwnerId()) {
                playlistDao.removePlaylist(toRemove);
                return successfullyRemovedPlaylist;
            } else {
                return noRightsToRemoveAPlaylist;
            }
        }
        //todo redirect to login
        return unsuccessfullyRemovedPlaylist;
    }

    @GetMapping(value = "addVideoToPlaylist/{playlistId}/{videoId}")
    public Object addVideoToPlaylist(@PathVariable long playlistId, @PathVariable long videoId){
        //session
        //owner id  = user id   - moje da dobavq samo v svoi playlist;
        try {
            playlistDao.getPlaylistById(playlistId);
            playlistDao.addVideoToPlaylist(playlistId, videoId);
            return successfullyAddedVideoToPlaylist;
        } catch (PlaylistDao.PlaylistNotFoundException e) {
            return e.getMessage();
        }
    }

}
