- Access Token thì vẫn dùng JWT bình thường. Bảo mật với Sign và verify
- Refresh Token dùng cách lưu vào DB rồi thực hiện query mỗi khi có yêu cầu cấp refreshToken mới
- Refresh Token không dùng JWT như cách thông thường (vì ứng dụng nhỏ), thay vào đó dùng randomUUID
- Nhưng để đảm bảo bảo mật thì sẽ encrypt UUID này với 1 secret key trước khi lưu DB

