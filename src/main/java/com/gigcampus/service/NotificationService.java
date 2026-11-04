package com.gigcampus.service;

import com.gigcampus.dto.NotificationDto;
import com.gigcampus.entity.Notification;
import com.gigcampus.entity.User;
import com.gigcampus.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               @Lazy SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Notification createNotification(User recipient, String title, String message, String linkUrl) {
        Notification notification = new Notification(recipient, title, message, linkUrl);
        Notification saved = notificationRepository.save(notification);

        try {
            long unreadCount = notificationRepository.countByRecipientAndReadFalse(recipient);
            NotificationDto dto = new NotificationDto(
                    saved.getId(),
                    saved.getTitle(),
                    saved.getMessage(),
                    saved.getLinkUrl(),
                    saved.isRead(),
                    "Just now",
                    unreadCount
            );

            // Real-time broadcast to recipient's private topic
            messagingTemplate.convertAndSend("/topic/notifications." + recipient.getId(), dto);

            // Also send to user queue if authenticated via user destination
            if (recipient.getEmail() != null) {
                messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/notifications", dto);
            }
        } catch (Exception ex) {
            log.warn("GigCampus: Failed to broadcast WebSocket notification: {}", ex.getMessage());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(User user) {
        return notificationRepository.findTop10ByRecipientOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getRecipient().getId().equals(userId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    public void markAllAsRead(User user) {
        List<Notification> list = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(list);
    }

    public static String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        if (seconds < 60) return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }
}
