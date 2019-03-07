package ittalents.javaee1.util.exceptions;

public class UserNotFoundExeption extends NotFoundException {
	public UserNotFoundExeption() {
		super("User not found!");
	}
}
