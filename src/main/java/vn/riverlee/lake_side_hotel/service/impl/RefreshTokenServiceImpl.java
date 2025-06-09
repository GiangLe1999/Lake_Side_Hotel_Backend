package vn.riverlee.lake_side_hotel.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.riverlee.lake_side_hotel.model.RefreshToken;
import vn.riverlee.lake_side_hotel.model.User;
import vn.riverlee.lake_side_hotel.repository.RefreshTokenRepository;
import vn.riverlee.lake_side_hotel.service.RefreshTokenService;
import vn.riverlee.lake_side_hotel.util.EncryptionUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final EncryptionUtil encryptionUtil;

    @Value("${app.refresh-token.expiration:604800}") // 7 days
    private Long refreshTokenDurationSeconds;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Xóa refresh token cũ của user này
        refreshTokenRepository.deleteByUser(user);

        // Tạo raw token
        String rawToken = UUID.randomUUID().toString();

        // Encrypt token trước khi lưu vào DB
        String encryptedToken = encryptionUtil.encrypt(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(encryptedToken) // Lưu encrypted token
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenDurationSeconds))
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

        // Trả về token với raw token để client sử dụng
        savedToken.setToken(rawToken);
        return savedToken;
    }

    @Override
    public Optional<RefreshToken> findByToken(String rawToken) {
        try {
            // Encrypt token từ client để tìm trong DB
            String encryptedToken = encryptionUtil.encrypt(rawToken);

            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(encryptedToken);

            // Nếu tìm thấy, set lại raw token cho client
            if (tokenOpt.isPresent()) {
                RefreshToken token = tokenOpt.get();
                token.setToken(rawToken); // Set raw token for client usage
                return Optional.of(token);
            }

            return Optional.empty();
        } catch (Exception e) {
            // Log error và return empty nếu có lỗi encryption
            return Optional.empty();
        }
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            // Encrypt lại token để xóa khỏi DB
            try {
                String encryptedToken = encryptionUtil.encrypt(token.getToken());
                refreshTokenRepository.deleteByToken(encryptedToken);
            } catch (Exception e) {
                // Log error
            }
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByToken(String rawToken) {
        try {
            String encryptedToken = encryptionUtil.encrypt(rawToken);
            refreshTokenRepository.deleteByToken(encryptedToken);
        } catch (Exception e) {
            // Log error
            throw new RuntimeException("Error deleting refresh token");
        }
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}