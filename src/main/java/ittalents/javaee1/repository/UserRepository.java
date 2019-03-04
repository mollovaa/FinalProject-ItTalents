package ittalents.javaee1.repository;

import ittalents.javaee1.models.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User getByUsername(String username);

    User getByUserId(long id);

    List<User> findAllByFullNameContaining(String searchWord);
}
