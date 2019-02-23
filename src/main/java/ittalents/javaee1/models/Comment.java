package ittalents.javaee1.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@AllArgsConstructor
@Getter
@Setter
public class Comment {

    private long id;
    private String message;
    private LocalDate dateOfPublication;
    private int numberOfLikes;
    private int numberOfDislikes;
    private long publisherId;
    private long responseId;
    private long videoId;
}
