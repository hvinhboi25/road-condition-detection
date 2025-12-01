# Hướng Dẫn Đăng Nhập/Đăng Ký - Road Condition Detection

## Tổng quan

Hệ thống đã được cập nhật với tính năng xác thực người dùng để theo dõi nguồn gốc của các phát hiện ổ gà.

## Tính năng mới

### Web Application
- ✅ **Đăng ký tài khoản mới** với thông tin:
  - Tên đầy đủ
  - Tên tài khoản (username)
  - Email
  - Mật khẩu
  
- ✅ **Đăng nhập** vào hệ thống
- ✅ **Hiển thị thông tin người dùng** trên header
- ✅ **Đăng xuất** khỏi hệ thống
- ✅ **Xem nguồn gốc dữ liệu**: Mỗi detection hiển thị ai đã tạo (từ Web hoặc Android)

### Android Application
- ✅ **Đăng nhập** vào hệ thống (chỉ đăng nhập, không đăng ký)
- ✅ **Hiển thị thông tin người dùng** trên màn hình chính
- ✅ **Đăng xuất** khỏi hệ thống
- ✅ **Ghi nhận user**: Mỗi detection tự động ghi nhận user đã upload

## Hướng dẫn sử dụng

### Bước 1: Đăng ký tài khoản (Web)

1. Mở web application trong trình duyệt
2. Click vào nút **"Đăng ký ngay"**
3. Điền đầy đủ thông tin:
   - Tên đầy đủ: Tên hiển thị của bạn
   - Tên tài khoản: Username của bạn
   - Email: Email hợp lệ
   - Mật khẩu: Tối thiểu 6 ký tự
   - Xác nhận mật khẩu: Nhập lại mật khẩu
4. Click **"Đăng ký"**
5. Sau khi đăng ký thành công, bạn sẽ tự động đăng nhập

### Bước 2: Đăng nhập

#### Web:
1. Mở web application
2. Nhập Email và Mật khẩu
3. Click **"Đăng nhập"**

#### Android:
1. Mở ứng dụng Android
2. Nhập Email và Mật khẩu (sử dụng tài khoản đã tạo từ Web)
3. Click **"Đăng nhập"**

### Bước 3: Sử dụng hệ thống

Sau khi đăng nhập thành công:

#### Web:
- Xem danh sách các detection
- Mỗi detection card hiển thị:
  - Thông tin người upload (tên + nguồn)
  - Vị trí phát hiện
  - Thời gian
  - Ảnh/video
- Click vào card để xem chi tiết
- Click nút **"Đăng xuất"** ở góc phải trên để đăng xuất

#### Android:
- Chụp ảnh hoặc chọn từ thư viện
- Phát hiện ổ gà
- Upload lên hệ thống
- Thông tin user tự động được gắn vào detection
- Click nút **"Đăng xuất"** để đăng xuất

## Cấu trúc dữ liệu Firestore

### Collection: `users`
```json
{
  "uid": "user_id",
  "name": "Nguyễn Văn A",
  "username": "nguyenvana",
  "email": "example@email.com",
  "createdAt": "2025-12-01T10:00:00Z",
  "role": "user"
}
```

### Collection: `detections` (đã cập nhật)
```json
{
  "imageUrl": "...",
  "videoUrl": "...",
  "detections": [...],
  "location": {...},
  "timestamp": 1234567890,
  "userId": "user_id",
  "userName": "Nguyễn Văn A",
  "userEmail": "example@email.com",
  "source": "android" | "web"
}
```

## Bảo mật

- Mật khẩu được mã hóa bởi Firebase Authentication
- Chỉ user đã đăng nhập mới có thể sử dụng hệ thống
- Session tự động duy trì cho đến khi đăng xuất
- Firebase Authentication quản lý toàn bộ bảo mật

## Lỗi thường gặp

### "Email đã được sử dụng"
- Email này đã có người đăng ký
- Sử dụng email khác hoặc đăng nhập

### "Mật khẩu không chính xác"
- Kiểm tra lại mật khẩu
- Đảm bảo không có khoảng trắng thừa

### "Tài khoản hoặc mật khẩu không đúng"
- Kiểm tra lại email và mật khẩu
- Đảm bảo đã đăng ký trước khi đăng nhập

### Android: "Sử dụng tài khoản đã được cấp từ hệ thống web"
- Trước tiên phải đăng ký tài khoản trên Web
- Sau đó mới có thể đăng nhập trên Android

## Liên hệ hỗ trợ

Nếu gặp vấn đề, vui lòng liên hệ quản trị viên hệ thống.

---

## Cập nhật kỹ thuật

### Web (React)
- Thêm Firebase Authentication
- Components: `Login.js`, `Register.js`
- Cập nhật `App.js` với auth state management
- Cập nhật `Header.js` với user info và logout
- Cập nhật `DetectionCard.js` và `Modal.js` để hiển thị user info

### Android (Kotlin)
- Thêm Firebase Authentication dependency
- Tạo `LoginActivity.kt`
- Cập nhật `MainActivity.kt` với auth check
- Cập nhật `FirebaseService.kt` để lưu user info
- Cập nhật `AndroidManifest.xml`

### Firebase
- Collection mới: `users`
- Collection `detections` thêm các field: `userId`, `userName`, `userEmail`, `source`

