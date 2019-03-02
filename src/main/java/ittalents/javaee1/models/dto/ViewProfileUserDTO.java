package ittalents.javaee1.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ViewProfileUserDTO {
    private long userId;
    private int numberOfSubscribers;
    private String fullName;
    private List<SearchableVideoDTO> videos;
    private List<SearchablePlaylistDTO> playlists;


}
