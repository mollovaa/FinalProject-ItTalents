package ittalents.javaee1.models;

import ittalents.javaee1.models.search.SearchType;
import ittalents.javaee1.models.search.Searchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Playlist implements Searchable {

    private long id;
    private String name;
    private long ownerId;

    public Playlist(String name) {
        this.name = name;
    }

    @Override
    public SearchType getType() {
        return SearchType.PLAYLIST;
    }
}
