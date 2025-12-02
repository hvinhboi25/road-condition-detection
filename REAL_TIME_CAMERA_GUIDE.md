# 📹 Camera Tự Động Phát Hiện Ổ Gà - Real-time Detection

## 🎯 Tính năng mới

### ✨ AUTO DETECTION CAMERA
- 📷 **Mở camera** → Camera quét real-time
- 🎯 **Tự động phát hiện** ổ gà khi thấy
- ✅ **Tự động chụp** khi confidence >= 60%
- 🎨 **Vẽ bounding box** tự động
- ⚡ **Return về màn hình chính** với kết quả
- 🚀 **Auto upload** lên Firebase

---

## 🔥 Cách hoạt động

### Flow hoàn chỉnh:

```
User click "📷 Camera"
    ↓
Mở CameraActivity (full screen)
    ↓
Camera Preview + Real-time AI Analysis
    ↓
[Quét liên tục]
    ├─ Không thấy ổ gà → Tiếp tục quét
    └─ Thấy ổ gà (confidence >= 60%)
        ↓
    TỰ ĐỘNG CHỤP!
        ↓
    Vẽ bounding box
        ↓
    Return về MainActivity
        ↓
    Hiển thị kết quả với boxes
        ↓
    Tự động upload lên Firebase
        ↓
    ✅ HOÀN THÀNH!
```

---

## 💻 Technical Details

### 1. CameraActivity.kt (MỚI)
- **Real-time camera preview** với CameraX
- **ImageAnalysis** use case cho AI
- **Auto capture** khi detect ổ gà
- **UI overlay** hiển thị status

```kotlin
// Threshold để tự động chụp
val CONFIDENCE_THRESHOLD = 0.6f  // 60%
val MIN_DETECTIONS = 1           // Tối thiểu 1 ổ gà
```

### 2. ImageUtils.kt (CẬP NHẬT)
- Thêm `ImageProxy.toBitmap()` extension
- Convert camera frame sang Bitmap
- Handle rotation tự động

### 3. MainActivity.kt (CẬP NHẬT)
- Launch CameraActivity
- Nhận kết quả qua `CapturedImageHolder`
- Auto display + upload

---

## 🎨 UI/UX Features

### Camera Screen:
1. **Full Screen Preview**
   - Camera chiếm toàn màn hình
   - Tỷ lệ phù hợp với màn hình

2. **Status Overlay** (top)
   - 🔍 "Đang quét..."
   - 🎯 "Phát hiện X đối tượng"
   - ✅ "Phát hiện X ổ gà!"
   - 💡 Hướng dẫn: "Hướng camera vào ổ gà trên đường"

3. **Cancel Button** (bottom)
   - ❌ Nút Hủy màu đỏ
   - Return về main nếu không muốn dùng

4. **Loading State**
   - Progress indicator khi đang xử lý
   - "Đang xử lý..." text

### Main Screen After Detection:
- Ảnh với bounding boxes
- Danh sách detections
- Ready để upload

---

## 🚀 Cách sử dụng

### Bước 1: Build app
```bash
cd app
./gradlew clean build
```

### Bước 2: Install & Run
1. Cài đặt app trên thiết bị
2. Đăng nhập vào app

### Bước 3: Sử dụng Camera
1. Click nút **"📷 Camera"**
2. Cho phép quyền Camera (nếu lần đầu)
3. CameraActivity mở full screen
4. **Hướng camera vào ổ gà trên đường**
5. Chờ auto detection (1-3 giây)
6. ✅ Tự động chụp khi thấy ổ gà!
7. Return về main với kết quả
8. Kết quả tự động upload

### Tips:
- 📍 Đứng gần ổ gà (1-3m)
- 💡 Ánh sáng tốt
- 📏 Ổ gà trong khung hình
- ⏱️ Giữ camera ổn định 1-2 giây

---

## ⚙️ Configuration

### Threshold Settings (trong CameraActivity.kt):

```kotlin
// Có thể điều chỉnh các giá trị này:
val CONFIDENCE_THRESHOLD = 0.6f  // 60% confidence
val MIN_DETECTIONS = 1           // Tối thiểu 1 ổ gà
```

**Giảm threshold** (ví dụ 0.5f = 50%):
- ✅ Dễ detect hơn
- ❌ Nhiều false positive hơn

**Tăng threshold** (ví dụ 0.7f = 70%):
- ✅ Chính xác hơn
- ❌ Khó detect hơn

---

## 🔧 Architecture

### Files Changed/Created:

1. **CameraActivity.kt** (MỚI - 300+ lines)
   - Real-time camera với CameraX
   - Image analysis với TensorFlow
   - Auto capture logic
   - UI overlay

