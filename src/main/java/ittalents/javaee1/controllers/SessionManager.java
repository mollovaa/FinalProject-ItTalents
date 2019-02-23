package ittalents.javaee1.controllers;

import javax.servlet.http.HttpSession;

public class SessionManager {
	private static final String LOGGED = "logged";
	private static final String USER_ID = "user_id";
	
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
		if(SessionManager.isLogged(session)){
			return ((Long)session.getAttribute(USER_ID));
		}
		throw new ExpiredSessionException();
	}

	public static class ExpiredSessionException extends Exception {
		public ExpiredSessionException() {
			super("Please, login!");
		}
	}

	public static void logUser(HttpSession session, long user_id) {
		session.setAttribute(LOGGED, true);
		session.setAttribute(USER_ID, user_id);
	}
}
