import React from 'react';
import './LoadingState.css';

const LoadingState = ({ geocodingLoading }) => {
  return (
    <div className="loading">
      <div className="spinner"></div>
      <p>Đang tải dữ liệu tình trạng đường bộ...</p>
      {geocodingLoading && <p className="geocoding-loading">Đang cập nhật địa chỉ từ tọa độ...</p>}
    </div>
  );
};

export default LoadingState;
