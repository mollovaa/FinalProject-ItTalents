package ittalents.javaee1.models.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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
