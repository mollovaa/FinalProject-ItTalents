package ittalents.javaee1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements Searchable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    private int age;
    private String fullName;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String email;

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "liked_videos_by_users",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "liked_video_id")}
    )
    private List<Video> likedVideos = new ArrayList<>();

    public void addLikedVideo(Video video) {
        this.likedVideos.add(video);
    }


    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "disliked_videos_by_users",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "disliked_video_id")}
    )
    private List<Video> dislikedVideos = new ArrayList<>();

    public void addDislikedVideo(Video video) {
        this.dislikedVideos.add(video);
    }

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "liked_comments_by_users",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "liked_comment_id")}
    )
    private List<Comment> likedComments = new ArrayList<>();

    public void addLikedComment(Comment comment) {
        this.likedComments.add(comment);
    }

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "disliked_comments_by_users",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "disliked_comment_id")}
    )
    private List<Comment> dislikedComments = new ArrayList<>();

    @OneToMany(mappedBy = "publisherId",orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ownerId",orphanRemoval = true)
    private List<Playlist> playlists = new ArrayList<>();

    @OneToMany(mappedBy = "uploaderId",orphanRemoval = true)
    private List<Video> videos = new ArrayList<>();
	
	
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "subscriptions",
            joinColumns = {@JoinColumn(name = "subscriber_id")},
            inverseJoinColumns = {@JoinColumn(name = "subscribed_to_id")}
    )
    private List<User> subscribedToUsers = new ArrayList<>();
	
	@JsonIgnore
    @ManyToMany(mappedBy = "subscribedToUsers")
    private List<User> mySubscribers = new ArrayList<>();

    @OneToMany(mappedBy = "observerId", orphanRemoval = true)
    private List<Notification> notifications;

    public User(int age, String fullName, String username, String password, String email) {
        this.age = age;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    public SearchType getType() {
        return SearchType.USER;
    }
}
