package ittalents.javaee1.hibernate;

import ittalents.javaee1.models.SearchQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchQueryRepository extends JpaRepository<SearchQuery, Long> {

    boolean existsBySearchQuery(String search_query);

    SearchQuery getBySearchQuery(String search_query);
}
