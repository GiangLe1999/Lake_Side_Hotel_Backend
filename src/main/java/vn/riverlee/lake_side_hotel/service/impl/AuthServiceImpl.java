package vn.riverlee.lake_side_hotel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.riverlee.lake_side_hotel.dto.request.LoginRequest;
import vn.riverlee.lake_side_hotel.dto.request.RefreshTokenRequest;
import vn.riverlee.lake_side_hotel.dto.request.RegisterRequest;
import vn.riverlee.lake_side_hotel.dto.response.AuthResponse;
import vn.riverlee.lake_side_hotel.dto.response.UserInfoResponse;
import vn.riverlee.lake_side_hotel.model.RefreshToken;
import vn.riverlee.lake_side_hotel.model.User;
import vn.riverlee.lake_side_hotel.service.AuthService;
import vn.riverlee.lake_side_hotel.service.RefreshTokenService;
import vn.riverlee.lake_side_hotel.service.UserService;
import vn.riverlee.lake_side_hotel.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        User user = userService.registerUser(request.getEmail(), request.getPassword(), request.getFullName());

        // Tạo tokens
        String accessToken = jwtUtil.generateToken(user);
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user);

        UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();

        return new AuthResponse(accessToken, refreshTokenEntity.getToken(), userInfoResponse);
    }

    @Override
    public AuthResponse login(LoginRequest request) throws BadRequestException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();

            // Generate tokens
            String accessToken = jwtUtil.generateToken(user);
            RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user);

            UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .build();

            return new AuthResponse(accessToken, refreshTokenEntity.getToken(), userInfoResponse);
        } catch (AuthenticationException ex) {
            // Return 400 Bad Request if login fails
            throw new BadRequestException("Invalid email or password");
        }
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtil.generateToken(user);
                    UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .role(user.getRole())
                            .build();
                    return new AuthResponse(token, requestRefreshToken, userInfoResponse);
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @Override
    public String logout(RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());
        return "Logged out successfully";
    }

    @Override
    public UserInfoResponse getCurrentUserProfile() {
        // SecurityContextHolder là nơi Spring Security lưu thông tin bảo mật hiện tại
        // .getContext() trả về SecurityContext, nơi chứa thông tin xác thực (authentication)
        // .getAuthentication() trả về đối tượng Authentication – đại diện cho người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // authentication.getPrincipal() trả về đối tượng đại diện cho người dùng hiện tại
        // Nếu bạn dùng UserDetailsService trong Spring Security,
        // principal sẽ là một đối tượng thuộc lớp org.springframework.security.core.userdetails.User
        // hoặc class User do bạn tự định nghĩa (implement UserDetails)
        User user = (User) authentication.getPrincipal();

        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}