package com.gigcampus.controller;

import com.gigcampus.dto.ChatMessageDto;
import com.gigcampus.entity.Conversation;
import com.gigcampus.security.CustomUserDetails;
import com.gigcampus.service.ChatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ChatWebController {

    private final ChatService chatService;

    public ChatWebController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public String chatPage(
            @RequestParam(required = false) Long conversationId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        List<Conversation> conversations = chatService.getUserConversations(userDetails.getId());
        model.addAttribute("conversations", conversations);

        Conversation activeConversation = null;
        List<ChatMessageDto> messages = List.of();

        if (conversationId != null) {
            try {
                activeConversation = chatService.getConversationById(conversationId, userDetails.getId());
                messages = chatService.getConversationMessages(conversationId, userDetails.getId());
            } catch (Exception ignored) {
            }
        } else if (!conversations.isEmpty()) {
            activeConversation = conversations.get(0);
            messages = chatService.getConversationMessages(activeConversation.getId(), userDetails.getId());
        }

        model.addAttribute("activeConversation", activeConversation);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUserId", userDetails.getId());
        model.addAttribute("currentUserFullName", userDetails.getUser().getFullName());

        return "common/chat";
    }

    @GetMapping("/chat/start/application/{applicationId}")
    public String startChatForApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Conversation conversation = chatService.getOrCreateConversationForApplication(applicationId, userDetails.getId());
        return "redirect:/chat?conversationId=" + conversation.getId();
    }

    @GetMapping("/chat/start/direct")
    public String startDirectChat(
            @RequestParam Long gigId,
            @RequestParam Long studentUserId,
            @RequestParam Long businessUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Conversation conversation = chatService.getOrCreateDirectConversation(gigId, studentUserId, businessUserId, userDetails.getId());
        return "redirect:/chat?conversationId=" + conversation.getId();
    }
}
