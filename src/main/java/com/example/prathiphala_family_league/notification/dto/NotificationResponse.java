package com.example.prathiphala_family_league.notification.dto;

import com.example.prathiphala_family_league.notification.entity.Notification;
import com.example.prathiphala_family_league.notification.entity.NotificationStatus;
import com.example.prathiphala_family_league.notification.entity.NotificationType;
import lombok.Getter;

import java.time.Instant;

@Getter
public class NotificationResponse {

    private final Long id;
    private final Long userId;
    private final NotificationType type;
    private final String subject;
    private final String body;
    private final NotificationStatus status;
    private final Instant sentAt;
    private final Instant createdAt;

    public NotificationResponse(Notification n) {
        this.id = n.getId();
        this.userId = n.getUserId();
        this.type = n.getType();
        this.subject = n.getSubject();
        this.body = n.getBody();
        this.status = n.getStatus();
        this.sentAt = n.getSentAt();
        this.createdAt = n.getCreatedAt();
    }
}
