package ittalents.javaee1.exceptions;

public class PlaylistNotFoundException extends BadRequestException {

    public PlaylistNotFoundException() {
        super("Sorry, playlist not found!");
    }
}
