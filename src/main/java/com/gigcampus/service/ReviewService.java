package com.gigcampus.service;

import com.gigcampus.dto.ReviewDto;
import com.gigcampus.entity.*;
import com.gigcampus.repository.ApplicationRepository;
import com.gigcampus.repository.GigRepository;
import com.gigcampus.repository.ReviewRepository;
import com.gigcampus.repository.StudentProfileRepository;
import com.gigcampus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GigRepository gigRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository,
                         GigRepository gigRepository,
                         UserRepository userRepository,
                         ApplicationRepository applicationRepository,
                         StudentProfileRepository studentProfileRepository,
                         NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.gigRepository = gigRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.notificationService = notificationService;
    }

    public Review submitReview(Long reviewerUserId, ReviewDto dto) {
        Gig gig = gigRepository.findById(dto.getGigId())
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));

        if (gig.getStatus() != GigStatus.COMPLETED) {
            throw new IllegalStateException("Reviews can only be submitted after a gig is marked as COMPLETED.");
        }

        User reviewer = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer user not found"));

        if (reviewRepository.existsByGigIdAndReviewerId(gig.getId(), reviewer.getId())) {
            throw new IllegalStateException("You have already submitted a review for this gig.");
        }

        // Determine recipient and verify eligibility
        User recipient;
        boolean isStudentReviewer = reviewer.getRole() == Role.ROLE_STUDENT;

        if (isStudentReviewer) {
            // Student reviewing business
            recipient = gig.getBusiness().getUser();
            // Verify student had an accepted/completed application
            StudentProfile student = studentProfileRepository.findByUserId(reviewerUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
            Application app = applicationRepository.findByGigIdAndStudentId(gig.getId(), student.getId())
                    .orElseThrow(() -> new SecurityException("Unauthorized: You did not work on this gig"));
            if (app.getStatus() != ApplicationStatus.ACCEPTED && app.getStatus() != ApplicationStatus.COMPLETED) {
                throw new SecurityException("Unauthorized: Your application for this gig was not accepted");
            }
        } else {
            // Business reviewing student
            if (!gig.getBusiness().getUser().getId().equals(reviewerUserId)) {
                throw new SecurityException("Unauthorized: You do not own this gig");
            }
            if (dto.getRecipientId() == null) {
                throw new IllegalArgumentException("Recipient student is required");
            }
            recipient = userRepository.findById(dto.getRecipientId())
                    .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
        }

        Review review = new Review(gig, reviewer, recipient, dto.getRating(), dto.getComment().trim());
        Review saved = reviewRepository.save(review);

        // If student was reviewed, update their profile rating stats
        if (recipient.getRole() == Role.ROLE_STUDENT) {
            studentProfileRepository.findByUserId(recipient.getId()).ifPresent(sp -> {
                Double avg = reviewRepository.calculateAverageRatingForRecipient(recipient.getId());
                long count = reviewRepository.countByRecipient(recipient);
                sp.setRatingAverage(BigDecimal.valueOf(avg != null ? avg : 0.0).setScale(2, RoundingMode.HALF_UP));
                sp.setReviewsCount((int) count);
                studentProfileRepository.save(sp);
            });
        }

        notificationService.createNotification(
                recipient,
                "New Review Received ⭐ (" + dto.getRating() + "/5)",
                reviewer.getFullName() + " left a review for '" + gig.getTitle() + "': \"" + dto.getComment() + "\"",
                recipient.getRole() == Role.ROLE_STUDENT ? "/student/profile" : "/business/profile"
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return reviewRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsForGig(Long gigId) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));
        return reviewRepository.findByGig(gig);
    }

    @Transactional(readOnly = true)
    public boolean canReview(Long reviewerUserId, Long gigId) {
        Gig gig = gigRepository.findById(gigId).orElse(null);
        if (gig == null || gig.getStatus() != GigStatus.COMPLETED) {
            return false;
        }
        return !reviewRepository.existsByGigIdAndReviewerId(gigId, reviewerUserId);
    }
}
