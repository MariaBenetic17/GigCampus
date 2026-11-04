package com.gigcampus.controller;

import com.gigcampus.dto.SendMessageRequest;
import com.gigcampus.entity.User;
import com.gigcampus.service.ChatService;
import com.gigcampus.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatService chatService;
    private final UserService userService;

    public ChatWebSocketController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload SendMessageRequest request, Principal principal) {
        if (principal == null) {
            log.warn("GigCampus LiveChat: Rejected unauthenticated WebSocket message");
            return;
        }

        User sender = userService.findByEmail(principal.getName()).orElse(null);
        if (sender == null) {
            log.warn("GigCampus LiveChat: User not found for principal {}", principal.getName());
            return;
        }

        try {
            chatService.sendMessage(sender.getId(), request.getConversationId(), request.getContent());
        } catch (Exception ex) {
            log.error("GigCampus LiveChat: Error sending message via WebSocket: {}", ex.getMessage());
        }
    }
}
