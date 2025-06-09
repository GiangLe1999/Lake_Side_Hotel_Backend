package vn.riverlee.lake_side_hotel.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.riverlee.lake_side_hotel.model.RefreshToken;
import vn.riverlee.lake_side_hotel.model.User;
import vn.riverlee.lake_side_hotel.service.RefreshTokenService;
import vn.riverlee.lake_side_hotel.service.UserService;
import vn.riverlee.lake_side_hotel.util.JwtUtil;

import java.io.IOException;

// OAuth2SuccessHandler kế thừa từ SimpleUrlAuthenticationSuccessHandler
// dùng để tùy biến logic sau khi người dùng xác thực OAuth2 thành công
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // Phương thức này được gọi khi người dùng đăng nhập OAuth2 thành công
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Lấy thông tin người dùng từ Google
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String googleId = oauth2User.getAttribute("sub");
        String fullName = oauth2User.getAttribute("name");

        // Tìm hoặc tạo user
        User user = userService.findOrCreateGoogleUser(email, googleId, fullName);

        // Tạo tokens
        String accessToken = jwtUtil.generateToken(user);
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user);

        // Tạo URL với query parameters
        String redirectUrl = String.format(
                // Dùng Fragment/Hash (#) thay vì Query Parameters (?param=value) vì token có trong URL
                // Do token nằm trong #, server sẽ không bao giờ thấy được token này, và sẽ không lưu trong access log/nginx log/... tránh bị rò rỉ.
                frontendUrl + "/oauth/callback#accessToken=%s&refreshToken=%s&userId=%d&email=%s&fullName=%s&role=%s",
                accessToken,
                refreshTokenEntity.getToken(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );

        response.sendRedirect(redirectUrl);
    }
}