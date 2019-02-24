package ittalents.javaee1.exceptions;

public class PlaylistNotFoundException extends Exception {

    public PlaylistNotFoundException() {
        super("Sorry, playlist not found!");
    }
}
