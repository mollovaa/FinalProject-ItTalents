package ittalents.javaee1.models.dao;


import ittalents.javaee1.models.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDate;

@Component
public class VideoDao {
    private static VideoDao ourInstance = new VideoDao();

    public static VideoDao getInstance() {
        return ourInstance;
    }

    @Autowired
    private JdbcTemplate template;

    private VideoDao() {
    }

    public void addVideo(Video toAdd) {

        String sql = "INSERT INTO videos (title, category, description, upload_date, duration, uploader_id)" +
                "VALUES (?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        Date uploadDate = Date.valueOf(LocalDate.now());
        template.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, toAdd.getTitle());
                    ps.setString(2, toAdd.getCategory().name());
                    ps.setString(3, toAdd.getDescription());
                    ps.setDate(4, uploadDate);
                    ps.setInt(5, (int) toAdd.getDuration().getSeconds());
                    ps.setLong(6, toAdd.getUploaderId());
                    return ps;
                }
                , keyHolder);

        toAdd.setVideoId(keyHolder.getKey().longValue());
        toAdd.setUploadDate(uploadDate.toLocalDate());
        //System.out.println(toAdd.toString());
    }

    public void removeVideo(Video toRemove) {
        String sql = "DELETE FROM videos WHERE video_id = ?";
        template.update(connection -> {
            //connection.setAutoCommit(false);
            try (PreparedStatement deleteVideo = connection.prepareStatement(sql)) {   ///should be in transaction
                deleteVideo.setLong(1, toRemove.getVideoId());

                //todo delete all comments and responses -> delete liked/disliked comments
                //delete liked/disliked videos;
                //delete video at the end;

                return deleteVideo;
            }
        });
    }

    private int checkingUserVideoMatches(long videoId, long userId, String sql) {
        int matches = template.query(sql, new ResultSetExtractor<Integer>() {
            @Override
            public Integer extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                resultSet.next();
                if (resultSet.getInt(1) == 0) {
                    return 0;
                }
                return 1;
            }
        }, userId, videoId);
        return matches;
    }

    private boolean hasAlreadyLikedVideo(long videoId, long userId) {
        String sql = "SELECT COUNT(*) FROM liked_videos_by_users " +
                "WHERE user_id = ? AND liked_video_id = ?";
        return this.checkingUserVideoMatches(videoId, userId, sql) != 0;
    }

    private boolean hasAlreadyDislikedVideo(long videoId, long userId) {
        String sql = "SELECT COUNT(*) FROM disliked_videos_by_users " +
                "WHERE user_id = ? AND disliked_video_id = ?";
        return this.checkingUserVideoMatches(videoId, userId, sql) != 0;
    }

    private int getLikesOrDislikes(long videoId, String sql) {
        int result = template.query(sql, new ResultSetExtractor<Integer>() {
            @Override
            public Integer extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }, videoId);
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
            ps.setLong(1, newLikesOrDislikes);
            ps.setLong(2, videoId);
            return ps;
        });
    }

    private void increaseDislikes(long videoId) {
        String sql = "UPDATE videos SET number_of_dislikes = ? WHERE video_id = ?";
        this.increaseOrDecreaseLikesOrDislikes(videoId, this.dislikesOfVideo(videoId) + 1, sql);
    }

    private void insertOrRemoveUserAndVideo(long videoId, long userId, String sql) {
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setLong(2, videoId);
            return ps;
        });
    }

    private void insertUserLikedVideo(long videoId, long userId) {
        String sql = "INSERT INTO liked_videos_by_users (user_id, liked_video_id) VALUES (?,?)";
        this.insertOrRemoveUserAndVideo(videoId, userId, sql);
    }

    private void insertUserDislikedVideo(long videoId, long userId) {
        String sql = "INSERT INTO disliked_videos_by_users (user_id, disliked_video_id) VALUES (?,?)";
        this.insertOrRemoveUserAndVideo(videoId, userId, sql);
    }

    public boolean likeVideo(Video toLike, long userId) {
        if (this.hasAlreadyLikedVideo(toLike.getVideoId(), userId)) {  //alreadyLiked
            return false;
        } else {
            if (this.hasAlreadyDislikedVideo(toLike.getVideoId(), userId)) {  //liked
                this.removeDislike(toLike.getVideoId(), userId);
            }
            this.increaseLikes(toLike.getVideoId());
            this.insertUserLikedVideo(toLike.getVideoId(), userId);
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

    public boolean dislikeVideo(Video toDislike, long userId) {
        if (this.hasAlreadyDislikedVideo(toDislike.getVideoId(), userId)) {  //alreadyDisLiked
            return false;
        } else {
            if (this.hasAlreadyLikedVideo(toDislike.getVideoId(), userId)) {  //liked
                this.removeLike(toDislike.getVideoId(), userId);
            }
            this.increaseDislikes(toDislike.getVideoId());    //decreaseLikes
            this.insertUserDislikedVideo(toDislike.getVideoId(), userId);
            return true;
        }
    }
}
