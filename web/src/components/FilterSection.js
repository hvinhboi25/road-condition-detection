import React from 'react';
import './FilterSection.css';

const FilterSection = ({ 
  searchTerm, 
  selectedLocation, 
  uniqueLocations, 
  items,
  onSearchChange, 
  onLocationChange, 
  onClearFilters 
}) => {
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

  return (
    <div className="filter-section">
      <div className="filter-header">
        <h3>Bộ lọc và tìm kiếm</h3>
      </div>
      
      <div className="filter-controls">
        <div className="filter-group">
          <label htmlFor="search-input" className="filter-label">
            Tìm kiếm
          </label>
          <input
            id="search-input"
            type="text"
            placeholder="Nhập địa điểm, tên đường hoặc điều kiện..."
            value={searchTerm}
            onChange={onSearchChange}
            className="search-input"
          />
        </div>
        
        <div className="filter-group">
          <label htmlFor="location-select" className="filter-label">
            Lọc theo vị trí
          </label>
          <select
            id="location-select"
            value={selectedLocation}
            onChange={onLocationChange}
            className="location-select"
          >
            <option value="all">Tất cả vị trí ({items.length})</option>
            {uniqueLocations.map((location, index) => (
              <option key={index} value={location}>
                {location} ({items.filter(item => {
                  const itemLocation = item.location?.address || `${item.location?.lat?.toFixed(4)}, ${item.location?.lng?.toFixed(4)}`;
                  return itemLocation === location;
                }).length})
              </option>
            ))}
          </select>
        </div>
        
        <div className="filter-actions">
          <button
            onClick={onClearFilters}
            className="clear-filters-btn"
            disabled={searchTerm === "" && selectedLocation === "all"}
          >
            Xóa bộ lọc
          </button>
        </div>
      </div>
      
      {(searchTerm !== "" || selectedLocation !== "all") && (
        <div className="filter-results-info">
          <span className="filter-info">
            Hiển thị {filteredItems.length} trong tổng số {items.length} phát hiện
            {searchTerm && ` cho từ khóa "${searchTerm}"`}
            {selectedLocation !== "all" && ` tại ${selectedLocation}`}
          </span>
        </div>
      )}
    </div>
  );
};

export default FilterSection;
