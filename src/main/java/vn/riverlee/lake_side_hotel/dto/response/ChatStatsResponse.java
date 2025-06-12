package vn.riverlee.lake_side_hotel.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatStatsResponse {
    private long activeConversations;
    private long closedConversations;
    private long waitingConversations;
    private long totalConversations;
}
