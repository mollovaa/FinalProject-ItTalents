package ittalents.javaee1.models.dao;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor
@Component
class GlobalDao {

    boolean checkManyToManyMatches(long firstId, long secondId, JdbcTemplate template, String sql) {
        int matches = template.queryForObject(sql, new Object[]{firstId, secondId}, Integer.class);
        return matches != 0;
    }

    void manyXmanyUpdate(String query, JdbcTemplate template, long firstId, long secondId) {
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setLong(1, firstId);
            ps.setLong(2, secondId);
            return ps;
        });
    }

    void deleteRowFromManyToMany(long id, JdbcTemplate template, String sql) {
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            return ps;
        });
    }
}
