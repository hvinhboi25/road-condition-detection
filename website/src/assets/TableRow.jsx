import React from 'react';

// Định nghĩa props mà component này sẽ nhận (data)
function TableRow({ report }) {
  let typeTag;
  let tagColor;
  
  if (report.type === 'Ổ Gà Nghiêm Trọng') {
    typeTag = 'Ổ Gà Nghiêm Trọng';
    tagColor = 'bg-red-100 text-danger';
  } else if (report.type === 'Ổ Gà Nhẹ') {
    typeTag = 'Ổ Gà Nhẹ';
    tagColor = 'bg-green-100 text-success';
  } else {
    typeTag = 'Không Phát Hiện';
    tagColor = 'bg-blue-100 text-primary-blue';
  }

  return (
    <tr>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {report.time}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-medium">
        {report.confidence}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
        {report.location}
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
        <button className="text-primary-blue hover:text-primary-blue/80 mr-3">Xem Chi Tiết</button>
        <button className="text-gray-500 hover:text-danger">Xóa</button>
      </td>
    </tr>
  );
}

export default TableRow;