package ittalents.javaee1.exceptions;

public class VideoNotFoundException extends NotFoundException {

    public VideoNotFoundException() {
        super("Video not found!");
    }
}
