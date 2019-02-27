package ittalents.javaee1.exceptions;

public class CommentNotFoundException extends BadRequestException {
    public CommentNotFoundException() {
        super("Sorry, comment not found!");
    }
}
