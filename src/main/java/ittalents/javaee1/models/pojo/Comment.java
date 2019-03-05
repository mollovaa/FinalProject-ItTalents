package ittalents.javaee1.models.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ittalents.javaee1.models.dto.CommentDTO;
import ittalents.javaee1.models.dto.ViewCommentDTO;
import ittalents.javaee1.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
public class Comment implements CommentDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long commentId;
    private String message;
    private LocalDate dateOfPublication;
    private int numberOfLikes;
    private int numberOfDislikes;
    private long publisherId;
    private Long responseToId;
    private long videoId;

    @JsonIgnore
    @ManyToMany(mappedBy = "likedComments")
    private List<User> usersLikedComment = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "dislikedComments")
    private List<User> usersDislikedComment = new ArrayList<>();

    @OneToMany(mappedBy = "responseToId", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Comment> responses = new ArrayList<>();

    @Autowired
    private UserRepository userRepository;

    @Override
    public ViewCommentDTO convertToCommentDTO() {
        return new ViewCommentDTO(this.commentId, this.message, this.dateOfPublication,
                this.numberOfLikes, this.numberOfDislikes,
                userRepository.findById(this.publisherId).get().getFullName(), this.responses.size());
    }
}
