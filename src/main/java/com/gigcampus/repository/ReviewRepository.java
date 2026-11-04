package com.gigcampus.repository;

import com.gigcampus.entity.Gig;
import com.gigcampus.entity.Review;
import com.gigcampus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<Review> findByGig(Gig gig);

    boolean existsByGigIdAndReviewerId(Long gigId, Long reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.recipient.id = :recipientId")
    Double calculateAverageRatingForRecipient(@Param("recipientId") Long recipientId);

    long countByRecipient(User recipient);
}
