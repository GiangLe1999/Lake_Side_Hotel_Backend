package vn.riverlee.lake_side_hotel.configuration;

import org.springframework.web.filter.CorsFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Đánh dấu lớp này là một cấu hình Spring (@Configuration). Spring Boot sẽ quét và sử dụng khi khởi động
@Configuration
public class CorsConfig {
    @Value("${frontend.url}")
    private String frontendUrl;

    // Đăng ký một bean của FilterRegistrationBean
    // Bọc bên trong là một CORS filter (CorsFilter) để xử lý CORS cho mọi HTTP request
    // Không giống như WebMvcConfigurer, Spring không có cơ chế tự động tìm kiếm và gọi các instance của CorsFilter
    // Bạn phải rõ ràng khai báo nó như một bean để Spring Boot nhận diện và sử dụng
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        // UrlBasedCorsConfigurationSource: cho phép ánh xạ các cấu hình CORS đến các endpoint cụ thể (áp dụng ở dòng 42)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // orsConfiguration: chứa các thiết lập CORS chi tiết.
        CorsConfiguration config = new CorsConfiguration();

        // Cấu hình CORS
        // Chỉ cho phép các request đến từ frontend
        config.addAllowedOrigin(frontendUrl);
        // Cho phép tất cả phương thức HTTP
        config.addAllowedMethod("*");
        // Cho phép tất cả tiêu đề
        config.addAllowedHeader("*");
        // Cho phép gửi cookie
        config.setAllowCredentials(true);
        // Thời gian cache preflight
        config.setMaxAge(3600L);

        // Áp dụng cấu hình cho tất cả endpoint
        source.registerCorsConfiguration("/**", config);

        // CorsFilter: là filter thực thi CORS logic
        // FilterRegistrationBean: giúp Spring Boot biết phải đăng ký filter này
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        // setOrder(Ordered.HIGHEST_PRECEDENCE): bảo đảm filter này chạy sớm nhất trong chuỗi các filter.
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Hoặc bean.setOrder(0);
        return bean;
    }
}
