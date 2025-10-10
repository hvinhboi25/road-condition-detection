# Road Condition Detection Web Dashboard

Dashboard web để giám sát thời gian thực tình trạng đường bộ với khả năng chuyển đổi tọa độ thành địa chỉ thực tế.

## ✨ Tính năng mới

### 🕐 Thời gian thực
- Hiển thị thời gian hiện tại cập nhật mỗi giây
- Hiển thị ngày tháng theo định dạng Việt Nam
- Trạng thái kết nối mạng (trực tuyến/ngoại tuyến)
- Thời gian tương đối cho các phát hiện (ví dụ: "5 phút trước")

### 🗺️ Chuyển đổi tọa độ thành địa chỉ
- Tự động chuyển đổi tọa độ (lat, lng) thành địa chỉ thực tế
- Sử dụng Google Maps Geocoding API
- Cache địa chỉ để tối ưu hiệu suất
- Hiển thị địa chỉ ngắn gọn và dễ đọc

### 🔍 Tìm kiếm và lọc nâng cao
- Tìm kiếm theo địa chỉ hoặc điều kiện đường
- Lọc theo vị trí cụ thể
- Hiển thị số lượng phát hiện cho mỗi vị trí

## 🚀 Cài đặt và chạy

### 1. Cài đặt dependencies
```bash
cd web
npm install
```

### 2. Cấu hình Google Maps API
Xem hướng dẫn chi tiết trong [GOOGLE_MAPS_SETUP.md](./GOOGLE_MAPS_SETUP.md)

Tạo file `.env` trong thư mục `web/`:
```
REACT_APP_GOOGLE_MAPS_API_KEY=your_api_key_here
```

### 3. Chạy ứng dụng
```bash
npm start
```

Ứng dụng sẽ chạy tại `http://localhost:3000`

## 📁 Cấu trúc dự án

```
web/
├── src/
│   ├── components/          # React components
│   ├── hooks/              # Custom React hooks
│   │   └── useRealTime.js  # Hook quản lý thời gian thực
│   ├── services/           # API services
│   │   └── geocodingService.js  # Google Maps Geocoding service
│   ├── App.js              # Component chính
│   ├── App.css             # Styles
│   └── firebase.js         # Firebase configuration
├── .env                    # Environment variables
├── GOOGLE_MAPS_SETUP.md    # Hướng dẫn cấu hình Google Maps
└── README.md              # File này
```

## 🔧 Cấu hình

### Environment Variables
- `REACT_APP_GOOGLE_MAPS_API_KEY`: API key của Google Maps

### Firebase Configuration
Cấu hình Firebase trong `src/firebase.js` để kết nối với Firestore database.

## 🎨 Giao diện

### Header
- Tiêu đề ứng dụng với icon
- Thời gian thực hiển thị
- Trạng thái kết nối mạng
- Thống kê tổng quan

### Bộ lọc
- Tìm kiếm theo từ khóa
- Lọc theo vị trí
- Nút xóa bộ lọc

### Danh sách phát hiện
- Card hiển thị từng phát hiện
- Hình ảnh với overlay confidence
- Địa chỉ đã được chuyển đổi
- Thời gian tương đối
- Tags điều kiện đường

## 🔄 Tối ưu hiệu suất

### Cache
- Cache địa chỉ đã chuyển đổi để tránh gọi API lặp lại
- Cache được lưu trong memory

### Lazy Loading
- Hình ảnh được load lazy
- Chỉ hiển thị items được filter

### Real-time Updates
- Cập nhật thời gian mỗi giây
- Lắng nghe thay đổi từ Firestore
- Tự động cập nhật địa chỉ cho items mới

## 📱 Responsive Design

- Tối ưu cho desktop, tablet và mobile
- Layout linh hoạt với CSS Grid và Flexbox
- Breakpoints: 768px, 480px

## 🛠️ Development

### Scripts
```bash
npm start          # Chạy development server
npm run build      # Build cho production
npm test           # Chạy tests
npm run eject      # Eject từ Create React App
```

### Dependencies chính
- React 19.2.0
- Firebase 12.3.0
- Axios 1.6.0 (cho Google Maps API)

## 🐛 Troubleshooting

### Lỗi thường gặp

1. **"Google Maps API Key chưa được cấu hình"**
   - Kiểm tra file `.env` có API key đúng không
   - Xem hướng dẫn trong `GOOGLE_MAPS_SETUP.md`

2. **Địa chỉ không hiển thị**
   - Kiểm tra kết nối internet
   - Kiểm tra quota Google Maps API
   - Xem console để debug

3. **Thời gian không cập nhật**
   - Kiểm tra browser có hỗ trợ JavaScript không
   - Refresh trang

## 📄 License

Dự án này thuộc về Road Condition Detection System.

## 🤝 Đóng góp

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request