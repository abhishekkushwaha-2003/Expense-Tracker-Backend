package com.spendsmart.notification.repository;

import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.entity.NotificationSeverity;
import com.spendsmart.notification.entity.NotificationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    List<Notification> findBySeverityOrderByCreatedAtDesc(NotificationSeverity severity);

    List<Notification> findByIsAcknowledgedFalseOrderByCreatedAtDesc();
}
