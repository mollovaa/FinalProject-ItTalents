package ittalents.javaee1.models.dao;

import ittalents.javaee1.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class UserDao {
	private static final String ADD_USER_QUERY =
			"INSERT  INTO users (full_name,username,password,email,age) VALUES (?,?,?,?,?)";
	private static final String GET_USER_QUERY_BY_USERNAME =
			"SELECT user_id,full_name,username,password,email,age FROM users WHERE username = ?";
	private static final String GET_USER_QUERY_BY_EMAIL =
			"SELECT user_id,full_name,username,password,email,age FROM users WHERE email = ?";
	private static final String GET_USER_QUERY_BY_ID =
			"SELECT user_id,full_name,username,password,email,age FROM users WHERE user_id = ?";
	private static final String INSERT_INTO_SUBSCRIPTIONS =
			"INSERT INTO subscriptions (subcriber_id,subscribedTo_id) VALUES (?,?)";
	public static final String DELETE_FROM_SUBSCRIPTIONS =
			"DELETE FROM subscriptions WHERE subcriber_id = ? AND subscribedTo_id = ?";
	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public static void manyXmanyUpdate(String query, JdbcTemplate template, long fromId, long toId) {
		template.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setLong(1, fromId);
			ps.setLong(2, toId);
			return ps;
		});
	}
	
	public void addSubscription(long fromId, long toId) {
		manyXmanyUpdate(INSERT_INTO_SUBSCRIPTIONS, jdbcTemplate, fromId, toId);
	}
	
	public void removeSubscription(long fromId, long toId) {
		manyXmanyUpdate(DELETE_FROM_SUBSCRIPTIONS, jdbcTemplate, fromId, toId);
	}
	
	public boolean isSubscribed(long id, long toId) {
		Integer isSubed = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM subscriptions WHERE subcriber_id = ? AND subscribedTo_id = ?",
				new Object[]{id, toId},
				Integer.class);
		return isSubed == 1;
	}
	
	public void addUser(User user) { // includes user and setting him the generated id from DB
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(ADD_USER_QUERY, PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setString(1, user.getFull_name());
			ps.setString(2, user.getUsername());
			ps.setString(3, CryptWithMD5.cryptWithMD5(user.getPassword()));
			ps.setString(4, user.getEmail());
			ps.setInt(5, user.getAge());
			return ps;
		}, keyHolder);
		user.setId(keyHolder.getKey().longValue());
	}
	
	public User getByUsername(String username) { // return user that matches username in DB
		List<User> userList = jdbcTemplate.query(GET_USER_QUERY_BY_USERNAME, userRowMapper, username);
		if (userList.isEmpty()) {
			return null;
		} else {
			return userList.get(0);
		}
	}
	
	public User getById(long id) { // return user that matches username in DB
		List<User> userList = jdbcTemplate.query(GET_USER_QUERY_BY_ID, userRowMapper, id);
		if (userList.isEmpty()) {
			return null;
		} else {
			return userList.get(0);
		}
	}
	
	public User getByEmail(String email) { // return user that matches username in DB
		List<User> userList = jdbcTemplate.query(GET_USER_QUERY_BY_EMAIL, userRowMapper, email);
		if (userList.isEmpty()) {
			return null;
		} else {
			return userList.get(0);
		}
	}
	
	private RowMapper<User> userRowMapper = (resultSet, i) -> {
		long id = resultSet.getInt("user_id");
		String fullName = resultSet.getString("full_name");
		String password = resultSet.getString("password");
		String username = resultSet.getString("username");
		String email = resultSet.getString("email");
		int age = resultSet.getInt("age");
		User user = new User(age, fullName, username, password, email);
		user.setId(id);
		return user;
	};
}
