package ittalents.javaee1.exceptions;

public class VideoNotFoundException extends Exception {

    public VideoNotFoundException() {
        super("{\"error\" : \"Sorry, video not found!\"}");
    }
}
