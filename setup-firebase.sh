#!/bin/bash
# Quick Firebase Setup Script

echo "🔥 Firebase Setup Helper"
echo "========================"
echo ""
echo "❌ Lỗi đăng ký thường do:"
echo "   1. Firebase Authentication chưa enable"
echo "   2. Firestore Rules chưa cấu hình"
echo ""
echo "🔧 Các bước sửa:"
echo ""
echo "📍 Bước 1: Enable Authentication"
echo "   → Mở: https://console.firebase.google.com/project/road-condition-detection-dd79a/authentication/providers"
echo "   → Enable: Email/Password"
echo ""
echo "📍 Bước 2: Cập nhật Firestore Rules"  
echo "   → Mở: https://console.firebase.google.com/project/road-condition-detection-dd79a/firestore/rules"
echo "   → Copy rules từ file: FIREBASE_RULES_SETUP.md"
echo ""
echo "📍 Bước 3: Test lại"
echo "   → Refresh trang web"
echo "   → Mở Console (F12) để xem lỗi chi tiết"
echo "   → Thử đăng ký lại"
echo ""
echo "💡 Quick Debug:"
echo "   → Xem Console log (F12 → Console tab)"
echo "   → Lỗi chi tiết sẽ hiển thị ở đó"
echo ""

# Firestore Rules cho production
cat > firestore.rules << 'EOF'
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth != null && request.auth.uid == userId;
      allow delete: if false;
    }
    
    match /detections/{detectionId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null && resource.data.userId == request.auth.uid;
      allow delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    match /daily_stats/{date} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
EOF

echo "✅ File firestore.rules đã được tạo!"
echo "📋 Copy nội dung file này vào Firebase Console"
echo ""
echo "🔗 Links nhanh:"
echo "   Authentication: https://console.firebase.google.com/project/road-condition-detection-dd79a/authentication/providers"
echo "   Firestore Rules: https://console.firebase.google.com/project/road-condition-detection-dd79a/firestore/rules"

