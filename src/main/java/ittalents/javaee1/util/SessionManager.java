package ittalents.javaee1.util;

import ittalents.javaee1.util.exceptions.NotLoggedException;
import ittalents.javaee1.models.dto.UserSessionDTO;

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
	
	public static long getLoggedUserId(HttpSession session) throws NotLoggedException {
		if (SessionManager.isLogged(session)) {
			return ((UserSessionDTO) session.getAttribute(USER)).getId();
		}
		throw new NotLoggedException();
	}
	
	public static UserSessionDTO getLoggedUser(HttpSession session) throws NotLoggedException {
		if (SessionManager.isLogged(session)) {
			return ((UserSessionDTO) session.getAttribute(USER));
		}
		throw new NotLoggedException();
	}

	public static void logUser(HttpSession session, UserSessionDTO user) {
		session.setAttribute(LOGGED, true);
		session.setAttribute(USER, user);
	}
}
