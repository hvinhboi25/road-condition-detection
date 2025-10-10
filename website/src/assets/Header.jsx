
import React from 'react';

function Header() {
  return (
    <header className="bg-white shadow-md sticky top-0 z-10">
      <div className="container mx-auto px-4 py-4 flex justify-between items-center">
        <div className="flex items-center space-x-2">
          <span className="text-2xl font-bold text-primary-blue">Pothole AI</span>
          <p className="hidden sm:block text-sm text-gray-500 border-l ml-2 pl-2">Lịch sử phân loại hư hỏng mặt đường</p>
        </div>
        <nav className="space-x-4 text-sm font-medium hidden sm:flex">
          <a href="#" className="text-gray-600 hover:text-primary-blue">Trang chủ</a>
          <a href="#" className="text-primary-blue hover:text-primary-blue/80">Lịch sử</a>
          <a href="#" className="text-gray-600 hover:text-primary-blue">Giới thiệu</a>
        </nav>
      </div>
    </header>
  );
}

export default Header;