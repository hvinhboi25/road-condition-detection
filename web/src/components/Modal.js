import React from 'react';
import './Modal.css';

const Modal = ({ isOpen, item, onClose, formatTimestamp }) => {
  if (!isOpen || !item) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Chi tiết phát hiện</h2>
          <button className="modal-close" onClick={onClose}>
            <span>×</span>
          </button>
        </div>
        
        <div className="modal-body">
          {/* Hình ảnh lớn */}
          <div className="modal-image-container">
            <img
              src={item.imageUrl}
              alt="Road condition detection"
              className="modal-image"
            />
            <div className="modal-image-overlay">
              <span className="modal-confidence">
                {Math.round((item.detections?.[0]?.confidence || 0) * 100)}% confidence
              </span>
            </div>
          </div>

          {/* Thông tin chi tiết */}
          <div className="modal-details">
            <div className="detail-section">
              <h3>Thông tin vị trí</h3>
              <p className="detail-item">
                <strong>Địa chỉ:</strong> {item.location?.address || 'Vị trí không xác định'}
              </p>
              {item.location?.lat && item.location?.lng && (
                <p className="detail-item">
                  <strong>Tọa độ:</strong> {item.location.lat.toFixed(6)}, {item.location.lng.toFixed(6)}
                </p>
              )}
            </div>

            <div className="detail-section">
              <h3>Thông tin thời gian</h3>
              <p className="detail-item">
                <strong>Thời gian chụp:</strong> {
                  item.date 
                    ? new Date(item.date).toLocaleString('vi-VN')
                    : item.timestamp?.toDate?.().toLocaleString('vi-VN') || 'Không xác định'
                }
              </p>
            </div>


            <div className="detail-section">
              <h3>Thống kê</h3>
              <p className="detail-item">
                <strong>Số lượng phát hiện:</strong> {item.detectionCount || item.detections?.length || 0}
              </p>
              <p className="detail-item">
                <strong>ID phát hiện:</strong> {item.id}
              </p>
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button className="modal-btn modal-btn-secondary" onClick={onClose}>
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
};

export default Modal;
