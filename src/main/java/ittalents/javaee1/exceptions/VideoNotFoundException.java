package ittalents.javaee1.exceptions;

public class VideoNotFoundException extends NotFoundException {

    public VideoNotFoundException() {
        super("Sorry, video not found!");
    }
}
