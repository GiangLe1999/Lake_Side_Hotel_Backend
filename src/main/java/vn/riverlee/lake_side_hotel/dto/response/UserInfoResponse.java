package vn.riverlee.lake_side_hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.riverlee.lake_side_hotel.enums.Role;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class UserInfoResponse {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
}
