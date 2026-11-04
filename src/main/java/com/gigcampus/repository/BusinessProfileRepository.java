package com.gigcampus.repository;

import com.gigcampus.entity.BusinessProfile;
import com.gigcampus.entity.User;
import com.gigcampus.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByUser(User user);

    Optional<BusinessProfile> findByUserId(Long userId);

    List<BusinessProfile> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);

    long countByVerificationStatus(VerificationStatus status);
}
