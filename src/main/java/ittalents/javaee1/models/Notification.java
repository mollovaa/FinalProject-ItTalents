package ittalents.javaee1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long notificationId;
    private String message;
    private LocalDate date;
    private boolean isRead;
    private long observerId;           //user

    public Notification(String message, long observerId) {
        this.message = message;
        this.date = LocalDate.now();
        this.isRead = false;
        this.observerId = observerId;
    }




}
