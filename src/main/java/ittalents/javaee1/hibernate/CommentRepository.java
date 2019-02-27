package ittalents.javaee1.hibernate;

import ittalents.javaee1.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
