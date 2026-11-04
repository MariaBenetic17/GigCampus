package com.gigcampus.controller;

import com.gigcampus.dto.BusinessRegistrationDto;
import com.gigcampus.dto.StudentRegistrationDto;
import com.gigcampus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please verify your credentials.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerHub() {
        return "auth/register-hub";
    }

    @GetMapping("/register/student")
    public String showStudentRegistration(Model model) {
        if (!model.containsAttribute("studentDto")) {
            model.addAttribute("studentDto", new StudentRegistrationDto());
        }
        return "auth/register-student";
    }

    @PostMapping("/register/student")
    public String processStudentRegistration(
            @Valid @ModelAttribute("studentDto") StudentRegistrationDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/register-student";
        }

        try {
            userService.registerStudent(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please login to your GigCampus student account.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "error.studentDto", ex.getMessage());
            return "auth/register-student";
        }
    }

    @GetMapping("/register/business")
    public String showBusinessRegistration(Model model) {
        if (!model.containsAttribute("businessDto")) {
            model.addAttribute("businessDto", new BusinessRegistrationDto());
        }
        return "auth/register-business";
    }

    @PostMapping("/register/business")
    public String processBusinessRegistration(
            @Valid @ModelAttribute("businessDto") BusinessRegistrationDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/register-business";
        }

        try {
            userService.registerBusiness(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please login with your business credentials.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "error.businessDto", ex.getMessage());
            return "auth/register-business";
        }
    }
}
