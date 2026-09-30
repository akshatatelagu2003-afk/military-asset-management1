import React, { useState, useEffect, useContext } from 'react';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';
import {
  ArrowRightLeft, Plus, Filter, AlertCircle, CheckCircle2,
  RefreshCw, Trash2, ArrowRight
} from 'lucide-react';
import './Transfers.css';

const initialForm = () => ({
  fromBaseId: '',
  toBaseId: '',
  equipmentId: '',
  quantity: '',
  transferDate: new Date().toISOString().split('T')[0],
});

const initialFilters = () => ({
  fromBaseId: '',
  toBaseId: '',
  equipmentId: '',
  startDate: '',
  endDate: '',
});

const Transfers = () => {
  const { user } = useContext(AuthContext);

  // Data state
  const [transfers, setTransfers] = useState([]);
  const [bases, setBases] = useState([]);
  const [equipment, setEquipment] = useState([]);

  // Loading / feedback
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Form & filters
  const [formData, setFormData] = useState(initialForm());
  const [formErrors, setFormErrors] = useState({});
  const [filters, setFilters] = useState(initialFilters());

  const isAdmin = user?.role === 'ADMIN';
  const isBaseCommander = user?.role === 'BASE_COMMANDER';

  // ─── Data fetching ──────────────────────────────────────────────────────────

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const [basesRes, equipmentRes, transfersRes] = await Promise.all([
        api.get('/bases'),
        api.get('/equipment'),
        api.get('/transfers'),
      ]);
      setBases(basesRes.data);
      setEquipment(equipmentRes.data);
      setTransfers(transfersRes.data);
    } catch (err) {
      console.error('Failed to load transfers data', err);
      if (err.response?.status === 403) {
        setError('You do not have permission to view transfers.');
      } else {
        setError('Failed to load transfers. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  // ─── Form handling ──────────────────────────────────────────────────────────

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Clear individual field error on change
    if (formErrors[name]) setFormErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const errs = {};
    if (!formData.fromBaseId) errs.fromBaseId = 'From Base is required.';
    if (!formData.toBaseId)   errs.toBaseId   = 'To Base is required.';
    if (formData.fromBaseId && formData.toBaseId && formData.fromBaseId === formData.toBaseId) {
      errs.toBaseId = 'From Base and To Base must be different.';
    }
    if (!formData.equipmentId) errs.equipmentId = 'Equipment is required.';
    if (!formData.quantity || parseInt(formData.quantity) <= 0) {
      errs.quantity = 'Quantity must be greater than 0.';
    }
    if (!formData.transferDate) errs.transferDate = 'Transfer date is required.';
    return errs;
  };

  const handleCreateTransfer = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const errs = validate();
    if (Object.keys(errs).length > 0) {
      setFormErrors(errs);
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        fromBase:     { id: parseInt(formData.fromBaseId) },
        toBase:       { id: parseInt(formData.toBaseId) },
        equipment:    { id: parseInt(formData.equipmentId) },
        quantity:     parseInt(formData.quantity),
        transferDate: formData.transferDate,
        // createdBy is intentionally omitted — backend sets it from the JWT
      };

      await api.post('/transfers', payload);

      setSuccess('Transfer recorded successfully!');
      setFormData(initialForm());
      setFormErrors({});
      fetchData();
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      console.error('Failed to create transfer', err);
      if (err.response?.status === 403) {
        setError('You do not have permission to create this transfer.');
      } else if (err.response?.status === 400) {
        setError('Invalid data. Please check your inputs.');
      } else {
        setError(err.response?.data?.message || 'Failed to record transfer.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  // ─── Delete handling (ADMIN only) ───────────────────────────────────────────

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this transfer? This action cannot be undone.')) return;
    try {
      await api.delete(`/transfers/${id}`);
      setSuccess('Transfer deleted.');
      fetchData();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Failed to delete transfer. You may not have permission.');
    }
  };

  // ─── Client-side filtering ───────────────────────────────────────────────────
  // GET /api/transfers does NOT support query parameters — filtering is done
  // client-side on the dataset already authorised and returned by the backend.

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const filteredTransfers = transfers.filter(t => {
    if (filters.fromBaseId && String(t.fromBase?.id) !== filters.fromBaseId) return false;
    if (filters.toBaseId   && String(t.toBase?.id)   !== filters.toBaseId)   return false;
    if (filters.equipmentId && String(t.equipment?.id) !== filters.equipmentId) return false;
    if (filters.startDate && t.transferDate < filters.startDate) return false;
    if (filters.endDate   && t.transferDate > filters.endDate)   return false;
    return true;
  });

  // ─── Render ──────────────────────────────────────────────────────────────────

  return (
    <div className="transfers-page">
      {/* Page header */}
      <div className="page-header">
        <h2>Transfers</h2>
        <p>Record and manage equipment movements between military bases.</p>
      </div>

      {/* Feedback banners */}
      {error && (
        <div className="alert error-alert">
          <AlertCircle size={18} /><span>{error}</span>
        </div>
      )}
      {success && (
        <div className="alert success-alert">
          <CheckCircle2 size={18} /><span>{success}</span>
        </div>
      )}

      <div className="transfers-content">

        {/* ── CREATE FORM ── */}
        <div className="panel form-section">
          <div className="panel-header">
            <Plus size={18} />
            <h3>Create Transfer</h3>
          </div>

          <form onSubmit={handleCreateTransfer} className="transfer-form" noValidate>

            <div className="form-group">
              <label>From Base</label>
              <select
                name="fromBaseId"
                value={formData.fromBaseId}
                onChange={handleFormChange}
                className={`form-input ${formErrors.fromBaseId ? 'input-error' : ''}`}
                disabled={isBaseCommander}  // backend forces their own base
              >
                <option value="">-- Select From Base --</option>
                {bases.map(b => (
                  <option key={b.id} value={b.id}>{b.name} ({b.location})</option>
                ))}
              </select>
              {isBaseCommander && (
                <small className="hint">Auto-set to your assigned base by the server.</small>
              )}
              {formErrors.fromBaseId && <span className="field-error">{formErrors.fromBaseId}</span>}
            </div>

            <div className="form-group direction-arrow">
              <ArrowRight size={20} className="arrow-icon" />
            </div>

            <div className="form-group">
              <label>To Base</label>
              <select
                name="toBaseId"
                value={formData.toBaseId}
                onChange={handleFormChange}
                className={`form-input ${formErrors.toBaseId ? 'input-error' : ''}`}
              >
                <option value="">-- Select To Base --</option>
                {bases
                  .filter(b => String(b.id) !== formData.fromBaseId) // prevent same-base selection
                  .map(b => (
                    <option key={b.id} value={b.id}>{b.name} ({b.location})</option>
                  ))}
              </select>
              {formErrors.toBaseId && <span className="field-error">{formErrors.toBaseId}</span>}
            </div>

            <div className="form-group">
              <label>Equipment</label>
              <select
                name="equipmentId"
                value={formData.equipmentId}
                onChange={handleFormChange}
                className={`form-input ${formErrors.equipmentId ? 'input-error' : ''}`}
              >
                <option value="">-- Select Equipment --</option>
                {equipment.map(eq => (
                  <option key={eq.id} value={eq.id}>{eq.name} ({eq.type})</option>
                ))}
              </select>
              {formErrors.equipmentId && <span className="field-error">{formErrors.equipmentId}</span>}
            </div>

            <div className="form-group">
              <label>Quantity</label>
              <input
                type="number"
                name="quantity"
                value={formData.quantity}
                onChange={handleFormChange}
                min="1"
                className={`form-input ${formErrors.quantity ? 'input-error' : ''}`}
              />
              {formErrors.quantity && <span className="field-error">{formErrors.quantity}</span>}
            </div>

            <div className="form-group">
              <label>Transfer Date</label>
              <input
                type="date"
                name="transferDate"
                value={formData.transferDate}
                onChange={handleFormChange}
                className={`form-input ${formErrors.transferDate ? 'input-error' : ''}`}
              />
              {formErrors.transferDate && <span className="field-error">{formErrors.transferDate}</span>}
            </div>

            <button type="submit" className="submit-btn" disabled={submitting}>
              {submitting ? 'Recording...' : 'Create Transfer'}
            </button>
          </form>
        </div>

        {/* ── HISTORY ── */}
        <div className="panel history-section">
          <div className="panel-header flex-between">
            <div className="flex-align">
              <ArrowRightLeft size={18} />
              <h3>Transfer History</h3>
            </div>
            <button onClick={fetchData} className="refresh-btn" disabled={loading}>
              <RefreshCw size={16} className={loading ? 'spinning' : ''} />
              <span>Refresh</span>
            </button>
          </div>

          {/* Client-side filters */}
          <div className="client-filters">
            <div className="filter-title">
              <Filter size={13} />
              <span>Client-side Filtering — backend does not support query params</span>
            </div>
            <div className="filters-row">
              <select name="fromBaseId" value={filters.fromBaseId} onChange={handleFilterChange} className="form-input small">
                <option value="">Any From Base</option>
                {bases.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
              <select name="toBaseId" value={filters.toBaseId} onChange={handleFilterChange} className="form-input small">
                <option value="">Any To Base</option>
                {bases.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
              <select name="equipmentId" value={filters.equipmentId} onChange={handleFilterChange} className="form-input small">
                <option value="">Any Equipment</option>
                {equipment.map(e => <option key={e.id} value={e.id}>{e.name}</option>)}
              </select>
              <input type="date" name="startDate" value={filters.startDate} onChange={handleFilterChange} className="form-input small" />
              <input type="date" name="endDate"   value={filters.endDate}   onChange={handleFilterChange} className="form-input small" />
              <button className="clear-btn" onClick={() => setFilters(initialFilters())}>Clear</button>
            </div>
          </div>

          {/* Table */}
          <div className="table-responsive">
            {loading && transfers.length === 0 ? (
              <div className="loading-state">
                <div className="spinner" />
                <p>Loading history…</p>
              </div>
            ) : filteredTransfers.length === 0 ? (
              <div className="empty-state">
                <ArrowRightLeft size={40} />
                <h4>No transfers found</h4>
                <p>Try adjusting your filters or create a new transfer.</p>
              </div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>From Base</th>
                    <th></th>
                    <th>To Base</th>
                    <th>Equipment</th>
                    <th>Qty</th>
                    <th>Recorded At</th>
                    {isAdmin && <th>Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {filteredTransfers.map(t => (
                    <tr key={t.id}>
                      <td>{t.transferDate}</td>
                      <td className="base-cell">{t.fromBase?.name ?? '—'}</td>
                      <td className="arrow-cell"><ArrowRight size={14} /></td>
                      <td className="base-cell">{t.toBase?.name ?? '—'}</td>
                      <td>{t.equipment?.name ?? '—'} <span className="type-badge">{t.equipment?.type}</span></td>
                      <td className="font-mono">{t.quantity}</td>
                      <td className="muted">{t.createdAt ? new Date(t.createdAt).toLocaleString() : '—'}</td>
                      {isAdmin && (
                        <td>
                          <button className="delete-btn" onClick={() => handleDelete(t.id)} title="Delete Transfer">
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

export default Transfers;
