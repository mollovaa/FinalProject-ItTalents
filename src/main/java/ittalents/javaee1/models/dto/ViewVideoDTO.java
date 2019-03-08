package ittalents.javaee1.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ViewVideoDTO {

    private long id;
    private String title;
    private String category;
    private String description;
    private String URL;
    private LocalDate date;              //seconds
    private int likes;
    private int dislikes;
    private long views;
    private String uploader;
    private int comments_number;

}
