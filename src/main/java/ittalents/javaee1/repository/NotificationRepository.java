package ittalents.javaee1.repository;

import ittalents.javaee1.models.pojo.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository  extends JpaRepository<Notification, Long> {

}
