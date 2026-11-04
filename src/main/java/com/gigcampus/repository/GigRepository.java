package com.gigcampus.repository;

import com.gigcampus.entity.BusinessProfile;
import com.gigcampus.entity.Gig;
import com.gigcampus.entity.GigStatus;
import com.gigcampus.entity.WorkMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GigRepository extends JpaRepository<Gig, Long> {

    List<Gig> findByBusinessOrderByCreatedAtDesc(BusinessProfile business);

    List<Gig> findTop6ByStatusOrderByCreatedAtDesc(GigStatus status);

    long countByStatus(GigStatus status);

    @Query("""
        SELECT g FROM Gig g
        WHERE (:status IS NULL OR g.status = :status)
          AND (:categoryId IS NULL OR g.category.id = :categoryId)
          AND (:workMode IS NULL OR g.workMode = :workMode)
          AND (:minBudget IS NULL OR g.budget >= :minBudget)
          AND (:maxBudget IS NULL OR g.budget <= :maxBudget)
          AND (:keyword IS NULL OR :keyword = '' OR
               LOWER(g.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(g.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(g.location) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY g.createdAt DESC
    """)
    Page<Gig> searchGigs(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("workMode") WorkMode workMode,
        @Param("minBudget") BigDecimal minBudget,
        @Param("maxBudget") BigDecimal maxBudget,
        @Param("status") GigStatus status,
        Pageable pageable
    );
}
