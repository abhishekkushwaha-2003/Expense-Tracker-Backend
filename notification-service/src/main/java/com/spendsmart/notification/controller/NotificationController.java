package com.spendsmart.notification.controller;

import com.spendsmart.notification.dto.BudgetAlertRequest;
import com.spendsmart.notification.dto.BulkNotificationRequest;
import com.spendsmart.notification.dto.EmailRequest;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public Notification send(@Valid @RequestBody NotificationRequest request) {
        return notificationService.send(request);
    }

    @PostMapping("/budget-alert")
    @ResponseStatus(HttpStatus.CREATED)
    public Notification sendBudgetAlert(@Valid @RequestBody BudgetAlertRequest request) {
        return notificationService.sendBudgetAlert(request);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Notification> sendBulk(@Valid @RequestBody BulkNotificationRequest request) {
        return notificationService.sendBulk(request);
    }

    @PostMapping("/email")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendEmail(@Valid @RequestBody EmailRequest request) {
        notificationService.sendEmail(request);
    }

    @GetMapping("/recipient/{recipientId}")
    public List<Notification> getByRecipient(@PathVariable Long recipientId) {
        return notificationService.getByRecipient(recipientId);
    }

    @GetMapping("/recipient/{recipientId}/unread")
    public List<Notification> getUnread(@PathVariable Long recipientId) {
        return notificationService.getUnreadByRecipient(recipientId);
    }

    @GetMapping("/recipient/{recipientId}/unread/count")
    public long getUnreadCount(@PathVariable Long recipientId) {
        return notificationService.getUnreadCount(recipientId);
    }

    @GetMapping
    public List<Notification> getAll() {
        return notificationService.getAll();
    }

    @PutMapping("/{notificationId}/read")
    public Notification markAsRead(@PathVariable Long notificationId) {
        return notificationService.markAsRead(notificationId);
    }

    @PutMapping("/recipient/{recipientId}/read-all")
    public List<Notification> markAllRead(@PathVariable Long recipientId) {
        return notificationService.markAllRead(recipientId);
    }

    @PutMapping("/{notificationId}/acknowledge")
    public Notification acknowledge(@PathVariable Long notificationId) {
        return notificationService.acknowledge(notificationId);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
    }
}
