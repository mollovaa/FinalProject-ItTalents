package ittalents.javaee1.hibernate;

import ittalents.javaee1.models.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findAllByPlaylistNameContaining(String playlistName);
}
