package ittalents.javaee1.controllers;

import ittalents.javaee1.util.exceptions.AccessDeniedException;
import ittalents.javaee1.util.exceptions.BadRequestException;

import ittalents.javaee1.util.exceptions.NotFoundException;
import ittalents.javaee1.util.exceptions.NotLoggedException;
import ittalents.javaee1.models.repository.*;
import ittalents.javaee1.util.ResponseMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;


import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping(produces = "application/json")
public abstract class GlobalController {
	
	protected final String EMPTY_HISTORY = "Empty history!";
	protected final String SUCCESSFULLY_CLEARED_HISTORY = "Successfully cleared history!";
	
	@Autowired
	NotificationRepository notificationRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	VideoRepository videoRepository;
	@Autowired
	CommentRepository commentRepository;
	@Autowired
	PlaylistRepository playlistRepository;
	@Autowired
	WatchHistoryRepository watchHistoryRepository;
	@Autowired
	SearchHistoryRepository searchHistoryRepository;
	
	protected static Logger logger = LogManager.getLogger(GlobalController.class);
	
	@ExceptionHandler({Exception.class})
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public Object handleServerError(Exception e) {
		logger.error(e.getMessage(), e);
		return new ResponseMessage(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());
	}
	
	@ExceptionHandler({NotFoundException.class})
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ResponseMessage handleNotFound(NotFoundException e) {
		logger.error(e.getMessage(), e);
		return new ResponseMessage(e.getMessage(), HttpStatus.NOT_FOUND.value(), LocalDateTime.now());
	}
	
	@ExceptionHandler({NotLoggedException.class, AccessDeniedException.class})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public ResponseMessage handleNotLogged(Exception e) {
		logger.error(e.getMessage());
		return new ResponseMessage(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());
	}
	
	@ExceptionHandler({IOException.class})
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ResponseBody
	public ResponseMessage handleNotFoundFiles(Exception e) {
		logger.error(e.getMessage(), e);
		return new ResponseMessage(e.getMessage(), HttpStatus.FORBIDDEN.value(), LocalDateTime.now());
	}
	
	@ExceptionHandler({BadRequestException.class,
			MissingServletRequestParameterException.class,
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class,
			MissingServletRequestPartException.class,
			MultipartException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ResponseMessage handleOwnException(Exception e) {
		logger.error(e.getMessage(), e);
		return new ResponseMessage(e.getMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
	}
	
	boolean isValidString(String text) {
		return text != null && !text.isEmpty();
	}
}
