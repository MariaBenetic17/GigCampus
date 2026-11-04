package com.gigcampus.service;

import com.gigcampus.dto.BusinessProfileUpdateDto;
import com.gigcampus.dto.BusinessRegistrationDto;
import com.gigcampus.dto.StudentProfileUpdateDto;
import com.gigcampus.dto.StudentRegistrationDto;
import com.gigcampus.entity.*;
import com.gigcampus.repository.BusinessProfileRepository;
import com.gigcampus.repository.StudentProfileRepository;
import com.gigcampus.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository,
                       StudentProfileRepository studentProfileRepository,
                       BusinessProfileRepository businessProfileRepository,
                       PasswordEncoder passwordEncoder,
                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public User registerStudent(StudentRegistrationDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email " + email + " already exists");
        }

        User user = new User(
                email,
                passwordEncoder.encode(dto.getPassword()),
                dto.getFullName().trim(),
                dto.getPhone() != null ? dto.getPhone().trim() : null,
                Role.ROLE_STUDENT
        );
        User savedUser = userRepository.save(user);

        StudentProfile profile = new StudentProfile(savedUser);
        profile.setCollegeName(dto.getCollegeName().trim());
        profile.setDepartment(dto.getDepartment().trim());
        profile.setYearOfStudy(dto.getYearOfStudy());
        profile.setSkills(dto.getSkills());
        profile.setLocation(dto.getLocation() != null ? dto.getLocation() : "Erode, Tamil Nadu");
        studentProfileRepository.save(profile);

        notificationService.createNotification(
                savedUser,
                "Welcome to GigCampus!",
                "Your student account is active. Complete your profile and browse verified gigs from local Erode businesses.",
                "/student/profile"
        );

        return savedUser;
    }

    public User registerBusiness(BusinessRegistrationDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email " + email + " already exists");
        }

        User user = new User(
                email,
                passwordEncoder.encode(dto.getPassword()),
                dto.getFullName().trim(),
                dto.getPhone() != null ? dto.getPhone().trim() : null,
                Role.ROLE_BUSINESS
        );
        User savedUser = userRepository.save(user);

        BusinessProfile profile = new BusinessProfile(savedUser, dto.getBusinessName().trim());
        profile.setBusinessCategory(dto.getBusinessCategory());
        profile.setLocation(dto.getLocation().trim());
        profile.setDescription(dto.getDescription());
        profile.setContactEmail(email);
        profile.setContactPhone(dto.getPhone());
        profile.setVerificationStatus(VerificationStatus.UNVERIFIED);
        businessProfileRepository.save(profile);

        notificationService.createNotification(
                savedUser,
                "Welcome to GigCampus for Business!",
                "Your business account has been created. Post your first gig or submit business details for verification.",
                "/business/dashboard"
        );

        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<StudentProfile> getStudentProfileByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<BusinessProfile> getBusinessProfileByUserId(Long userId) {
        return businessProfileRepository.findByUserId(userId);
    }

    public StudentProfile updateStudentProfile(Long userId, StudentProfileUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone());
        userRepository.save(user);

        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> new StudentProfile(user));

        profile.setCollegeName(dto.getCollegeName());
        profile.setDepartment(dto.getDepartment());
        profile.setYearOfStudy(dto.getYearOfStudy());
        profile.setSkills(dto.getSkills());
        profile.setLocation(dto.getLocation());
        profile.setBio(dto.getBio());
        profile.setPortfolioUrl(dto.getPortfolioUrl());
        profile.setGithubUrl(dto.getGithubUrl());
        profile.setLinkedinUrl(dto.getLinkedinUrl());

        return studentProfileRepository.save(profile);
    }

    public BusinessProfile updateBusinessProfile(Long userId, BusinessProfileUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone());
        userRepository.save(user);

        BusinessProfile profile = businessProfileRepository.findByUserId(userId)
                .orElseGet(() -> new BusinessProfile(user, dto.getBusinessName()));

        profile.setBusinessName(dto.getBusinessName().trim());
        profile.setBusinessCategory(dto.getBusinessCategory());
        profile.setLocation(dto.getLocation());
        profile.setDescription(dto.getDescription());
        profile.setContactEmail(dto.getContactEmail());
        profile.setContactPhone(dto.getContactPhone());
        profile.setWebsiteUrl(dto.getWebsiteUrl());
        profile.setGstinOrRegNumber(dto.getGstinOrRegNumber());

        return businessProfileRepository.save(profile);
    }

    public BusinessProfile submitVerificationRequest(Long userId, String gstin, String notes) {
        BusinessProfile profile = businessProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Business profile not found"));

        if (gstin != null && !gstin.isBlank()) {
            profile.setGstinOrRegNumber(gstin.trim());
        }
        profile.setVerificationNotes(notes);
        profile.setVerificationStatus(VerificationStatus.PENDING_REVIEW);

        notificationService.createNotification(
                profile.getUser(),
                "Verification Request Submitted",
                "Your business verification request has been received by GigCampus administrators for review.",
                "/business/profile"
        );

        return businessProfileRepository.save(profile);
    }

    public void approveBusinessVerification(Long businessProfileId, String adminNotes) {
        BusinessProfile profile = businessProfileRepository.findById(businessProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Business profile not found"));

        profile.setVerificationStatus(VerificationStatus.VERIFIED);
        profile.setVerifiedAt(LocalDateTime.now());
        if (adminNotes != null && !adminNotes.isBlank()) {
            profile.setVerificationNotes(adminNotes);
        }
        businessProfileRepository.save(profile);

        notificationService.createNotification(
                profile.getUser(),
                "Business Verification Approved! 🎉",
                "Congratulations! Your business is now verified on GigCampus. A verified badge will appear on your gigs.",
                "/business/dashboard"
        );
    }

    public void rejectBusinessVerification(Long businessProfileId, String reason) {
        BusinessProfile profile = businessProfileRepository.findById(businessProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Business profile not found"));

        profile.setVerificationStatus(VerificationStatus.REJECTED);
        profile.setVerificationNotes(reason);
        businessProfileRepository.save(profile);

        notificationService.createNotification(
                profile.getUser(),
                "Verification Request Update",
                "Your verification request was not approved: " + reason + ". You may update your details and resubmit.",
                "/business/profile"
        );
    }

    public void toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<StudentProfile> searchStudents(String keyword, int page, int size) {
        String query = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(Math.max(0, page), size);
        return studentProfileRepository.searchStudents(query, pageRequest);
    }
}
