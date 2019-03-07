package ittalents.javaee1.controllers;

import ittalents.javaee1.util.SessionManager;
import ittalents.javaee1.util.AmazonClient;
import ittalents.javaee1.util.exceptions.*;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.models.dto.*;
import ittalents.javaee1.util.CryptWithBCrypt;
import ittalents.javaee1.util.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController extends GlobalController {
	private static final String SUCCESSFUL_REGISTRATION = "Successfully registered!";
	private static final String SUCCESSFUL_LOG_OUT = "Successfully logged out!";
	private static final String SUBSCRIBED = "Subscribed!";
	private static final String UNSUBSCRIBED = "Unsubscribed!";
	private static final String ACCOUNT_DELETED = "Account deleted!";
	private static final String WRONG_CREDENTIALS = "Invalid Username or Password";
	private static final String EXISTING_EMAIL = "Email already exists!";
	private static final String EXISTING_USERNAME = "Username already exists!";
	private static final String INVALID_FULL_NAME = "Invalid Registration Full name!";
	private static final String INVALID_EMAIL = "Invalid Registration Email!";
	private static final String INVALID_AGE = "Invalid Age!";
	private static final String NOT_SUBSCRIBED = "Not subscribed.";
	private static final String ALREADY_LOGGED = "Already logged in.";
	private static final String ALREADY_SUBSCRIBED = "Already subscribed.";
	private static final String INVALID_USER = "Invalid user!";
	private static final String INVALID_JSON_BODY = "Invalid Json Body!";
	private static final String INCORRECT_PASSWORD = "Incorrect password!";
	private static final String MSG_PASSWORD_CHANGED = "Password changed!";
	private static final String MUST_BE_DIFFERENT_FROM_THE_OLD = "New password must be different from the old!";
	private static final int MIN_PASS_LENGTH = 5;
	private static final String INVALID_NEW_PASSWORD = "Invalid new Password";
	private static final String NO_VIDEOS = "No videos!";
	private static final String NO_PLAYLISTS = "No playlists";
	
	@Autowired
	private AmazonClient amazonClient;
	
	@PostMapping(value = "/logout")
	public Object logout(HttpSession session) throws BadRequestException {
		if (SessionManager.isLogged(session)) {
			session.invalidate();
			return new ResponseMessage(SUCCESSFUL_LOG_OUT, HttpStatus.OK.value(), LocalDateTime.now());
		}
		throw new NotLoggedException();
	}
	
	@GetMapping(value = "/profile")
	public Object viewMyProfile(HttpSession session) throws NotLoggedException {
		if (SessionManager.isLogged(session)) {
			return userRepository.findById(SessionManager.getLoggedUserId(session)).get()
					.convertToViewProfileUserDTO(userRepository);
		} else {
			throw new NotLoggedException();
		}
	}
	
	@GetMapping(value = "/view/profile/{id}")
	public Object viewProfile(@PathVariable("id") long id) throws UserNotFoundExeption {
		if (userRepository.existsById(id)) {
			return userRepository.findById(id).get().convertToSearchableDTO();
		} else {
			throw new UserNotFoundExeption();
		}
	}
	
	@GetMapping(value = "/view/profile/{id}/videos")
	public Object viewVideos(@PathVariable("id") long id) throws UserNotFoundExeption {
		if (userRepository.existsById(id)) {
			List<SearchableVideoDTO> videos = userRepository.findById(id).get()
					.getVideos()
					.stream()
					.map(video -> video.convertToSearchableVideoDTO(userRepository))
					.collect(Collectors.toList());
			if (videos.isEmpty()) {
				return new ResponseMessage(NO_VIDEOS, HttpStatus.OK.value(), LocalDateTime.now());
			}
			return videos;
		} else {
			throw new UserNotFoundExeption();
		}
	}
	
	@GetMapping(value = "/view/profile/{id}/playlists")
	public Object viewPlaylists(@PathVariable("id") long id) throws UserNotFoundExeption {
		if (userRepository.existsById(id)) {
			List<SearchablePlaylistDTO> playlists = userRepository.findById(id).get()
					.getPlaylists()
					.stream()
					.map(playlist -> playlist.convertToSearchablePlaylistDTO(userRepository))
					.collect(Collectors.toList());
			if (playlists.isEmpty()) {
				return new ResponseMessage(NO_PLAYLISTS, HttpStatus.OK.value(), LocalDateTime.now());
			}
			return playlists;
		} else {
			throw new UserNotFoundExeption();
		}
	}
	
	@DeleteMapping(value = "/profile/delete-account")
	public Object deleteProfile(@RequestPart(value = "password") String password, HttpSession session)
			throws BadRequestException {
		if (password == null) {
			throw new InvalidInputException(INCORRECT_PASSWORD);
		}
		if (SessionManager.isLogged(session)) {
			User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
			if (!CryptWithBCrypt.checkPassword(password, user.getPassword())) {
				throw new InvalidInputException(INCORRECT_PASSWORD);
			}
			logout(session);
			//delete all video urls from amazon storage
			user.getVideos().stream().map(video -> amazonClient.deleteFileFromS3Bucket(video.getURL()));
			userRepository.deleteById(user.getUserId());
			return new ResponseMessage(ACCOUNT_DELETED, HttpStatus.OK.value(), LocalDateTime.now());
		} else {
			throw new NotLoggedException();
		}
	}
	
	@PutMapping(value = "/profile/edit/password")
	public Object editPassword(@RequestBody ChangePasswordDTO dto, HttpSession session) throws BadRequestException {
		if (SessionManager.isLogged(session)) {
			User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
			if (!validatePassword(dto.getNewPassword()) || !validatePassword(dto.getOldPassword())) {
				throw new InvalidInputException(INCORRECT_PASSWORD);
			}
			if (!CryptWithBCrypt.checkPassword(dto.getOldPassword(), user.getPassword())) {
				throw new InvalidInputException(INCORRECT_PASSWORD);
			}
			if (dto.getOldPassword().equals(dto.getNewPassword())) {
				throw new InvalidInputException(MUST_BE_DIFFERENT_FROM_THE_OLD);
			}
			user.setPassword(CryptWithBCrypt.hashPassword(dto.getNewPassword()));
			userRepository.save(user);
			return new ResponseMessage(MSG_PASSWORD_CHANGED, HttpStatus.OK.value(), LocalDateTime.now());
		} else {
			throw new NotLoggedException();
		}
	}
	
	@Transactional
	@PutMapping(value = "/unsubscribe/{id}")
	public Object unSubscribeFrom(HttpSession session, @PathVariable("id") long id) throws BadRequestException {
		
		if (SessionManager.isLogged(session)) {
			if (userRepository.existsById(id)) {  // user we are subscribing to exists
				User unSubscribeFrom = userRepository.findById(id).get();//subscriber
				User loggedUser = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
				if (loggedUser.getMySubscribers().contains(unSubscribeFrom)) {
					loggedUser.getSubscribedToUsers().remove(unSubscribeFrom);
					unSubscribeFrom.getMySubscribers().remove(loggedUser);
					userRepository.save(loggedUser);
					userRepository.save(unSubscribeFrom);
					return new ResponseMessage(UNSUBSCRIBED, HttpStatus.OK.value(), LocalDateTime.now());
				} else {  //not subbed
					throw new InvalidInputException(NOT_SUBSCRIBED);
				}
			} else {
				throw new UserNotFoundExeption();
			}
		}
		throw new NotLoggedException();
	}
	
	@Transactional
	@PutMapping(value = "/subscribe/{id}")
	public Object subscribeTo(HttpSession session, @PathVariable("id") long id) throws BadRequestException {
		
		if (SessionManager.isLogged(session)) {
			if (userRepository.existsById(id)) {  // user we are subscribing to exists
				User subscribeTo = userRepository.findById(id).get();//subscriber
				User loggedUser = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
				if (loggedUser.getMySubscribers().contains(subscribeTo)) { // already subbed
					throw new InvalidInputException(ALREADY_SUBSCRIBED);
				} else { // add subscription
					loggedUser.getSubscribedToUsers().add(subscribeTo);
					subscribeTo.getMySubscribers().add(loggedUser);
					userRepository.save(loggedUser);
					userRepository.save(subscribeTo);
					return new ResponseMessage(SUBSCRIBED, HttpStatus.OK.value(), LocalDateTime.now());
				}
			} else {
				throw new UserNotFoundExeption();
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
			if (CryptWithBCrypt.checkPassword(userLoginDTO.getPassword(), dbUser.getPassword())) {
				UserSessionDTO userSessionDTO = dbUser.convertToUserSessionDTO();
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
						userRegisterDTO.getUsername(),
						CryptWithBCrypt.hashPassword(userRegisterDTO.getPassword()),
						userRegisterDTO.getEmail()));
				return new ResponseMessage(SUCCESSFUL_REGISTRATION, HttpStatus.OK.value(), LocalDateTime.now());
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
		if (!validatePassword(user.getPassword())) {
			throw new InvalidInputException(WRONG_CREDENTIALS);
		}
	}
	
	private boolean validatePassword(String password) {
		if (password == null || password.isEmpty() || password.contains(" ") || password.length() < MIN_PASS_LENGTH) {
			return false;
		}
		return true;
	}
	
	private void validateRegister(UserRegisterDTO user) throws InvalidInputException {
		validateLogin(user);
		
		String email = user.getEmail();
		if (email == null || email.isEmpty()) {
			throw new InvalidInputException(INVALID_EMAIL);
		}
		String[] tokens = email.split("@");
		if (tokens.length != 2) {
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
