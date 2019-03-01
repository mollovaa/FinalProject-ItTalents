package ittalents.javaee1.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VideoInPlaylistDTO {

    private long id;
    private String title;
    private String uploader;
}
