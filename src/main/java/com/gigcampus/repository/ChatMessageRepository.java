package com.gigcampus.repository;

import com.gigcampus.entity.ChatMessage;
import com.gigcampus.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationOrderBySentAtAsc(Conversation conversation);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.sender.id <> :userId AND m.read = false")
    long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
