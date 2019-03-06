package ittalents.javaee1.models.repository;

import ittalents.javaee1.models.pojo.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Playlist getByPlaylistId(long playlistId);

    List<Playlist> findAllByPlaylistNameContaining(String playlistName);
}
