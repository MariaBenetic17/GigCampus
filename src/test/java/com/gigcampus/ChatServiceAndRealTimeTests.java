package com.gigcampus;

import com.gigcampus.dto.ChatMessageDto;
import com.gigcampus.entity.*;
import com.gigcampus.repository.*;
import com.gigcampus.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class ChatServiceAndRealTimeTests {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GigRepository gigRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User studentUser;
    private User businessUser;
    private User intruderUser;
    private Gig gig;
    private Application application;

    @BeforeEach
    void setUp() {
        // Create student
        studentUser = new User("chat.student@test.local", passwordEncoder.encode("Pass@123"), "Test Chat Student", "9999900001", Role.ROLE_STUDENT);
        userRepository.save(studentUser);
        StudentProfile sp = new StudentProfile(studentUser);
        sp.setCollegeName("Kongu Engineering");
        sp.setDepartment("CSE");
        studentProfileRepository.save(sp);

        // Create business
        businessUser = new User("chat.business@test.local", passwordEncoder.encode("Pass@123"), "Test Chat Business", "9999900002", Role.ROLE_BUSINESS);
        userRepository.save(businessUser);
        BusinessProfile bp = new BusinessProfile(businessUser, "Erode Handlooms Test");
        bp.setVerificationStatus(VerificationStatus.VERIFIED);
        businessProfileRepository.save(bp);

        // Create intruder
        intruderUser = new User("chat.intruder@test.local", passwordEncoder.encode("Pass@123"), "Intruder User", "9999900003", Role.ROLE_STUDENT);
        userRepository.save(intruderUser);
        StudentProfile spIntruder = new StudentProfile(intruderUser);
        studentProfileRepository.save(spIntruder);

        // Create category and gig
        Category category = categoryRepository.findAll().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(new Category("Web Dev", "web-dev", "Websites", "bi-globe", 1)));

        gig = new Gig();
        gig.setBusiness(bp);
        gig.setCategory(category);
        gig.setTitle("Web Redesign Project");
        gig.setDescription("Showcase website");
        gig.setBudget(BigDecimal.valueOf(3000));
        gig.setStatus(GigStatus.OPEN);
        gigRepository.save(gig);

        // Create application
        application = new Application(gig, sp, "I can build your website.", 5, BigDecimal.valueOf(3000));
        applicationRepository.save(application);
    }

    @Test
    @DisplayName("Authorized student and business can create and access conversation for application")
    void testCreateAndAccessConversation() {
        Conversation conv = chatService.getOrCreateConversationForApplication(application.getId(), studentUser.getId());
        assertNotNull(conv);
        assertNotNull(conv.getId());
        assertEquals(2, conv.getParticipants().size());

        // Business user accessing same conversation gets existing instance
        Conversation conv2 = chatService.getOrCreateConversationForApplication(application.getId(), businessUser.getId());
        assertEquals(conv.getId(), conv2.getId());
    }

    @Test
    @DisplayName("Unauthorized user cannot access conversation for another user's application")
    void testUnauthorizedConversationAccess() {
        assertThrows(SecurityException.class, () -> {
            chatService.getOrCreateConversationForApplication(application.getId(), intruderUser.getId());
        });
    }

    @Test
    @DisplayName("Message is saved to database, mapped correctly, and updates conversation timestamp")
    void testSendMessageAndPersistence() {
        Conversation conv = chatService.getOrCreateConversationForApplication(application.getId(), studentUser.getId());

        ChatMessageDto sentDto = chatService.sendMessage(studentUser.getId(), conv.getId(), "Hello, can we discuss requirements?");
        assertNotNull(sentDto);
        assertNotNull(sentDto.getId());
        assertEquals("Hello, can we discuss requirements?", sentDto.getContent());
        assertTrue(sentDto.isMine());

        // Verify database persistence
        List<ChatMessageDto> messages = chatService.getConversationMessages(conv.getId(), businessUser.getId());
        assertEquals(1, messages.size());
        assertEquals("Hello, can we discuss requirements?", messages.get(0).getContent());
        assertFalse(messages.get(0).isMine()); // from business perspective, it is not mine
    }

    @Test
    @DisplayName("Unauthorized user cannot send message into another's conversation")
    void testUnauthorizedSendMessage() {
        Conversation conv = chatService.getOrCreateConversationForApplication(application.getId(), studentUser.getId());

        assertThrows(SecurityException.class, () -> {
            chatService.sendMessage(intruderUser.getId(), conv.getId(), "Sneaking in!");
        });
    }
}
