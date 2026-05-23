package com.example.prathiphala_family_league.notification.controller;

import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.common.response.PagedResponse;
import com.example.prathiphala_family_league.notification.dto.BulkNotificationRequest;
import com.example.prathiphala_family_league.notification.dto.NotificationResponse;
import com.example.prathiphala_family_league.notification.entity.NotificationStatus;
import com.example.prathiphala_family_league.notification.entity.NotificationType;
import com.example.prathiphala_family_league.notification.service.NotificationService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notifications", description = "Send bulk notifications and view notification history")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('SEND_NOTIFICATION')")
    public ResponseEntity<ApiResponse<Void>> sendBulk(@Valid @RequestBody BulkNotificationRequest request) {
        notificationService.sendBulk(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SEND_NOTIFICATION')")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getAll(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                new PagedResponse<>(notificationService.getAll(type, status, pageable))));
    }
}
