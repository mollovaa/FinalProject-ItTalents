package ittalents.javaee1.exceptions;

public class InvalidJsonBodyException extends BadRequestException {
	public InvalidJsonBodyException(String msg) {
		super(msg);
	}
}
