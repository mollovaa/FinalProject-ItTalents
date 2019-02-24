package ittalents.javaee1.models.dao;

import ittalents.javaee1.models.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;


@Component
public class CommentDao {

    @Autowired
    JdbcTemplate template;
    @Autowired
    GlobalDao globalDao;

    public CommentDao() {
    }


    public void addCommentToVideo(Comment comment) {
        String sql = "INSERT INTO comments (message, date_of_publication, publisher_id, video_id)" +
                "VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, comment.getMessage());
            ps.setDate(2, Date.valueOf(comment.getDateOfPublication()));
            ps.setLong(3, comment.getPublisherId());
            ps.setLong(4, comment.getVideoId());
            return ps;
        }, keyHolder);
        comment.setId(keyHolder.getKey().longValue());
    }

    public void addResponseToComment(Comment comment) {
        String sql = "INSERT INTO comments (message, date_of_publication, publisher_id, response_to_id, video_id)" +
                "VALUES (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, comment.getMessage());
            ps.setDate(2, Date.valueOf(comment.getDateOfPublication()));
            ps.setLong(3, comment.getPublisherId());
            ps.setLong(4, comment.getResponseToId());
            ps.setLong(5, comment.getVideoId());
            return ps;
        }, keyHolder);
        comment.setId(keyHolder.getKey().longValue());
    }

    public boolean checkIfCommentExists(long commentId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE comment_id = ?";
        int matches = template.queryForObject(sql, new Object[]{commentId}, Integer.class);
        return matches != 0;
    }

    public long getVideoIdByComment(long commentId) {
        String sql = "SELECT video_id FROM comments WHERE comment_id = ?";
        int videoId = template.queryForObject(sql, new Object[]{commentId}, Integer.class);
        return videoId;
    }

    public long getPublisherIdByComment(long commentId) {
        String sql = "SELECT publisher_id FROM comments WHERE comment_id = ?";
        int publisherId = template.queryForObject(sql, new Object[]{commentId}, Integer.class);
        return publisherId;
    }


    private boolean hasAlreadyLikedComment(long commentId, long userId) {
        String sql = "SELECT COUNT(*) FROM liked_comments_by_users " +
                "WHERE user_id = ? AND liked_comment_id = ?";
        return globalDao.checkManyToManyMatches(userId, commentId, template, sql);
    }

    private boolean hasAlreadyDislikedComment(long commentId, long userId) {
        String sql = "SELECT COUNT(*) FROM disliked_comments_by_users " +
                "WHERE user_id = ? AND disliked_comment_id = ?";
        return globalDao.checkManyToManyMatches(userId, commentId, template, sql);
    }

    private int getLikesOrDislikes(long commentId, String sql) {
        int result = template.queryForObject(sql, new Object[]{commentId}, Integer.class);
        return result;
    }

    private int likesOfComment(long commentId) {
        String sql = "SELECT number_of_likes FROM comments WHERE comment_id = ?";
        return this.getLikesOrDislikes(commentId, sql);
    }

    private int dislikesOfComment(long commentId) {
        String sql = "SELECT number_of_dislikes FROM comments WHERE comment_id = ?";
        return this.getLikesOrDislikes(commentId, sql);
    }

    private void increaseLikes(long commentId) {
        String sql = "UPDATE comments SET number_of_likes = ? WHERE comment_id = ?";
        this.increaseOrDecreaseLikesOrDislikes(commentId, this.likesOfComment(commentId) + 1, sql);
    }

    private void increaseOrDecreaseLikesOrDislikes(long commentId, int newLikesOrDislikes, String sql) {
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, newLikesOrDislikes);
            ps.setLong(2, commentId);
            return ps;
        });
    }

    private void increaseDislikes(long commentId) {
        String sql = "UPDATE comments SET number_of_dislikes = ? WHERE comment_id = ?";
        this.increaseOrDecreaseLikesOrDislikes(commentId, this.dislikesOfComment(commentId) + 1, sql);
    }

    private void insertOrRemoveUserAndComment(long commentId, long userId, String sql) {
        globalDao.manyXmanyUpdate(sql, template, userId, commentId);
    }

    private void insertUserLikedComment(long commentId, long userId) {
        String sql = "INSERT INTO liked_comments_by_users (user_id, liked_comment_id) VALUES (?,?)";
        this.insertOrRemoveUserAndComment(commentId, userId, sql);
    }

    private void insertUserDislikedComment(long commentId, long userId) {
        String sql = "INSERT INTO disliked_comments_by_users (user_id, disliked_comment_id) VALUES (?,?)";
        this.insertOrRemoveUserAndComment(commentId, userId, sql);
    }

    @Transactional
    public boolean likeComment(long commentId, long userId) {
        if (this.hasAlreadyLikedComment(commentId, userId)) {  //alreadyLiked
            return false;
        } else {
            if (this.hasAlreadyDislikedComment(commentId, userId)) {  //liked
                this.removeDislike(commentId, userId);
            }
            this.increaseLikes(commentId);
            this.insertUserLikedComment(commentId, userId);
            return true;
        }
    }

    private void removeUserLikedComment(long commentId, long userId) {
        String sql = "DELETE FROM liked_comments_by_users WHERE user_id = ? AND liked_comment_id = ?";
        this.insertOrRemoveUserAndComment(commentId, userId, sql);
    }

    private void removeUserDislikedComment(long commentId, long userId) {
        String sql = "DELETE FROM disliked_comments_by_users WHERE user_id = ? AND disliked_comment_id = ?";
        this.insertOrRemoveUserAndComment(commentId, userId, sql);
    }

    private void decreaseLikes(long commentId) {
        String sql = "UPDATE comments SET number_of_likes = ? WHERE comment_id = ?";
        this.increaseOrDecreaseLikesOrDislikes(commentId, this.likesOfComment(commentId) - 1, sql);
    }

    private void decreaseDislikes(long commentId) {
        String sql = "UPDATE comments SET number_of_dislikes = ? WHERE comment_id = ?";
        this.increaseOrDecreaseLikesOrDislikes(commentId, this.dislikesOfComment(commentId) - 1, sql);
    }

    private void removeLike(long commentId, long userId) {
        this.removeUserLikedComment(commentId, userId);
        this.decreaseLikes(commentId);
    }

    private void removeDislike(long commentId, long userId) {
        this.removeUserDislikedComment(commentId, userId);
        this.decreaseDislikes(commentId);
    }

    @Transactional
    public boolean dislikeComment(long commentId, long userId) {
        if (this.hasAlreadyDislikedComment(commentId, userId)) {  //alreadyDisLiked
            return false;
        } else {
            if (this.hasAlreadyLikedComment(commentId, userId)) {  //liked
                this.removeLike(commentId, userId);
            }
            this.increaseDislikes(commentId);    //decreaseLikes
            this.insertUserDislikedComment(commentId, userId);
            return true;
        }
    }

    private void deleteVideoFromPlaylists(long videoId) {
        String sql = "DELETE FROM playlists_videos WHERE video_id = ?";
        globalDao.deleteRowFromManyToMany(videoId, template, sql);
    }

    private void deleteCommentFromLiked(long commentId) {
        String sql = "DELETE FROM liked_comments_by_users WHERE liked_comment_id = ?";
        globalDao.deleteRowFromManyToMany(commentId, template, sql);
    }

    private void deleteCommentFromDisliked(long commentId) {
        String sql = "DELETE FROM disliked_comments_by_users WHERE disliked_comment_id = ?";
        globalDao.deleteRowFromManyToMany(commentId, template, sql);
    }

    private void deleteAllResponsesToComment(long commentId) {
        String sql = "SELECT comment_id FROM comments WHERE response_to_id = ?";
        List<Integer> responsesIds = template.queryForList(sql, new Object[]{commentId}, Integer.class);
        for (Integer id : responsesIds) {
            removeComment(id);
        }
    }

    @Transactional
    public void removeComment(long commentId) {
        String sql = "DELETE FROM comments WHERE comment_id = ?";
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, commentId);
            this.deleteAllResponsesToComment(commentId);
            this.deleteCommentFromDisliked(commentId);
            this.deleteCommentFromLiked(commentId);
            return ps;
        });
    }


}
