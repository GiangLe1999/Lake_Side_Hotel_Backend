package vn.riverlee.lake_side_hotel.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

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
}
