package ittalents.javaee1.models.dao;


import ittalents.javaee1.exceptions.PlaylistNotFoundException;
import ittalents.javaee1.exceptions.VideoNotFoundException;
import ittalents.javaee1.models.Playlist;
import ittalents.javaee1.models.Video;
import ittalents.javaee1.models.VideoCategory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.Duration;

import java.util.List;


@Component
public class VideoDao {

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private GlobalDao globalDao;

    public VideoDao() {
    }

    public boolean checkIfVideoExists(long id) {
        String sql = "SELECT COUNT(*) FROM videos WHERE video_id = ?";
        int result = template.queryForObject(sql, new Object[]{id}, Integer.class);
        return result != 0;
    }

    public Video getVideoById(long videoId) throws VideoNotFoundException {
        String sql = "SELECT video_id, title, category, description, upload_date, duration, number_of_likes, " +
                "number_of_dislikes, views, uploader_id FROM videos WHERE video_id = ?";

        Video result = template.query(sql, rs -> {
            rs.next();
            return new Video(rs.getLong(1), rs.getString(2),
                    VideoCategory.valueOf(rs.getString(3)), rs.getString(4),
                    rs.getDate(5).toLocalDate(), Duration.ofSeconds(rs.getLong(6)),
                    rs.getInt(7), rs.getInt(8), rs.getInt(9),
                    rs.getLong(10));

        }, videoId);
        if (result != null) {
            return result;
        }
        throw new VideoNotFoundException();
    }

    public List<Video> getVideoByTitle(String title) {
        String sql = "SELECT video_id, title, category, description, upload_date, duration, number_of_likes, " +
                "number_of_dislikes, views, uploader_id FROM videos WHERE title like ?";

        List<Video> videos = template.query(sql, (rs, i) -> {
            Video video = new Video(rs.getLong(1), rs.getString(2),
                    VideoCategory.valueOf(rs.getString(3)), rs.getString(4),
                    rs.getDate(5).toLocalDate(), Duration.ofSeconds(rs.getLong(6)),
                    rs.getInt(7), rs.getInt(8), rs.getInt(9),
                    rs.getLong(10));
            return video;
        }, "%" + title + "%");
        return videos;
    }

    public List<Video> getVideoByTitleOrderBy(String title, String order) {
        String sql = "SELECT video_id, title, category, description, upload_date, duration, number_of_likes, " +
                "number_of_dislikes, views, uploader_id FROM videos WHERE title like ? ORDER BY ? DESC";
        List<Video> videos = template.query(sql, (rs, i) -> {
            Video video = new Video(rs.getLong(1), rs.getString(2),
                    VideoCategory.valueOf(rs.getString(3)), rs.getString(4),
                    rs.getDate(5).toLocalDate(), Duration.ofSeconds(rs.getLong(6)),
                    rs.getInt(7), rs.getInt(8), rs.getInt(9),
                    rs.getLong(10));
            return video;
        }, "%" + title + "%", order);
        return videos;
    }

