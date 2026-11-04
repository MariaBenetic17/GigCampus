package com.gigcampus.controller;

import com.gigcampus.dto.ApplicationDto;
import com.gigcampus.dto.ReviewDto;
import com.gigcampus.dto.StudentProfileUpdateDto;
import com.gigcampus.entity.Application;
import com.gigcampus.entity.StudentProfile;
import com.gigcampus.security.CustomUserDetails;
import com.gigcampus.service.ApplicationService;
import com.gigcampus.service.GigService;
import com.gigcampus.service.ReviewService;
import com.gigcampus.service.SavedGigService;
import com.gigcampus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final UserService userService;
    private final ApplicationService applicationService;
    private final GigService gigService;
    private final ReviewService reviewService;
    private final SavedGigService savedGigService;

    public StudentController(UserService userService,
                             ApplicationService applicationService,
                             GigService gigService,
                             ReviewService reviewService,
                             SavedGigService savedGigService) {
        this.userService = userService;
        this.applicationService = applicationService;
        this.gigService = gigService;
        this.reviewService = reviewService;
        this.savedGigService = savedGigService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        StudentProfile profile = userService.getStudentProfileByUserId(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException("Student profile not found"));

        List<Application> applications = applicationService.getStudentApplications(userDetails.getId());
        List<Application> activeProjects = applicationService.getStudentActiveProjects(userDetails.getId());
        List<Application> completed = applicationService.getStudentCompletedProjects(userDetails.getId());

        model.addAttribute("profile", profile);
        model.addAttribute("totalApplications", applications.size());
        model.addAttribute("activeProjectsCount", activeProjects.size());
        model.addAttribute("completedProjectsCount", completed.size());
        model.addAttribute("savedGigsCount", savedGigService.getSavedGigs(userDetails.getId()).size());
        model.addAttribute("recentApplications", applications.stream().limit(5).toList());
        model.addAttribute("recommendedGigs", gigService.getFeaturedGigs());

        return "student/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        StudentProfile profile = userService.getStudentProfileByUserId(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException("Student profile not found"));

        if (!model.containsAttribute("profileDto")) {
            StudentProfileUpdateDto dto = new StudentProfileUpdateDto();
            dto.setFullName(userDetails.getUser().getFullName());
            dto.setPhone(userDetails.getUser().getPhone());
            dto.setCollegeName(profile.getCollegeName());
            dto.setDepartment(profile.getDepartment());
            dto.setYearOfStudy(profile.getYearOfStudy());
            dto.setSkills(profile.getSkills());
            dto.setLocation(profile.getLocation());
            dto.setBio(profile.getBio());
            dto.setPortfolioUrl(profile.getPortfolioUrl());
            dto.setGithubUrl(profile.getGithubUrl());
            dto.setLinkedinUrl(profile.getLinkedinUrl());
            model.addAttribute("profileDto", dto);
        }

        model.addAttribute("profile", profile);
        model.addAttribute("reviews", reviewService.getReviewsForUser(userDetails.getId()));
        return "student/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("profileDto") StudentProfileUpdateDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            StudentProfile profile = userService.getStudentProfileByUserId(userDetails.getId()).orElse(null);
            model.addAttribute("profile", profile);
            return "student/profile";
        }

        userService.updateStudentProfile(userDetails.getId(), dto);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/student/profile";
    }

    @PostMapping("/gigs/{gigId}/apply")
    public String applyForGig(
            @PathVariable Long gigId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("applicationDto") ApplicationDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        dto.setGigId(gigId);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/gigs/" + gigId;
        }

        try {
            applicationService.applyForGig(userDetails.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your application has been submitted successfully to the business!");
            return "redirect:/student/applications";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/gigs/" + gigId;
        }
    }

    @GetMapping("/applications")
    public String listApplications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Application> applications = applicationService.getStudentApplications(userDetails.getId());
        model.addAttribute("applications", applications);
        return "student/applications";
    }

    @PostMapping("/applications/{id}/withdraw")
    public String withdrawApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            applicationService.withdrawApplication(userDetails.getId(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Application withdrawn successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/applications";
    }

    @PostMapping("/gigs/{id}/toggle-save")
    public String toggleSaveGig(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String redirectUrl,
            RedirectAttributes redirectAttributes) {

        try {
            boolean saved = savedGigService.toggleSaveGig(userDetails.getId(), id);
            if (saved) {
                redirectAttributes.addFlashAttribute("successMessage", "Gig saved to your bookmarks!");
            } else {
                redirectAttributes.addFlashAttribute("infoMessage", "Gig removed from bookmarks.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/student/saved-gigs";
    }

    @GetMapping("/saved-gigs")
    public String savedGigs(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("savedGigs", savedGigService.getSavedGigs(userDetails.getId()));
        return "student/saved-gigs";
    }

    @GetMapping("/projects")
    public String activeProjects(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Application> activeProjects = applicationService.getStudentActiveProjects(userDetails.getId());
        model.addAttribute("projects", activeProjects);
        return "student/projects";
    }

    @GetMapping("/completed")
    public String completedProjects(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Application> completed = applicationService.getStudentCompletedProjects(userDetails.getId());
        model.addAttribute("projects", completed);
        return "student/completed";
    }

    @GetMapping("/reviews/new")
    public String showReviewForm(
            @RequestParam Long gigId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (!reviewService.canReview(userDetails.getId(), gigId)) {
            model.addAttribute("errorMessage", "You are not eligible to review this gig or have already submitted one.");
            return "redirect:/student/completed";
        }

        ReviewDto dto = new ReviewDto();
        dto.setGigId(gigId);

        model.addAttribute("reviewDto", dto);
        model.addAttribute("gig", gigService.getGigById(gigId));
        return "student/review-form";
    }

    @PostMapping("/reviews")
    public String submitReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("reviewDto") ReviewDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("gig", gigService.getGigById(dto.getGigId()));
            return "student/review-form";
        }

        try {
            reviewService.submitReview(userDetails.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your review has been published.");
            return "redirect:/student/completed";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/completed";
        }
    }
}
