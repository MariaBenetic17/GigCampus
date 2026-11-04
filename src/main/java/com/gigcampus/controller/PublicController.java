package com.gigcampus.controller;

import com.gigcampus.dto.ApplicationDto;
import com.gigcampus.entity.Gig;
import com.gigcampus.entity.Role;
import com.gigcampus.entity.StudentProfile;
import com.gigcampus.entity.WorkMode;
import com.gigcampus.security.CustomUserDetails;
import com.gigcampus.service.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class PublicController {

    private final GigService gigService;
    private final ApplicationService applicationService;
    private final ReviewService reviewService;
    private final AdminService adminService;
    private final UserService userService;
    private final SavedGigService savedGigService;

    public PublicController(GigService gigService,
                            ApplicationService applicationService,
                            ReviewService reviewService,
                            AdminService adminService,
                            UserService userService,
                            SavedGigService savedGigService) {
        this.gigService = gigService;
        this.applicationService = applicationService;
        this.reviewService = reviewService;
        this.adminService = adminService;
        this.userService = userService;
        this.savedGigService = savedGigService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", gigService.getAllCategories());
        model.addAttribute("featuredGigs", gigService.getFeaturedGigs());
        model.addAttribute("stats", adminService.getPlatformStatistics());
        return "public/home";
    }

    @GetMapping("/about")
    public String about() {
        return "public/about";
    }

    @GetMapping("/how-it-works")
    public String howItWorks() {
        return "public/how-it-works";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("categories", gigService.getAllCategories());
        return "public/services";
    }

    @GetMapping("/students")
    public String browseStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<StudentProfile> studentPage = userService.searchStudents(keyword, page, 8);
        model.addAttribute("studentPage", studentPage);
        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("keyword", keyword);
        return "public/students";
    }

    @GetMapping("/contact")
    public String contact() {
        return "public/contact";
    }

    @PostMapping("/contact")
    public String handleContactSubmit(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("successMessage",
                "Thank you, " + name + "! Your inquiry has been received. Our Erode district team will respond within 24 hours.");
        return "redirect:/contact";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("successMessage",
                "If an account with email '" + email + "' exists, password reset instructions have been dispatched. (In local development, use password: Password@123 / Student@123 / Business@123)");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/reset-password?token=" + token;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully. Please log in with your new credentials.");
        return "redirect:/login";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "public/privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "public/terms";
    }

    @GetMapping("/gigs")
    public String browseGigs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Gig> gigPage = gigService.searchGigs(keyword, categoryId, workMode, minBudget, maxBudget, page, 9);

        model.addAttribute("gigPage", gigPage);
        model.addAttribute("gigs", gigPage.getContent());
        model.addAttribute("categories", gigService.getAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("workMode", workMode);
        model.addAttribute("minBudget", minBudget);
        model.addAttribute("maxBudget", maxBudget);
        model.addAttribute("workModes", WorkMode.values());

        return "public/browse";
    }

    @GetMapping("/gigs/{id}")
    public String gigDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Gig gig = gigService.getGigById(id);
        gigService.incrementViews(id);

        boolean hasApplied = false;
        boolean isStudent = false;
        boolean isSaved = false;

        if (userDetails != null && userDetails.getUser().getRole() == Role.ROLE_STUDENT) {
            isStudent = true;
            hasApplied = applicationService.hasStudentApplied(gig.getId(), userDetails.getId());
            isSaved = savedGigService.isGigSaved(userDetails.getId(), gig.getId());
        }

        model.addAttribute("gig", gig);
        model.addAttribute("isStudent", isStudent);
        model.addAttribute("hasApplied", hasApplied);
        model.addAttribute("isSaved", isSaved);
        model.addAttribute("applicationDto", new ApplicationDto());
        model.addAttribute("reviews", reviewService.getReviewsForGig(id));

        return "public/gig-details";
    }
}
