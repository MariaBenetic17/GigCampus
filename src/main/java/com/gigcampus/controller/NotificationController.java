package com.gigcampus.controller;

import com.gigcampus.entity.Notification;
import com.gigcampus.security.CustomUserDetails;
import com.gigcampus.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String viewNotifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Notification> notifications = notificationService.getUserNotifications(userDetails.getUser());
        model.addAttribute("notifications", notifications);
        return "common/notifications";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAsRead(id, userDetails.getId());
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUser());
        return "redirect:/notifications";
    }
}
