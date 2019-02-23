package ittalents.javaee1.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;


@Component
public class PlaylistDao {

    private static PlaylistDao ourInstance = new PlaylistDao();

    @Autowired
    private JdbcTemplate template;

    public static PlaylistDao getInstance() {
        return ourInstance;
    }

    private PlaylistDao() {
    }

    public void addPlaylist(Playlist playlist) {
        String sql = "INSERT INTO playlists (playlist_name, owner_id) VALUES (?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, playlist.getName());
            ps.setLong(2, playlist.getOwnerId());
            return ps;
        }, keyHolder);
        playlist.setId(keyHolder.getKey().longValue());
    }

    public Playlist getPlaylistById(long id) throws PlaylistNotFoundException {
        String sql = "SELECT playlist_id, playlist_name, owner_id FROM playlists WHERE playlist_id = ?";
        System.out.println(template);
        Playlist result = template.query(sql,
                new ResultSetExtractor<Playlist>() {
                    @Override
                    public Playlist extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                        resultSet.next();
                        Playlist playlist = new Playlist(resultSet.getLong(1), resultSet.getString(2),
                                resultSet.getLong(3));
                        return playlist;
                    }
                }, id);
        if (result != null) {
            return result;
        }
        throw new PlaylistNotFoundException();
    }

    private void removeVideosOnPlaylist(long playlistId) {
        String sql = "DELETE FROM playlists_videos WHERE playlist_id = ?";
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, playlistId);
            return ps;
        });
    }

    public void removePlaylist(Playlist toRemove) {         //should be in transaction
        this.removeVideosOnPlaylist(toRemove.getId());
        String sql = "DELETE FROM playlists WHERE playlist_id = ?";
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, toRemove.getId());
            return ps;
        });
    }

    public void addVideoToPlaylist(long playlistId, long videoId) {  //check if it is already there
        String sql = "INSERT INTO playlists_videos (playlist_id, video_id) VALUES (?,?)";
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, playlistId);
            ps.setLong(2, videoId);
            return ps;
        });
    }

    public class PlaylistNotFoundException extends Exception {
        public PlaylistNotFoundException() {
            super("Sorry, playlist not found!");
        }
    }
}
