package vn.riverlee.lake_side_hotel.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.riverlee.lake_side_hotel.service.impl.UserServiceImpl;
import vn.riverlee.lake_side_hotel.util.JwtUtil;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserServiceImpl userService;

    // doFilterInternal là nơi xử lý chính – Spring gọi hàm này mỗi khi có request đến.
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        log.info("---------- doFilterInternal ----------");

        final String requestTokenHeader = request.getHeader(AUTHORIZATION);

        String username = null;
        String jwtToken = null;

        // JWT Token format: "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                logger.error("Unable to get JWT Token");
            }
        }

        // Validate token
        // SecurityContextHolder.getContext().getAuthentication() == null : chưa có ai login (Authentication chưa set)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                // UsernamePasswordAuthenticationToken là một lớp con của Authentication.
                // Nó chứa:
                // - principal: thông tin người dùng → chính là userDetails.
                // - credentials: mật khẩu → ở đây null vì bạn không cần mật khẩu nữa (đã xác thực từ JWT).
                // - authorities: danh sách quyền (roles).
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // Gắn thêm thông tin về request gốc (ví dụ IP, sessionId…)
                // vào Authentication → phục vụ logging, audit, hoặc logic bảo mật nâng cao.
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Nhét authentication vào context → giống như ghi thông tin người dùng vào phiên làm việc hiện tại
                // Sau đó ghi cái SecurityContext bạn vừa tạo vào "kho toàn cục" của Spring Security.
                // Từ giờ trở đi, ở bất cứ đâu trong app, bạn có thể lấy lại thông tin người dùng qua:
                // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }
}