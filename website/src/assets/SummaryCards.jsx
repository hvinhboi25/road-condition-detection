import React from 'react';

const summaryData = [
  { 
    label: 'Tổng Số Báo Cáo', 
    value: '1,250', 
    borderClass: 'border-blue-600', 
    bgClass: 'bg-blue-50',          
    valueClass: 'text-gray-800' 
  },
];

function SummaryCards() {
  return (
    <>
      <h2 className="text-xl font-semibold text-gray-800 mb-4 pb-2 border-b">
        Tổng Quan Dữ Liệu
      </h2>
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        {summaryData.map((item, index) => (
          <div 
            key={index} 
            className={`p-4 rounded-lg shadow-md border-l-4 ${item.bgClass} ${item.borderClass}`}
          >
            <p className="text-sm text-gray-500">{item.label}</p>
            <p className={`text-2xl font-bold mt-1 ${item.valueClass}`}>{item.value}</p>
          </div>
        ))}
      </div>
    </>
  );
}

export default SummaryCards;
