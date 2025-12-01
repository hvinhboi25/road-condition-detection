# Cấu hình Firebase Security Rules

## Vấn đề hiện tại

Bạn đang gặp lỗi đăng ký có thể do **Firebase Security Rules** chưa được cấu hình đúng.

## Giải pháp

### Bước 1: Truy cập Firebase Console
1. Mở https://console.firebase.google.com
2. Chọn project: `road-condition-detection-dd79a`
3. Chọn **Firestore Database** từ menu bên trái
4. Click tab **Rules**

### Bước 2: Cập nhật Firestore Rules

Thay thế rules hiện tại bằng:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection - cho phép tạo tài khoản mới
    match /users/{userId} {
      // Cho phép đọc nếu là user đó hoặc đã authenticated
      allow read: if request.auth != null;
      
      // Cho phép tạo user mới khi đăng ký
      allow create: if request.auth != null 
                    && request.auth.uid == userId
                    && request.resource.data.uid == userId;
      
      // Cho phép user cập nhật thông tin của chính họ
      allow update: if request.auth != null && request.auth.uid == userId;
      
      // Không cho phép xóa
      allow delete: if false;
    }
    
    // Detections collection
    match /detections/{detectionId} {
      // Cho phép đọc nếu đã authenticated
      allow read: if request.auth != null;
      
      // Cho phép tạo detection nếu đã authenticated
      allow create: if request.auth != null;
      
      // Chỉ cho phép user cập nhật detection của chính họ
      allow update: if request.auth != null 
                    && resource.data.userId == request.auth.uid;
      
      // Chỉ cho phép user xóa detection của chính họ
      allow delete: if request.auth != null 
                    && resource.data.userId == request.auth.uid;
    }
    
    // Daily stats collection
    match /daily_stats/{date} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

### Bước 3: Hoặc sử dụng Rules đơn giản cho development

Nếu đang development và muốn test nhanh:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**⚠️ Lưu ý**: Rules này cho phép mọi user đã đăng nhập đọc/ghi mọi dữ liệu. Chỉ dùng cho development!

### Bước 4: Enable Email/Password Authentication

1. Trong Firebase Console, chọn **Authentication**
2. Click tab **Sign-in method**
3. Tìm **Email/Password**
4. Click để enable nếu chưa enable
5. Bật cả 2 options:
   - ✅ Email/Password
   - ✅ Email link (passwordless sign-in) - optional

### Bước 5: Kiểm tra lại

1. Save rules
2. Refresh trang web đăng ký
3. Thử đăng ký lại
4. Mở Console (F12) để xem log chi tiết

## Debug Tips

### Xem lỗi chi tiết
1. Mở Developer Console (F12)
2. Xem tab **Console**
3. Lỗi sẽ hiển thị chi tiết hơn

### Các lỗi thường gặp

**"permission-denied"**
- Rules chưa cho phép write
- Giải pháp: Cập nhật rules như trên

**"auth/operation-not-allowed"**
- Email/Password authentication chưa enable
- Giải pháp: Enable trong Authentication settings

**"auth/network-request-failed"**
- Lỗi kết nối
- Giải pháp: Kiểm tra internet

**"Firebase: Error (auth/invalid-api-key)"**
- API key không đúng
- Giải pháp: Kiểm tra lại file firebase.js

## Test sau khi cấu hình

Thử đăng ký với:
- Email: test@example.com
- Password: test123456
- Các thông tin khác điền đầy đủ

Nếu vẫn lỗi, copy toàn bộ error message từ Console và gửi lại.

