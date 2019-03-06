package ittalents.javaee1.models.pojo;

import ittalents.javaee1.models.dto.PlaylistDTOs;
import ittalents.javaee1.models.dto.SearchablePlaylistDTO;
import ittalents.javaee1.models.dto.SearchableVideoDTO;
import ittalents.javaee1.models.dto.ViewPlaylistDTO;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import ittalents.javaee1.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "playlists")
public class Playlist implements Searchable, PlaylistDTOs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long playlistId;
    private String playlistName;
    private long ownerId;

    public Playlist(String playlistName) {
        this.playlistName = playlistName;
    }

    @ManyToMany
    @JoinTable(
            name = "playlists_videos",
            joinColumns = {@JoinColumn(name = "playlist_id")},
            inverseJoinColumns = {@JoinColumn(name = "video_id")}
    )
    private List<Video> videosInPlaylist = new ArrayList<>();

    @Override
    public SearchType getType() {
        return SearchType.PLAYLIST;
    }


    @Override
    public SearchablePlaylistDTO convertToSearchablePlaylistDTO(UserRepository userRepository) {
        return new SearchablePlaylistDTO(this.playlistId, this.playlistName,
                userRepository.findById(this.ownerId).get().getFullName(),
                this.videosInPlaylist.size());
    }

    @Override
    public ViewPlaylistDTO convertToViewPlaylistDTO(UserRepository userRepository) {
        List<Video> videos = this.videosInPlaylist;
        List<SearchableVideoDTO> videosToShow = new ArrayList<>();
        videosToShow.addAll(videos
                .stream()
                .map(video -> video.convertToSearchableVideoDTO(userRepository))
                .collect(Collectors.toList()));
        return new ViewPlaylistDTO(this.playlistId, this.playlistName,
                userRepository.findById(this.ownerId).get().getFullName(), videosToShow.size(), videosToShow);
    }
}
