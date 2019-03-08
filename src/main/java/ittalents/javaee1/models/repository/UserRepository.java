package ittalents.javaee1.models.repository;

import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.util.exceptions.UserNotFoundExeption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User getByUsername(String username);

    User getByUserId(long id);

    List<User> findAllByFullNameContaining(String searchWord);
    
    default User getById(long id) throws UserNotFoundExeption {
        Optional<User> user = findById(id);
        if(!user.isPresent()){
            throw new UserNotFoundExeption();
        }
        return user.get();
    }
}
