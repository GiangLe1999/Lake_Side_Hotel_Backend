package vn.riverlee.lake_side_hotel.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.PageRequest;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
import vn.riverlee.lake_side_hotel.enums.ChatStatus;
import vn.riverlee.lake_side_hotel.model.ChatConversation;
import vn.riverlee.lake_side_hotel.util.ChatMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchRepository {
    @PersistenceContext
    private EntityManager entityManager;
    private final ChatMapper chatMapper;

    public PaginationResponse<?> getConversations(int pageNo, int pageSize, String search, String sortBy, String status) {
        StringBuilder sqlQuery = new StringBuilder(
                "select distinct c from ChatConversation c " +
                        "left join c.messages m " +
                        "where 1=1"
        );

        if (StringUtils.hasLength(search)) {
            sqlQuery.append(" and (lower(c.guestName) like :search " +
                    "or lower(c.guestEmail) like :search " +
                    "or lower(m.content) like :search)");
        }

        if (status != null && !status.equals("ALL")) {
            sqlQuery.append(" and c.status = :status");
        }

        // Validate sortBy direction to prevent SQL injection
        String sortDirection = "desc"; // default
        if ("asc".equalsIgnoreCase(sortBy)) {
            sortDirection = "asc";
        } else if ("desc".equalsIgnoreCase(sortBy)) {
            sortDirection = "desc";
        }

        sqlQuery.append(" order by c.lastMessageAt ").append(sortDirection);

        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        selectQuery.setFirstResult(pageNo * pageSize);
        selectQuery.setMaxResults(pageSize);

        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        if (status != null && !status.equals("ALL")) {
            selectQuery.setParameter("status", ChatStatus.valueOf(status));
        }

        List<ChatConversation> resultList = selectQuery.getResultList();
        List<ChatConversationResponse> conversations = resultList.stream()
                .map(chatMapper::toDTO)
                .toList();

        // Count Query (no order by needed for count)
        StringBuilder sqlCountQuery = new StringBuilder(
                "select count(distinct c) from ChatConversation c " +
                        "left join c.messages m " +
                        "where 1=1"
        );

        if (StringUtils.hasLength(search)) {
            sqlCountQuery.append(" and (lower(c.guestName) like :search " +
                    "or lower(c.guestEmail) like :search " +
                    "or lower(m.content) like :search)");
        }

        if (status != null && !status.equals("ALL")) {
            sqlCountQuery.append(" and c.status = :status");
        }

        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());

        if (StringUtils.hasLength(search)) {
            selectCountQuery.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        if (status != null && !status.equals("ALL")) {
            selectCountQuery.setParameter("status", ChatStatus.valueOf(status));
        }

        Long totalItems = (Long) selectCountQuery.getSingleResult();
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<ChatConversationResponse> page = new PageImpl<>(conversations, pageable, totalItems);

        boolean hasNextPage = (pageNo + 1) * pageSize < totalItems;

        return PaginationResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(conversations)
                .totalPages(page.getTotalPages())
                .hasNextPage(hasNextPage)
                .build();
    }
}
