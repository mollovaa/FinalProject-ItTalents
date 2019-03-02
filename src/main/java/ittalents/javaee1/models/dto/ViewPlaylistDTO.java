package ittalents.javaee1.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ViewPlaylistDTO extends SearchablePlaylistDTO {

    private List<SearchableVideoDTO> videos = new ArrayList<>();

    public ViewPlaylistDTO(long id, String name, String owner, int videos_number, List<SearchableVideoDTO> videos) {
        super(id, name, owner, videos_number);
        this.videos = videos;
    }
}
