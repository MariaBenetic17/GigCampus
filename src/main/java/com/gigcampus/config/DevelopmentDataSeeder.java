package com.gigcampus.config;

import com.gigcampus.entity.*;
import com.gigcampus.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Order(2)
public class DevelopmentDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentDataSeeder.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final CategoryRepository categoryRepository;
    private final GigRepository gigRepository;
    private final ApplicationRepository applicationRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SavedGigRepository savedGigRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.dev.seed-sample-data:true}")
    private boolean seedEnabled;

    public DevelopmentDataSeeder(UserRepository userRepository,
                                 StudentProfileRepository studentProfileRepository,
                                 BusinessProfileRepository businessProfileRepository,
                                 CategoryRepository categoryRepository,
                                 GigRepository gigRepository,
                                 ApplicationRepository applicationRepository,
                                 ConversationRepository conversationRepository,
                                 ChatMessageRepository chatMessageRepository,
                                 SavedGigRepository savedGigRepository,
                                 NotificationRepository notificationRepository,
                                 AuditLogRepository auditLogRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.categoryRepository = categoryRepository;
        this.gigRepository = gigRepository;
        this.applicationRepository = applicationRepository;
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.savedGigRepository = savedGigRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        // Ensure categories exist
        seedCategoriesIfEmpty();

        // Check if sample businesses exist
        if (businessProfileRepository.count() == 0) {
            seedSampleDevelopmentData();
        }
    }

    private void seedCategoriesIfEmpty() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Website Development", "website-development", "Modern business websites, landing pages, and portfolio sites", "bi-globe", 1));
            categoryRepository.save(new Category("Graphic & Poster Design", "graphic-design", "Promotional posters, social media banners, and shop branding", "bi-palette", 2));
            categoryRepository.save(new Category("Social Media & Marketing", "social-media-marketing", "Instagram Reels, Google Maps setup, and digital promotions", "bi-megaphone", 3));
            categoryRepository.save(new Category("Excel & Data Organization", "data-organization", "Inventory sheets, billing spreadsheets, and data cleaning", "bi-file-earmark-spreadsheet", 4));
            categoryRepository.save(new Category("Software & Billing Assistance", "software-billing", "Basic desktop tools, GST billing setup, and barcode labels", "bi-cpu", 5));
            categoryRepository.save(new Category("Content & Translation", "content-translation", "Tamil & English content, product descriptions, and catalogs", "bi-translate", 6));
            log.info("GigCampus: Seeded default categories.");
        }
    }

    private void seedSampleDevelopmentData() {
        log.info("GigCampus: Seeding clearly labelled fictional development data for local demonstration...");

        String defaultHash = passwordEncoder.encode("Student@123");
        String bizHash = passwordEncoder.encode("Business@123");

        // Fictional Student 1: Priya - Kongu Engineering College
        User studentUser1 = new User("priya.erode@kongu.edu.in", defaultHash, "Priya Sundaram", "9842100001", Role.ROLE_STUDENT);
        userRepository.save(studentUser1);
        StudentProfile sp1 = new StudentProfile(studentUser1);
        sp1.setCollegeName("Kongu Engineering College, Perundurai");
        sp1.setDepartment("Computer Science and Engineering");
        sp1.setYearOfStudy("Final Year");
        sp1.setSkills("HTML5, CSS3, JavaScript, Canva, WordPress, Poster Design");
        sp1.setLocation("Perundurai, Erode");
        sp1.setBio("Final year CSE student passionate about responsive web design and creating clean promotional graphics for local retail brands.");
        sp1.setPortfolioUrl("https://github.com/example/priya-portfolio");
        sp1.setRatingAverage(BigDecimal.valueOf(4.8));
        sp1.setReviewsCount(3);
        studentProfileRepository.save(sp1);

        // Fictional Student 2: Karthik - Bannari Amman Institute
        User studentUser2 = new User("karthik.bannari@bitsathy.ac.in", defaultHash, "Karthik Raja", "9842100002", Role.ROLE_STUDENT);
        userRepository.save(studentUser2);
        StudentProfile sp2 = new StudentProfile(studentUser2);
        sp2.setCollegeName("Bannari Amman Institute of Technology, Sathyamangalam");
        sp2.setDepartment("Information Technology");
        sp2.setYearOfStudy("Third Year");
        sp2.setSkills("Excel VBA, Google Sheets, Python, GST Billing, SQL");
        sp2.setLocation("Sathyamangalam / Erode");
        sp2.setBio("IT undergraduate with deep proficiency in inventory data automation, formula auditing, and spreadsheet dashboards.");
        studentProfileRepository.save(sp2);

        // Fictional Business 1: Kaveri Handlooms (Textiles)
        User bizUser1 = new User("kaveri.handlooms@sample-erode.local", bizHash, "S. Muthuswamy", "9842200001", Role.ROLE_BUSINESS);
        userRepository.save(bizUser1);
        BusinessProfile bp1 = new BusinessProfile(bizUser1, "Kaveri Handlooms & Textile Traders");
        bp1.setBusinessCategory("Textiles & Garments");
        bp1.setLocation("Brough Road, Erode");
        bp1.setDescription("Wholesale and retail distributor of Erode cotton bedsheets, lungis, and traditional handloom fabrics.");
        bp1.setContactEmail("contact@kaveri-handlooms.local");
        bp1.setContactPhone("9842200001");
        bp1.setGstinOrRegNumber("33ABCDE1234F1Z5");
        bp1.setVerificationStatus(VerificationStatus.VERIFIED);
        bp1.setVerifiedAt(LocalDateTime.now().minusDays(10));
        businessProfileRepository.save(bp1);

        // Fictional Business 2: Sri Krishna Powerloom Spares
        User bizUser2 = new User("krishna.spares@sample-erode.local", bizHash, "K. Rangarajan", "9842200002", Role.ROLE_BUSINESS);
        bizUser2.setActive(true);
        userRepository.save(bizUser2);
        BusinessProfile bp2 = new BusinessProfile(bizUser2, "Sri Krishna Powerloom Spares & Machinery");
        bp2.setBusinessCategory("Powerloom & Machinery");
        bp2.setLocation("Veerappanchatram, Erode");
        bp2.setDescription("Machinery parts and loom spare parts supplier serving over 150 weaving units in the Erode industrial cluster.");
        bp2.setContactEmail("sales@krishna-spares.local");
        bp2.setContactPhone("9842200002");
        bp2.setGstinOrRegNumber("33FGHIJ5678K2Z9");
        bp2.setVerificationStatus(VerificationStatus.PENDING_REVIEW);
        bp2.setVerificationNotes("Submitted shop registration certificate for review.");
        businessProfileRepository.save(bp2);

        // Sample Gigs
        Category webCat = categoryRepository.findBySlug("website-development").orElse(null);
        Category posterCat = categoryRepository.findBySlug("graphic-design").orElse(null);
        Category dataCat = categoryRepository.findBySlug("data-organization").orElse(null);
        Category socialCat = categoryRepository.findBySlug("social-media-marketing").orElse(null);

        Gig gig1 = null;
        Application app1 = null;

        if (webCat != null) {
            gig1 = new Gig();
            gig1.setBusiness(bp1);
            gig1.setCategory(webCat);
            gig1.setTitle("5-Page Responsive Showcase Website for Textile Wholesale Shop");
            gig1.setDescription("We need a responsive, mobile-friendly showcase website with Home, Fabric Catalog, Bulk Orders Inquiry Form, About Our Erode Mills, and Google Map Location. Photos of fabrics and text details will be provided.");
            gig1.setRequiredSkills("HTML5, Bootstrap, CSS, Responsive Web Design");
            gig1.setBudget(BigDecimal.valueOf(4500.00));
            gig1.setDeadline(LocalDate.now().plusDays(14));
            gig1.setWorkMode(WorkMode.HYBRID);
            gig1.setLocation("Brough Road, Erode");
            gig1.setStatus(GigStatus.OPEN);
            gig1.setViewsCount(42);
            gigRepository.save(gig1);

            // Sample Application on gig1 by student 1
            app1 = new Application(
                    gig1,
                    sp1,
                    "Respected Sir, I am a final year CSE student at Kongu Engineering College. I have built 4 responsive websites using HTML, CSS, and Bootstrap. I can finish your 5-page fabric showcase in 7 days with mobile optimization.",
                    7,
                    BigDecimal.valueOf(4000.00)
            );
            applicationRepository.save(app1);

            // Seed Sample Live Chat Conversation between Student 1 and Business 1
            Conversation conv1 = new Conversation(gig1, app1, "Discussion: " + gig1.getTitle());
            conv1.addParticipant(studentUser1);
            conv1.addParticipant(bizUser1);
            conversationRepository.save(conv1);

            ChatMessage msg1 = new ChatMessage(conv1, studentUser1, "Hello Sir, I submitted my proposal for your fabric showcase website. When would you like to review sample designs?");
            chatMessageRepository.save(msg1);

            ChatMessage msg2 = new ChatMessage(conv1, bizUser1, "Vanakkam Priya! We saw your portfolio from Kongu. Could you send 2 color palette ideas for our handlooms showroom?");
            chatMessageRepository.save(msg2);

            // Seed Saved Gig for Student 1
            savedGigRepository.save(new SavedGig(sp1, gig1));
        }

        if (posterCat != null) {
            Gig gig2 = new Gig();
            gig2.setBusiness(bp1);
            gig2.setCategory(posterCat);
            gig2.setTitle("Social Media Promotional Posters for Pongal / Festive Sale");
            gig2.setDescription("Looking for a creative student to design 6 festive promotional banners for Instagram, Facebook, and WhatsApp status highlighting our discount bundles on pure cotton sheets.");
            gig2.setRequiredSkills("Canva, Photoshop / Illustrator, Poster Design");
            gig2.setBudget(BigDecimal.valueOf(1500.00));
            gig2.setDeadline(LocalDate.now().plusDays(7));
            gig2.setWorkMode(WorkMode.REMOTE);
            gig2.setLocation("Erode");
            gig2.setStatus(GigStatus.OPEN);
            gig2.setViewsCount(28);
            gigRepository.save(gig2);

            // Save gig2 as bookmark for Student 2
            savedGigRepository.save(new SavedGig(sp2, gig2));
        }

        if (dataCat != null) {
            Gig gig3 = new Gig();
            gig3.setBusiness(bp2);
            gig3.setCategory(dataCat);
            gig3.setTitle("Weaving Spares Catalog Inventory Spreadsheet with Formula Automation");
            gig3.setDescription("We have 350+ loom spare parts listed on paper notebooks and old text files. Need a student to organize them into an Excel workbook with categories, part numbers, standard prices, and automatic stock alerts.");
            gig3.setRequiredSkills("Microsoft Excel, Google Sheets, VLOOKUP, Inventory Sheets");
            gig3.setBudget(BigDecimal.valueOf(2500.00));
            gig3.setDeadline(LocalDate.now().plusDays(10));
            gig3.setWorkMode(WorkMode.HYBRID);
            gig3.setLocation("Veerappanchatram, Erode");
            gig3.setStatus(GigStatus.OPEN);
            gig3.setViewsCount(19);
            gigRepository.save(gig3);

            // Sample Application on gig3 by student 2
            Application app2 = new Application(
                    gig3,
                    sp2,
                    "Hello Sir, I have strong experience building structured inventory spreadsheets in Excel with conditional formatting and stock alerts. I study at Bannari Amman and can deliver this accurately in 4 days.",
                    4,
                    BigDecimal.valueOf(2500.00)
            );
            applicationRepository.save(app2);
        }

        if (socialCat != null) {
            Gig gig4 = new Gig();
            gig4.setBusiness(bp2);
            gig4.setCategory(socialCat);
            gig4.setTitle("Google Business Profile Verification & Local SEO Setup in Erode");
            gig4.setDescription("Need assistance claiming and fully setting up our Google Maps shop listing, adding high-resolution photos of our machinery showroom, business hours, and generating a review link.");
            gig4.setRequiredSkills("Google Maps / GBP, Local SEO, Basic Photography");
            gig4.setBudget(BigDecimal.valueOf(1200.00));
            gig4.setDeadline(LocalDate.now().plusDays(5));
            gig4.setWorkMode(WorkMode.ON_SITE);
            gig4.setLocation("Veerappanchatram, Erode");
            gig4.setStatus(GigStatus.OPEN);
            gig4.setViewsCount(15);
            gigRepository.save(gig4);
        }

        // Seed Sample Notifications
        Notification n1 = new Notification(
                studentUser1,
                "Welcome to GigCampus! 🎓",
                "Your student profile is active. Browse open gigs from local Erode shops and start earning practical experience.",
                "/gigs"
        );
        notificationRepository.save(n1);

        Notification n2 = new Notification(
                bizUser1,
                "New Student Proposal Received",
                "Priya Sundaram submitted a proposal for '5-Page Responsive Showcase Website'.",
                "/business/dashboard"
        );
        notificationRepository.save(n2);

        // Seed Sample Audit Log
        AuditLog audit1 = new AuditLog(
                null,
                "SYSTEM_INITIALIZE",
                "PLATFORM",
                1L,
                "GigCampus demo cluster initialized with Erode regional seed data.",
                "127.0.0.1"
        );
        auditLogRepository.save(audit1);

        log.info("GigCampus: Fictional development seed data successfully created.");
    }
}
