package com.gigcampus.controller;

import com.gigcampus.dto.ChatMessageDto;
import com.gigcampus.dto.SendMessageRequest;
import com.gigcampus.security.CustomUserDetails;
import com.gigcampus.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ChatMessageDto> messages = chatService.getConversationMessages(conversationId, userDetails.getId());
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChatMessageDto sent = chatService.sendMessage(userDetails.getId(), request.getConversationId(), request.getContent());
        return ResponseEntity.ok(sent);
    }
}
