package ittalents.javaee1.models.dao;

import ittalents.javaee1.models.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

@Component
public class CommentDao {

    @Autowired
    JdbcTemplate template;

    public CommentDao(){}


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
}
