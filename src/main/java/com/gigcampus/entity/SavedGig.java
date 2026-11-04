package com.gigcampus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_gigs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "gig_id"})
})
public class SavedGig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gig_id", nullable = false)
    private Gig gig;

    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt;

    public SavedGig() {
    }

    public SavedGig(StudentProfile student, Gig gig) {
        this.student = student;
        this.gig = gig;
    }

    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public void setStudent(StudentProfile student) {
        this.student = student;
    }

    public Gig getGig() {
        return gig;
    }

    public void setGig(Gig gig) {
        this.gig = gig;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
}
