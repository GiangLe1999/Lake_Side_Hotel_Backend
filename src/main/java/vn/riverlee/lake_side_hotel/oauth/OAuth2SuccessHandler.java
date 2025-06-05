package vn.riverlee.lake_side_hotel.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
import java.util.HashMap;
import java.util.Map;

// OAuth2SuccessHandler kế thừa từ SimpleUrlAuthenticationSuccessHandler
// dùng để tùy biến logic sau khi người dùng xác thực OAuth2 thành công
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

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

        // Tạo response
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("accessToken", accessToken);
        tokenInfo.put("refreshToken", refreshTokenEntity.getToken());
        tokenInfo.put("tokenType", "Bearer");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("role", user.getRole());

        tokenInfo.put("user", userInfo);

        // Gửi dữ liệu JSON về frontend qua HTTP response body theo cấu trúc AuthResponse
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(tokenInfo));
    }
}