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
public class ViewCommentDTO {

    private long id;
    private String message;
    private LocalDate date;
    private int likes;
    private int dislikes;
    private String publisher;
    private int responses_number;

}
