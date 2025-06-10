package vn.riverlee.lake_side_hotel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {
    private Long id;
    private String title;
    private String comment;
    private int rating;
    private Date createdAt;
    private UserInfoResponse user;
}
