package ittalents.javaee1.controllers;

import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.models.User;

import javax.servlet.http.HttpSession;

public class SessionManager {
	private static final String LOGGED = "logged";
	private static final String USER = "user";
	
	public static boolean isLogged(HttpSession session) {
		if (session.isNew()) {
			return false;
		}
		if (session.getAttribute(LOGGED) == null) {
			return false;
		}
		return true;
	}
	
	public static long getLoggedUserId(HttpSession session) throws ExpiredSessionException {
		if (SessionManager.isLogged(session)) {
			return ((User) session.getAttribute("user")).getId();
		}
		throw new ExpiredSessionException();
	}
	
	public static User getLoggedUser(HttpSession session) throws ExpiredSessionException {
		if (SessionManager.isLogged(session)) {
			return ((User) session.getAttribute("user"));
		}
		throw new ExpiredSessionException();
	}
	
	public static class ExpiredSessionException extends BadRequestException {
		public ExpiredSessionException() {
			super("Please, login!");
		}
	}
	
	public static void logUser(HttpSession session, User user) {
		session.setAttribute(LOGGED, true);
		session.setAttribute(USER, user);
	}
}
