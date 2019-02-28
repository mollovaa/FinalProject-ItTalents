package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.models.Notification;
import ittalents.javaee1.models.User;
import ittalents.javaee1.util.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/notifications")
public class NotificationController extends GlobalController {

    @GetMapping(value = "/unread")     //only unread notifications
    public Object[] showUnreadNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getNotifications() == null || user.getNotifications().isEmpty()) {
            throw new BadRequestException("No notifications");
        }
        ArrayList<Notification> result = new ArrayList<>();
        for (Notification n : user.getNotifications()) {
            if (!n.isRead()) {
                result.add(n);
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
        if (result.isEmpty()) {
            throw new BadRequestException("No unread notifications");
        }
        return result.toArray();
    }

    @GetMapping(value = "/all")//todo make unread->read and save
    public Object[] showAllNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        List<Notification> notifications = user.getNotifications();
        if (notifications == null || notifications.isEmpty()) {
            throw new BadRequestException("No notifications");
        }
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
        return notifications.toArray();
    }

    @PutMapping(value = "/all/clear")
    public Object clearAllNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        List<Notification> notifications = user.getNotifications();
        if (notifications == null || notifications.isEmpty()) {
            throw new BadRequestException("No notifications");
        }
        for (Notification n : notifications) {
            notificationRepository.delete(n);
        }
        return new ErrorMessage("Successfully cleared notifications!", HttpStatus.OK.value(), LocalDateTime.now());
    }
}
