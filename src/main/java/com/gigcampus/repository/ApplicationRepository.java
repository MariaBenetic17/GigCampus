package com.gigcampus.repository;

import com.gigcampus.entity.Application;
import com.gigcampus.entity.ApplicationStatus;
import com.gigcampus.entity.Gig;
import com.gigcampus.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentOrderByAppliedAtDesc(StudentProfile student);

    List<Application> findByGigOrderByAppliedAtDesc(Gig gig);

    List<Application> findByGigIdOrderByAppliedAtDesc(Long gigId);

    boolean existsByGigIdAndStudentId(Long gigId, Long studentId);

    Optional<Application> findByGigIdAndStudentId(Long gigId, Long studentId);

    long countByStatus(ApplicationStatus status);

    List<Application> findByStudentAndStatusOrderByAppliedAtDesc(StudentProfile student, ApplicationStatus status);
}
