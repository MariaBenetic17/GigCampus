package com.gigcampus;

import com.gigcampus.dto.ApplicationDto;
import com.gigcampus.dto.BusinessRegistrationDto;
import com.gigcampus.dto.GigDto;
import com.gigcampus.dto.ReviewDto;
import com.gigcampus.dto.StudentRegistrationDto;
import com.gigcampus.entity.*;
import com.gigcampus.repository.CategoryRepository;
import com.gigcampus.service.ApplicationService;
import com.gigcampus.service.GigService;
import com.gigcampus.service.ReviewService;
import com.gigcampus.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class GigAndApplicationServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private GigService gigService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Complete Gig Lifecycle: Post -> Apply -> Prevent Duplicate -> Accept -> Complete -> Review")
    void testCompleteGigLifecycle() {
        // 1. Register Business
        BusinessRegistrationDto bizDto = new BusinessRegistrationDto();
        bizDto.setFullName("K. Loganathan");
        bizDto.setEmail("loga.traders@erode.local");
        bizDto.setPassword("Pass@12345");
        bizDto.setConfirmPassword("Pass@12345");
        bizDto.setPhone("9842500001");
        bizDto.setBusinessName("Loganathan Turmeric Trading Co.");
        bizDto.setBusinessCategory("Food, Dairy & Agriculture");
        bizDto.setLocation("Semmampalayam, Erode");
        User bizUser = userService.registerBusiness(bizDto);

        // 2. Register Student
        StudentRegistrationDto studDto = new StudentRegistrationDto();
        studDto.setFullName("Deepa Mohan");
        studDto.setEmail("deepa.kongu@erode.local");
        studDto.setPassword("Pass@12345");
        studDto.setConfirmPassword("Pass@12345");
        studDto.setPhone("9842500002");
        studDto.setCollegeName("Kongu Engineering College");
        studDto.setDepartment("Information Technology");
        studDto.setYearOfStudy("Final Year");
        studDto.setSkills("HTML, CSS, Web Design");
        User studUser = userService.registerStudent(studDto);

        // 3. Post a Gig
        Category cat = categoryRepository.findAll().get(0);
        GigDto gigDto = new GigDto();
        gigDto.setTitle("Turmeric Export Product Catalog Webpage");
        gigDto.setCategoryId(cat.getId());
        gigDto.setDescription("Need a clean single-page showcase with grade specifications of Erode Turmeric.");
        gigDto.setRequiredSkills("HTML, Bootstrap, CSS");
        gigDto.setBudget(BigDecimal.valueOf(3000.00));
        gigDto.setDeadline(LocalDate.now().plusDays(10));
        gigDto.setWorkMode(WorkMode.REMOTE);
        gigDto.setLocation("Erode");
        gigDto.setStatus(GigStatus.OPEN);

        Gig postedGig = gigService.createGig(bizUser.getId(), gigDto);
        assertNotNull(postedGig.getId());
        assertEquals(GigStatus.OPEN, postedGig.getStatus());

        // 4. Student Applies for the Gig
        ApplicationDto appDto = new ApplicationDto();
        appDto.setGigId(postedGig.getId());
        appDto.setProposal("I am ready to build your turmeric catalog with responsive tables and export standards in 5 days.");
        appDto.setEstimatedDays(5);
        appDto.setProposedBudget(BigDecimal.valueOf(2800.00));

        Application application = applicationService.applyForGig(studUser.getId(), appDto);
        assertNotNull(application.getId());
        assertEquals(ApplicationStatus.PENDING, application.getStatus());

        // 5. Test Duplicate Application Prevention
        assertThrows(IllegalStateException.class, () -> applicationService.applyForGig(studUser.getId(), appDto),
                "Student must not be allowed to apply more than once to the same gig");

        // 6. Business Accepts Application -> Gig transitions to IN_PROGRESS
        applicationService.updateApplicationStatus(bizUser.getId(), application.getId(), ApplicationStatus.ACCEPTED, "Accepted. Please proceed!");
        Gig updatedGig = gigService.getGigById(postedGig.getId());
        assertEquals(GigStatus.IN_PROGRESS, updatedGig.getStatus(), "Gig status should transition to IN_PROGRESS upon acceptance");

        // 7. Another student tries to apply while IN_PROGRESS -> must be rejected
        StudentRegistrationDto stud2Dto = new StudentRegistrationDto();
        stud2Dto.setFullName("Student Two");
        stud2Dto.setEmail("student2@erode.local");
        stud2Dto.setPassword("Pass@12345");
        stud2Dto.setConfirmPassword("Pass@12345");
        stud2Dto.setCollegeName("IRTT");
        stud2Dto.setDepartment("CSE");
        User stud2 = userService.registerStudent(stud2Dto);

        ApplicationDto lateApp = new ApplicationDto();
        lateApp.setGigId(postedGig.getId());
        lateApp.setProposal("Late application test");
        lateApp.setEstimatedDays(3);
        assertThrows(IllegalStateException.class, () -> applicationService.applyForGig(stud2.getId(), lateApp));

        // 8. Business Marks Work Completed
        applicationService.markWorkCompleted(bizUser.getId(), application.getId());
        Gig completedGig = gigService.getGigById(postedGig.getId());
        assertEquals(GigStatus.COMPLETED, completedGig.getStatus());

        // 9. Review: Business Reviews Student
        ReviewDto revDto = new ReviewDto();
        revDto.setGigId(completedGig.getId());
        revDto.setRecipientId(studUser.getId());
        revDto.setRating(5);
        revDto.setComment("Deepa built a clean catalog on time. Highly recommended!");

        Review review = reviewService.submitReview(bizUser.getId(), revDto);
        assertNotNull(review.getId());
        assertEquals(5, review.getRating());

        // Verify student rating recalculated
        var spOpt = userService.getStudentProfileByUserId(studUser.getId());
        assertTrue(spOpt.isPresent());
        assertTrue(spOpt.get().getRatingAverage().doubleValue() > 0.0);
    }

    @Test
    @DisplayName("Unauthorized user cannot modify another business's gig")
    void testGigOwnershipProtection() {
        BusinessRegistrationDto biz1 = new BusinessRegistrationDto();
        biz1.setFullName("Owner One");
        biz1.setEmail("owner1@erode.local");
        biz1.setPassword("Pass@12345");
        biz1.setConfirmPassword("Pass@12345");
        biz1.setBusinessName("Shop One");
        biz1.setBusinessCategory("Retail & Supermarket");
        biz1.setLocation("Erode");
        User user1 = userService.registerBusiness(biz1);

        BusinessRegistrationDto biz2 = new BusinessRegistrationDto();
        biz2.setFullName("Owner Two");
        biz2.setEmail("owner2@erode.local");
        biz2.setPassword("Pass@12345");
        biz2.setConfirmPassword("Pass@12345");
        biz2.setBusinessName("Shop Two");
        biz2.setBusinessCategory("Retail & Supermarket");
        biz2.setLocation("Erode");
        User user2 = userService.registerBusiness(biz2);

        Category cat = categoryRepository.findAll().get(0);
        GigDto gigDto = new GigDto();
        gigDto.setTitle("Owner One Original Gig");
        gigDto.setCategoryId(cat.getId());
        gigDto.setDescription("Initial requirements description here.");
        gigDto.setRequiredSkills("Canva");
        gigDto.setBudget(BigDecimal.valueOf(1000.00));
        gigDto.setWorkMode(WorkMode.REMOTE);
        Gig gig = gigService.createGig(user1.getId(), gigDto);

        // User 2 attempts to edit User 1's gig -> SecurityException
        gigDto.setTitle("Hacked Title Attempt");
        assertThrows(SecurityException.class, () -> gigService.updateGig(user2.getId(), gig.getId(), gigDto));
    }
}