    public void addVideo(Video toAdd) {
        String sql = "INSERT INTO videos (title, category, description, upload_date, duration, uploader_id)" +
                "VALUES (?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, toAdd.getTitle());
                    ps.setString(2, toAdd.getCategory().name());
                    ps.setString(3, toAdd.getDescription());
                    ps.setDate(4, Date.valueOf(toAdd.getUploadDate()));
                    ps.setInt(5, (int) toAdd.getDuration().getSeconds());
                    ps.setLong(6, toAdd.getUploaderId());
                    return ps;
                }
                , keyHolder);
        toAdd.setVideoId(keyHolder.getKey().longValue());
    }

    private void deleteVideoFromPlaylists(long videoId) {
        String sql = "DELETE FROM playlists_videos WHERE video_id = ?";
        globalDao.deleteRowFromManyToMany(videoId, template, sql);
    }

    private void deleteVideoFromLiked(long videoId) {
        String sql = "DELETE FROM liked_videos_by_users WHERE liked_video_id = ?";
        globalDao.deleteRowFromManyToMany(videoId, template, sql);
    }

    private void deleteVideoFromDisliked(long videoId) {
        String sql = "DELETE FROM disliked_videos_by_users WHERE disliked_video_id = ?";
        globalDao.deleteRowFromManyToMany(videoId, template, sql);
    }

    @Transactional
    public void removeVideo(long videoId) {
        String sql = "DELETE FROM videos WHERE video_id = ?";
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, videoId);
            //todo delete all comments and responses -> delete liked/disliked comments
            this.deleteVideoFromPlaylists(videoId);
            this.deleteVideoFromDisliked(videoId);
            this.deleteVideoFromLiked(videoId);
            return ps;
        });
    }

    private boolean hasAlreadyLikedVideo(long videoId, long userId) {
        String sql = "SELECT COUNT(*) FROM liked_videos_by_users " +
                "WHERE user_id = ? AND liked_video_id = ?";
        return globalDao.checkManyToManyMatches(userId, videoId, template, sql);
    }

    private boolean hasAlreadyDislikedVideo(long videoId, long userId) {
        String sql = "SELECT COUNT(*) FROM disliked_videos_by_users " +
                "WHERE user_id = ? AND disliked_video_id = ?";
        return globalDao.checkManyToManyMatches(userId, videoId, template, sql);
    }

    private int getLikesOrDislikes(long videoId, String sql) {
        int result = template.queryForObject(sql, new Object[]{videoId}, Integer.class);
        return result;
    }

    private int likesOfVideo(long videoId) {
        String sql = "SELECT number_of_likes FROM videos WHERE video_id = ?";
        return this.getLikesOrDislikes(videoId, sql);
    }

    private int dislikesOfVideo(long videoId) {
        String sql = "SELECT number_of_dislikes FROM videos WHERE video_id = ?";
        return this.getLikesOrDislikes(videoId, sql);
    }

    private void increaseLikes(long videoId) {
        String sql = "UPDATE videos SET number_of_likes = ? WHERE video_id = ?";
        this.increaseOrDecreaseLikesOrDislikes(videoId, this.likesOfVideo(videoId) + 1, sql);
    }

    private void increaseOrDecreaseLikesOrDislikes(long videoId, int newLikesOrDislikes, String sql) {
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, newLikesOrDislikes);
            ps.setLong(2, videoId);
            return ps;
        });
    }

    private void increaseDislikes(long videoId) {
        String sql = "UPDATE videos SET number_of_dislikes = ? WHERE video_id = ?";
        this.increaseOrDecreaseLikesOrDislikes(videoId, this.dislikesOfVideo(videoId) + 1, sql);
    }

    private void insertOrRemoveUserAndVideo(long videoId, long userId, String sql) {
        globalDao.manyXmanyUpdate(sql, template, userId, videoId);
    }

    private void insertUserLikedVideo(long videoId, long userId) {
        String sql = "INSERT INTO liked_videos_by_users (user_id, liked_video_id) VALUES (?,?)";
        this.insertOrRemoveUserAndVideo(videoId, userId, sql);
    }

    private void insertUserDislikedVideo(long videoId, long userId) {
        String sql = "INSERT INTO disliked_videos_by_users (user_id, disliked_video_id) VALUES (?,?)";
        this.insertOrRemoveUserAndVideo(videoId, userId, sql);
    }

    @Transactional
    public boolean likeVideo(long videoId, long userId) {
        if (this.hasAlreadyLikedVideo(videoId, userId)) {  //alreadyLiked
            return false;
        } else {
            if (this.hasAlreadyDislikedVideo(videoId, userId)) {  //liked
                this.removeDislike(videoId, userId);
            }
            this.increaseLikes(videoId);
            this.insertUserLikedVideo(videoId, userId);
            return true;
        }
    }

    private void removeUserLikedVideo(long videoId, long userId) {
        String sql = "DELETE FROM liked_videos_by_users WHERE user_id = ? AND liked_video_id = ?";
        this.insertOrRemoveUserAndVideo(videoId, userId, sql);
    }

    private void removeUserDislikedVideo(long videoId, long userId) {
        String sql = "DELETE FROM disliked_videos_by_users WHERE user_id = ? AND disliked_video_id = ?";
        this.insertOrRemoveUserAndVideo(videoId, userId, sql);
    }

    private void decreaseLikes(long videoId) {
        String sql = "UPDATE videos SET number_of_likes = ? WHERE video_id = ?";
        this.increaseOrDecreaseLikesOrDislikes(videoId, this.likesOfVideo(videoId) - 1, sql);
    }

    private void decreaseDislikes(long videoId) {
        String sql = "UPDATE videos SET number_of_dislikes = ? WHERE video_id = ?";
        this.increaseOrDecreaseLikesOrDislikes(videoId, this.dislikesOfVideo(videoId) - 1, sql);
    }

    private void removeLike(long videoId, long userId) {
        this.removeUserLikedVideo(videoId, userId);
        this.decreaseLikes(videoId);
    }

    private void removeDislike(long videoId, long userId) {
        this.removeUserDislikedVideo(videoId, userId);
        this.decreaseDislikes(videoId);
    }

    @Transactional
    public boolean dislikeVideo(long videoId, long userId) {
        if (this.hasAlreadyDislikedVideo(videoId, userId)) {  //alreadyDisLiked
            return false;
        } else {
            if (this.hasAlreadyLikedVideo(videoId, userId)) {  //liked
                this.removeLike(videoId, userId);
            }
            this.increaseDislikes(videoId);    //decreaseLikes
            this.insertUserDislikedVideo(videoId, userId);
            return true;
        }
    }
}
