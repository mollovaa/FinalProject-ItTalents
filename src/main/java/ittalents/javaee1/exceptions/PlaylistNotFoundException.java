package ittalents.javaee1.exceptions;

public class PlaylistNotFoundException extends NotFoundException {

    public PlaylistNotFoundException() {
        super("Playlist not found!");
    }
}
