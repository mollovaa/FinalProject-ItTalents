package ittalents.javaee1.models.dto;

import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class SearchableUserDTO implements Searchable {

    private long userId;
    private int numberOfSubscribers;
    private String fullName;
    private int videos_number;
    private int playlists_number;


    @Override
    public SearchType getType() {
        return SearchType.USER;
    }
}
