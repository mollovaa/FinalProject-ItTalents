package ittalents.javaee1.models.dao;

import ittalents.javaee1.exceptions.PlaylistNotFoundException;
import ittalents.javaee1.models.Playlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.sql.*;
import java.util.List;

@Component
public class PlaylistDao {

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private GlobalDao globalDao;

    public PlaylistDao() {
    }

    public boolean checkIfPlaylistExists(long id) {
        String sql = "SELECT COUNT(*) FROM playlists WHERE playlist_id = ?";
        int result = template.queryForObject(sql, new Object[]{id}, Integer.class);
        return result != 0;
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
        Playlist result = template.query(sql, resultSet -> {
            resultSet.next();
            return new Playlist(resultSet.getLong(1), resultSet.getString(2),
                    resultSet.getLong(3));
        }, id);
        if (result != null) {
            return result;
        }
        throw new PlaylistNotFoundException();
    }

    private void removeVideosOnPlaylist(long playlistId) {
        String sql = "DELETE FROM playlists_videos WHERE playlist_id = ?";
        globalDao.deleteRowFromManyToMany(playlistId, template, sql);
    }

    @Transactional
    public void removePlaylist(long playlistId) {
        this.removeVideosOnPlaylist(playlistId);
        String sql = "DELETE FROM playlists WHERE playlist_id = ?";
        globalDao.deleteRowFromManyToMany(playlistId, template, sql);
    }

    public boolean isVideoAddedToPlaylist(long playlistId, long videoId) {
        String sql = "SELECT COUNT(*) FROM playlists_videos WHERE playlist_id = ? AND video_id = ?";
        return globalDao.checkManyToManyMatches(playlistId, videoId, template, sql);
    }

    public void addVideoToPlaylist(long playlistId, long videoId) {
        String sql = "INSERT INTO playlists_videos (playlist_id, video_id) VALUES (?,?)";
        globalDao.manyXmanyUpdate(sql, template, playlistId, videoId);

    }

    public List<Playlist> getPlaylistByTitle(String title) {
        String sql = "SELECT playlist_id, playlist_name, owner_id FROM playlists WHERE playlist_name like ?";
        List<Playlist> playlists = template.query(sql, (rs, i) -> new Playlist(rs.getLong(1),
                        rs.getString(2), rs.getLong(3)),
                "%" + title + "%");
        return playlists;
    }

    public void removeVideoFromPlaylist(long playlistId, long videoId) {
        String sql = "DELETE FROM playlists_videos WHERE playlist_id = ? AND video_id = ?";
        globalDao.manyXmanyUpdate(sql, template, playlistId, videoId);
    }
}
