package ittalents.javaee1.hibernate;

import ittalents.javaee1.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
	
	User getByUsername(String username);
	
	List<User> findAllByFullNameContaining(String searchWord);
}
