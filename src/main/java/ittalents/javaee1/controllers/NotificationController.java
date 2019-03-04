package ittalents.javaee1.controllers;


import ittalents.javaee1.exceptions.BadRequestException;
import ittalents.javaee1.exceptions.NotLoggedException;
import ittalents.javaee1.models.pojo.Notification;
import ittalents.javaee1.models.pojo.User;
import ittalents.javaee1.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/notifications")
public class NotificationController extends GlobalController {

    private static final String NO_NOTIFICATIONS = "No notifications";
    private static final String SUCCESSFULLY_CLEARED_NOTIFICATIONS = "Successfully cleared notifications!";

    @GetMapping(value = "/unread")     //only unread notifications
    public Object showUnreadNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        if (user.getNotifications() == null || user.getNotifications().isEmpty()) {
            return new ResponseMessage(NO_NOTIFICATIONS, HttpStatus.OK.value(), LocalDateTime.now());
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
            return new ResponseMessage(NO_NOTIFICATIONS, HttpStatus.OK.value(), LocalDateTime.now());
        }
        return result;
    }

    @GetMapping(value = "/all")
    public Object showAllNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        List<Notification> notifications = user.getNotifications();
        if (notifications == null || notifications.isEmpty()) {
            return new ResponseMessage(NO_NOTIFICATIONS, HttpStatus.OK.value(), LocalDateTime.now());
        }
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
        return notifications;
    }

    @PutMapping(value = "/all/clear")
    public Object clearAllNotifications(HttpSession session) throws BadRequestException {
        if (!SessionManager.isLogged(session)) {
            throw new NotLoggedException();
        }
        User user = userRepository.findById(SessionManager.getLoggedUserId(session)).get();
        List<Notification> notifications = user.getNotifications();
        if (notifications == null || notifications.isEmpty()) {
            return new ResponseMessage(NO_NOTIFICATIONS, HttpStatus.OK.value(), LocalDateTime.now());
        }
        for (Notification n : notifications) {
            notificationRepository.delete(n);
        }
        return new ResponseMessage(SUCCESSFULLY_CLEARED_NOTIFICATIONS, HttpStatus.OK.value(), LocalDateTime.now());
    }
}
