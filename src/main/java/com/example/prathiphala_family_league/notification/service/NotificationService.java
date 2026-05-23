package com.example.prathiphala_family_league.notification.service;

import com.example.prathiphala_family_league.notification.dto.BulkNotificationRequest;
import com.example.prathiphala_family_league.notification.dto.NotificationResponse;
import com.example.prathiphala_family_league.notification.entity.NotificationStatus;
import com.example.prathiphala_family_league.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void send(Long userId, NotificationType type, String subject, String body);
    void sendBulk(BulkNotificationRequest request);
    Page<NotificationResponse> getAll(NotificationType type, NotificationStatus status, Pageable pageable);
}
