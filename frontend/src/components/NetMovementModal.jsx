import React from 'react';
import { X, Plus, Minus, Equal } from 'lucide-react';
import './NetMovementModal.css';

const NetMovementModal = ({ isOpen, onClose, metrics }) => {
  if (!isOpen || !metrics) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Net Movement Breakdown</h2>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>
        
        <div className="modal-body">
          <div className="formula-display">
            <div className="formula-part">
              <span className="part-label">Purchases</span>
              <span className="part-value positive">{metrics.purchases}</span>
            </div>
            
            <div className="formula-operator"><Plus size={16} /></div>
            
            <div className="formula-part">
              <span className="part-label">Transfer In</span>
              <span className="part-value positive">{metrics.transferIn}</span>
            </div>
            
            <div className="formula-operator"><Minus size={16} /></div>
            
            <div className="formula-part">
              <span className="part-label">Transfer Out</span>
              <span className="part-value negative">{metrics.transferOut}</span>
            </div>
            
            <div className="formula-operator"><Equal size={16} /></div>
            
            <div className="formula-part highlight">
              <span className="part-label">Net Movement</span>
              <span className={`part-value ${metrics.netMovement >= 0 ? 'positive' : 'negative'}`}>
                {metrics.netMovement}
              </span>
            </div>
          </div>
          
          <div className="formula-explanation">
            <p><strong>Formula:</strong> Net Movement = Purchases + Transfer In - Transfer Out</p>
            <p className="note">Note: Assigned and Expended items do not affect Net Movement, they only affect the Closing Balance.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NetMovementModal;
