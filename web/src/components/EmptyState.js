import React from 'react';
import './EmptyState.css';

const EmptyState = ({ searchTerm, selectedLocation, onClearFilters }) => {
  const hasFilters = searchTerm !== "" || selectedLocation !== "all";
  
  return (
    <div className="empty-state">
      <div className="empty-icon">
        {hasFilters ? "SEARCH" : "NO DATA"}
      </div>
      <h3>
        {hasFilters 
          ? "Không tìm thấy kết quả phù hợp" 
          : "Chưa có dữ liệu phát hiện"}
      </h3>
      <p>
        {hasFilters
          ? "Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc vị trí"
          : "Dữ liệu tình trạng đường bộ sẽ hiển thị ở đây khi có sẵn"}
      </p>
      {hasFilters && (
        <button onClick={onClearFilters} className="clear-filters-btn">
          Xóa bộ lọc
        </button>
      )}
    </div>
  );
};

export default EmptyState;
