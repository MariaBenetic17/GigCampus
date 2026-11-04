package com.gigcampus.controller;

import com.gigcampus.entity.User;
import com.gigcampus.security.CustomUserDetails;
import com.gigcampus.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    public GlobalControllerAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            return userDetails.getUser();
        }
        return null;
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            return notificationService.getUnreadCount(userDetails.getUser());
        }
        return 0;
    }
}
