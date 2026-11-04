package com.gigcampus.service;

import com.gigcampus.entity.Gig;
import com.gigcampus.entity.SavedGig;
import com.gigcampus.entity.StudentProfile;
import com.gigcampus.repository.GigRepository;
import com.gigcampus.repository.SavedGigRepository;
import com.gigcampus.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SavedGigService {

    private final SavedGigRepository savedGigRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final GigRepository gigRepository;

    public SavedGigService(SavedGigRepository savedGigRepository,
                           StudentProfileRepository studentProfileRepository,
                           GigRepository gigRepository) {
        this.savedGigRepository = savedGigRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.gigRepository = gigRepository;
    }

    public boolean toggleSaveGig(Long studentUserId, Long gigId) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));

        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));

        if (savedGigRepository.existsByStudentIdAndGigId(student.getId(), gig.getId())) {
            savedGigRepository.deleteByStudentIdAndGigId(student.getId(), gig.getId());
            return false; // un-saved
        } else {
            savedGigRepository.save(new SavedGig(student, gig));
            return true; // saved
        }
    }

    @Transactional(readOnly = true)
    public boolean isGigSaved(Long studentUserId, Long gigId) {
        return studentProfileRepository.findByUserId(studentUserId)
                .map(sp -> savedGigRepository.existsByStudentIdAndGigId(sp.getId(), gigId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<SavedGig> getSavedGigs(Long studentUserId) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
        return savedGigRepository.findByStudentOrderBySavedAtDesc(student);
    }
}
