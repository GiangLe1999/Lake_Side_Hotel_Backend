package vn.riverlee.lake_side_hotel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.riverlee.lake_side_hotel.model.ChatConversation;
import vn.riverlee.lake_side_hotel.model.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationOrderByCreatedAtAsc(ChatConversation conversation);

    Page<ChatMessage> findByConversationOrderByCreatedAtDesc(ChatConversation conversation, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation = :conversation AND m.isRead = false")
    int countUnreadMessages(@Param("conversation") ChatConversation conversation);

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation = :conversation " +
            "ORDER BY m.createdAt DESC")
    List<ChatMessage> findRecentMessages(@Param("conversation") ChatConversation conversation, Pageable pageable);
}