package ittalents.javaee1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "videos")
public class Video implements Searchable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long videoId;
    private String title;
    private String category;
    private String description;
    private LocalDate uploadDate;
    private long duration;        //seconds
    private int numberOfLikes;
    private int numberOfDislikes;
    private long numberOfViews;
    private long uploaderId;

    @JsonIgnore
    @ManyToMany(mappedBy = "likedVideos")
    private List<User> usersLikedVideo = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "dislikedVideos")
    private List<User> usersDislikedVideo = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "videosInPlaylist")
    private List<Playlist> playlistContainingVideo = new ArrayList<>();

    @OneToMany(mappedBy = "commentedVideo")
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "uploaderId", insertable = false, updatable = false)   //!!!
    private User uploader;

    @Override
    public SearchType getType() {
        return SearchType.VIDEO;
    }
}
