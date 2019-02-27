package ittalents.javaee1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long commentId;
    private String message;
    private LocalDate dateOfPublication;
    private int numberOfLikes;
    private int numberOfDislikes;
    private long publisherId;     //todo one to many
    private Long responseToId;
    private long videoId;

    @JsonIgnore
    @ManyToMany(mappedBy = "likedComments")
    private List<User> usersLikedComment = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "dislikedComments")
    private List<User> usersDislikedComment = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "responseToId", insertable = false, updatable = false)   //!!!
    private Comment baseComment;


    @OneToMany(mappedBy = "baseComment")
    private List<Comment> responses = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "videoId", insertable = false, updatable = false)   //!!!
    private Video commentedVideo;

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "publisherId", insertable = false, updatable = false)   //!!!
    private User publisher;


}
