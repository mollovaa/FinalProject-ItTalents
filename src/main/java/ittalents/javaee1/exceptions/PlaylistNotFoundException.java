package ittalents.javaee1.exceptions;

public class PlaylistNotFoundException extends BadRequestException {

    public PlaylistNotFoundException() {
        super("{\"error\" : \"Sorry, playlist not found!\"}");
    }
}
