package com.gigcampus.service;

import com.gigcampus.dto.ApplicationDto;
import com.gigcampus.entity.*;
import com.gigcampus.repository.ApplicationRepository;
import com.gigcampus.repository.GigRepository;
import com.gigcampus.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final GigRepository gigRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              GigRepository gigRepository,
                              StudentProfileRepository studentProfileRepository,
                              NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.gigRepository = gigRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.notificationService = notificationService;
    }

    public Application applyForGig(Long studentUserId, ApplicationDto dto) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found for current user"));

        Gig gig = gigRepository.findById(dto.getGigId())
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));

        if (gig.getStatus() != GigStatus.OPEN) {
            throw new IllegalStateException("Applications can only be submitted to OPEN gigs. Current status: " + gig.getStatus());
        }

        if (applicationRepository.existsByGigIdAndStudentId(gig.getId(), student.getId())) {
            throw new IllegalStateException("You have already submitted an application for this gig.");
        }

        Application application = new Application(
                gig,
                student,
                dto.getProposal().trim(),
                dto.getEstimatedDays(),
                dto.getProposedBudget() != null ? dto.getProposedBudget() : gig.getBudget()
        );

        Application saved = applicationRepository.save(application);

        // Notify Business Owner
        notificationService.createNotification(
                gig.getBusiness().getUser(),
                "New Student Application",
                student.getUser().getFullName() + " submitted a proposal for your gig: '" + gig.getTitle() + "'.",
                "/business/gigs/" + gig.getId() + "/applicants"
        );

        return saved;
    }

    public void withdrawApplication(Long studentUserId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!application.getStudent().getUser().getId().equals(studentUserId)) {
            throw new SecurityException("Unauthorized: You do not own this application");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Only pending applications can be withdrawn. Current status: " + application.getStatus());
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<Application> getStudentApplications(Long studentUserId) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        return applicationRepository.findByStudentOrderByAppliedAtDesc(student);
    }

    @Transactional(readOnly = true)
    public List<Application> getStudentActiveProjects(Long studentUserId) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        return applicationRepository.findByStudentAndStatusOrderByAppliedAtDesc(student, ApplicationStatus.ACCEPTED);
    }

    @Transactional(readOnly = true)
    public List<Application> getStudentCompletedProjects(Long studentUserId) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        return applicationRepository.findByStudentAndStatusOrderByAppliedAtDesc(student, ApplicationStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<Application> getGigApplicationsForBusiness(Long businessUserId, Long gigId) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));

        if (!gig.getBusiness().getUser().getId().equals(businessUserId)) {
            throw new SecurityException("Unauthorized: You do not own this gig listing");
        }

        return applicationRepository.findByGigOrderByAppliedAtDesc(gig);
    }

    public void updateApplicationStatus(Long businessUserId, Long applicationId, ApplicationStatus newStatus, String notes) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        Gig gig = application.getGig();
        if (!gig.getBusiness().getUser().getId().equals(businessUserId)) {
            throw new SecurityException("Unauthorized: You do not own the gig for this application");
        }

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot update an application that was withdrawn by the student.");
        }

        application.setStatus(newStatus);
        if (notes != null && !notes.isBlank()) {
            application.setBusinessNotes(notes);
        }

        if (newStatus == ApplicationStatus.ACCEPTED) {
            gig.setStatus(GigStatus.IN_PROGRESS);
            gigRepository.save(gig);

            notificationService.createNotification(
                    application.getStudent().getUser(),
                    "Application Accepted! 🚀",
                    "Congratulations! " + gig.getBusiness().getBusinessName() + " has accepted your proposal for '" + gig.getTitle() + "'. Work can now begin!",
                    "/student/projects"
            );
        } else if (newStatus == ApplicationStatus.REJECTED) {
            notificationService.createNotification(
                    application.getStudent().getUser(),
                    "Application Status Update",
                    "Your proposal for '" + gig.getTitle() + "' was not selected by " + gig.getBusiness().getBusinessName() + ".",
                    "/student/applications"
            );
        }

        applicationRepository.save(application);
    }

    public void markWorkCompleted(Long businessUserId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        Gig gig = application.getGig();
        if (!gig.getBusiness().getUser().getId().equals(businessUserId)) {
            throw new SecurityException("Unauthorized: Only the gig business owner can mark work as completed");
        }

        application.setStatus(ApplicationStatus.COMPLETED);
        gig.setStatus(GigStatus.COMPLETED);

        applicationRepository.save(application);
        gigRepository.save(gig);

        // Notify student that project is completed and ready for review
        notificationService.createNotification(
                application.getStudent().getUser(),
                "Project Marked as Completed! 🌟",
                gig.getBusiness().getBusinessName() + " marked '" + gig.getTitle() + "' as completed. Please submit a review!",
                "/student/reviews/new?gigId=" + gig.getId()
        );
    }

    @Transactional(readOnly = true)
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
    }

    @Transactional(readOnly = true)
    public boolean hasStudentApplied(Long gigId, Long studentUserId) {
        return studentProfileRepository.findByUserId(studentUserId)
                .map(sp -> applicationRepository.existsByGigIdAndStudentId(gigId, sp.getId()))
                .orElse(false);
    }
}
