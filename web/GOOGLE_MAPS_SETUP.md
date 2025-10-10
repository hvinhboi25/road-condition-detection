# Hướng dẫn cấu hình Google Maps API

## Bước 1: Tạo Google Cloud Project

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo một project mới hoặc chọn project hiện có
3. Đảm bảo billing đã được kích hoạt

## Bước 2: Kích hoạt Geocoding API

1. Trong Google Cloud Console, đi tới **APIs & Services** > **Library**
2. Tìm kiếm "Geocoding API"
3. Nhấn **Enable** để kích hoạt API

## Bước 3: Tạo API Key

1. Đi tới **APIs & Services** > **Credentials**
2. Nhấn **Create Credentials** > **API Key**
3. Copy API key được tạo

## Bước 4: Cấu hình API Key trong ứng dụng

1. Mở file `web/src/services/geocodingService.js`
2. Thay thế `YOUR_GOOGLE_MAPS_API_KEY` bằng API key thực tế của bạn:

```javascript
this.apiKey = 'YOUR_ACTUAL_API_KEY_HERE';
```

## Bước 5: Bảo mật API Key (Khuyến nghị)

### Tùy chọn 1: Sử dụng Environment Variables

1. Tạo file `.env` trong thư mục `web/`:
```
REACT_APP_GOOGLE_MAPS_API_KEY=your_api_key_here
```

2. Cập nhật `geocodingService.js`:
```javascript
this.apiKey = process.env.REACT_APP_GOOGLE_MAPS_API_KEY;
```

### Tùy chọn 2: Giới hạn API Key

1. Trong Google Cloud Console, đi tới **APIs & Services** > **Credentials**
2. Nhấn vào API key của bạn
3. Trong **Application restrictions**, chọn:
   - **HTTP referrers** cho web app
   - **Android apps** cho Android app
   - **iOS apps** cho iOS app
4. Thêm domain/package name của bạn

## Bước 6: Giới hạn API Usage

1. Trong **APIs & Services** > **Quotas**
2. Đặt giới hạn cho Geocoding API để tránh chi phí không mong muốn
3. Khuyến nghị: 1000 requests/day cho development

## Lưu ý quan trọng

- **Không commit API key vào Git repository**
- **Sử dụng environment variables cho production**
- **Giới hạn API key theo domain/application**
- **Theo dõi usage và billing trong Google Cloud Console**

## Troubleshooting

### Lỗi "This API project is not authorized"
- Đảm bảo Geocoding API đã được kích hoạt
- Kiểm tra billing account

### Lỗi "API key not valid"
- Kiểm tra API key có đúng không
- Đảm bảo API key có quyền truy cập Geocoding API

### Lỗi "Quota exceeded"
- Kiểm tra quota limits trong Google Cloud Console
- Tăng quota nếu cần thiết

## Chi phí

- Geocoding API: $5 per 1,000 requests
- Có free tier: 40,000 requests/month
- Xem chi tiết tại [Google Maps Platform Pricing](https://cloud.google.com/maps-platform/pricing)
