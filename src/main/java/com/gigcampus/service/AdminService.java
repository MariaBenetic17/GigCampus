package com.gigcampus.service;

import com.gigcampus.entity.*;
import com.gigcampus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final GigRepository gigRepository;
    private final ApplicationRepository applicationRepository;
    private final ReportRepository reportRepository;

    public AdminService(UserRepository userRepository,
                        BusinessProfileRepository businessProfileRepository,
                        StudentProfileRepository studentProfileRepository,
                        GigRepository gigRepository,
                        ApplicationRepository applicationRepository,
                        ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.gigRepository = gigRepository;
        this.applicationRepository = applicationRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getPlatformStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalStudents", userRepository.countByRole(Role.ROLE_STUDENT));
        stats.put("totalBusinesses", userRepository.countByRole(Role.ROLE_BUSINESS));
        stats.put("totalGigs", gigRepository.count());
        stats.put("openGigs", gigRepository.countByStatus(GigStatus.OPEN));
        stats.put("totalApplications", applicationRepository.count());
        stats.put("completedProjects", applicationRepository.countByStatus(ApplicationStatus.COMPLETED));
        stats.put("pendingVerifications", businessProfileRepository.countByVerificationStatus(VerificationStatus.PENDING_REVIEW));
        return stats;
    }

    @Transactional(readOnly = true)
    public List<User> getAllStudents() {
        return userRepository.findByRoleOrderByCreatedAtDesc(Role.ROLE_STUDENT);
    }

    @Transactional(readOnly = true)
    public List<BusinessProfile> getAllBusinesses() {
        return businessProfileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<BusinessProfile> getPendingVerifications() {
        return businessProfileRepository.findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus.PENDING_REVIEW);
    }

    @Transactional(readOnly = true)
    public List<Gig> getAllGigs() {
        return gigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }
}
