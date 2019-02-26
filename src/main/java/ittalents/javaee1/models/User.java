package ittalents.javaee1.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements Searchable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    private int age;
    private String full_name;
    private String username;
    private String password;
    private String email;


    @ManyToMany(cascade = {CascadeType.REFRESH})
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

    @OneToMany(mappedBy = "publisher")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "owner")
    private List<Playlist> playlists = new ArrayList<>();

    @OneToMany(mappedBy = "uploader")
    private List<Video> videos = new ArrayList<>();

    public User(int age, String full_name, String username, String password, String email) {
        this.age = age;
        this.full_name = full_name;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(long userId, String full_name) {
        this.userId = userId;
        this.full_name = full_name;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public int getAge() {
        return age;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public SearchType getType() {
        return SearchType.USER;
    }
}
