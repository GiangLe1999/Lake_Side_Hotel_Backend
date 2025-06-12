package vn.riverlee.lake_side_hotel.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitChatRequest {
    private String guestName;
    private String guestEmail;
    private Long roomId; // Optional, nếu chat từ room detail
}