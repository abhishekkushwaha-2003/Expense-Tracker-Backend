package com.spendsmart.notification.service;

import com.spendsmart.notification.dto.BudgetAlertRequest;
import com.spendsmart.notification.dto.BulkNotificationRequest;
import com.spendsmart.notification.dto.EmailRequest;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import java.util.List;

public interface NotificationService {

    Notification send(NotificationRequest request);

    Notification sendBudgetAlert(BudgetAlertRequest request);

    List<Notification> sendBulk(BulkNotificationRequest request);

    void sendEmail(EmailRequest request);

    List<Notification> getByRecipient(Long recipientId);

    List<Notification> getUnreadByRecipient(Long recipientId);

    long getUnreadCount(Long recipientId);

    List<Notification> getAll();

    Notification markAsRead(Long notificationId);

    List<Notification> markAllRead(Long recipientId);

    Notification acknowledge(Long notificationId);

    void deleteNotification(Long notificationId);
}
