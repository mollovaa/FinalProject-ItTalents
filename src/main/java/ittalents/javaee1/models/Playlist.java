package ittalents.javaee1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "playlists")
public class Playlist implements Searchable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long playlistId;
    private String playlistName;
    private long ownerId;     //todo one to many

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
}
