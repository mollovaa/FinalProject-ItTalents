package ittalents.javaee1.repository;

import ittalents.javaee1.models.pojo.SearchQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchQueryRepository extends JpaRepository<SearchQuery, Long> {

    boolean existsBySearchQuery(String search_query);

    SearchQuery getBySearchQuery(String search_query);
}
