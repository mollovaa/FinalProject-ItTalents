package ittalents.javaee1.models.dto;

import lombok.*;

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
