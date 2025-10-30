// Sử dụng fetch API thay vì axios để tránh dependency

// OpenStreetMap (Nominatim) reverse geocoding service
class GeocodingService {
  constructor() {
    // Nominatim không cần API key. Khuyến nghị cung cấp email nhận liên hệ theo policy
    this.contactEmail = process.env.REACT_APP_CONTACT_EMAIL || 'hahuuvinh2003@gmail.comcom';
    // Giữ thuộc tính apiKey để tương thích với UI cũ
    this.apiKey = 'OSM_NOMINATIM';
    this.baseUrl = 'https://nominatim.openstreetmap.org/reverse';
    this.minIntervalMs = 1100; // giới hạn ~1 req/giây theo policy Nominatim
    this.lastRequestAt = 0;
    
    // Load cache từ localStorage
    this.cache = this.loadCacheFromStorage();
    
    console.log(`Sử dụng OpenStreetMap Nominatim cho reverse geocoding (đã load ${this.cache.size} địa chỉ từ cache)`);
  }

  // Load cache từ localStorage
  loadCacheFromStorage() {
    try {
      const saved = localStorage.getItem('geocodingCache');
      if (saved) {
        const parsed = JSON.parse(saved);
        return new Map(Object.entries(parsed));
      }
    } catch (error) {
      console.error('Lỗi khi load geocoding cache:', error);
    }
    return new Map();
  }

  // Lưu cache vào localStorage
  saveCacheToStorage() {
    try {
      // Giới hạn cache tối đa 1000 địa chỉ
      const MAX_CACHE_SIZE = 1000;
      let cacheToSave = this.cache;
      
      if (this.cache.size > MAX_CACHE_SIZE) {
        const entries = Array.from(this.cache.entries());
        cacheToSave = new Map(entries.slice(-MAX_CACHE_SIZE));
        this.cache = cacheToSave;
        console.log(`⚠️ Geocoding cache đã đạt giới hạn, chỉ giữ lại ${MAX_CACHE_SIZE} địa chỉ gần nhất`);
      }
      
      const cacheObj = Object.fromEntries(cacheToSave);
      localStorage.setItem('geocodingCache', JSON.stringify(cacheObj));
    } catch (error) {
      console.error('Lỗi khi lưu geocoding cache:', error);
      // Nếu localStorage đầy, xóa cache
      if (error.name === 'QuotaExceededError') {
        console.warn('localStorage đầy, xóa geocoding cache...');
        localStorage.removeItem('geocodingCache');
        this.cache.clear();
      }
    }
  }

  // Chuyển đổi tọa độ (lat, lng) thành địa chỉ
  async reverseGeocode(lat, lng) {
    if (!lat || !lng) {
      return 'Tọa độ không hợp lệ';
    }

    // Tạo key cho cache với độ chính xác 4 chữ số (≈10m) để tối ưu cache
    const cacheKey = `${lat.toFixed(4)},${lng.toFixed(4)}`;
    
    // Kiểm tra cache trước
    if (this.cache.has(cacheKey)) {
      console.log(`✓ Geocoding cache hit: ${cacheKey}`);
      return this.cache.get(cacheKey);
    }

    try {
      // Gọi Nominatim: https://nominatim.org/release-docs/develop/api/Reverse/
      // Tôn trọng rate-limit bằng cách chờ nếu cần
      const now = Date.now();
      const waitMs = Math.max(0, this.minIntervalMs - (now - this.lastRequestAt));
      if (waitMs > 0) {
        await new Promise(r => setTimeout(r, waitMs));
      }
      const params = new URLSearchParams({
        lat: String(lat),
        lon: String(lng),
        format: 'jsonv2',
        addressdetails: '1',
        'accept-language': 'vi',
        zoom: '18'
      });

      if (this.contactEmail) {
        params.set('email', this.contactEmail);
      }

      const response = await fetch(`${this.baseUrl}?${params.toString()}`, {
        headers: {
          'User-Agent': `RoadConditionApp/1.0 (${this.contactEmail || 'contact@unknown'})`,
          'Accept': 'application/json'
        }
      });
      this.lastRequestAt = Date.now();
      const data = await response.json();

      if (data && data.address) {
        const address = this.formatAddress(data);
        
        // Lưu vào cache
        this.cache.set(cacheKey, address);
        this.saveCacheToStorage();
        
        return address;
      } else {
        const fallbackAddress = `Vị trí: ${lat.toFixed(4)}, ${lng.toFixed(4)}`;
        this.cache.set(cacheKey, fallbackAddress);
        this.saveCacheToStorage();
        return fallbackAddress;
      }
    } catch (error) {
      console.error('Lỗi khi gọi OpenStreetMap Nominatim:', error);
      const fallbackAddress = `Vị trí: ${lat.toFixed(4)}, ${lng.toFixed(4)}`;
      this.cache.set(cacheKey, fallbackAddress);
      this.saveCacheToStorage();
      return fallbackAddress;
    }
  }

  // Format địa chỉ từ kết quả Nominatim
  formatAddress(result) {
    const addr = result.address || {};
    // Ưu tiên hiển thị: số nhà + đường, phường/xã, quận/huyện, thành phố/tỉnh
    const road = addr.road || addr.pedestrian || addr.cycleway || addr.footway || '';
    const houseNumber = addr.house_number || '';
    const suburb = addr.suburb || addr.quarter || addr.neighbourhood || addr.village || addr.town || '';
    const ward = addr.suburb || addr.city_district || addr.county || '';
    const district = addr.city_district || addr.district || addr.county || '';
    const city = addr.city || addr.town || addr.village || addr.state || '';

    let parts = [];
    if (houseNumber && road) parts.push(`${houseNumber} ${road}`);
    else if (road) parts.push(road);
    if (suburb && suburb !== road) parts.push(suburb);
    if (ward && ward !== suburb) parts.push(ward);
    if (district && district !== ward) parts.push(district);
    if (city) parts.push(city);

    const shortAddress = parts.filter(Boolean).join(', ');
    return shortAddress || result.display_name || '';
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
    try {
      localStorage.removeItem('geocodingCache');
    } catch (error) {
      console.error('Lỗi khi xóa geocoding cache:', error);
    }
  }

  // Lấy số lượng items trong cache
  getCacheSize() {
    return this.cache.size;
  }
}

// Export singleton instance
export default new GeocodingService();