2. **ImageUtils.kt** (THÊM)
   - `ImageProxy.toBitmap()` extension
   - Xử lý rotation

3. **MainActivity.kt** (CẬP NHẬT)
   - `realTimeCameraLauncher` result handler
   - Launch CameraActivity
   - Nhận và hiển thị kết quả

4. **CapturedImageHolder** (MỚI)
   - Singleton object
   - Giữ bitmap và detections tạm
   - Transfer data giữa activities

---

## 📊 Performance

### Real-time Analysis:
- **Framerate**: ~3-5 FPS (analysis)
- **Latency**: ~200-500ms per frame
- **Strategy**: `KEEP_ONLY_LATEST` (không queue)
- **Threading**: Single thread executor

### Memory:
- Bitmap được giải phóng sau mỗi frame
- Chỉ giữ kết quả cuối cùng
- CapturedImageHolder clear sau use

---

## 🐛 Troubleshooting

### Camera không mở:
```
- Check camera permission
- Check AndroidManifest.xml
- Restart app
```

### Không detect được:
```
- Ánh sáng tốt hơn
- Gần ổ gà hơn
- Giữ camera ổn định
- Check logcat: adb logcat | grep -i camera
```

### App crash:
```
- Check CameraX dependencies (build.gradle.kts)
- Rebuild: ./gradlew clean build
- Check logcat for errors
```

### Detection quá chậm:
```
- Sử dụng thiết bị mạnh hơn
- Giảm resolution (nếu cần)
- Check CPU/GPU usage
```

---

## 🎯 Test Cases

### ✅ Test 1: Basic Flow
1. Open app
2. Click "📷 Camera"
3. Point at pothole
4. Wait for auto capture
5. Verify: Returns to main with result

### ✅ Test 2: Multiple Detections
1. Point camera at area with 2-3 potholes
2. Verify: Detects multiple objects
3. Verify: Auto captures when confidence >= 60%

### ✅ Test 3: Cancel
1. Open camera
2. Click "❌ Hủy"
3. Verify: Returns to main without result

### ✅ Test 4: Permission
1. Fresh install
2. Click camera
3. Verify: Permission dialog shows
4. Allow → Camera opens

### ✅ Test 5: Auto Upload
1. Complete detection
2. Check Firestore
3. Verify: Detection with userId uploaded

---

## 📱 Screenshots Flow

```
[Main Screen]
    ↓ Click "📷 Camera"
[Full Screen Camera]
    - Preview
    - Status: "🔍 Đang quét..."
    - Cancel button
    ↓ Point at pothole
[Detecting...]
    - Status: "🎯 Phát hiện 1 đối tượng"
    ↓ Auto capture!
[Processing...]
    - Loading indicator
    - "Đang xử lý..."
    ↓ 500ms delay
[Back to Main]
    - Image with bounding boxes
    - Detection list
    - Ready to upload
    ↓ Auto upload
[Uploaded!]
    - Shows in detections list on web
    - With user info
```

---

## 🎉 Advantages

✅ **Hands-free**: Không cần nhấn nút chụp
✅ **Fast**: Tự động chụp ngay khi thấy
✅ **Accurate**: Chỉ chụp khi confident
✅ **User-friendly**: UI rõ ràng, dễ dùng
✅ **Reliable**: Error handling tốt
✅ **Integrated**: Auto upload sau khi detect

---

## 🚀 Next Steps (Optional)

1. **Thêm sound effect** khi detect
2. **Vibration feedback** khi auto capture
3. **Countdown** trước khi chụp (3-2-1)
4. **Manual capture button** (nếu muốn)
5. **Flash control** cho low light
6. **Gallery sau khi chụp** để xem lại

---

## 💡 Tips cho User

1. **Đứng gần ổ gà** (1-3 mét)
2. **Ánh sáng ban ngày** tốt nhất
3. **Giữ camera ổn định** 1-2 giây
4. **Ổ gà ở giữa khung hình**
5. **Không di chuyển** khi camera đang quét
6. **Chờ status** "Phát hiện" trước khi di chuyển

---

## 📝 Notes

- **Confidence Threshold 60%**: Cân bằng giữa accuracy và sensitivity
- **Auto Capture**: Chụp ngay khi thấy, không cần user action
- **Single Thread**: Tránh race condition
- **Memory Efficient**: Clear sau mỗi frame
- **Battery**: Tắt camera khi không dùng

---

**🎊 Tính năng Real-time Detection đã sẵn sàng!**

Build và test ngay thôi! 🚀📷

