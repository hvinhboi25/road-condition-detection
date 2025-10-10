import React from 'react';
import './Header.css';

const Header = ({ 
  currentTime, 
  isOnline, 
  formatTimeShort, 
  formatDate, 
  getTimeAgo, 
  lastUpdateTime, 
  geocodingLoading,
  filteredItems,
  items,
  uniqueLocations 
}) => {
  return (
    <header className="header">
      <div className="header-content">
        <h1 className="title">
        
          Road Condition Dashboard
        </h1>
        
        {/* Hiển thị thời gian thực */}
        <div className="realtime-info">
          <div className="realtime-clock">
            <span className="clock-icon">🕐</span>
            <span className="current-time">
              {lastUpdateTime 
                ? lastUpdateTime.toLocaleTimeString('vi-VN', { hour12: false })
                : currentTime.toLocaleTimeString('vi-VN', { hour12: false })
              }
            </span>
          </div>
          <div className="realtime-date">
            <span className="date-text">
              {lastUpdateTime 
                ? lastUpdateTime.toLocaleDateString('vi-VN', { 
                    year: 'numeric', 
                    month: 'long', 
                    day: 'numeric', 
                    weekday: 'long' 
                  })
                : currentTime.toLocaleDateString('vi-VN', { 
                    year: 'numeric', 
                    month: 'long', 
                    day: 'numeric', 
                    weekday: 'long' 
                  })
              }
            </span>
          </div>
          {geocodingLoading && (
            <div className="geocoding-status">
              <span className="loading-icon">LOADING</span>
              <span className="loading-text">Đang cập nhật địa chỉ...</span>
            </div>
          )}
          {process.env.REACT_APP_GOOGLE_MAPS_API_KEY === 'YOUR_GOOGLE_MAPS_API_KEY' && (
            <div className="api-key-notice">
              <span className="notice-icon">WARNING</span>
              <span className="notice-text">Chưa cấu hình Google Maps API - hiển thị tọa độ</span>
            </div>
          )}
          {lastUpdateTime && (
            <div className="last-update-info">
              <span className="update-text">
                Cập nhật lần cuối: {getTimeAgo({ toDate: () => lastUpdateTime })}
              </span>
            </div>
          )}
        </div>
        <div className="stats">
          <div className="stat-item">
            <span className="stat-number">{filteredItems.length}</span>
            <span className="stat-label">Filtered Detections</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">
              {filteredItems.filter(item => item.detections?.some(d => d.class?.toLowerCase() === 'good')).length}
            </span>
            <span className="stat-label">Good Conditions</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">
              {filteredItems.filter(item => item.detections?.some(d => d.class?.toLowerCase() === 'poor')).length}
            </span>
            <span className="stat-label">Poor Conditions</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">{uniqueLocations.length}</span>
            <span className="stat-label">Total Locations</span>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
