package ittalents.javaee1.models.dto;

import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SearchableVideoDTO implements Searchable {

    private long id;
    private String title;
    private String uploader;


    @Override
    public SearchType getType() {
        return SearchType.VIDEO;
    }
}