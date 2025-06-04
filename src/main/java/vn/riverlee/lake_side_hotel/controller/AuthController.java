package vn.riverlee.lake_side_hotel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.riverlee.lake_side_hotel.dto.request.LoginRequest;
import vn.riverlee.lake_side_hotel.dto.request.RefreshTokenRequest;
import vn.riverlee.lake_side_hotel.dto.request.RegisterRequest;
import vn.riverlee.lake_side_hotel.dto.response.AuthResponse;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;
import vn.riverlee.lake_side_hotel.service.AuthService;
import vn.riverlee.lake_side_hotel.service.RefreshTokenService;

@Slf4j
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public DataResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register new account with email: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);
        return new DataResponse<>(HttpStatus.OK.value(), "Register successfully", authResponse);
    }

    @PostMapping("/login")
    public DataResponse<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login account with email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        return new DataResponse<>(HttpStatus.OK.value(), "Login successfully", authResponse);
    }

    @PostMapping("/refresh")
    public DataResponse<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token with old refresh token: {}", request.getRefreshToken());
        AuthResponse authResponse = authService.refresh(request);
        return new DataResponse<>(HttpStatus.OK.value(), "Refresh token successfully", authResponse);
    }

    @PostMapping("/logout")
    public DataResponse<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token with refresh token: {}", request.getRefreshToken());
        String message = authService.logout(request);
        return new DataResponse<>(HttpStatus.OK.value(), "Refresh token successfully", message);
    }
}
