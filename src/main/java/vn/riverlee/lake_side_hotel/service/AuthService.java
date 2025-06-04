package vn.riverlee.lake_side_hotel.service;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import vn.riverlee.lake_side_hotel.dto.request.LoginRequest;
import vn.riverlee.lake_side_hotel.dto.request.RefreshTokenRequest;
import vn.riverlee.lake_side_hotel.dto.request.RegisterRequest;
import vn.riverlee.lake_side_hotel.dto.response.AuthResponse;
import vn.riverlee.lake_side_hotel.dto.response.DataResponse;

public interface AuthService {
    AuthResponse register(@Valid RegisterRequest request);

    AuthResponse login(@Valid LoginRequest request);

    AuthResponse refresh(@Valid RefreshTokenRequest request);

    String logout(@Valid @RequestBody RefreshTokenRequest request);
}
