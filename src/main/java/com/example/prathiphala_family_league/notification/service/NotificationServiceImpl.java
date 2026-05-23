package com.example.prathiphala_family_league.notification.service;

import com.example.prathiphala_family_league.notification.dto.BulkNotificationRequest;
import com.example.prathiphala_family_league.notification.dto.NotificationResponse;
import com.example.prathiphala_family_league.notification.entity.Notification;
import com.example.prathiphala_family_league.notification.entity.NotificationStatus;
import com.example.prathiphala_family_league.notification.entity.NotificationType;
import com.example.prathiphala_family_league.notification.repository.NotificationRepository;
import com.example.prathiphala_family_league.user.entity.User;
import com.example.prathiphala_family_league.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${app.notification.from-address}")
    private String fromAddress;

    @Override
    @Transactional
    public void send(Long userId, NotificationType type, String subject, String body) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setStatus(NotificationStatus.PENDING);
        notification = notificationRepository.save(notification);

        Optional<User> userOpt = userRepository.findByIdAndDeletedFalse(userId);
        if (userOpt.isEmpty()) {
            log.warn("Notification skipped: userId={} not found or deleted", userId);
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(userOpt.get().getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
        } catch (MailException ex) {
            log.error("Failed to send email to userId={} ({}): {}", userId, userOpt.get().getEmail(), ex.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
        }

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void sendBulk(BulkNotificationRequest request) {
        request.getUserIds().forEach(userId ->
                send(userId, request.getType(), request.getSubject(), request.getBody()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAll(NotificationType type, NotificationStatus status, Pageable pageable) {
        return notificationRepository.findWithFilters(type, status, pageable)
                .map(NotificationResponse::new);
    }
}
