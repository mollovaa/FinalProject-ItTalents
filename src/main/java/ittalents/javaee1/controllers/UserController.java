package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.InvalidJsonBodyException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.dao.CryptWithMD5;
import ittalents.javaee1.models.dao.UserDao;
import ittalents.javaee1.models.dto.UserLoginDTO;
import ittalents.javaee1.models.dto.UserRegisterDTO;
import ittalents.javaee1.models.dto.UserSessionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
public class UserController extends GlobalController {
	private static final String SUCCESSFUL_REGISTRATION = "{\"msg\" : \"You have successfully registered!\"}";
	private static final String SUCCESSFUL_LOG_IN = "{\"msg\" : \"You have successfully logged in!\"}";
	private static final String SUCCESSFUL_LOG_OUT = "{\"msg\" : \"You have successfully logged out!\"}";
	private static final String SUBSCRIBED = "{\"msg\" : \"Subscribed!\"}";
	private static final String UNSUBSCRIBED = "{\"msg\" : \"Unsubscribed!\"}";
	private static final String WRONG_CREDENTIALS = "Invalid Username or Password";
	private static final String EXISTING_EMAIL = "Email already exists!";
	private static final String EXISTING_USERNAME = "Username already exists!";
	private static final String INVALID_FULL_NAME = "Invalid Registration Full name!";
	private static final String INVALID_EMAIL = "Invalid Registration Email!";
	private static final String INVALID_AGE = "Invalid Age!";
	private static final String NOT_SUBSCRIBED = "You are not subscribed.";
	private static final String ALREADY_LOGGED = "You are already logged in.";
	private static final String ALREADY_SUBSCRIBED = "You are already subscribed.";
	private static final String INVALID_USER = "Invalid user!";
	private static final String INVALID_JSON_BODY = "Invalid Json Body!";
	
	
	@Autowired
	private UserDao userDao;
	
	@GetMapping(value = "/logout")
	public Object logout(HttpSession session) throws BadRequestException {
		if (SessionManager.isLogged(session)) {
			session.invalidate();
			return SUCCESSFUL_LOG_OUT;
		}
		throw new NotLoggedException();
	}
	
	@GetMapping(value = "/unsubscribe/{id}")
	public Object unsubscribeTo(HttpSession session, @PathVariable("id") long id) throws BadRequestException {
		
		if (SessionManager.isLogged(session)) {
			if (userDao.getById(id) != null) {  // user we are subscribing to exists
				if (userDao.isSubscribed(SessionManager.getLoggedUserId(session), id)) {
					userDao.removeSubscription(SessionManager.getLoggedUserId(session), id);
					return UNSUBSCRIBED;
				} else {  //not subbed
					throw new InvalidInputException(NOT_SUBSCRIBED);
				}
			} else {
				throw new InvalidInputException(INVALID_USER);
			}
		}
		throw new NotLoggedException();
	}
	
	@GetMapping(value = "/subscribe/{id}")
	public Object subscribeTo(HttpSession session, @PathVariable("id") long id) throws BadRequestException {
		
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
		throw new NotLoggedException();
	}
	
	@PostMapping(value = "/login")
	public Object loginUser(@RequestBody UserLoginDTO userLoginDTO, HttpSession session) throws BadRequestException {
		
		if (userLoginDTO == null) {
			throw new InvalidJsonBodyException(INVALID_JSON_BODY);
		}
		if (SessionManager.isLogged(session)) {
			throw new InvalidInputException(ALREADY_LOGGED);
		}
		validateLogin(userLoginDTO);
		User userCheckUsername = userDao.getByUsername(userLoginDTO.getUsername());
		if (userCheckUsername == null) {
			throw new InvalidInputException(WRONG_CREDENTIALS);
		} else {
			if (CryptWithMD5.cryptWithMD5(userLoginDTO.getPassword()).equals(userCheckUsername.getPassword())) {
				UserSessionDTO userSessionDTO = new UserSessionDTO(
						userCheckUsername.getId(),
						userCheckUsername.getAge(),
						userCheckUsername.getUsername(),
						userCheckUsername.getFull_name(),
						userCheckUsername.getEmail());
				SessionManager.logUser(session, userSessionDTO); // log into session then return as response
				return userSessionDTO;
			} else {
				throw new InvalidInputException(WRONG_CREDENTIALS);
			}
		}
		
	}
	
	@PostMapping(value = "/register")
	public Object registerUser(@RequestBody UserRegisterDTO userRegisterDTO) throws BadRequestException {
		if (userRegisterDTO == null) {
			throw new InvalidJsonBodyException(INVALID_JSON_BODY);
		}
		validateRegister(userRegisterDTO);
		User userCheckUsername = userDao.getByUsername(userRegisterDTO.getUsername());
		if (userCheckUsername == null) { // no such userRegisterDTO with that username
			User userCheckEmail = userDao.getByEmail(userRegisterDTO.getEmail());
			if (userCheckEmail == null) { // no such userRegisterDTO with that email
				userDao.addUser(new User(userRegisterDTO.getAge(), userRegisterDTO.getFullname(),
						userRegisterDTO.getUsername(), userRegisterDTO.getPassword(), userRegisterDTO.getEmail()));
				return SUCCESSFUL_REGISTRATION;
			} else {
				throw new InvalidInputException(EXISTING_EMAIL);
			}
		} else {
			throw new InvalidInputException(EXISTING_USERNAME);
		}
	}
	
	private void validateLogin(UserLoginDTO user) throws InvalidInputException {
		String username = user.getUsername();
		if (username == null || username.isEmpty()) {
			throw new InvalidInputException(WRONG_CREDENTIALS);
		}
		String password = user.getPassword();
		if (password == null || password.isEmpty()) {
			throw new InvalidInputException(WRONG_CREDENTIALS);
		}
	}
	
	private void validateRegister(UserRegisterDTO user) throws InvalidInputException {
		validateLogin(user);
		
		String email = user.getEmail();
		if (email == null || email.isEmpty()) {
			throw new InvalidInputException(INVALID_EMAIL);
		}
		String fullname = user.getFullname();
		if (fullname == null || fullname.isEmpty()) {
			throw new InvalidInputException(INVALID_FULL_NAME);
		}
		long age = user.getAge();
		if (age <= 0 || age > 120) {
			throw new InvalidInputException(INVALID_AGE);
		}
	}
}
