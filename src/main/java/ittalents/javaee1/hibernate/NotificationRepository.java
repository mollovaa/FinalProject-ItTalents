package ittalents.javaee1.hibernate;

import ittalents.javaee1.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository  extends JpaRepository<Notification, Long> {

}
