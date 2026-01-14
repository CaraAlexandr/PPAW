import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../services/apiClient';
import './VaultPage.css';

/**
 * LAB 8: Vault page with CRUD operations and subscription features
 */
function VaultPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [planLimits, setPlanLimits] = useState(null);
  const [servicePlanName, setServicePlanName] = useState('');
  const [formData, setFormData] = useState({
    title: '',
    username: '',
    password: '',
    url: '',
    notes: '',
  });
  const navigate = useNavigate();

  // Load vault items and plan info on mount
  useEffect(() => {
    loadItems();
    loadPlanInfo();
    const savedLimits = localStorage.getItem('planLimits');
    const savedPlanName = localStorage.getItem('servicePlanName');
    if (savedLimits) {
      setPlanLimits(JSON.parse(savedLimits));
    }
    if (savedPlanName) {
      setServicePlanName(savedPlanName);
    }
  }, []);

  const loadPlanInfo = async () => {
    try {
      const response = await apiClient.get('/user/plan');
      if (response.data.success && response.data.data) {
        setPlanLimits(response.data.data.limits);
        setServicePlanName(response.data.data.name);
      }
    } catch (err) {
      console.error('Failed to load plan info:', err);
    }
  };

  const loadItems = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/vault');
      setItems(response.data.data || []);
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load vault items');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await apiClient.post('/vault', formData);
      setShowForm(false);
      setFormData({ title: '', username: '', password: '', url: '', notes: '' });
      loadItems();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create vault item');
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await apiClient.put(`/vault/${editingItem.id}`, formData);
      setShowForm(false);
      setEditingItem(null);
      setFormData({ title: '', username: '', password: '', url: '', notes: '' });
      loadItems();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update vault item');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this item?')) {
      return;
    }
    try {
      await apiClient.delete(`/vault/${id}`);
      loadItems();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete vault item');
    }
  };

  const handleEdit = (item) => {
    setEditingItem(item);
    setFormData({
      title: item.title || '',
      username: item.username || '',
      password: '', // Don't show password
      url: item.url || '',
      notes: item.notes || '',
    });
    setShowForm(true);
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingItem(null);
    setFormData({ title: '', username: '', password: '', url: '', notes: '' });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('planLimits');
    localStorage.removeItem('servicePlanName');
    navigate('/login');
  };

  const handleExport = async () => {
    try {
      // Request with responseType: 'text' to get raw JSON string (not parsed as object)
      const response = await apiClient.get('/vault/export/download', {
        responseType: 'text'
      });
      
      // Check if response is an error JSON (starts with {"success":false})
      if (typeof response.data === 'string' && response.data.trim().startsWith('{"success":false')) {
        try {
          const errorData = JSON.parse(response.data);
          setError(errorData.message || 'Failed to export vault');
          return;
        } catch (e) {
          // If parsing fails, still try to download
        }
      }
      
      // Create blob from the JSON string
      const blob = new Blob([response.data], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'vault-export.json';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      setError(''); // Clear any previous errors
    } catch (err) {
      // Handle error response
      if (err.response?.data) {
        // If error response is a string (JSON), parse it
        if (typeof err.response.data === 'string') {
          try {
            const errorData = JSON.parse(err.response.data);
            setError(errorData.message || 'Failed to export vault');
          } catch (e) {
            setError('Failed to export vault');
          }
        } else if (err.response.data.message) {
          setError(err.response.data.message);
        } else {
          setError('Failed to export vault');
        }
      } else {
        setError('Failed to export vault');
      }
    }
  };

  const handleImport = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    try {
      const text = await file.text();
      const data = JSON.parse(text);
      
      if (!data.items || !Array.isArray(data.items)) {
        setError('Invalid import file format');
        return;
      }

      const response = await apiClient.post('/vault/import', { items: data.items });
      if (response.data.success) {
        const result = response.data.data;
        setError(`Import completed: ${result.importedCount} items imported${result.errorCount > 0 ? `, ${result.errorCount} errors` : ''}`);
        loadItems();
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to import vault');
    }
    
    // Reset file input
    event.target.value = '';
  };

  const handleShare = async (itemId, sharedWith) => {
    try {
      await apiClient.post('/vault/share', {
        vaultItemId: itemId,
        sharedWithUsernameOrEmail: sharedWith
      });
      setError('');
      alert('Vault item shared successfully!');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to share vault item');
    }
  };

  const getItemCountWarning = () => {
    if (!planLimits) return null;
    const used = items.length;
    const max = planLimits.maxVaultItems;
    const percentage = (used / max) * 100;
    
    if (percentage >= 90) {
      return { type: 'error', message: `Warning: You've used ${used}/${max} items. Please upgrade to add more.` };
    } else if (percentage >= 75) {
      return { type: 'warning', message: `You've used ${used}/${max} items (${Math.round(percentage)}%)` };
    }
    return null;
  };

  if (loading) {
    return <div className="vault-container"><div className="loading">Loading...</div></div>;
  }

  return (
    <div className="vault-container">
      <header className="vault-header">
        <div>
          <h1>Password Vault</h1>
          {servicePlanName && (
            <div className="plan-info">
              <span className="plan-badge">{servicePlanName} Plan</span>
              {planLimits && (
                <span className="item-count">
                  {items.length} / {planLimits.maxVaultItems} items
                </span>
              )}
            </div>
          )}
        </div>
        <div className="header-actions">
          <button 
            onClick={() => { 
              if (planLimits && items.length >= planLimits.maxVaultItems) {
                setError(`Maximum vault items limit (${planLimits.maxVaultItems}) reached for your plan. Please upgrade.`);
                return;
              }
              setShowForm(true); 
              setEditingItem(null); 
              setFormData({ title: '', username: '', password: '', url: '', notes: '' }); 
            }} 
            className="btn-primary"
            disabled={planLimits && items.length >= planLimits.maxVaultItems}
          >
            + Add Item
          </button>
          {planLimits?.canExport && (
            <button onClick={handleExport} className="btn-secondary">
              Export
            </button>
          )}
          {planLimits?.canImport && (
            <label className="btn-secondary" style={{ cursor: 'pointer' }}>
              Import
              <input type="file" accept=".json" onChange={handleImport} style={{ display: 'none' }} />
            </label>
          )}
          <button onClick={handleLogout} className="btn-secondary">
            Logout
          </button>
        </div>
      </header>

      {getItemCountWarning() && (
        <div className={`warning-message ${getItemCountWarning().type}`}>
          {getItemCountWarning().message}
        </div>
      )}
      {error && <div className="error-message">{error}</div>}

      {showForm && (
        <div className="form-modal">
          <div className="form-card">
            <h2>{editingItem ? 'Edit Item' : 'Create New Item'}</h2>
            <form onSubmit={editingItem ? handleUpdate : handleCreate}>
              <div className="form-group">
                <label>Title *</label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Username</label>
                <input
                  type="text"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Password {editingItem ? '(leave empty to keep current)' : '*'}</label>
                <input
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  required={!editingItem}
                  maxLength={planLimits?.maxPasswordLength || undefined}
                />
                {planLimits && (
                  <small className="form-hint">
                    Max length: {planLimits.maxPasswordLength} characters
                    {formData.password && ` (${formData.password.length}/${planLimits.maxPasswordLength})`}
                  </small>
                )}
              </div>
              <div className="form-group">
                <label>URL</label>
                <input
                  type="url"
                  value={formData.url}
                  onChange={(e) => setFormData({ ...formData, url: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Notes</label>
                <textarea
                  value={formData.notes}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  rows="3"
                />
              </div>
              <div className="form-actions">
                <button type="submit" className="btn-primary">
                  {editingItem ? 'Update' : 'Create'}
                </button>
                <button type="button" onClick={handleCancel} className="btn-secondary">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="items-grid">
        {items.length === 0 ? (
          <div className="empty-state">
            <p>No vault items yet. Click "Add Item" to create your first item.</p>
          </div>
        ) : (
          items.map((item) => (
            <div key={item.id} className="vault-item-card">
              <div className="item-header">
                <h3>{item.title}</h3>
                <div className="item-actions">
                  <button onClick={() => handleEdit(item)} className="btn-edit">Edit</button>
                  {planLimits?.canShare && (
                    <button 
                      onClick={() => {
                        const sharedWith = prompt('Enter username or email to share with:');
                        if (sharedWith) handleShare(item.id, sharedWith);
                      }} 
                      className="btn-share"
                      title="Share"
                    >
                      Share
                    </button>
                  )}
                  <button onClick={() => handleDelete(item.id)} className="btn-delete">Delete</button>
                </div>
              </div>
              {item.username && <p className="item-field"><strong>Username:</strong> {item.username}</p>}
              {item.url && (
                <p className="item-field">
                  <strong>URL:</strong>{' '}
                  <a href={item.url} target="_blank" rel="noopener noreferrer">
                    {item.url}
                  </a>
                </p>
              )}
              {item.notes && <p className="item-field"><strong>Notes:</strong> {item.notes}</p>}
              {item.createdAt && (
                <p className="item-date">Created: {new Date(item.createdAt).toLocaleDateString()}</p>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default VaultPage;

