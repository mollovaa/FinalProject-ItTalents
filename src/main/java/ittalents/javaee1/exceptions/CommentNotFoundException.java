package ittalents.javaee1.exceptions;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException() {
        super("Sorry, comment not found!");
    }
}
