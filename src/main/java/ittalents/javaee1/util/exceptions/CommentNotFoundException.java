package ittalents.javaee1.util.exceptions;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException() {
        super("Sorry, comment not found!");
    }
}
