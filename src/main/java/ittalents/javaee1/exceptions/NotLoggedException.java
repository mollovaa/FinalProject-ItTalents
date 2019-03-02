package ittalents.javaee1.exceptions;

public class NotLoggedException extends BadRequestException {
	private static final String NOT_LOGGED_IN = "Not logged in.";
	public NotLoggedException() {
		super(NOT_LOGGED_IN);
	}
}
