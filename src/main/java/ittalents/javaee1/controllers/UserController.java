package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.InvalidInputException;
import ittalents.javaee1.exceptions.InvalidJsonBodyException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.hibernate.UserRepository;
import ittalents.javaee1.models.User;
import ittalents.javaee1.models.dao.CryptWithMD5;
import ittalents.javaee1.models.dto.UserLoginDTO;
import ittalents.javaee1.models.dto.UserRegisterDTO;
import ittalents.javaee1.models.dto.UserSessionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

@RestController
public class UserController extends GlobalController {
	private static final String SUCCESSFUL_REGISTRATION = "{\"msg\" : \"You have successfully registered!\"}";
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
	private UserRepository userRepository;
	
	@GetMapping(value = "/logout")
	public Object logout(HttpSession session) throws BadRequestException {
		if (SessionManager.isLogged(session)) {
			session.invalidate();
			return SUCCESSFUL_LOG_OUT;
		}
		throw new NotLoggedException();
	}
	
	@GetMapping(value = "/unsubscribe/{id}")
	public Object unSubscribeFrom(HttpSession session, @PathVariable("id") long id) throws BadRequestException {
		
		if (SessionManager.isLogged(session)) {
			if (userRepository.existsById(id)) {  // user we are subscribing to exists
				User unSubscribeFrom = userRepository.findById(id).get();//subscriber
				User loggedUser = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
				if (loggedUser.getMySubscribers().contains(unSubscribeFrom)) {
					loggedUser.getMySubscribers().remove(unSubscribeFrom);
					unSubscribeFrom.getSubscribedToUsers().remove(loggedUser);
					userRepository.save(loggedUser);
					userRepository.save(unSubscribeFrom);
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
	public Object subscribeTo(HttpSession session, @PathVariable("id") long id) throws BadRequestException, MessagingException {
		
		if (SessionManager.isLogged(session)) {
			if (userRepository.existsById(id)) {  // user we are subscribing to exists
				User subscribeTo = userRepository.findById(id).get();//subscriber
				User loggedUser = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
				if (loggedUser.getMySubscribers().contains(subscribeTo)) { // already subbed
					throw new InvalidInputException(ALREADY_SUBSCRIBED);
				} else { // add subscription
					loggedUser.getMySubscribers().add(subscribeTo);
					subscribeTo.getSubscribedToUsers().add(loggedUser);
					userRepository.save(loggedUser);
					userRepository.save(subscribeTo);
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
		if (userRepository.existsByUsername(userLoginDTO.getUsername())) {
			
			User dbUser = userRepository.getByUsername(userLoginDTO.getUsername());
			if (CryptWithMD5.cryptWithMD5(userLoginDTO.getPassword()).equals(dbUser.getPassword())) {
				UserSessionDTO userSessionDTO = new UserSessionDTO(
						dbUser.getUserId(),
						dbUser.getAge(),
						dbUser.getUsername(),
						dbUser.getFullName(),
						dbUser.getEmail());
				SessionManager.logUser(session, userSessionDTO); // log into session then return as response
				return userSessionDTO;
			} else {
				throw new InvalidInputException(WRONG_CREDENTIALS);
			}
		} else {
			throw new InvalidInputException(WRONG_CREDENTIALS);
		}
	}
	
	@PostMapping(value = "/register")
	public Object registerUser(@RequestBody UserRegisterDTO userRegisterDTO) throws BadRequestException {
		if (userRegisterDTO == null) {
			throw new InvalidJsonBodyException(INVALID_JSON_BODY);
		}
		validateRegister(userRegisterDTO);
		if (!userRepository.existsByUsername(userRegisterDTO.getUsername())) {
			if (!userRepository.existsByEmail(userRegisterDTO.getEmail())) {
				userRepository.save(new User(userRegisterDTO.getAge(), userRegisterDTO.getFullName(),
						userRegisterDTO.getUsername(), CryptWithMD5.cryptWithMD5(userRegisterDTO.getPassword()),
						userRegisterDTO.getEmail()));
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
		if (username == null || username.isEmpty() || username.contains(" ")) {
			throw new InvalidInputException(WRONG_CREDENTIALS);
		}
		String password = user.getPassword();
		if (password == null || password.isEmpty() || password.contains(" ")) {
			throw new InvalidInputException(WRONG_CREDENTIALS);
		}
	}
	
	private void validateRegister(UserRegisterDTO user) throws InvalidInputException {
		validateLogin(user);
		
		String email = user.getEmail();
		if (email == null || email.isEmpty()) {
			throw new InvalidInputException(INVALID_EMAIL);
		}
		String fullname = user.getFullName();
		if (fullname == null || fullname.isEmpty()) {
			throw new InvalidInputException(INVALID_FULL_NAME);
		}
		long age = user.getAge();
		if (age <= 0 || age > 120) {
			throw new InvalidInputException(INVALID_AGE);
		}
	}
}
