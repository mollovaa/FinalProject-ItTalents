package ittalents.javaee1.controllers;


import ittalents.javaee1.util.SessionManager;
import ittalents.javaee1.util.exceptions.BadRequestException;
import ittalents.javaee1.util.exceptions.NotLoggedException;
import ittalents.javaee1.models.pojo.Notification;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.util.ResponseMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.swing.text.View;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/notifications")
public class NotificationController extends GlobalController {

    private static final String SUCCESSFULLY_CLEARED_NOTIFICATIONS = "Successfully cleared notifications!";

    @AllArgsConstructor
    @Getter
    class ViewNotificationDTO {
        private long notificationId;
        private String message;
        private LocalDate date;
    }

    ViewNotificationDTO convertToViewNotificationDTO(Notification n) {
        return new ViewNotificationDTO(n.getNotificationId(), n.getMessage(), n.getDate());
    }

    @GetMapping(value = "/unread")     //only unread notifications
    public Object showUnreadNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        ArrayList<ViewNotificationDTO> result = new ArrayList<>();
        for (Notification n : user.getNotifications()) {
            if (!n.isRead()) {
                result.add(convertToViewNotificationDTO(n));
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
        return result;
    }

    @GetMapping(value = "/all")
    public Object showAllNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        ArrayList<ViewNotificationDTO> result = new ArrayList<>();
        for (Notification n : user.getNotifications()) {
            result.add(convertToViewNotificationDTO(n));
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
        return result;
    }

    @Transactional
    @DeleteMapping(value = "/all/clear")
    public Object clearAllNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.getById(SessionManager.getLoggedUserId(session));
        List<Notification> notifications = user.getNotifications();
        for (Notification n : notifications) {
            notificationRepository.delete(n);
        }
        return new ResponseMessage(SUCCESSFULLY_CLEARED_NOTIFICATIONS, HttpStatus.OK.value(), LocalDateTime.now());
    }
}
