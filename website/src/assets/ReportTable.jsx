import React from 'react';
import TableRow from './TableRow'; // Import Component con

const mockReports = [
  { id: 1, time: '20/05/2025 14:30', type: 'Ổ Gà Nghiêm Trọng', confidence: '98.5%', location: '10.8231° N, 106.6297° E' },
  { id: 2, time: '19/05/2025 09:15', type: 'Ổ Gà Nhẹ', confidence: '75.2%', location: '16.0544° N, 108.2022° E' },
  { id: 3, time: '18/05/2025 17:00', type: 'Không Phát Hiện', confidence: '99.9%', location: '21.0285° N, 105.8542° E' },
];

function ReportTable() {
  return (
    <div className="mt-8">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold text-gray-800">Các Báo Cáo Đã Lưu</h2>
        <div className="flex space-x-2">
          <input 
            type="text" 
            placeholder="Tìm kiếm theo vị trí, thời gian..." 
            className="border border-gray-300 rounded-lg p-2 text-sm w-64 focus:ring-primary-blue focus:border-primary-blue" 
          />
          <button className="bg-gray-200 text-gray-700 font-semibold py-2 px-4 rounded-lg hover:bg-gray-300 transition duration-200 text-sm">
            Xuất CSV
          </button>
        </div>
      </div>

      <div className="overflow-x-auto shadow-md rounded-lg">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-blue-200">
            <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-bold text-black-900 uppercase tracking-wider">Thời Gian</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-bold text-black-900 uppercase tracking-wider">Độ Tin Cậy</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-bold text-black-900 uppercase tracking-wider">Vị Trí (Mô phỏng)</th>
                <th scope="col" className="px-6 py-3 text-right text-xs font-bold text-black-900 uppercase tracking-wider">Hành Động</th>
            </tr>
            </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {mockReports.map(report => (
              <TableRow key={report.id} report={report} />
            ))}
          </tbody>
        </table>
      </div>

        <div className="mt-4 flex justify-between items-center text-sm text-gray-600">
        <div>Hiển thị 1 đến 3 của 3 kết quả</div>
            <div>
                <button className="px-3 py-1 border rounded-md bg-gray-100 hover:bg-gray-200 disabled:opacity-50" disabled>Trước</button>
                <button className="px-3 py-1 border rounded-md bg-blue-500 text-white mx-1 hover:bg-blue-600">1</button>
                <button className="px-3 py-1 border rounded-md bg-gray-100 hover:bg-gray-200 disabled:opacity-50" disabled>Sau</button>
            </div>
        </div>
    </div>
  );
}

export default ReportTable;