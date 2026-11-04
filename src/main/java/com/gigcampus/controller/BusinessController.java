package com.gigcampus.controller;

import com.gigcampus.dto.BusinessProfileUpdateDto;
import com.gigcampus.dto.GigDto;
import com.gigcampus.dto.ReviewDto;
import com.gigcampus.entity.*;
import com.gigcampus.security.CustomUserDetails;
import com.gigcampus.service.ApplicationService;
import com.gigcampus.service.GigService;
import com.gigcampus.service.ReviewService;
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
@RequestMapping("/business")
public class BusinessController {

    private final UserService userService;
    private final GigService gigService;
    private final ApplicationService applicationService;
    private final ReviewService reviewService;

    public BusinessController(UserService userService,
                              GigService gigService,
                              ApplicationService applicationService,
                              ReviewService reviewService) {
        this.userService = userService;
        this.gigService = gigService;
        this.applicationService = applicationService;
        this.reviewService = reviewService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        BusinessProfile profile = userService.getBusinessProfileByUserId(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException("Business profile not found"));

        List<Gig> gigs = gigService.getGigsByBusinessUserId(userDetails.getId());
        long openGigsCount = gigs.stream().filter(g -> g.getStatus() == GigStatus.OPEN).count();
        long activeProjectsCount = gigs.stream().filter(g -> g.getStatus() == GigStatus.IN_PROGRESS).count();
        long completedGigsCount = gigs.stream().filter(g -> g.getStatus() == GigStatus.COMPLETED).count();

        model.addAttribute("profile", profile);
        model.addAttribute("gigs", gigs);
        model.addAttribute("openGigsCount", openGigsCount);
        model.addAttribute("activeProjectsCount", activeProjectsCount);
        model.addAttribute("completedGigsCount", completedGigsCount);

        return "business/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        BusinessProfile profile = userService.getBusinessProfileByUserId(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException("Business profile not found"));

        if (!model.containsAttribute("profileDto")) {
            BusinessProfileUpdateDto dto = new BusinessProfileUpdateDto();
            dto.setFullName(userDetails.getUser().getFullName());
            dto.setPhone(userDetails.getUser().getPhone());
            dto.setBusinessName(profile.getBusinessName());
            dto.setBusinessCategory(profile.getBusinessCategory());
            dto.setLocation(profile.getLocation());
            dto.setDescription(profile.getDescription());
            dto.setContactEmail(profile.getContactEmail());
            dto.setContactPhone(profile.getContactPhone());
            dto.setWebsiteUrl(profile.getWebsiteUrl());
            dto.setGstinOrRegNumber(profile.getGstinOrRegNumber());
            model.addAttribute("profileDto", dto);
        }

        model.addAttribute("profile", profile);
        model.addAttribute("reviews", reviewService.getReviewsForUser(userDetails.getId()));
        return "business/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("profileDto") BusinessProfileUpdateDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            BusinessProfile profile = userService.getBusinessProfileByUserId(userDetails.getId()).orElse(null);
            model.addAttribute("profile", profile);
            return "business/profile";
        }

