// Sử dụng fetch API thay vì axios để tránh dependency

// Google Maps Geocoding API service
class GeocodingService {
  constructor() {
    // Sử dụng environment variable hoặc fallback
    this.apiKey = process.env.REACT_APP_GOOGLE_MAPS_API_KEY || 'YOUR_GOOGLE_MAPS_API_KEY';
    this.baseUrl = 'https://maps.googleapis.com/maps/api/geocode/json';
    this.cache = new Map(); // Cache để tránh gọi API nhiều lần cho cùng một tọa độ
    
    // Kiểm tra API key
    if (this.apiKey === 'YOUR_GOOGLE_MAPS_API_KEY') {
      console.warn('⚠️ Google Maps API Key chưa được cấu hình. Vui lòng xem hướng dẫn trong GOOGLE_MAPS_SETUP.md');
    }
  }

  // Chuyển đổi tọa độ (lat, lng) thành địa chỉ
  async reverseGeocode(lat, lng) {
    if (!lat || !lng) {
      return 'Tọa độ không hợp lệ';
    }

    // Tạo key cho cache
    const cacheKey = `${lat.toFixed(6)},${lng.toFixed(6)}`;
    
    // Kiểm tra cache trước
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey);
    }

    // Kiểm tra nếu API key chưa được cấu hình
    if (this.apiKey === 'YOUR_GOOGLE_MAPS_API_KEY') {
      console.log('📍 Sử dụng tọa độ thay vì địa chỉ (API key chưa cấu hình)');
      const fallbackAddress = `Vị trí: ${lat.toFixed(4)}, ${lng.toFixed(4)}`;
      this.cache.set(cacheKey, fallbackAddress);
      return fallbackAddress;
    }

    try {
      // Sử dụng fetch API thay vì axios
      const params = new URLSearchParams({
        latlng: `${lat},${lng}`,
        key: this.apiKey,
        language: 'vi', // Ngôn ngữ tiếng Việt
        region: 'VN' // Khu vực Việt Nam
      });
      
      const response = await fetch(`${this.baseUrl}?${params}`);
      const data = await response.json();

      if (data.status === 'OK' && data.results.length > 0) {
        const result = data.results[0];
        const address = this.formatAddress(result);
        
        // Lưu vào cache
        this.cache.set(cacheKey, address);
        
        return address;
      } else {
        const fallbackAddress = `Vị trí: ${lat.toFixed(4)}, ${lng.toFixed(4)}`;
        this.cache.set(cacheKey, fallbackAddress);
        return fallbackAddress;
      }
    } catch (error) {
      console.error('Lỗi khi gọi Google Geocoding API:', error);
      const fallbackAddress = `Vị trí: ${lat.toFixed(4)}, ${lng.toFixed(4)}`;
      this.cache.set(cacheKey, fallbackAddress);
      return fallbackAddress;
    }
  }

  // Format địa chỉ từ kết quả Google API
  formatAddress(result) {
    const components = result.address_components;
    let address = result.formatted_address;

    // Tìm tên đường chính
    const route = components.find(comp => comp.types.includes('route'));
    const streetNumber = components.find(comp => comp.types.includes('street_number'));
    const ward = components.find(comp => comp.types.includes('sublocality_level_1'));
    const district = components.find(comp => comp.types.includes('administrative_area_level_2'));
    const city = components.find(comp => comp.types.includes('administrative_area_level_1'));

    // Tạo địa chỉ ngắn gọn hơn
    let shortAddress = '';
    
    if (streetNumber && route) {
      shortAddress = `${streetNumber.long_name} ${route.long_name}`;
    } else if (route) {
      shortAddress = route.long_name;
    }

    if (ward) {
      shortAddress += `, ${ward.long_name}`;
    }
    
    if (district) {
      shortAddress += `, ${district.long_name}`;
    }
    
    if (city) {
      shortAddress += `, ${city.long_name}`;
    }

    return shortAddress || address;
  }

  // Batch reverse geocoding cho nhiều tọa độ
  async batchReverseGeocode(coordinates) {
    const promises = coordinates.map(({ lat, lng }) => 
      this.reverseGeocode(lat, lng)
    );
    
    return Promise.all(promises);
  }

  // Xóa cache
  clearCache() {
    this.cache.clear();
  }

  // Lấy số lượng items trong cache
  getCacheSize() {
    return this.cache.size;
  }
}

// Export singleton instance
export default new GeocodingService();
