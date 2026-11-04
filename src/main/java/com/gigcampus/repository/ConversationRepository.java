package com.gigcampus.repository;

import com.gigcampus.entity.Conversation;
import com.gigcampus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 WHERE c.gig.id = :gigId AND p1.id = :user1Id AND p2.id = :user2Id")
    Optional<Conversation> findByGigIdAndParticipants(@Param("gigId") Long gigId, @Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    Optional<Conversation> findByApplicationId(Long applicationId);
}
