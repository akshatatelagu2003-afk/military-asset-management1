import React from 'react';
import './MetricCard.css';

const MetricCard = ({ title, value, icon, highlight, onClick }) => {
  return (
    <div 
      className={`metric-card ${highlight ? 'highlight' : ''} ${onClick ? 'clickable' : ''}`}
      onClick={onClick}
    >
      <div className="metric-header">
        <h3 className="metric-title">{title}</h3>
        {icon && <div className="metric-icon-wrapper">{icon}</div>}
      </div>
      <div className="metric-value">
        {value !== undefined && value !== null ? value : '-'}
      </div>
      {highlight && (
        <div className="highlight-indicator">
          Click for formula details
        </div>
      )}
    </div>
  );
};

export default MetricCard;
