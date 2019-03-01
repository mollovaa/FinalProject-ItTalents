package ittalents.javaee1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "watch_history")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class WatchHistory {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "watched_video_id")
    private Video video;

    private LocalDate date;

    public WatchHistory(User user, Video video) {
        this.user = user;
        this.video = video;
        this.date = LocalDate.now();
    }
}