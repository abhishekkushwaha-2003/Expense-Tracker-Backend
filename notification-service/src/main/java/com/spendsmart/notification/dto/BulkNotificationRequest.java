package com.spendsmart.notification.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class BulkNotificationRequest {

    @NotEmpty
    @Valid
    private List<NotificationRequest> notifications;
}
