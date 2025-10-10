// src/App.jsx
import React from 'react';
import './index.css';

import Header from './assets/Header';
import Footer from './assets/Footer';
import FilterSidebar from './assets/FilterSidebar';
import SummaryCards from './assets/SummaryCards';
import ReportTable from './assets/ReportTable';

function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="container mx-auto px-4 py-8 flex-grow">
        <div className="max-w-6xl mx-auto bg-white p-6 md:p-10 rounded-xl shadow-lg">
          
          <h1 className="text-3xl font-extrabold text-gray-800 mb-8 text-center">LỊCH SỬ BÁO CÁO </h1>

          <div id="history-section" className="flex flex-col lg:flex-row gap-8">
            
            <FilterSidebar onApply={(filters) => console.log('Bộ lọc được chọn:', filters)} />

            <div className="flex-1">
              <SummaryCards />
              <ReportTable />
            </div>
          </div>
          
        </div>
      </main>

      <Footer />
    </div>
  );
}

export default App;