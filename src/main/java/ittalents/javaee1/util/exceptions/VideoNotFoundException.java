package ittalents.javaee1.util.exceptions;

public class VideoNotFoundException extends NotFoundException {

    public VideoNotFoundException() {
        super("Video not found!");
    }
}
