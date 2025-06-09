package vn.riverlee.lake_side_hotel.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import vn.riverlee.lake_side_hotel.oauth.OAuth2SuccessHandler;
import vn.riverlee.lake_side_hotel.service.UserService;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/*  Luồng hoạt động:
    1. Người dùng gửi HTTP request (thường có header: Authorization: Bearer <jwt-token>)
    2. SecurityFilterChain được kích hoạt để xử lý các quy tắc bảo mật.
       Trong đó:
       .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
       ➜ chèn JwtAuthenticationFilter vào trước filter mặc định của Spring để xử lý JWT.
       .authenticationProvider(authenticationProvider())
       ➜ chỉ định cách xác thực username/password nếu có (tức là khi dùng AuthenticationManager).
    3. JwtAuthenticationFilter hoạt động:
       3.1. Lấy JWT từ header
       3.2. Dùng JwtUtil để: Giải mã và lấy username từ token / Kiểm tra tính hợp lệ của token (validateToken()).
       3.3. Nếu token hợp lệ và user chưa được xác thực thì:
            3.3.1. Gọi userService.loadUserByUsername(username) để lấy thông tin người dùng từ DB.
            3.3.2. Tạo UsernamePasswordAuthenticationToken từ UserDetails.
            3.3.3. Đưa token vào SecurityContextHolder ➜ đánh dấu người dùng là đã đăng nhập.
       3.4. Tiếp tục filter chain (chain.doFilter)
    4. Xét quyền truy cập theo cấu hình
        4.1. Các endpoint không yêu cầu token.
        4.2. Các endpoint yêu cầu role USER hoặc ADMIN.
        4.3. Các endpoint chỉ role ADMIN mới truy cập.
        4.4. Các URL còn lại → yêu cầu đăng nhập.
    5. Xác thực truyền thống (username/password)
       Khi người dùng gọi /api/auth/login và nhập username + password:
       Hệ thống dùng AuthenticationManager để xác thực.
       AuthenticationManager dùng DaoAuthenticationProvider, mà bạn cấu hình với:
            1. userService để truy vấn user từ DB.
            2. BCryptPasswordEncoder để so sánh mật khẩu mã hóa.
       Nếu đúng → Spring sinh ra JWT token và trả về client (bên trong controller /api/auth/login của bạn
*/

@Configuration
// @EnableWebSecurity: Kích hoạt bảo mật Spring Security
@EnableWebSecurity
// @EnableMethodSecurity(prePostEnabled = true): Cho phép dùng @PreAuthorize
// @PostAuthorize để bảo vệ phương thức theo quyền
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Tạo và cấu hình DaoAuthenticationProvider, một lớp chuyên xác thực username/password.
    // setUserDetailsService(...): Gắn logic để load user từ DB (bạn tự cài trong UserService - userDetailsService lấy từ UserService).
    // setPasswordEncoder(...): Gắn BCryptPasswordEncoder để so sánh mật khẩu đã mã hóa.
    // Đây là lớp quan trọng nhất giúp Spring xác thực tài khoản đăng nhập truyền thống (username/password).
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Vô hiệu CSRF vì đây là REST API
                // Bạn không cần bật CSRF protection trong ứng dụng của mình – và thậm chí nên vô hiệu hóa nó khi xây dựng
                // một hệ thống REST API hiện đại, đặc biệt là API dạng stateless (không dùng session, không lưu trạng thái đăng nhập trên server)
                .csrf(csrf -> csrf.disable())
                // Không dùng session (phù hợp với JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Các endpoint không cần xác thực
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/bookings/public/**").permitAll()
                        .requestMatchers("/rooms/public/**").permitAll()

                        // Các endpoint dành cho USER và ADMIN
                        .requestMatchers("/api/reviews/user/**").hasRole("USER")

                        // Các endpoint chỉ dành cho ADMIN

                        // Tất cả request khác cần authentication
                        .anyRequest().authenticated())
                // Sẽ kích hoạt khi client (frontend hoặc Postman) truy cập URL bắt đầu quy trình OAuth2
                // VD: http://localhost:8080/api/oauth2/authorization/google
                // Sau khi đăng nhập thành công, Google sẽ gọi lại redirect-uri, ví dụ:
                // http://localhost:8080/api/auth/oauth2/callback/flowName=GeneralOAuthFlow
                // Lúc này, Spring Security sẽ tự động:
                // - Gọi token endpoint của Google.
                // - Lấy access_token.
                // - Lấy thông tin user từ Google (email, name,...).
                // - Sau đó sẽ gọi successHandler mà bạn cấu hình
                .oauth2Login(oauth2 -> oauth2
                        .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/auth/oauth2/callback/flowName=GeneralOAuthFlow"))
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/login?error=true")
                )
                // authenticationProvider(provider()): Sử dụng DaoAuthenticationProvider
                .authenticationProvider(authenticationProvider())
                // Vì trong ứng dụng ta dùng JWT nên không dùng UsernamePasswordAuthenticationFilter để xác thực nữa.
                // Do đó cần dùng addFilterBefore(...): để thêm custom filter trước UsernamePasswordAuthenticationFilter
                // để chặn và xử lý JWT trước khi Spring cố xác thực bằng username/password theo kiểu truyền thống
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bỏ qua hoàn toàn cơ chế bảo mật cho một số endpoint cụ thể như:
    // /actuator/**: endpoint giám sát
    // /v3/**, /swagger-ui*/**: endpoint tài liệu Swagger
    // /webjars/**: tài nguyên tĩnh như JS, CSS
    //Các endpoint này được công khai, không cần bảo mật.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return webSecurity ->
                webSecurity.ignoring()
                        .requestMatchers("/actuator/**", "/v3/**", "/webjars/**", "/swagger-ui*/*swagger-initializer.js", "/swagger-ui*/**");
    }

}
