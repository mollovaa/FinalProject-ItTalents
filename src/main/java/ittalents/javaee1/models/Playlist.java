package ittalents.javaee1.models;

import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
