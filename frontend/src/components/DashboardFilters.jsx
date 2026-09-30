import React from 'react';
import { Filter } from 'lucide-react';
import './DashboardFilters.css';

const DashboardFilters = ({ 
  bases, 
  equipment, 
  selectedBase, 
  setSelectedBase, 
  selectedEquipment, 
  setSelectedEquipment, 
  startDate, 
  setStartDate, 
  endDate, 
  setEndDate,
  onApply,
  userRole,
  userBaseId
}) => {
  
  // Base Commander can only see their own base (or we just disable the selector)
  const isBaseCommander = userRole === 'BASE_COMMANDER';

  return (
    <div className="filters-container">
      <div className="filters-header">
        <Filter size={18} />
        <h3>Dashboard Filters</h3>
      </div>
      
      <div className="filters-grid">
        <div className="filter-group">
          <label>Military Base</label>
          <select 
            value={selectedBase} 
            onChange={(e) => setSelectedBase(e.target.value)}
            disabled={isBaseCommander}
            className="filter-input"
          >
            <option value="">-- Select Base --</option>
            {bases.map(base => (
              <option key={base.id} value={base.id}>
                {base.name} ({base.location})
              </option>
            ))}
          </select>
          {isBaseCommander && <small className="filter-hint">Restricted to your assigned base</small>}
        </div>

        <div className="filter-group">
          <label>Equipment Type</label>
          <select 
            value={selectedEquipment} 
            onChange={(e) => setSelectedEquipment(e.target.value)}
            className="filter-input"
          >
            <option value="">-- Select Equipment --</option>
            {equipment.map(eq => (
              <option key={eq.id} value={eq.id}>
                {eq.name} ({eq.type})
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label>Start Date</label>
          <input 
            type="date" 
            value={startDate} 
            onChange={(e) => setStartDate(e.target.value)}
            className="filter-input"
          />
        </div>

        <div className="filter-group">
          <label>End Date</label>
          <input 
            type="date" 
            value={endDate} 
            onChange={(e) => setEndDate(e.target.value)}
            className="filter-input"
          />
        </div>
      </div>
      
      <div className="filters-actions">
        <button 
          className="apply-btn" 
          onClick={onApply}
          disabled={!selectedEquipment || !startDate || !endDate || (!selectedBase && !isBaseCommander)}
        >
          Apply Filters
        </button>
      </div>
    </div>
  );
};

export default DashboardFilters;
