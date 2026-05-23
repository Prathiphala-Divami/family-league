package com.example.prathiphala_family_league.notification.repository;

import com.example.prathiphala_family_league.notification.entity.Notification;
import com.example.prathiphala_family_league.notification.entity.NotificationStatus;
import com.example.prathiphala_family_league.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Idempotency check — prevents sending the same notification twice.
    boolean existsByUserIdAndTypeAndSubjectAndStatus(Long userId, NotificationType type,
                                                     String subject, NotificationStatus status);

    // Admin history with optional filters — nulls are treated as "no filter".
    @Query("SELECT n FROM Notification n WHERE n.deleted = false " +
           "AND (:type IS NULL OR n.type = :type) " +
           "AND (:status IS NULL OR n.status = :status)")
    Page<Notification> findWithFilters(@Param("type") NotificationType type,
                                       @Param("status") NotificationStatus status,
                                       Pageable pageable);
}
