import React, { useState, useEffect, useContext } from 'react';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';
import {
  TrendingDown, Plus, Filter, AlertCircle, CheckCircle2,
  RefreshCw, Trash2
} from 'lucide-react';
import './Expenditures.css';

const initialForm = () => ({
  baseId: '',
  equipmentId: '',
  quantity: '',
  reason: '',
  expendedDate: new Date().toISOString().split('T')[0],
});

const initialFilters = () => ({
  baseId: '',
  equipmentId: '',
  reason: '',
  startDate: '',
  endDate: '',
});

const Expenditures = () => {
  const { user } = useContext(AuthContext);

  const [expenditures, setExpenditures] = useState([]);
  const [bases, setBases]               = useState([]);
  const [equipment, setEquipment]       = useState([]);

  const [loading, setLoading]       = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError]           = useState('');
  const [success, setSuccess]       = useState('');

  const [formData, setFormData]     = useState(initialForm());
  const [formErrors, setFormErrors] = useState({});
  const [filters, setFilters]       = useState(initialFilters());

  const isAdmin         = user?.role === 'ADMIN';
  const isBaseCommander = user?.role === 'BASE_COMMANDER';

  // ── Fetch ─────────────────────────────────────────────────────────────────

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const [basesRes, equipRes, expendRes] = await Promise.all([
        api.get('/bases'),
        api.get('/equipment'),
        api.get('/expenditures'),
      ]);
      setBases(basesRes.data);
      setEquipment(equipRes.data);
      setExpenditures(expendRes.data);
    } catch (err) {
      if (err.response?.status === 403) {
        setError('You do not have permission to view expenditures.');
      } else {
        setError('Failed to load expenditures. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  // ── Form ──────────────────────────────────────────────────────────────────

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (formErrors[name]) setFormErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const errs = {};
    if (!formData.baseId)      errs.baseId      = 'Base is required.';
    if (!formData.equipmentId) errs.equipmentId = 'Equipment is required.';
    if (!formData.quantity || parseInt(formData.quantity) <= 0) {
      errs.quantity = 'Quantity must be greater than 0.';
    }
    if (!formData.reason || formData.reason.trim() === '') {
      errs.reason = 'Reason is required.';
    }
    if (!formData.expendedDate) errs.expendedDate = 'Expenditure date is required.';
    return errs;
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');

    const errs = validate();
    if (Object.keys(errs).length > 0) { setFormErrors(errs); return; }

    setSubmitting(true);
    try {
      const payload = {
        base:         { id: parseInt(formData.baseId) },
        equipment:    { id: parseInt(formData.equipmentId) },
        quantity:     parseInt(formData.quantity),
        reason:       formData.reason.trim(),
        expendedDate: formData.expendedDate,
        // createdBy intentionally omitted — set by backend from JWT
      };
      await api.post('/expenditures', payload);
      setSuccess('Expenditure recorded successfully!');
      setFormData(initialForm());
      setFormErrors({});
      fetchData();
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      if (err.response?.status === 403) {
        setError('You do not have permission to record expenditures.');
      } else {
        setError(err.response?.data?.message || 'Failed to record expenditure.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  // ── Delete ────────────────────────────────────────────────────────────────

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this expenditure record? This action cannot be undone.')) return;
    try {
      await api.delete(`/expenditures/${id}`);
      setSuccess('Expenditure deleted.');
      fetchData();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Failed to delete expenditure. You may not have permission.');
    }
  };

  // ── Client-side filtering ─────────────────────────────────────────────────
  // GET /api/expenditures has no query params — filtering is done locally.

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const filtered = expenditures.filter(ex => {
    if (filters.baseId      && String(ex.base?.id) !== filters.baseId)             return false;
    if (filters.equipmentId && String(ex.equipment?.id) !== filters.equipmentId)   return false;
    if (filters.reason      && !ex.reason?.toLowerCase().includes(filters.reason.toLowerCase())) return false;
    if (filters.startDate   && ex.expendedDate < filters.startDate)                return false;
    if (filters.endDate     && ex.expendedDate > filters.endDate)                  return false;
    return true;
  });

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <div className="expenditures-page">
      <div className="page-header">
        <h2>Expenditures</h2>
        <p>Record equipment consumed, lost, or written off at each base.</p>
      </div>

      {error && (
        <div className="alert error-alert"><AlertCircle size={18} /><span>{error}</span></div>
      )}
      {success && (
        <div className="alert success-alert"><CheckCircle2 size={18} /><span>{success}</span></div>
      )}

      <div className="page-content-grid">

        {/* ── FORM ── */}
        <div className="panel">
          <div className="panel-header">
            <Plus size={18} /><h3>Record Expenditure</h3>
          </div>

          <form onSubmit={handleCreate} className="entry-form" noValidate>

            <div className="form-group">
              <label>Base</label>
              <select name="baseId" value={formData.baseId} onChange={handleFormChange}
                className={`form-input ${formErrors.baseId ? 'input-error' : ''}`}
                disabled={isBaseCommander}>
                <option value="">-- Select Base --</option>
                {bases.map(b => <option key={b.id} value={b.id}>{b.name} ({b.location})</option>)}
              </select>
              {isBaseCommander && <small className="hint">Auto-set to your assigned base by the server.</small>}
              {formErrors.baseId && <span className="field-error">{formErrors.baseId}</span>}
            </div>

            <div className="form-group">
              <label>Equipment</label>
              <select name="equipmentId" value={formData.equipmentId} onChange={handleFormChange}
                className={`form-input ${formErrors.equipmentId ? 'input-error' : ''}`}>
                <option value="">-- Select Equipment --</option>
                {equipment.map(eq => <option key={eq.id} value={eq.id}>{eq.name} ({eq.type})</option>)}
              </select>
              {formErrors.equipmentId && <span className="field-error">{formErrors.equipmentId}</span>}
            </div>

            <div className="form-group">
              <label>Quantity</label>
              <input type="number" name="quantity" value={formData.quantity}
                onChange={handleFormChange} min="1"
                className={`form-input ${formErrors.quantity ? 'input-error' : ''}`} />
              {formErrors.quantity && <span className="field-error">{formErrors.quantity}</span>}
            </div>

            <div className="form-group">
              <label>Reason</label>
              <input type="text" name="reason" value={formData.reason}
                onChange={handleFormChange} placeholder="e.g. Combat usage, damage, loss"
                className={`form-input ${formErrors.reason ? 'input-error' : ''}`} />
              {formErrors.reason && <span className="field-error">{formErrors.reason}</span>}
            </div>

            <div className="form-group">
              <label>Expenditure Date</label>
              <input type="date" name="expendedDate" value={formData.expendedDate}
                onChange={handleFormChange}
                className={`form-input ${formErrors.expendedDate ? 'input-error' : ''}`} />
              {formErrors.expendedDate && <span className="field-error">{formErrors.expendedDate}</span>}
            </div>

            <button type="submit" className="submit-btn" disabled={submitting}>
              {submitting ? 'Recording...' : 'Record Expenditure'}
            </button>
          </form>
        </div>

        {/* ── HISTORY ── */}
        <div className="panel">
          <div className="panel-header flex-between">
            <div className="flex-align"><TrendingDown size={18} /><h3>Expenditure History</h3></div>
            <button onClick={fetchData} className="refresh-btn" disabled={loading}>
              <RefreshCw size={16} className={loading ? 'spinning' : ''} /><span>Refresh</span>
            </button>
          </div>

          {/* Filters */}
          <div className="client-filters">
            <div className="filter-title"><Filter size={13} /><span>Client-side Filtering</span></div>
            <div className="filters-row">
              <select name="baseId" value={filters.baseId} onChange={handleFilterChange} className="form-input small">
                <option value="">All Bases</option>
                {bases.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
              <select name="equipmentId" value={filters.equipmentId} onChange={handleFilterChange} className="form-input small">
                <option value="">All Equipment</option>
                {equipment.map(e => <option key={e.id} value={e.id}>{e.name}</option>)}
              </select>
              <input type="text" name="reason" value={filters.reason}
                onChange={handleFilterChange} placeholder="Reason…" className="form-input small" />
              <input type="date" name="startDate" value={filters.startDate} onChange={handleFilterChange} className="form-input small" />
              <input type="date" name="endDate"   value={filters.endDate}   onChange={handleFilterChange} className="form-input small" />
              <button className="clear-btn" onClick={() => setFilters(initialFilters())}>Clear</button>
            </div>
          </div>

          {/* Table */}
          <div className="table-responsive">
            {loading && expenditures.length === 0 ? (
              <div className="loading-state"><div className="spinner" /><p>Loading…</p></div>
            ) : filtered.length === 0 ? (
              <div className="empty-state">
                <TrendingDown size={40} /><h4>No expenditures found</h4>
                <p>Adjust filters or record a new expenditure.</p>
              </div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Base</th>
                    <th>Equipment</th>
                    <th>Qty</th>
                    <th>Reason</th>
                    <th>Recorded At</th>
                    {isAdmin && <th>Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {filtered.map(ex => (
                    <tr key={ex.id}>
                      <td>{ex.expendedDate}</td>
                      <td>{ex.base?.name ?? '—'}</td>
                      <td>{ex.equipment?.name ?? '—'} <span className="type-badge">{ex.equipment?.type}</span></td>
                      <td className="font-mono">{ex.quantity}</td>
                      <td className="reason-cell">{ex.reason}</td>
                      <td className="muted">{ex.createdAt ? new Date(ex.createdAt).toLocaleString() : '—'}</td>
                      {isAdmin && (
                        <td>
                          <button className="delete-btn" onClick={() => handleDelete(ex.id)} title="Delete">
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

export default Expenditures;
