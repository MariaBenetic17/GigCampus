package com.gigcampus.controller;

import com.gigcampus.dto.NotificationDto;
import com.gigcampus.entity.Notification;
import com.gigcampus.security.CustomUserDetails;
import com.gigcampus.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationService notificationService;

    public NotificationApiController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getUser());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<NotificationDto>> getRecentNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Notification> list = notificationService.getRecentNotifications(userDetails.getUser());
        long unreadCount = notificationService.getUnreadCount(userDetails.getUser());
        List<NotificationDto> dtos = new ArrayList<>();
        for (Notification n : list) {
            dtos.add(new NotificationDto(
                    n.getId(),
                    n.getTitle(),
                    n.getMessage(),
                    n.getLinkUrl(),
                    n.isRead(),
                    NotificationService.formatTimeAgo(n.getCreatedAt()),
                    unreadCount
            ));
        }
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAsRead(id, userDetails.getId());
        long unread = notificationService.getUnreadCount(userDetails.getUser());
        return ResponseEntity.ok(Map.of("success", true, "unreadCount", unread));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUser());
        return ResponseEntity.ok(Map.of("success", true, "unreadCount", 0));
    }
}
