package ittalents.javaee1.hibernate;

import ittalents.javaee1.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
