package ittalents.javaee1.models.repository;

import ittalents.javaee1.models.pojo.SearchHistory;
import ittalents.javaee1.models.pojo.SearchQuery;
import ittalents.javaee1.models.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    boolean existsByUserAndSearchQuery(User user, SearchQuery searchQuery);

    SearchHistory getByUserAndSearchQuery(User user, SearchQuery searchQuery);

    List<SearchHistory> getAllByUser(User user);

    SearchHistory getById(long id);
}
