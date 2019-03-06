package ittalents.javaee1.util.exceptions;

public class PlaylistNotFoundException extends NotFoundException {

    public PlaylistNotFoundException() {
        super("Playlist not found!");
    }
}
