import React, { useEffect, useMemo, useRef, useState } from 'react';
import './Modal.css';
// Removed auto/manual recording & upload logic

const Modal = ({ isOpen, item, onClose, formatTimestamp }) => {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const [isRecording] = useState(false);

  const videoDetections = useMemo(() => item?.videoDetections || [], [item]);

  useEffect(() => {
    if (!isOpen || !item?.videoUrl) return;

    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (!video || !canvas) return;

    const ctx = canvas.getContext('2d');
    const compositeCtx = null;

    const resizeCanvas = () => {
      // Match canvas size to displayed video size
      const rect = video.getBoundingClientRect();
      canvas.width = rect.width;
      canvas.height = rect.height;
    };

    const drawForTime = () => {
      if (!videoDetections || videoDetections.length === 0) return;
      const tMs = (video.currentTime || 0) * 1000;

      // Find nearest detection entry by timeMs
      let nearest = null;
      let bestDiff = Infinity;
      for (const entry of videoDetections) {
        const diff = Math.abs((entry.timeMs || 0) - tMs);
        if (diff < bestDiff) {
          bestDiff = diff;
          nearest = entry;
        }
      }

      // Clear canvas
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      if (!nearest || !nearest.detections) return;

      // Draw boxes (normalized top-left x,y and width,height)
      ctx.lineWidth = 2;
      ctx.strokeStyle = '#ff3b30';
      ctx.font = '12px sans-serif';
      ctx.fillStyle = 'rgba(0,0,0,0.6)';
      ctx.textBaseline = 'top';

      nearest.detections.forEach((d) => {
        const x = (d.x || 0) * canvas.width;
        const y = (d.y || 0) * canvas.height;
        const w = (d.width || 0) * canvas.width;
        const h = (d.height || 0) * canvas.height;
        if (w > 1 && h > 1) {
          ctx.strokeRect(x, y, w, h);
          const label = `${d.className || ''} ${(Math.round((d.confidence || 0) * 100))}%`;
          const tw = ctx.measureText(label).width;
          const th = 14;
          ctx.fillRect(x, Math.max(0, y - th - 2), tw + 8, th + 2);
          ctx.fillStyle = '#fff';
          ctx.fillText(label, x + 4, Math.max(0, y - th - 2));
          ctx.fillStyle = 'rgba(0,0,0,0.6)';
        }
      });

      // No background composite drawing
    };

    const onLoadedMetadata = () => {
      resizeCanvas();
      drawForTime();
    };
    const onTimeUpdate = () => drawForTime();
    const onResize = () => resizeCanvas();

    video.addEventListener('loadedmetadata', onLoadedMetadata);
    video.addEventListener('timeupdate', onTimeUpdate);
    window.addEventListener('resize', onResize);

    // Cleanup
    return () => {
      video.removeEventListener('loadedmetadata', onLoadedMetadata);
      video.removeEventListener('timeupdate', onTimeUpdate);
      window.removeEventListener('resize', onResize);
    };
  }, [isOpen, item, videoDetections]);

  // No auto recording or uploading

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

          {/* Video nếu có */}
          {item.videoUrl && (
            <div className="modal-video-container">
              <div className="video-overlay-wrapper">
                <video
                  ref={videoRef}
                  className="modal-video"
                  src={item.videoUrl}
                  controls
                  controlsList="nodownload"
                />
                <canvas ref={canvasRef} className="video-overlay-canvas" />
              </div>
              {(!item.videoDetections || item.videoDetections.length === 0) && (
                <div className="video-hint">Không có dữ liệu bbox để overlay</div>
              )}
            </div>
          )}

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
          {item.videoUrl && (
            <a
              className="modal-btn modal-btn-primary"
              href={item.videoUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              Mở video tại tab mới
            </a>
          )}
          {/* No manual recording/upload buttons */}
        </div>

        {/* Composite canvas removed */}
      </div>
    </div>
  );
};

export default Modal;
