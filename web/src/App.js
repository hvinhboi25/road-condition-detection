import { useEffect, useState, useCallback } from "react";
import { db } from "./firebase";
import { collection, onSnapshot, orderBy, query } from "firebase/firestore";
import { useRealTime } from "./hooks/useRealTime";
import geocodingService from "./services/geocodingService";

// Components
import Header from "./components/Header";
import FilterSection from "./components/FilterSection";
import DetectionCard from "./components/DetectionCard";
import Modal from "./components/Modal";
import LoadingState from "./components/LoadingState";
import EmptyState from "./components/EmptyState";

// Styles
import "./styles/global.css";

export default function App() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedLocation, setSelectedLocation] = useState("all");
  const [uniqueLocations, setUniqueLocations] = useState([]);
  const [addressCache, setAddressCache] = useState(new Map());
  const [geocodingLoading, setGeocodingLoading] = useState(false);
  const [selectedItem, setSelectedItem] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  // Sử dụng hook thời gian thực
  const { currentTime, isOnline, formatTime, formatTimeShort, formatDate, getTimeAgo } = useRealTime();
  
  // State cho thời gian từ Firebase
  const [firebaseTime, setFirebaseTime] = useState(new Date());
  const [lastUpdateTime, setLastUpdateTime] = useState(null);
  
  // Cập nhật thời gian mỗi giây
  useEffect(() => {
    const interval = setInterval(() => {
      setFirebaseTime(new Date());
    }, 1000);
    return () => clearInterval(interval);
  }, []);
  
  // Debug log
  console.log('Current time:', currentTime);
  console.log('Firebase time:', firebaseTime);
  console.log('Last update time:', lastUpdateTime);
  console.log('Items data:', items);
  console.log('Formatted time:', formatTimeShort());
  console.log('Formatted date:', formatDate());

  // Hàm để lấy địa chỉ từ tọa độ
  const getAddressFromCoordinates = useCallback(async (lat, lng) => {
    if (!lat || !lng) return 'Tọa độ không hợp lệ';
    
    const cacheKey = `${lat.toFixed(6)},${lng.toFixed(6)}`;
    
    // Kiểm tra cache trước
    if (addressCache.has(cacheKey)) {
      return addressCache.get(cacheKey);
    }
    
    try {
      setGeocodingLoading(true);
      const address = await geocodingService.reverseGeocode(lat, lng);
      
      // Cập nhật cache
      setAddressCache(prev => new Map(prev).set(cacheKey, address));
      
      return address;
    } catch (error) {
      console.error('Lỗi khi lấy địa chỉ:', error);
      return `Vị trí: ${lat.toFixed(4)}, ${lng.toFixed(4)}`;
    } finally {
      setGeocodingLoading(false);
    }
  }, [addressCache]);

  useEffect(() => {
    const q = query(collection(db, "detections"), orderBy("timestamp", "desc"));
    const unsub = onSnapshot(q, async (snap) => {
      const data = snap.docs.map((d) => ({ id: d.id, ...d.data() }));
      setItems(data);
      
      // Xử lý địa chỉ cho các items mới
      const processedData = await Promise.all(data.map(async (item) => {
        // Kiểm tra nếu có tọa độ và chưa có địa chỉ
        if (item.location?.lat && item.location?.lng && !item.location?.address) {
          const address = await getAddressFromCoordinates(item.location.lat, item.location.lng);
          return {
            ...item,
            location: {
              ...item.location,
              address
            }
          };
        }
        // Nếu không có tọa độ, tạo fallback
        if (!item.location?.lat || !item.location?.lng) {
          return {
            ...item,
            location: {
              lat: 0,
              lng: 0,
              address: 'Vị trí không xác định'
            }
          };
        }
        return item;
      }));
      
      setItems(processedData);
      
      // Lấy thời gian mới nhất từ Firebase
      if (processedData.length > 0) {
        const latestItem = processedData[0]; // Đã được sort theo timestamp desc
        
        // Ưu tiên lấy từ field 'date' trước, sau đó mới đến timestamp
        if (latestItem.date) {
          // Parse thời gian từ string "2025-10-09 12:20:10"
          const dateTime = new Date(latestItem.date);
          if (!isNaN(dateTime.getTime())) {
            setLastUpdateTime(dateTime);
          }
        } else if (latestItem.timestamp?.toDate) {
          setLastUpdateTime(latestItem.timestamp.toDate());
        }
      }
      
      // Extract unique locations for filter
      const locations = [...new Set(processedData.map(item => {
        return item.location?.address || `${item.location?.lat?.toFixed(4)}, ${item.location?.lng?.toFixed(4)}`;
      }).filter(Boolean))];
      setUniqueLocations(locations);
      
      setLoading(false);
    });
    return () => unsub();
  }, [getAddressFromCoordinates]);

  const getConditionColor = (condition) => {
    switch (condition?.toLowerCase()) {
      case 'good':
        return '#10B981'; // Green
      case 'fair':
        return '#F59E0B'; // Yellow
      case 'poor':
        return '#EF4444'; // Red
      default:
        return '#6B7280'; // Gray
    }
  };

  const getConditionIcon = (condition) => {
    switch (condition?.toLowerCase()) {
      case 'good':
        return '🟢';
      case 'fair':
        return '🟡';
      case 'poor':
        return '🔴';
      default:
        return '⚪';
    }
  };

  const formatTimestamp = (item) => {
    // Ưu tiên sử dụng field 'date' trước
    if (item?.date) {
      const dateTime = new Date(item.date);
      if (!isNaN(dateTime.getTime())) {
        return getTimeAgo({ toDate: () => dateTime });
      }
    }
    
    // Fallback về timestamp
    if (item?.timestamp?.toDate) {
      return getTimeAgo(item.timestamp);
    }
    
    return 'Không xác định';
  };

  // Filter items based on search term and selected location
  const filteredItems = items.filter(item => {
    const itemLocation = item.location?.address || `${item.location?.lat?.toFixed(4)}, ${item.location?.lng?.toFixed(4)}`;
    
    // Filter by search term
    const matchesSearch = searchTerm === "" || 
      itemLocation.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.detections?.some(d => d.class?.toLowerCase().includes(searchTerm.toLowerCase()));
    
    // Filter by selected location
    const matchesLocation = selectedLocation === "all" || itemLocation === selectedLocation;
    
    return matchesSearch && matchesLocation;
  });

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleLocationChange = (e) => {
    setSelectedLocation(e.target.value);
  };

  const clearFilters = () => {
    setSearchTerm("");
    setSelectedLocation("all");
  };

  // Hàm để mở modal chi tiết
  const openModal = (item) => {
    setSelectedItem(item);
    setIsModalOpen(true);
  };

  // Hàm để đóng modal
  const closeModal = () => {
    setSelectedItem(null);
    setIsModalOpen(false);
  };

  // Hàm để đóng modal khi nhấn ESC
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') {
        closeModal();
      }
    };

    if (isModalOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden'; // Ngăn scroll background
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isModalOpen]);

  return (
    <div className="app">
      <Header 
        currentTime={firebaseTime}
        isOnline={isOnline}
        formatTimeShort={formatTimeShort}
        formatDate={formatDate}
        getTimeAgo={getTimeAgo}
        lastUpdateTime={lastUpdateTime}
        geocodingLoading={geocodingLoading}
        filteredItems={filteredItems}
        items={items}
        uniqueLocations={uniqueLocations}
      />

      <main className="main-content">
        <FilterSection 
          searchTerm={searchTerm}
          selectedLocation={selectedLocation}
          uniqueLocations={uniqueLocations}
          items={items}
          onSearchChange={handleSearchChange}
          onLocationChange={handleLocationChange}
          onClearFilters={clearFilters}
        />

        {loading ? (
          <LoadingState geocodingLoading={geocodingLoading} />
        ) : filteredItems.length === 0 ? (
          <EmptyState 
            searchTerm={searchTerm}
            selectedLocation={selectedLocation}
            onClearFilters={clearFilters}
          />
        ) : (
          <div className="detections-grid">
            {filteredItems.map((item) => (
              <DetectionCard
                key={item.id}
                item={item}
                onCardClick={openModal}
                getConditionIcon={getConditionIcon}
                getConditionColor={getConditionColor}
                formatTimestamp={formatTimestamp}
              />
            ))}
          </div>
        )}
      </main>

      <footer className="footer">
        <p>© 2025 Road Condition Detection System. Real-time monitoring.</p>
      </footer>

      <Modal 
        isOpen={isModalOpen}
        item={selectedItem}
        onClose={closeModal}
        formatTimestamp={formatTimestamp}
      />
    </div>
  );
}
