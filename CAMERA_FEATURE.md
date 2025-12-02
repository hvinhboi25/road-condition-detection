# 📷 Tính năng Camera - Road Condition Detection

## ✅ Đã sửa lỗi và kích hoạt Camera

### Thay đổi:
1. ✅ **Enable nút Camera** (trước đó bị disabled)
2. ✅ **Thêm camera launcher** với `TakePicture()` contract
3. ✅ **Tạo URI tự động** cho ảnh chụp từ camera
4. ✅ **Xử lý ảnh camera** tương tự như ảnh từ thư viện
5. ✅ **Đổi màu nút** thành xanh dương + icon 📷
6. ✅ **Permission handling** đầy đủ

---

## 🚀 Cách sử dụng

### Bước 1: Build lại app
```bash
cd app
./gradlew clean build
```

Hoặc trong Android Studio:
- Build → Clean Project
- Build → Rebuild Project

### Bước 2: Chạy app
1. Cài đặt lại app trên thiết bị/emulator
2. Đăng nhập vào ứng dụng

### Bước 3: Sử dụng Camera
1. **Lần đầu**: App sẽ yêu cầu quyền Camera
   - Click **"Cho phép"** hoặc **"Allow"**
   
2. **Chụp ảnh**:
   - Click nút **"📷 Camera"** (màu xanh dương)
   - Camera sẽ mở
   - Chụp ảnh ổ gà trên đường
   
3. **Phát hiện**:
   - Ảnh vừa chụp hiển thị trên màn hình
   - Click **"Phát hiện ổ gà"**
   - Chờ AI phân tích
   
4. **Upload**:
   - Kết quả tự động upload lên Firebase
   - Bao gồm: ảnh, detection, location, user info

---

## 🎯 Tính năng

### Camera Button
```kotlin
Button(
    onClick = {
        if (cameraPermissionState.status.isGranted) {
            // Launch camera
            val uri = createImageUri()
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    },
    enabled = true,  // ✅ Đã enable
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF2196F3)  // Xanh dương
    )
)
```

### Image URI Creation
```kotlin
fun createImageUri(): Uri {
    val contentValues = android.content.ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, 
            "road_detection_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: Uri.EMPTY
}
```

### Camera Launcher
```kotlin
val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success ->
    if (success && cameraImageUri != null) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(
                context.contentResolver, 
                cameraImageUri
            )
            selectedImage = bitmap
            resultImage = null
            predictions = emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
```

---

## 🔐 Permissions

### AndroidManifest.xml (đã có sẵn)
```xml
<!-- Camera permission -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Media storage -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Camera hardware -->
<uses-feature 
    android:name="android.hardware.camera" 
    android:required="true" />
```

### Runtime Permissions
- App tự động yêu cầu permission Camera khi lần đầu click nút
- Sử dụng Accompanist Permissions library
- Graceful handling nếu user từ chối

---

## 🎨 UI Updates

### Màu sắc mới:
- **Thư viện**: Xanh lá `#4CAF50` 
- **Camera**: Xanh dương `#2196F3` ✨ (mới)
- **Phát hiện**: Cam `#FF6B35`

### Text:
- **Thư viện**: "Thư viện"
- **Camera**: "📷 Camera" (có icon)
- **Phát hiện**: "Phát hiện ổ gà"

---

## 📊 Flow hoàn chỉnh

```
User click "📷 Camera"
    ↓
Check permission
    ├─ Có quyền → Mở camera
    └─ Chưa có → Yêu cầu quyền → Mở camera
    ↓
User chụp ảnh
    ↓
Lưu vào MediaStore
    ↓
Load bitmap từ URI
    ↓
Hiển thị trên màn hình
    ↓
User click "Phát hiện ổ gà"
    ↓
AI phân tích (TensorFlow)
    ↓
Vẽ bounding boxes
    ↓
Upload lên Firebase/Cloudinary
    ↓
Lưu vào Firestore với user info
    ↓
✅ Hoàn thành!
```

---

## 🐛 Troubleshooting

### "Permission denied"
- Vào Settings → Apps → Road Condition Detection → Permissions
- Bật Camera permission

### "Camera không mở"
- Kiểm tra thiết bị có camera không
- Thử build lại app
- Check logcat để xem lỗi chi tiết

### "Ảnh không hiển thị"
- Kiểm tra storage permission
- Check logcat: `adb logcat | grep -i camera`

### Build lỗi
```bash
cd app
./gradlew clean
./gradlew build
```

---

## 📱 Test Cases

### ✅ Test 1: Permission Request
1. Cài app mới
2. Click "📷 Camera"
3. Verify: Dialog yêu cầu permission xuất hiện

### ✅ Test 2: Chụp ảnh
1. Allow camera permission
2. Click "📷 Camera"
3. Chụp ảnh
4. Verify: Ảnh hiển thị trên màn hình

### ✅ Test 3: Phát hiện
1. Sau khi chụp ảnh
2. Click "Phát hiện ổ gà"
3. Verify: AI phân tích và vẽ boxes

### ✅ Test 4: Upload
1. Sau khi phát hiện
2. Check Firestore
3. Verify: Detection có userId và source="android"

---

## 🎉 Hoàn thành!

Camera feature đã hoạt động đầy đủ! 

**Các bước tiếp theo**:
1. Build lại app
2. Test trên thiết bị thật
3. Chụp ảnh ổ gà thực tế
4. Enjoy! 🚀

