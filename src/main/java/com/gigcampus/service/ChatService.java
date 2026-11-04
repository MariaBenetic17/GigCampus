package com.gigcampus.service;

import com.gigcampus.dto.ChatMessageDto;
import com.gigcampus.entity.*;
import com.gigcampus.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a, dd MMM");

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final GigRepository gigRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ConversationRepository conversationRepository,
                       ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       ApplicationRepository applicationRepository,
                       GigRepository gigRepository,
                       NotificationService notificationService,
                       SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.gigRepository = gigRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    public Conversation getOrCreateConversationForApplication(Long applicationId, Long currentUserId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        User studentUser = application.getStudent().getUser();
        User businessUser = application.getGig().getBusiness().getUser();

        if (!currentUserId.equals(studentUser.getId()) && !currentUserId.equals(businessUser.getId())) {
            throw new SecurityException("Unauthorized: You are not a participant in this gig proposal");
        }

        Optional<Conversation> existing = conversationRepository.findByApplicationId(applicationId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation conversation = new Conversation(
                application.getGig(),
                application,
                "Discussion: " + application.getGig().getTitle()
        );
        conversation.addParticipant(studentUser);
        conversation.addParticipant(businessUser);

        return conversationRepository.save(conversation);
    }

    public Conversation getOrCreateDirectConversation(Long gigId, Long studentUserId, Long businessUserId, Long currentUserId) {
        if (!currentUserId.equals(studentUserId) && !currentUserId.equals(businessUserId)) {
            throw new SecurityException("Unauthorized: You must be a conversation participant");
        }

        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));

        User student = userRepository.findById(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        User business = userRepository.findById(businessUserId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        Optional<Conversation> existing = conversationRepository.findByGigIdAndParticipants(gigId, studentUserId, businessUserId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation conversation = new Conversation(gig, null, "Inquiry: " + gig.getTitle());
        conversation.addParticipant(student);
        conversation.addParticipant(business);
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findByParticipantUserId(userId);
    }

    @Transactional(readOnly = true)
    public Conversation getConversationById(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        boolean isMember = conversation.getParticipants().stream().anyMatch(u -> u.getId().equals(userId));
        if (!isMember) {
            throw new SecurityException("Unauthorized: You do not have access to this conversation");
        }

        return conversation;
    }

    public List<ChatMessageDto> getConversationMessages(Long conversationId, Long currentUserId) {
        Conversation conversation = getConversationById(conversationId, currentUserId);
        List<ChatMessage> messages = chatMessageRepository.findByConversationOrderBySentAtAsc(conversation);

        // Mark incoming messages as read
        for (ChatMessage msg : messages) {
            if (!msg.getSender().getId().equals(currentUserId) && !msg.isRead()) {
                msg.setRead(true);
            }
        }
        chatMessageRepository.saveAll(messages);

        List<ChatMessageDto> dtos = new ArrayList<>();
        for (ChatMessage msg : messages) {
            dtos.add(mapToDto(msg, currentUserId));
        }
        return dtos;
    }

    public ChatMessageDto sendMessage(Long senderUserId, Long conversationId, String rawContent) {
        Conversation conversation = getConversationById(conversationId, senderUserId);
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (rawContent == null || rawContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        // Sanitize content against XSS
        String sanitizedContent = HtmlUtils.htmlEscape(rawContent.trim());

        ChatMessage message = new ChatMessage(conversation, sender, sanitizedContent);
        ChatMessage saved = chatMessageRepository.save(message);

        // Update conversation timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessageDto dto = mapToDto(saved, senderUserId);

        // 1. Broadcast to conversation STOMP topic for instant live chat delivery
        String destination = "/topic/conversation." + conversationId;
        messagingTemplate.convertAndSend(destination, dto);

        // 2. Identify the other participant to push in-app notification
        User recipient = conversation.getParticipants().stream()
                .filter(u -> !u.getId().equals(senderUserId))
                .findFirst()
                .orElse(null);

        if (recipient != null) {
            notificationService.createNotification(
                    recipient,
                    "New Message from " + sender.getFullName(),
                    sanitizedContent.length() > 60 ? sanitizedContent.substring(0, 57) + "..." : sanitizedContent,
                    "/chat?conversationId=" + conversationId
            );
        }

        return dto;
    }

    private ChatMessageDto mapToDto(ChatMessage msg, Long currentUserId) {
        boolean isMine = msg.getSender().getId().equals(currentUserId);
        String formattedTime = msg.getSentAt() != null ? msg.getSentAt().format(TIME_FORMATTER) : "Just now";
        return new ChatMessageDto(
                msg.getId(),
                msg.getConversation().getId(),
                msg.getSender().getId(),
                msg.getSender().getFullName(),
                msg.getSender().getRole().name().replace("ROLE_", ""),
                msg.getContent(),
                formattedTime,
                isMine
        );
    }
}
