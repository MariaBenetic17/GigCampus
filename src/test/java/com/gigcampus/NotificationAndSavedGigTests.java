package com.gigcampus;

import com.gigcampus.entity.*;
import com.gigcampus.repository.*;
import com.gigcampus.service.AuditLogService;
import com.gigcampus.service.NotificationService;
import com.gigcampus.service.SavedGigService;
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
public class NotificationAndSavedGigTests {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SavedGigService savedGigService;

    @Autowired
    private AuditLogService auditLogService;

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
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private StudentProfile studentProfile;
    private Gig testGig;

    @BeforeEach
    void setUp() {
        testUser = new User("notif.test@test.local", passwordEncoder.encode("Pass@123"), "Test Notif User", "9999900010", Role.ROLE_STUDENT);
        userRepository.save(testUser);

        studentProfile = new StudentProfile(testUser);
        studentProfile.setCollegeName("Kongu Engineering");
        studentProfileRepository.save(studentProfile);

        User bizUser = new User("biz.notif@test.local", passwordEncoder.encode("Pass@123"), "Test Biz Notif", "9999900011", Role.ROLE_BUSINESS);
        userRepository.save(bizUser);
        BusinessProfile bp = new BusinessProfile(bizUser, "Erode Handloom Mills");
        businessProfileRepository.save(bp);

        Category cat = categoryRepository.findAll().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(new Category("Design", "design", "Graphics", "bi-palette", 1)));

        testGig = new Gig();
        testGig.setBusiness(bp);
        testGig.setCategory(cat);
        testGig.setTitle("Poster Design for Shop");
        testGig.setDescription("Festive poster design");
        testGig.setBudget(BigDecimal.valueOf(1500));
        testGig.setStatus(GigStatus.OPEN);
        gigRepository.save(testGig);
    }

    @Test
    @DisplayName("Notifications are persisted, unread count increases, and mark-as-read works")
    void testNotificationLifecycle() {
        assertEquals(0, notificationService.getUnreadCount(testUser));

        Notification n1 = notificationService.createNotification(testUser, "Application Accepted", "Your proposal was approved", "/student/projects");
        assertNotNull(n1.getId());
        assertEquals(1, notificationService.getUnreadCount(testUser));

        Notification n2 = notificationService.createNotification(testUser, "New Message", "Business sent you a message", "/chat");
        assertEquals(2, notificationService.getUnreadCount(testUser));

        // Mark first notification as read
        notificationService.markAsRead(n1.getId(), testUser.getId());
        assertEquals(1, notificationService.getUnreadCount(testUser));

        // Mark all as read
        notificationService.markAllAsRead(testUser);
        assertEquals(0, notificationService.getUnreadCount(testUser));
    }

    @Test
    @DisplayName("Student can bookmark and un-bookmark gigs (Saved Gigs)")
    void testToggleSavedGig() {
        assertFalse(savedGigService.isGigSaved(testUser.getId(), testGig.getId()));

        // Save
        boolean saved = savedGigService.toggleSaveGig(testUser.getId(), testGig.getId());
        assertTrue(saved);
        assertTrue(savedGigService.isGigSaved(testUser.getId(), testGig.getId()));
        assertEquals(1, savedGigService.getSavedGigs(testUser.getId()).size());

        // Unsave
        boolean unsaved = savedGigService.toggleSaveGig(testUser.getId(), testGig.getId());
        assertFalse(unsaved);
        assertFalse(savedGigService.isGigSaved(testUser.getId(), testGig.getId()));
        assertEquals(0, savedGigService.getSavedGigs(testUser.getId()).size());
    }

    @Test
    @DisplayName("Admin audit log records action and retrieves history")
    void testAuditLogService() {
        int initialCount = auditLogService.getAllAuditLogs().size();

        auditLogService.logAction(testUser, "TEST_ACTION", "GIG", testGig.getId(), "Test audit details", "127.0.0.1");

        List<AuditLog> logs = auditLogService.getAllAuditLogs();
        assertEquals(initialCount + 1, logs.size());
        assertEquals("TEST_ACTION", logs.get(0).getAction());
        assertEquals("GIG", logs.get(0).getTargetType());
        assertEquals(testGig.getId(), logs.get(0).getTargetId());
    }
}
