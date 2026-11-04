package com.gigcampus.repository;

import com.gigcampus.entity.StudentProfile;
import com.gigcampus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser(User user);

    Optional<StudentProfile> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT sp FROM StudentProfile sp WHERE sp.user.active = true AND " +
            "(:keyword IS NULL OR LOWER(sp.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sp.skills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sp.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sp.collegeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sp.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY sp.ratingAverage DESC, sp.reviewsCount DESC")
    org.springframework.data.domain.Page<StudentProfile> searchStudents(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);
}
