package ittalents.javaee1.util.exceptions;

public class InvalidJsonBodyException extends BadRequestException {
	public InvalidJsonBodyException(String msg) {
		super(msg);
	}
}
