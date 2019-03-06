package ittalents.javaee1.util.exceptions;

public class NotFoundException extends BadRequestException {
    public NotFoundException(String message) {
        super(message);
    }
}
