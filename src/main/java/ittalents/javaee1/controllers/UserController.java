package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.dao.CryptWithMD5;
import ittalents.javaee1.models.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class UserController {
	public static final String SERVER_ERROR = "{\"error\" : \"Please try again later!\"}";
	private static final String SUCCESSFUL_REGISTRATION = "{\"msg\" : \"You have successfully registered!\"}";
	private static final String SUCCESSFUL_LOG_IN = "{\"msg\" : \"You have successfully logged in!\"}";
	private static final String SUCCESSFUL_LOG_OUT = "{\"msg\" : \"You have successfully logged out!\"}";
	private static final String SUBSCRIBED = "{\"msg\" : \"Subscribed!\"}";
	private static final String UNSUBSCRIBED = "{\"msg\" : \"Unsubscribed!\"}";
	private static final String WRONG_CREDENTIALS = "{\"error\" : \"Invalid Username or Password\"}";
	private static final String EXISTING_EMAIL = "{\"error\" : \"Email already exists!\"}";
	private static final String EXISTING_USERNAME = "{\"error\" : \"Username already exists!\"}";
	private static final String INVALID_USERNAME = "{\"error\" : \"Invalid Username!\"}";
	private static final String INVALID_FULL_NAME = "{\"error\" : \"Invalid Full playlistName!\"}";
	private static final String INVALID_EMAIL = "{\"error\" : \"Invalid Email!\"}";
	private static final String INVALID_AGE = "{\"error\" : \"Invalid Age!\"}";
	private static final String INVALID_PASSWORD = "{\"error\" : \"Invalid Password!\"}";
	private static final String NOT_LOGED_IN = "{\"error\" : \"You are not logged in.\"}";
	private static final String NOT_SUBSCRIBED = "{\"error\" : \"You are not subscribed.\"}";
	private static final String ALREADY_LOGGED = "{\"error\" : \"You are already logged in.\"}";
	private static final String ALREADY_SUBSCRIBED = "{\"error\" : \"You are already subscribed.\"}";
	private static final String INVALID_USER = "{\"error\" : \"Invalid user!\"}";
	
	
	@Autowired
	private UserDao userDao;
	
	@GetMapping(value = "/logout")
	public Object logout(HttpSession session, HttpServletResponse response) {
		if (SessionManager.isLogged(session)) {
			session.invalidate();
			return SUCCESSFUL_LOG_OUT;
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return NOT_LOGED_IN;
		}
	}
	
	@GetMapping(value = "/unsubscribe/{id}")
	public Object unsubscribeTo(HttpServletResponse response, HttpSession session, @PathVariable("id") long id) {
		if (SessionManager.isLogged(session)) {
			try {
				if (userDao.getById(id) != null) {  // user we are subscribing to exists
					if (userDao.isSubscribed(SessionManager.getLoggedUserId(session), id)) {
						userDao.removeSubscription(SessionManager.getLoggedUserId(session), id);
						return UNSUBSCRIBED;
					} else { // //not subbed
						throw new InvalidInputException(NOT_SUBSCRIBED);
					}
				} else {
					throw new InvalidInputException(INVALID_USER);
				}
			} catch (SessionManager.ExpiredSessionException | InvalidInputException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return e.getMessage();
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return SERVER_ERROR;
			}
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return NOT_LOGED_IN;
		}
	}
	
	@GetMapping(value = "/subscribe/{id}")
	public Object subscribeTo(HttpServletResponse response, HttpSession session, @PathVariable("id") long id) {
		if (SessionManager.isLogged(session)) {
			try {
				if (userDao.getById(id) != null) {  // user we are subscribing to exists
					if (userDao.isSubscribed(SessionManager.getLoggedUserId(session), id)) { // already subbed
						throw new InvalidInputException(ALREADY_SUBSCRIBED);
					} else { // add subscribtion
						userDao.addSubscription(SessionManager.getLoggedUserId(session), id);
						return SUBSCRIBED;
					}
				} else {
					throw new InvalidInputException(INVALID_USER);
				}
			} catch (SessionManager.ExpiredSessionException | InvalidInputException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return e.getMessage();
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return SERVER_ERROR;
			}
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return NOT_LOGED_IN;
		}
	}
	
	@PostMapping(value = "/login")
	public Object loginUser(@RequestBody User user, HttpServletResponse response, HttpSession session) {
		try {
			if (SessionManager.isLogged(session)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return ALREADY_LOGGED;
			}
			validateLogin(user);
			User userCheckUsername = userDao.getByUsername(user.getUsername());
			if (userCheckUsername == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return WRONG_CREDENTIALS;
			} else {
				if (CryptWithMD5.cryptWithMD5(user.getPassword()).equals(userCheckUsername.getPassword())) {
					SessionManager.logUser(session, userCheckUsername.getUserId());
					return SUCCESSFUL_LOG_IN;
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return WRONG_CREDENTIALS;
				}
			}
		} catch (InvalidInputException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return WRONG_CREDENTIALS;
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return SERVER_ERROR;
		}
	}
	
	@PostMapping(value = "/register")
	public Object registerUser(@RequestBody User user, HttpServletResponse response) {
		try {
			validateRegister(user);
			User userCheckUsername = userDao.getByUsername(user.getUsername());
			if (userCheckUsername == null) { // no such user with that username
				User userCheckEmail = userDao.getByEmail(user.getEmail());
				if (userCheckEmail == null) { // no such user with that email
					userDao.addUser(user);
					return SUCCESSFUL_REGISTRATION;
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return EXISTING_EMAIL;
				}
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return EXISTING_USERNAME;
			}
		} catch (InvalidInputException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return e.getMessage();
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return SERVER_ERROR;
		}
	}
	
	private void validateLogin(User user) throws InvalidInputException {
		String username = user.getUsername();
		if (username == null || username.isEmpty()) {
			throw new InvalidInputException(INVALID_USERNAME);
		}
		String password = user.getPassword();
		if (password == null || password.isEmpty()) {
			throw new InvalidInputException(INVALID_PASSWORD);
		}
	}
	
	private void validateRegister(User user) throws InvalidInputException {
		validateLogin(user);
		
		String email = user.getEmail();
		if (email == null || email.isEmpty()) {
			throw new InvalidInputException(INVALID_EMAIL);
		}
		String fullname = user.getFull_name();
		if (fullname == null || fullname.isEmpty()) {
			throw new InvalidInputException(INVALID_FULL_NAME);
		}
		long age = user.getAge();
		if (age <= 0 || age > 120) {
			throw new InvalidInputException(INVALID_AGE);
		}
	}
}
