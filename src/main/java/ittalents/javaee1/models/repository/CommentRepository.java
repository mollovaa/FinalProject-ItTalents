package ittalents.javaee1.models.repository;

import ittalents.javaee1.models.pojo.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
