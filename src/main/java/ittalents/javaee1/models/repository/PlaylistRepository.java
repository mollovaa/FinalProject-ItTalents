package ittalents.javaee1.models.repository;

import ittalents.javaee1.models.pojo.Playlist;
import ittalents.javaee1.util.exceptions.PlaylistNotFoundException;
import org.aspectj.apache.bcel.util.Play;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    default Playlist getByPlaylistId(long playlistId) throws PlaylistNotFoundException {
        Optional<Playlist> playlist = this.findById(playlistId);
        if (!playlist.isPresent()) {
            throw new PlaylistNotFoundException();
        }
        return playlist.get();
    }

    List<Playlist> findAllByPlaylistNameContaining(String playlistName);
}
