- Access Token thì vẫn dùng JWT bình thường. Bảo mật với Sign và verify
- Refresh Token dùng cách lưu vào DB rồi thực hiện query mỗi khi có yêu cầu cấp refreshToken mới
- Refresh Token không dùng JWT như cách thông thường (vì ứng dụng nhỏ), thay vào đó dùng randomUUID
- Nhưng để đảm bảo bảo mật thì sẽ encrypt UUID này với 1 secret key trước khi lưu DB

Đường login Google	http://localhost:8080/api/oauth2/authorization/google
redirect-uri YAML	{baseUrl}/api/auth/oauth2/callback/flowName=GeneralOAuthFlow
Google đăng ký	http://localhost:8080/api/auth/oauth2/callback/flowName=GeneralOAuthFlow (chính xác 100%)

Trong Security Config, đoạn này nhớ thêm .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/auth/oauth2/callback/flowName=GeneralOAuthFlow"))
.oauth2Login(oauth2 -> oauth2
.redirectionEndpoint(endpoint -> endpoint.baseUri("/api/auth/oauth2/callback/flowName=GeneralOAuthFlow"))
.successHandler(oAuth2SuccessHandler)
.failureUrl("/login?error=true")
)