        userService.updateBusinessProfile(userDetails.getId(), dto);
        redirectAttributes.addFlashAttribute("successMessage", "Business details updated successfully!");
        return "redirect:/business/profile";
    }

    @PostMapping("/verification/request")
    public String submitVerification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String gstin,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            userService.submitVerificationRequest(userDetails.getId(), gstin, notes);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Verification request submitted! GigCampus administrators will review your shop details.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/business/profile";
    }

    @GetMapping("/gigs/new")
    public String newGigForm(Model model) {
        if (!model.containsAttribute("gigDto")) {
            model.addAttribute("gigDto", new GigDto());
        }
        model.addAttribute("categories", gigService.getAllCategories());
        model.addAttribute("workModes", WorkMode.values());
        return "business/gig-form";
    }

    @PostMapping("/gigs/new")
    public String createGig(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("gigDto") GigDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", gigService.getAllCategories());
            model.addAttribute("workModes", WorkMode.values());
            return "business/gig-form";
        }

        try {
            Gig created = gigService.createGig(userDetails.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Gig posted successfully! College students can now apply.");
            return "redirect:/business/dashboard";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/business/gigs/new";
        }
    }

    @GetMapping("/gigs/{id}/edit")
    public String editGigForm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Gig gig = gigService.getGigById(id);

        if (!gig.getBusiness().getUser().getId().equals(userDetails.getId())) {
            throw new SecurityException("Unauthorized");
        }

        GigDto dto = new GigDto();
        dto.setId(gig.getId());
        dto.setTitle(gig.getTitle());
        dto.setCategoryId(gig.getCategory().getId());
        dto.setDescription(gig.getDescription());
        dto.setRequiredSkills(gig.getRequiredSkills());
        dto.setBudget(gig.getBudget());
        dto.setDeadline(gig.getDeadline());
        dto.setWorkMode(gig.getWorkMode());
        dto.setLocation(gig.getLocation());
        dto.setStatus(gig.getStatus());

        model.addAttribute("gigDto", dto);
        model.addAttribute("categories", gigService.getAllCategories());
        model.addAttribute("workModes", WorkMode.values());
        model.addAttribute("gigStatuses", GigStatus.values());
        return "business/gig-edit";
    }

    @PostMapping("/gigs/{id}/edit")
    public String updateGig(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("gigDto") GigDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", gigService.getAllCategories());
            model.addAttribute("workModes", WorkMode.values());
            model.addAttribute("gigStatuses", GigStatus.values());
            return "business/gig-edit";
        }

        try {
            gigService.updateGig(userDetails.getId(), id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Gig updated successfully!");
            return "redirect:/business/dashboard";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/business/gigs/" + id + "/edit";
        }
    }

    @PostMapping("/gigs/{id}/status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam GigStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            gigService.updateGigStatus(userDetails.getId(), id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Gig status changed to " + status.getDisplayName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/business/dashboard";
    }

    @GetMapping("/gigs/{id}/applicants")
    public String viewApplicants(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Gig gig = gigService.getGigById(id);
        if (!gig.getBusiness().getUser().getId().equals(userDetails.getId())) {
            throw new SecurityException("Unauthorized access to applicants");
        }

        List<Application> applications = applicationService.getGigApplicationsForBusiness(userDetails.getId(), id);

        model.addAttribute("gig", gig);
        model.addAttribute("applications", applications);
        return "business/applicants";
    }

    @PostMapping("/applications/{appId}/status")
    public String updateApplicationStatus(
            @PathVariable Long appId,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            applicationService.updateApplicationStatus(userDetails.getId(), appId, status, notes);
            Application app = applicationService.getApplicationById(appId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Application status updated to: " + status.getDisplayName());
            return "redirect:/business/gigs/" + app.getGig().getId() + "/applicants";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/business/dashboard";
        }
    }

    @PostMapping("/applications/{appId}/complete")
    public String markCompleted(
            @PathVariable Long appId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            applicationService.markWorkCompleted(userDetails.getId(), appId);
            Application app = applicationService.getApplicationById(appId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Work marked as completed! You can now leave a review for the student.");
            return "redirect:/business/reviews/new?gigId=" + app.getGig().getId() + "&studentUserId=" + app.getStudent().getUser().getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/business/dashboard";
        }
    }

    @GetMapping("/reviews/new")
    public String showBusinessReviewForm(
            @RequestParam Long gigId,
            @RequestParam Long studentUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (!reviewService.canReview(userDetails.getId(), gigId)) {
            model.addAttribute("errorMessage", "Review already submitted or not eligible.");
            return "redirect:/business/dashboard";
        }

        ReviewDto dto = new ReviewDto();
        dto.setGigId(gigId);
        dto.setRecipientId(studentUserId);

        model.addAttribute("reviewDto", dto);
        model.addAttribute("gig", gigService.getGigById(gigId));
        model.addAttribute("studentUser", userService.findById(studentUserId).orElse(null));
        return "business/review-form";
    }

    @PostMapping("/reviews")
    public String submitBusinessReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("reviewDto") ReviewDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("gig", gigService.getGigById(dto.getGigId()));
            model.addAttribute("studentUser", userService.findById(dto.getRecipientId()).orElse(null));
            return "business/review-form";
        }

        try {
            reviewService.submitReview(userDetails.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted for the student!");
            return "redirect:/business/dashboard";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/business/dashboard";
        }
    }
}
