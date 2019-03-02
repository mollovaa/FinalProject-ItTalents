package ittalents.javaee1.hibernate;

import ittalents.javaee1.models.SearchHistory;
import ittalents.javaee1.models.SearchQuery;
import ittalents.javaee1.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    boolean existsByUserAndSearchQuery(User user, SearchQuery searchQuery);

    SearchHistory getByUserAndSearchQuery(User user, SearchQuery searchQuery);

    List<SearchHistory> getAllByUser(User user);

    SearchHistory getById(long id);
}
