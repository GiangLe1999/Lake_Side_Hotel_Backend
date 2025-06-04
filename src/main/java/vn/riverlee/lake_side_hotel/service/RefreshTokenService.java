package vn.riverlee.lake_side_hotel.service;

import vn.riverlee.lake_side_hotel.model.RefreshToken;
import vn.riverlee.lake_side_hotel.model.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteByToken(String token);

    void deleteByUser(User user);
}
