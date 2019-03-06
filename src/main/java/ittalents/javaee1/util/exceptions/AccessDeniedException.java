package ittalents.javaee1.util.exceptions;

public class AccessDeniedException extends BadRequestException {
    static final String ACCESS_DENIED = "Permitted operation!";

    public AccessDeniedException() {
        super(ACCESS_DENIED);
    }
}
