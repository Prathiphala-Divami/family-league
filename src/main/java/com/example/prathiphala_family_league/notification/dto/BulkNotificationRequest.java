package com.example.prathiphala_family_league.notification.dto;

import com.example.prathiphala_family_league.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class BulkNotificationRequest {

    @NotEmpty(message = "At least one user ID is required")
    private List<Long> userIds;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;
}
