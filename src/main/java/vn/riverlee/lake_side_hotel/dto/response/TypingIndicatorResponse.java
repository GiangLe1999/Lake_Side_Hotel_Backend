package vn.riverlee.lake_side_hotel.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TypingIndicatorResponse implements Serializable {
    private boolean typing;
    private String senderName;
}
