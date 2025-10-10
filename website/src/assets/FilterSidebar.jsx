
import React, { useState } from 'react';

function FilterSidebar({ onApply }) {
  const [confidence, setConfidence] = useState(80);
  // Các state khác cho thời gian, v.v.

  const handleApply = () => {
    onApply({ confidence });
  };

  return (
    <div className="lg:w-64 p-4 bg-gray-50 rounded-xl shadow-inner h-fit space-y-6">
      <h3 className="text-lg font-bold text-gray-700 border-b pb-2 mb-4">Bộ Lọc Dữ Liệu</h3>
      
      {/* Ô lọc Thời gian */}
      <div>
        <label htmlFor="time-quick-select" className="block text-sm font-bold text-gray-700 mb-1">Thời Gian</label>
        <select id="time-quick-select" className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-primary-blue focus:border-primary-blue">
          <option>Tất cả thời gian</option>
          <option>Hôm nay</option>
          <option>7 ngày qua</option>
          <option>30 ngày qua</option>
          <option>Tùy chỉnh...</option>
        </select>
        <p className="text-xs font-semibold text-gray-500 pt-2">Khoảng ngày tùy chỉnh</p>
        <div className="space-y-2 mt-1">
          <div className="relative">
            <label htmlFor="date-start" className="block text-sm font-medium text-gray-700 mb-1">Từ ngày</label>
            <input type="date" id="date-start" className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-primary-blue focus:border-primary-blue" />
          </div>
          <div className="relative">
            <label htmlFor="date-end" className="block text-sm font-medium text-gray-700 mb-1">Đến ngày</label>
            <input type="date" id="date-end" className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-primary-blue focus:border-primary-blue" />
          </div>
        </div>
      </div>
      
      {/* Ô lọc Độ tin cậy */}
      <div>
        <label htmlFor="confidence-range" className="block text-sm font-bold text-gray-700 mb-2">Độ Tin Cậy (Tối thiểu)</label>
        <div className="flex items-center space-x-2">
          <input
            type="range"
            id="confidence-range"
            min="0"
            max="100"
            value={confidence}
            className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer range-lg"
            onChange={(e) => setConfidence(e.target.value)}
          />
          <span id="confidence-value" className="text-sm font-semibold text-gray-800 w-10">{confidence}%</span>
        </div>
        <p className="text-xs text-gray-500 mt-1">Chỉ hiển thị kết quả &gt;= {confidence}%</p>
      </div>

      {/* 🔘 Nút Áp Dụng */}

      <button
        onClick={handleApply}
        className="w-full py-2 rounded-lg font-bold bg-blue-500 text-white hover:bg-blue-600 transition-all duration-200 shadow-md"
      >
        Áp Dụng Bộ Lọc
      </button>
    </div>
  );
}

export default FilterSidebar;
