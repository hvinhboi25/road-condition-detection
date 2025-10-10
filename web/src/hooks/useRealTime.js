import { useState, useEffect } from 'react';

// Hook để quản lý thời gian thực
export const useRealTime = () => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  useEffect(() => {
    // Cập nhật thời gian mỗi giây
    const timeInterval = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    // Lắng nghe sự kiện online/offline
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    // Cleanup
    return () => {
      clearInterval(timeInterval);
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // Format thời gian theo định dạng Việt Nam
  const formatTime = (date = currentTime) => {
    return date.toLocaleString('vi-VN', {
      timeZone: 'Asia/Ho_Chi_Minh',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    });
  };

  // Format thời gian ngắn gọn
  const formatTimeShort = (date = currentTime) => {
    return date.toLocaleTimeString('vi-VN', {
      timeZone: 'Asia/Ho_Chi_Minh',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    });
  };

  // Format ngày
  const formatDate = (date = currentTime) => {
    return date.toLocaleDateString('vi-VN', {
      timeZone: 'Asia/Ho_Chi_Minh',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'long'
    });
  };

  // Tính thời gian đã trôi qua từ một thời điểm
  const getTimeAgo = (timestamp) => {
    if (!timestamp) return 'Không xác định';
    
    const now = currentTime;
    const past = timestamp.toDate ? timestamp.toDate() : new Date(timestamp);
    const diffMs = now - past;
    
    if (diffMs < 0) return 'Trong tương lai';
    
    const diffSeconds = Math.floor(diffMs / 1000);
    const diffMinutes = Math.floor(diffSeconds / 60);
    const diffHours = Math.floor(diffMinutes / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffSeconds < 60) return 'Vừa xong';
    if (diffMinutes < 60) return `${diffMinutes} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    if (diffDays < 7) return `${diffDays} ngày trước`;
    
    return past.toLocaleDateString('vi-VN');
  };

  return {
    currentTime,
    isOnline,
    formatTime,
    formatTimeShort,
    formatDate,
    getTimeAgo
  };
};
