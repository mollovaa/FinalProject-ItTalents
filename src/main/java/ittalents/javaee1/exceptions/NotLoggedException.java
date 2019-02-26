package ittalents.javaee1.exceptions;

public class NotLoggedException extends BadRequestException {
	private static final String NOT_LOGED_IN = "You are not logged in.";
	public NotLoggedException() {
		super(NOT_LOGED_IN);
	}
}
