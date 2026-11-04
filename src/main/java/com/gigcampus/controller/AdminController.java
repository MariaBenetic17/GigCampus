package com.gigcampus.controller;

import com.gigcampus.entity.GigStatus;
import com.gigcampus.security.CustomUserDetails;
import com.gigcampus.service.AdminService;
import com.gigcampus.service.AuditLogService;
import com.gigcampus.service.GigService;
import com.gigcampus.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final GigService gigService;
    private final AuditLogService auditLogService;

    public AdminController(AdminService adminService,
                           UserService userService,
                           GigService gigService,
                           AuditLogService auditLogService) {
        this.adminService = adminService;
        this.userService = userService;
        this.gigService = gigService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getPlatformStatistics());
        model.addAttribute("pendingVerifications", adminService.getPendingVerifications());
        model.addAttribute("recentGigs", gigService.getFeaturedGigs());
        return "admin/dashboard";
    }

    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", adminService.getAllStudents());
        return "admin/students";
    }

    @PostMapping("/users/{userId}/toggle-active")
    public String toggleUserStatus(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserActive(userId);
            auditLogService.logAction(
                    userDetails.getUser(),
                    "USER_STATUS_TOGGLE",
                    "USER",
                    userId,
                    "Admin toggled active status for user ID: " + userId,
                    request.getRemoteAddr()
            );
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/students";
    }

    @GetMapping("/businesses")
    public String listBusinesses(Model model) {
        model.addAttribute("businesses", adminService.getAllBusinesses());
        return "admin/businesses";
    }

    @GetMapping("/verifications")
    public String listVerifications(Model model) {
        model.addAttribute("pendingList", adminService.getPendingVerifications());
        return "admin/verifications";
    }

    @PostMapping("/verifications/{id}/approve")
    public String approveVerification(
            @PathVariable Long id,
            @RequestParam(required = false) String adminNotes,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            userService.approveBusinessVerification(id, adminNotes);
            auditLogService.logAction(
                    userDetails.getUser(),
                    "VERIFICATION_APPROVE",
                    "BUSINESS_PROFILE",
                    id,
                    "Approved business verification. Notes: " + adminNotes,
                    request.getRemoteAddr()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Business verification approved!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/verifications";
    }

    @PostMapping("/verifications/{id}/reject")
    public String rejectVerification(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Incomplete shop details or documentation") String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            userService.rejectBusinessVerification(id, reason);
            auditLogService.logAction(
                    userDetails.getUser(),
                    "VERIFICATION_REJECT",
                    "BUSINESS_PROFILE",
                    id,
                    "Rejected business verification. Reason: " + reason,
                    request.getRemoteAddr()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Business verification marked as rejected.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/verifications";
    }

    @GetMapping("/gigs")
    public String manageGigs(Model model) {
        model.addAttribute("gigs", adminService.getAllGigs());
        return "admin/gigs";
    }

    @PostMapping("/gigs/{id}/status")
    public String changeGigStatus(
            @PathVariable Long id,
            @RequestParam GigStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            gigService.adminUpdateGigStatus(id, status);
            auditLogService.logAction(
                    userDetails.getUser(),
                    "GIG_STATUS_CHANGE",
                    "GIG",
                    id,
                    "Admin changed gig status to: " + status,
                    request.getRemoteAddr()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Gig status updated by admin.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/gigs";
    }

    @GetMapping("/reports")
    public String listReports(Model model) {
        model.addAttribute("reports", adminService.getAllReports());
        return "admin/reports";
    }

    @GetMapping("/audit-logs")
    public String listAuditLogs(Model model) {
        model.addAttribute("logs", auditLogService.getAllAuditLogs());
        return "admin/audit-logs";
    }
}
