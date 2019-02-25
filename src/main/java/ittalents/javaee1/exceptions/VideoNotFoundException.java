package ittalents.javaee1.exceptions;

public class VideoNotFoundException extends BadRequestException {

    public VideoNotFoundException() {
        super("{\"error\" : \"Sorry, video not found!\"}");
    }
}
