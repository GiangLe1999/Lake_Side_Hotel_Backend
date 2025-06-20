package vn.riverlee.lake_side_hotel.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypingIndicatorRequest {
    private boolean typing;
    private String senderName;
}
