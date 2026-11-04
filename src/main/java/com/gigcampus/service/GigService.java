package com.gigcampus.service;

import com.gigcampus.dto.GigDto;
import com.gigcampus.entity.*;
import com.gigcampus.repository.BusinessProfileRepository;
import com.gigcampus.repository.CategoryRepository;
import com.gigcampus.repository.GigRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class GigService {

    private final GigRepository gigRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final CategoryRepository categoryRepository;

    public GigService(GigRepository gigRepository,
                      BusinessProfileRepository businessProfileRepository,
                      CategoryRepository categoryRepository) {
        this.gigRepository = gigRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.categoryRepository = categoryRepository;
    }

    public Gig createGig(Long userId, GigDto dto) {
        BusinessProfile business = businessProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Business profile not found for current user"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Selected category does not exist"));

        Gig gig = new Gig();
        gig.setBusiness(business);
        gig.setCategory(category);
        gig.setTitle(dto.getTitle().trim());
        gig.setDescription(dto.getDescription().trim());
        gig.setRequiredSkills(dto.getRequiredSkills().trim());
        gig.setBudget(dto.getBudget());
        gig.setDeadline(dto.getDeadline());
        gig.setWorkMode(dto.getWorkMode());
        gig.setLocation(dto.getLocation() != null && !dto.getLocation().isBlank() ? dto.getLocation().trim() : business.getLocation());
        gig.setStatus(dto.getStatus() != null ? dto.getStatus() : GigStatus.OPEN);

        return gigRepository.save(gig);
    }

    public Gig updateGig(Long userId, Long gigId, GigDto dto) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));

        if (!gig.getBusiness().getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized: You do not own this gig listing");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        gig.setTitle(dto.getTitle().trim());
        gig.setCategory(category);
        gig.setDescription(dto.getDescription().trim());
        gig.setRequiredSkills(dto.getRequiredSkills().trim());
        gig.setBudget(dto.getBudget());
        gig.setDeadline(dto.getDeadline());
        gig.setWorkMode(dto.getWorkMode());
        if (dto.getLocation() != null && !dto.getLocation().isBlank()) {
            gig.setLocation(dto.getLocation().trim());
        }
        if (dto.getStatus() != null) {
            gig.setStatus(dto.getStatus());
        }

        return gigRepository.save(gig);
    }

    public void updateGigStatus(Long userId, Long gigId, GigStatus newStatus) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));

        if (!gig.getBusiness().getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized: You do not own this gig listing");
        }

        gig.setStatus(newStatus);
        gigRepository.save(gig);
    }

    public void adminUpdateGigStatus(Long gigId, GigStatus newStatus) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));
        gig.setStatus(newStatus);
        gigRepository.save(gig);
    }

    @Transactional(readOnly = true)
    public Gig getGigById(Long id) {
        return gigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found with id: " + id));
    }

    public void incrementViews(Long gigId) {
        gigRepository.findById(gigId).ifPresent(g -> {
            g.setViewsCount(g.getViewsCount() == null ? 1 : g.getViewsCount() + 1);
            gigRepository.save(g);
        });
    }

    @Transactional(readOnly = true)
    public List<Gig> getGigsByBusinessUserId(Long userId) {
        BusinessProfile business = businessProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Business profile not found"));
        return gigRepository.findByBusinessOrderByCreatedAtDesc(business);
    }

    @Transactional(readOnly = true)
    public List<Gig> getFeaturedGigs() {
        return gigRepository.findTop6ByStatusOrderByCreatedAtDesc(GigStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public Page<Gig> searchGigs(String keyword, Long categoryId, WorkMode workMode,
                                BigDecimal minBudget, BigDecimal maxBudget, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size);
        return gigRepository.searchGigs(
                keyword != null && !keyword.isBlank() ? keyword.trim() : null,
                categoryId,
                workMode,
                minBudget,
                maxBudget,
                GigStatus.OPEN,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }
}
