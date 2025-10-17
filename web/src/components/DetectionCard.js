import React from 'react';
import './DetectionCard.css';

const DetectionCard = ({ item, onCardClick, getConditionIcon, getConditionColor, formatTimestamp }) => {
  return (
    <div 
      className="detection-card"
      onClick={() => onCardClick(item)}
      style={{ cursor: 'pointer' }}
    >
      <div className="card-header">
        <div className="timestamp">
          {formatTimestamp(item)}
        </div>
      </div>

      <div className="image-container">
        <img
          src={item.imageUrl}
          alt="Road condition detection"
          className="detection-image"
          loading="lazy"
        />
        <div className="image-overlay">
          <span className="confidence">
            {Math.round((item.detections?.[0]?.confidence || 0) * 100)}% confidence
          </span>
        </div>
      </div>

      <div className="card-content">
        <div className="location">
          <span className="location-icon">📍</span>
          <span className="location-text">
            {item.location?.address || 
             `${item.location?.lat?.toFixed(4)}, ${item.location?.lng?.toFixed(4)}`}
          </span>
        </div>

        {item.detections && item.detections.length > 0 && (
          <div className="detections">
            <h4>Detected Conditions:</h4>
            <div className="detection-tags">
              {item.detections.map((detection, index) => (
                <span 
                  key={index} 
                  className="detection-tag"
                  style={{ backgroundColor: getConditionColor(detection.class) }}
                >
                  {detection.class} ({Math.round(detection.confidence * 100)}%)
                </span>
              ))}
            </div>
          </div>
        )}

        <div className="card-footer">
          <span className="full-time">
            {item.date 
              ? new Date(item.date).toLocaleString('vi-VN')
              : item.timestamp?.toDate?.().toLocaleString('vi-VN')
            }
          </span>

          {item.videoUrl && (
            <a
              className="view-video-button"
              href={item.videoUrl}
              target="_blank"
              rel="noopener noreferrer"
              onClick={(e) => e.stopPropagation()}
            >
              🎬 Xem video
            </a>
          )}
        </div>
      </div>
    </div>
  );
};

export default DetectionCard;
