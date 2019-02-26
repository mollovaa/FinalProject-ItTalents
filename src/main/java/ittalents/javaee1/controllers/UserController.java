package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.InvalidJsonBodyException;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.dao.CryptWithMD5;
import ittalents.javaee1.models.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
public class UserController implements GlobalController {
	public static final String SERVER_ERROR = "{\"error\" : \"Please try again later!\"}";
	private static final String SUCCESSFUL_REGISTRATION = "{\"msg\" : \"You have successfully registered!\"}";
	private static final String SUCCESSFUL_LOG_IN = "{\"msg\" : \"You have successfully logged in!\"}";
	private static final String SUCCESSFUL_LOG_OUT = "{\"msg\" : \"You have successfully logged out!\"}";
	private static final String SUBSCRIBED = "{\"msg\" : \"Subscribed!\"}";
	private static final String UNSUBSCRIBED = "{\"msg\" : \"Unsubscribed!\"}";
	private static final String WRONG_CREDENTIALS = "{\"error\" : \"Invalid Username or Password\"}";
	private static final String EXISTING_EMAIL = "{\"error\" : \"Email already exists!\"}";
	private static final String EXISTING_USERNAME = "{\"error\" : \"Username already exists!\"}";
	private static final String INVALID_USERNAME = "{\"error\" : \"Invalid Registration Username!\"}";
	private static final String INVALID_FULL_NAME = "{\"error\" : \"Invalid Registration Full name!\"}";
	private static final String INVALID_EMAIL = "{\"error\" : \"Invalid Registration Email!\"}";
	private static final String INVALID_AGE = "{\"error\" : \"Invalid Age!\"}";
	private static final String INVALID_PASSWORD = "{\"error\" : \"Invalid Registration Password!\"}";
	private static final String NOT_LOGED_IN = "{\"error\" : \"You are not logged in.\"}";
	private static final String NOT_SUBSCRIBED = "{\"error\" : \"You are not subscribed.\"}";
	private static final String ALREADY_LOGGED = "{\"error\" : \"You are already logged in.\"}";
	private static final String ALREADY_SUBSCRIBED = "{\"error\" : \"You are already subscribed.\"}";
	private static final String INVALID_USER = "{\"error\" : \"Invalid user!\"}";
	private static final String INVALID_JSON_BODY = "{\"error\" : \"Invalid Json Body!\"}";
	
	
	@Autowired
	private UserDao userDao;
	
	@GetMapping(value = "/logout")
	public Object logout(HttpSession session) throws InvalidInputException {
		if (SessionManager.isLogged(session)) {
			session.invalidate();
			return SUCCESSFUL_LOG_OUT;
		}
		throw new InvalidInputException(NOT_LOGED_IN);
	}
	
	@GetMapping(value = "/unsubscribe/{id}")
	public Object unsubscribeTo(HttpSession session, @PathVariable("id") long id)
			throws InvalidInputException, SessionManager.ExpiredSessionException {
		
		if (SessionManager.isLogged(session)) {
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
		}
		throw new InvalidInputException(NOT_LOGED_IN);
	}
	
	@GetMapping(value = "/subscribe/{id}")
	public Object subscribeTo(HttpSession session, @PathVariable("id") long id)
			throws InvalidInputException, SessionManager.ExpiredSessionException {
		
		if (SessionManager.isLogged(session)) {
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
		}
		throw new InvalidInputException(NOT_LOGED_IN);
	}
	
	@PostMapping(value = "/login")
	public Object loginUser(@RequestBody User user, HttpSession session)
			throws InvalidInputException, InvalidJsonBodyException {
		
		if (user == null) {
			throw new InvalidJsonBodyException(INVALID_JSON_BODY);
		}
		if (SessionManager.isLogged(session)) {
			throw new InvalidInputException(ALREADY_LOGGED);
		}
		validateLogin(user);
		User userCheckUsername = userDao.getByUsername(user.getUsername());
		if (userCheckUsername == null) {
			throw new InvalidInputException(WRONG_CREDENTIALS);
		} else {
			if (CryptWithMD5.cryptWithMD5(user.getPassword()).equals(userCheckUsername.getPassword())) {
				SessionManager.logUser(session, userCheckUsername);
				return SUCCESSFUL_LOG_IN;
			} else {
				throw new InvalidInputException(WRONG_CREDENTIALS);
			}
		}
		
	}
	
	@PostMapping(value = "/register")
	public Object registerUser(@RequestBody User user) throws InvalidInputException, InvalidJsonBodyException {
		if (user == null) {
			throw new InvalidJsonBodyException(INVALID_JSON_BODY);
		}
		validateRegister(user);
		User userCheckUsername = userDao.getByUsername(user.getUsername());
		if (userCheckUsername == null) { // no such user with that username
			User userCheckEmail = userDao.getByEmail(user.getEmail());
			if (userCheckEmail == null) { // no such user with that email
				userDao.addUser(user);
				return SUCCESSFUL_REGISTRATION;
			} else {
				throw new InvalidInputException(EXISTING_EMAIL);
			}
		} else {
			throw new InvalidInputException(EXISTING_USERNAME);
		}
	}
	
	private void validateLogin(User user) throws InvalidInputException {
		String username = user.getUsername();
		if (username == null || username.isEmpty()) {
			throw new InvalidInputException(WRONG_CREDENTIALS);
		}
		String password = user.getPassword();
		if (password == null || password.isEmpty()) {
			throw new InvalidInputException(WRONG_CREDENTIALS);
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
