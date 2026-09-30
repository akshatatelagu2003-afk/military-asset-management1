import React, { useState, useEffect, useContext } from 'react';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';
import MetricCard from '../components/MetricCard';
import DashboardFilters from '../components/DashboardFilters';
import NetMovementModal from '../components/NetMovementModal';
import { 
  Package, 
  ShoppingCart, 
  ArrowDownToLine, 
  ArrowUpFromLine, 
  Activity, 
  UserCheck, 
  Trash2, 
  Layers 
} from 'lucide-react';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useContext(AuthContext);
  
  // Dropdown data
  const [bases, setBases] = useState([]);
  const [equipment, setEquipment] = useState([]);
  
  // Filter state
  const [selectedBase, setSelectedBase] = useState('');
  const [selectedEquipment, setSelectedEquipment] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  
  // Metrics state
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Load dropdown data on mount
  useEffect(() => {
    const fetchDropdownData = async () => {
      try {
        const [basesRes, equipmentRes] = await Promise.all([
          api.get('/bases'),
          api.get('/equipment')
        ]);
        setBases(basesRes.data);
        setEquipment(equipmentRes.data);
        
        // If Base Commander, set the selected base automatically based on their profile
        // Wait, the backend already enforces it, but the UI should show it if possible.
        // We will just let the backend handle the logic. 
      } catch (err) {
        console.error("Failed to load initial data", err);
        setError("Failed to load bases and equipment data.");
      }
    };
    
    fetchDropdownData();
  }, []);

  const fetchMetrics = async () => {
    setLoading(true);
    setError('');
    setMetrics(null);
    
    try {
      const params = {
        equipmentId: selectedEquipment,
        startDate,
        endDate
      };
      
      // Only attach baseId if it's selected (for ADMIN/LOGISTICS_OFFICER)
      if (selectedBase) {
        params.baseId = selectedBase;
      }

      const response = await api.get('/dashboard/metrics', { params });
      setMetrics(response.data);
    } catch (err) {
      console.error("Failed to fetch metrics", err);
      if (err.response?.status === 403) {
        setError("You do not have permission to view this data.");
      } else {
        setError(err.response?.data?.message || "Failed to load dashboard metrics. Please check your filters.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-page">
      <div className="page-header">
        <h2>Dashboard Overview</h2>
        <p>Monitor military asset balances and movements across bases.</p>
      </div>

      {error && (
        <div className="dashboard-error">
          <p>{error}</p>
        </div>
      )}

      <DashboardFilters 
        bases={bases}
        equipment={equipment}
        selectedBase={selectedBase}
        setSelectedBase={setSelectedBase}
        selectedEquipment={selectedEquipment}
        setSelectedEquipment={setSelectedEquipment}
        startDate={startDate}
        setStartDate={setStartDate}
        endDate={endDate}
        setEndDate={setEndDate}
        onApply={fetchMetrics}
        userRole={user?.role}
      />

      <div className="metrics-section">
        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Calculating metrics...</p>
          </div>
        ) : !metrics ? (
          <div className="empty-state">
            <Package size={48} />
            <h3>No Data Displayed</h3>
            <p>Select a base, equipment type, and date range, then click "Apply Filters" to view metrics.</p>
          </div>
        ) : (
          <div className="metrics-grid">
            <MetricCard 
              title="Opening Balance" 
              value={metrics.openingBalance} 
              icon={<Layers size={24} />} 
            />
            <MetricCard 
              title="Purchases" 
              value={metrics.purchases} 
              icon={<ShoppingCart size={24} />} 
            />
            <MetricCard 
              title="Transfer In" 
              value={metrics.transferIn} 
              icon={<ArrowDownToLine size={24} />} 
            />
            <MetricCard 
              title="Transfer Out" 
              value={metrics.transferOut} 
              icon={<ArrowUpFromLine size={24} />} 
            />
            <MetricCard 
              title="Net Movement" 
              value={metrics.netMovement} 
              icon={<Activity size={24} />}
              highlight={true}
              onClick={() => setIsModalOpen(true)}
            />
            <MetricCard 
              title="Assigned" 
              value={metrics.assigned} 
              icon={<UserCheck size={24} />} 
            />
            <MetricCard 
              title="Expended" 
              value={metrics.expended} 
              icon={<Trash2 size={24} />} 
            />
            <MetricCard 
              title="Closing Balance" 
              value={metrics.closingBalance} 
              icon={<Package size={24} />} 
            />
          </div>
        )}
      </div>

      <NetMovementModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        metrics={metrics} 
      />
    </div>
  );
};

export default Dashboard;
