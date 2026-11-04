package com.gigcampus.repository;

import com.gigcampus.entity.SavedGig;
import com.gigcampus.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedGigRepository extends JpaRepository<SavedGig, Long> {

    List<SavedGig> findByStudentOrderBySavedAtDesc(StudentProfile student);

    boolean existsByStudentIdAndGigId(Long studentId, Long gigId);

    Optional<SavedGig> findByStudentIdAndGigId(Long studentId, Long gigId);

    void deleteByStudentIdAndGigId(Long studentId, Long gigId);
}
