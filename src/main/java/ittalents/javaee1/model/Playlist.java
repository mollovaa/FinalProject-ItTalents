package ittalents.javaee1.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Playlist {

    private long id;
    private String name;
    private long ownerId;

}
