package ittalents.javaee1.models.pojo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "search_queries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SearchQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String searchQuery;

    public SearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "searchQuery", orphanRemoval = true)
    Set<SearchHistory> searchHistorySet = new HashSet<>();

}
