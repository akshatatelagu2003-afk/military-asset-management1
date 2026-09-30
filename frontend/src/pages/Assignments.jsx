import React, { useState, useEffect, useContext } from 'react';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';
import {
  ClipboardList, Plus, Filter, AlertCircle, CheckCircle2,
  RefreshCw, Trash2, User
} from 'lucide-react';
import './Assignments.css';

const initialForm = () => ({
  baseId: '',
  equipmentId: '',
  personnelName: '',
  quantity: '',
  assignedDate: new Date().toISOString().split('T')[0],
});

const initialFilters = () => ({
  baseId: '',
  equipmentId: '',
  personnelName: '',
  startDate: '',
  endDate: '',
});

const Assignments = () => {
  const { user } = useContext(AuthContext);

  const [assignments, setAssignments] = useState([]);
  const [bases, setBases]             = useState([]);
  const [equipment, setEquipment]     = useState([]);

  const [loading, setLoading]     = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError]         = useState('');
  const [success, setSuccess]     = useState('');

  const [formData, setFormData]   = useState(initialForm());
  const [formErrors, setFormErrors] = useState({});
  const [filters, setFilters]     = useState(initialFilters());

  const isAdmin         = user?.role === 'ADMIN';
  const isBaseCommander = user?.role === 'BASE_COMMANDER';

  // ── Fetch ─────────────────────────────────────────────────────────────────

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const [basesRes, equipRes, assignRes] = await Promise.all([
        api.get('/bases'),
        api.get('/equipment'),
        api.get('/assignments'),
      ]);
      setBases(basesRes.data);
      setEquipment(equipRes.data);
      setAssignments(assignRes.data);
    } catch (err) {
      if (err.response?.status === 403) {
        setError('You do not have permission to view assignments.');
      } else {
        setError('Failed to load assignments. Please try again.');
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
    if (!formData.baseId)        errs.baseId        = 'Base is required.';
    if (!formData.equipmentId)   errs.equipmentId   = 'Equipment is required.';
    if (!formData.personnelName || formData.personnelName.trim() === '') {
      errs.personnelName = 'Personnel name is required.';
    }
    if (!formData.quantity || parseInt(formData.quantity) <= 0) {
      errs.quantity = 'Quantity must be greater than 0.';
    }
    if (!formData.assignedDate)  errs.assignedDate  = 'Assignment date is required.';
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
        base:          { id: parseInt(formData.baseId) },
        equipment:     { id: parseInt(formData.equipmentId) },
        personnelName: formData.personnelName.trim(),
        quantity:      parseInt(formData.quantity),
        assignedDate:  formData.assignedDate,
        // createdBy intentionally omitted — set by backend from JWT
      };
      await api.post('/assignments', payload);
      setSuccess('Assignment recorded successfully!');
      setFormData(initialForm());
      setFormErrors({});
      fetchData();
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      if (err.response?.status === 403) {
        setError('You do not have permission to create assignments.');
      } else {
        setError(err.response?.data?.message || 'Failed to record assignment.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  // ── Delete ────────────────────────────────────────────────────────────────

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this assignment? This action cannot be undone.')) return;
    try {
      await api.delete(`/assignments/${id}`);
      setSuccess('Assignment deleted.');
      fetchData();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Failed to delete assignment. You may not have permission.');
    }
  };

  // ── Client-side filtering ─────────────────────────────────────────────────
  // GET /api/assignments has no query params — filtering done locally.

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const filtered = assignments.filter(a => {
    if (filters.baseId      && String(a.base?.id) !== filters.baseId)           return false;
    if (filters.equipmentId && String(a.equipment?.id) !== filters.equipmentId) return false;
    if (filters.personnelName && !a.personnelName?.toLowerCase().includes(filters.personnelName.toLowerCase())) return false;
    if (filters.startDate   && a.assignedDate < filters.startDate)              return false;
    if (filters.endDate     && a.assignedDate > filters.endDate)                return false;
    return true;
  });

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <div className="assignments-page">
      <div className="page-header">
        <h2>Assignments</h2>
        <p>Track equipment issued to personnel at each base.</p>
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
            <Plus size={18} /><h3>Assign Asset</h3>
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
              <label>Personnel Name</label>
              <input type="text" name="personnelName" value={formData.personnelName}
                onChange={handleFormChange} placeholder="e.g. Sgt. John Smith"
                className={`form-input ${formErrors.personnelName ? 'input-error' : ''}`} />
              {formErrors.personnelName && <span className="field-error">{formErrors.personnelName}</span>}
            </div>

            <div className="form-group">
              <label>Quantity</label>
              <input type="number" name="quantity" value={formData.quantity}
                onChange={handleFormChange} min="1"
                className={`form-input ${formErrors.quantity ? 'input-error' : ''}`} />
              {formErrors.quantity && <span className="field-error">{formErrors.quantity}</span>}
            </div>

            <div className="form-group">
              <label>Assignment Date</label>
              <input type="date" name="assignedDate" value={formData.assignedDate}
                onChange={handleFormChange}
                className={`form-input ${formErrors.assignedDate ? 'input-error' : ''}`} />
              {formErrors.assignedDate && <span className="field-error">{formErrors.assignedDate}</span>}
            </div>

            <button type="submit" className="submit-btn" disabled={submitting}>
              {submitting ? 'Recording...' : 'Record Assignment'}
            </button>
          </form>
        </div>

        {/* ── HISTORY ── */}
        <div className="panel">
          <div className="panel-header flex-between">
            <div className="flex-align"><ClipboardList size={18} /><h3>Assignment History</h3></div>
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
              <input type="text" name="personnelName" value={filters.personnelName}
                onChange={handleFilterChange} placeholder="Personnel…" className="form-input small" />
              <input type="date" name="startDate" value={filters.startDate} onChange={handleFilterChange} className="form-input small" />
              <input type="date" name="endDate"   value={filters.endDate}   onChange={handleFilterChange} className="form-input small" />
              <button className="clear-btn" onClick={() => setFilters(initialFilters())}>Clear</button>
            </div>
          </div>

          {/* Table */}
          <div className="table-responsive">
            {loading && assignments.length === 0 ? (
              <div className="loading-state"><div className="spinner" /><p>Loading…</p></div>
            ) : filtered.length === 0 ? (
              <div className="empty-state">
                <ClipboardList size={40} /><h4>No assignments found</h4>
                <p>Adjust filters or record a new assignment.</p>
              </div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Base</th>
                    <th>Equipment</th>
                    <th><User size={13} style={{verticalAlign:'middle'}}/> Personnel</th>
                    <th>Qty</th>
                    <th>Recorded At</th>
                    {isAdmin && <th>Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {filtered.map(a => (
                    <tr key={a.id}>
                      <td>{a.assignedDate}</td>
                      <td>{a.base?.name ?? '—'}</td>
                      <td>{a.equipment?.name ?? '—'} <span className="type-badge">{a.equipment?.type}</span></td>
                      <td>{a.personnelName}</td>
                      <td className="font-mono">{a.quantity}</td>
                      <td className="muted">{a.createdAt ? new Date(a.createdAt).toLocaleString() : '—'}</td>
                      {isAdmin && (
                        <td>
                          <button className="delete-btn" onClick={() => handleDelete(a.id)} title="Delete">
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

export default Assignments;
