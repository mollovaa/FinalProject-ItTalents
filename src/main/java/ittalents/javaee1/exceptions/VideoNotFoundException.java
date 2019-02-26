package ittalents.javaee1.exceptions;

public class VideoNotFoundException extends BadRequestException {

    public VideoNotFoundException() {
        super("Sorry, video not found!");
    }
}
