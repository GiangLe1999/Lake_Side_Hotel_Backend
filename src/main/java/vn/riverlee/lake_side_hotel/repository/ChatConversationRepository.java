package vn.riverlee.lake_side_hotel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.riverlee.lake_side_hotel.enums.ChatStatus;
import vn.riverlee.lake_side_hotel.model.ChatConversation;
import vn.riverlee.lake_side_hotel.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findBySessionId(String sessionId);

    List<ChatConversation> findByUserAndStatus(User user, ChatStatus status);

    Page<ChatConversation> findByStatusOrderByLastMessageAtDesc(ChatStatus status, Pageable pageable);

    @Query("SELECT c FROM ChatConversation c WHERE c.status = :status " +
            "ORDER BY c.lastMessageAt DESC")
    Page<ChatConversation> findActiveConversations(@Param("status") ChatStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM ChatConversation c WHERE c.status = :status")
    long countByStatus(@Param("status") ChatStatus status);

    void deleteBySessionId(String sessionId);
}