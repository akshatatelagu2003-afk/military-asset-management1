import React, { useState, useEffect, useContext } from 'react';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';
import { ShoppingCart, Plus, Filter, AlertCircle, CheckCircle2, RefreshCw, Trash2 } from 'lucide-react';
import './Purchases.css';

const Purchases = () => {
  const { user } = useContext(AuthContext);
  
  // Data state
  const [purchases, setPurchases] = useState([]);
  const [bases, setBases] = useState([]);
  const [equipment, setEquipment] = useState([]);
  
  // Loading and feedback states
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // Form state
  const [formData, setFormData] = useState({
    baseId: '',
    equipmentId: '',
    quantity: '',
    purchaseDate: new Date().toISOString().split('T')[0]
  });
  
  // Filter state
  const [filters, setFilters] = useState({
    baseId: '',
    equipmentId: '',
    startDate: '',
    endDate: ''
  });

  const isBaseCommander = user?.role === 'BASE_COMMANDER';
  const isAdmin = user?.role === 'ADMIN';

  // Fetch initial data
  const fetchData = async () => {
    setLoading(true);
    setError('');
    
    try {
      // Fetch dropdowns and purchases concurrently
      const [basesRes, equipmentRes, purchasesRes] = await Promise.all([
        api.get('/bases'),
        api.get('/equipment'),
        api.get('/purchases')
      ]);
      
      setBases(basesRes.data);
      setEquipment(equipmentRes.data);
      setPurchases(purchasesRes.data);
    } catch (err) {
      console.error("Failed to load data", err);
      if (err.response?.status === 403) {
        setError("You do not have permission to view purchases.");
      } else {
        setError("Failed to load purchases data. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const handleCreatePurchase = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      const payload = {
        base: { id: parseInt(formData.baseId) },
        equipment: { id: parseInt(formData.equipmentId) },
        quantity: parseInt(formData.quantity),
        purchaseDate: formData.purchaseDate
        // Note: The backend Purchase entity requires createdBy, but it is not 
        // returned in the JWT login response. If the backend fails here with 500, 
        // the backend needs updating to populate createdBy from CurrentUserService.
      };

      await api.post('/purchases', payload);
      
      setSuccess('Purchase recorded successfully!');
      
      // Reset form (keeping the date as today)
      setFormData({
        baseId: '',
        equipmentId: '',
        quantity: '',
        purchaseDate: new Date().toISOString().split('T')[0]
      });
      
      // Refresh list
      fetchData();
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error("Failed to create purchase", err);
      if (err.response?.status === 403) {
        setError("You do not have permission to record purchases.");
      } else if (err.response?.status === 400) {
        setError("Invalid data provided. Please check your inputs.");
      } else {
        setError(err.response?.data?.message || "Failed to record purchase. The server might require additional fields.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this purchase? This action cannot be undone.")) {
      return;
    }
    
    try {
      await api.delete(`/purchases/${id}`);
      setSuccess("Purchase deleted successfully.");
      fetchData();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error("Failed to delete purchase", err);
      setError("Failed to delete purchase. You may not have permission.");
    }
  };

  // Client-side filtering
  // The backend GET /api/purchases currently does NOT support query parameters.
  const filteredPurchases = purchases.filter(p => {
    let matches = true;
    
    if (filters.baseId && p.base?.id.toString() !== filters.baseId) matches = false;
    if (filters.equipmentId && p.equipment?.id.toString() !== filters.equipmentId) matches = false;
    
    if (filters.startDate && p.purchaseDate < filters.startDate) matches = false;
    if (filters.endDate && p.purchaseDate > filters.endDate) matches = false;
    
    return matches;
  });

  return (
    <div className="purchases-page">
      <div className="page-header">
        <h2>Purchases</h2>
        <p>Record and manage incoming equipment acquisitions.</p>
      </div>

      {error && (
        <div className="alert error-alert">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="alert success-alert">
          <CheckCircle2 size={18} />
          <span>{success}</span>
        </div>
      )}

      <div className="purchases-content">
        {/* FORM SECTION */}
        <div className="form-section panel">
          <div className="panel-header">
            <Plus size={18} />
            <h3>Record Purchase</h3>
          </div>
          
          <form onSubmit={handleCreatePurchase} className="purchase-form">
            <div className="form-group">
              <label>Base</label>
              <select 
                name="baseId"
                value={formData.baseId} 
                onChange={handleFormChange}
                disabled={isBaseCommander}
                required
                className="form-input"
              >
                <option value="">-- Select Base --</option>
                {bases.map(base => (
                  <option key={base.id} value={base.id}>
                    {base.name} ({base.location})
                  </option>
                ))}
              </select>
              {isBaseCommander && <small className="hint">Auto-restricted to your assigned base.</small>}
            </div>

            <div className="form-group">
              <label>Equipment Type</label>
              <select 
                name="equipmentId"
                value={formData.equipmentId} 
                onChange={handleFormChange}
                required
                className="form-input"
              >
                <option value="">-- Select Equipment --</option>
                {equipment.map(eq => (
                  <option key={eq.id} value={eq.id}>
                    {eq.name} ({eq.type})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Quantity</label>
              <input 
                type="number"
                name="quantity"
                value={formData.quantity}
                onChange={handleFormChange}
                min="1"
                required
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label>Purchase Date</label>
              <input 
                type="date"
                name="purchaseDate"
                value={formData.purchaseDate}
                onChange={handleFormChange}
                required
                className="form-input"
              />
            </div>

            <button type="submit" className="submit-btn" disabled={submitting}>
              {submitting ? 'Recording...' : 'Record Purchase'}
            </button>
          </form>
        </div>

        {/* HISTORY SECTION */}
        <div className="history-section panel">
          <div className="panel-header flex-between">
            <div className="flex-align">
              <ShoppingCart size={18} />
              <h3>Purchase History</h3>
            </div>
            <button onClick={fetchData} className="refresh-btn" disabled={loading}>
              <RefreshCw size={16} className={loading ? 'spinning' : ''} />
              <span>Refresh</span>
            </button>
          </div>

          <div className="client-filters">
            <div className="filter-title">
              <Filter size={14} /> 
              <span>Client-side Filtering (Backend filters not supported)</span>
            </div>
            <div className="filters-row">
              <select name="baseId" value={filters.baseId} onChange={handleFilterChange} className="form-input small">
                <option value="">All Bases</option>
                {bases.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
              <select name="equipmentId" value={filters.equipmentId} onChange={handleFilterChange} className="form-input small">
                <option value="">All Equipment</option>
                {equipment.map(e => <option key={e.id} value={e.id}>{e.name}</option>)}
              </select>
              <input type="date" name="startDate" value={filters.startDate} onChange={handleFilterChange} className="form-input small" placeholder="Start Date" />
              <input type="date" name="endDate" value={filters.endDate} onChange={handleFilterChange} className="form-input small" placeholder="End Date" />
              <button 
                className="clear-btn" 
                onClick={() => setFilters({baseId: '', equipmentId: '', startDate: '', endDate: ''})}
              >
                Clear
              </button>
            </div>
          </div>

          <div className="table-responsive">
            {loading && purchases.length === 0 ? (
              <div className="loading-state">
                <div className="spinner"></div>
                <p>Loading history...</p>
              </div>
            ) : filteredPurchases.length === 0 ? (
              <div className="empty-state">
                <ShoppingCart size={40} />
                <h4>No purchases found</h4>
                <p>Try adjusting your filters or record a new purchase.</p>
              </div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Base</th>
                    <th>Equipment</th>
                    <th>Quantity</th>
                    <th>Created At</th>
                    {isAdmin && <th>Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {filteredPurchases.map(p => (
                    <tr key={p.id}>
                      <td>{p.purchaseDate}</td>
                      <td>{p.base?.name || 'Unknown'}</td>
                      <td>{p.equipment?.name || 'Unknown'} ({p.equipment?.type})</td>
                      <td className="font-mono">{p.quantity}</td>
                      <td>{p.createdAt ? new Date(p.createdAt).toLocaleString() : '-'}</td>
                      {isAdmin && (
                        <td>
                          <button 
                            className="delete-btn"
                            onClick={() => handleDelete(p.id)}
                            title="Delete Purchase"
                          >
                            <Trash2 size={16} />
                          </button>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Purchases;
