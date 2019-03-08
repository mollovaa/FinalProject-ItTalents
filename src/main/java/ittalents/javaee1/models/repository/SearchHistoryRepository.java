package ittalents.javaee1.models.repository;

import ittalents.javaee1.models.pojo.SearchHistory;
import ittalents.javaee1.models.pojo.SearchQuery;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.util.exceptions.SearchNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    boolean existsByUserAndSearchQuery(User user, SearchQuery searchQuery);

    SearchHistory getByUserAndSearchQuery(User user, SearchQuery searchQuery);

    List<SearchHistory> getAllByUser(User user);

    default SearchHistory getById(long id) throws SearchNotFoundException {
        Optional<SearchHistory> searchHistory = this.findById(id);
        if (!searchHistory.isPresent()) {
            throw new SearchNotFoundException();
        }
        return searchHistory.get();
    }
}
