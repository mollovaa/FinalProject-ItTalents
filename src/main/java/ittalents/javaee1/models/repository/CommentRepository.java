package ittalents.javaee1.models.repository;

import ittalents.javaee1.models.pojo.Comment;
import ittalents.javaee1.util.exceptions.CommentNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    default Comment getByCommentId(long commentId) throws CommentNotFoundException {
        Optional<Comment> comment = this.findById(commentId);
        if (!comment.isPresent()) {
            throw new CommentNotFoundException();
        }
        return comment.get();
    }

}
