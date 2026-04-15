import { useEffect, useState } from 'react'
import { Search, Users, X, Plus, Edit2, Trash2 } from 'lucide-react'
import { membersAPI } from '../services/api.js'
import toast from 'react-hot-toast'
import { useAuth } from '../context/AuthContext.jsx'


const emptyForm = {
  username: '',
  email: '',
  password: '',
  roles: ['USER'],
  enabled: true,
}

export default function MembersPage() {
  const { isAdmin, user } = useAuth()
  const [members, setMembers] = useState([])
  const [filtered, setFiltered] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editMember, setEditMember] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)

  const load = () => {
    setLoading(true)
    membersAPI.getAll()
      .then(res => {
        setMembers(res.data)
        setFiltered(res.data)
      })
      .catch((err) => toast.error(err.response?.data?.message || err.response?.data || 'Failed to load users'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  useEffect(() => {
    if (!search.trim()) {
      setFiltered(members)
      return
    }
    const q = search.toLowerCase()
    setFiltered(members.filter(member =>
      member.username.toLowerCase().includes(q) ||
      member.email.toLowerCase().includes(q) ||
      member.primaryRole.toLowerCase().includes(q) ||
      member.roles.some(role => role.toLowerCase().includes(q))
    ))
  }, [search, members])

  const roleBadge = (role) => {
    if (role === 'ADMIN') return 'badge-danger'
    if (role === 'LIBRARIAN') return 'badge-info'
    return 'badge-success'
  }

  const statusBadge = (status) => {
    if (status === 'ACTIVE') return 'badge-success'
    return 'badge-secondary'
  }

  const openCreate = () => {
    setEditMember(null)
    setForm(emptyForm)
    setShowModal(true)
  }

  const openEdit = (member) => {
    setEditMember(member)
    setForm({
      username: member.username,
      email: member.email,
      password: '',
      roles: member.roles,
      enabled: member.status === 'ACTIVE',
    })
    setShowModal(true)
  }

  const closeModal = () => {
    setShowModal(false)
    setEditMember(null)
    setForm(emptyForm)
  }

  const toggleRole = (role) => {
    const exists = form.roles.includes(role)
    const nextRoles = exists ? form.roles.filter((r) => r !== role) : [...form.roles, role]
    setForm({ ...form, roles: nextRoles })
  }

  const handleSave = async (e) => {
    e.preventDefault()
    if (!form.roles.length) {
      toast.error('Select at least one role')
      return
    }

    setSaving(true)
    try {
      if (editMember) {
        await membersAPI.update(editMember.id, {
          username: form.username,
          email: form.email,
          roles: form.roles,
          enabled: form.enabled,
        })
        toast.success('User updated successfully')
      } else {
        await membersAPI.create(form)
        toast.success('User created successfully')
      }
      load()
      closeModal()
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to save user')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (member) => {
    if (!window.confirm(`Delete user "${member.username}"?`)) return
    try {
      await membersAPI.delete(member.id)
      toast.success('User deleted successfully')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to delete user')
    }
  }

  if (loading) return <div className="spinner"><div className="spinner-ring" /></div>

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Users</h1>
          <p className="page-subtitle">
            {isAdmin ? 'Admin can add, update, disable and delete users' : 'Admin and librarian can view all registered users'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <div className="search-box">
            <Search size={16} color="#94a3b8" />
            <input
              placeholder="Search users..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
            {search && <X size={14} color="#94a3b8" style={{ cursor: 'pointer' }} onClick={() => setSearch('')} />}
          </div>
          {isAdmin && (
            <button className="btn btn-primary" onClick={openCreate}>
              <Plus size={16} /> Add User
            </button>
          )}
        </div>
      </div>

      <div className="card">
        {filtered.length === 0 ? (
          <div className="empty-state">
            <Users size={48} />
            <h3>No users found</h3>
            <p>{search ? 'Try a different search term' : 'Registered users will appear here automatically'}</p>
          </div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Roles</th>
                <th>Status</th>
                <th>Borrowings</th>
                <th>Fine</th>
                <th>Created</th>
                {isAdmin && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {filtered.map(member => (
                <tr key={member.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{member.username}</div>
                    <div style={{ fontSize: 12, color: '#64748b' }}>User ID: {member.id}</div>
                  </td>
                  <td style={{ fontSize: 13 }}>{member.email}</td>
                  <td>
                    <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                      {member.roles.map(role => (
                        <span key={role} className={`badge ${roleBadge(role)}`}>{role}</span>
                      ))}
                    </div>
                  </td>
                  <td><span className={`badge ${statusBadge(member.status)}`}>{member.status}</span></td>
                  <td>
                    <span style={{ fontWeight: 600, color: member.activeBorrowings > 0 ? '#d97706' : '#94a3b8' }}>
                      {member.activeBorrowings} active
                    </span>
                    <span style={{ fontSize: 12, color: '#94a3b8', marginLeft: 4 }}>/ {member.totalBorrowings} total</span>
                  </td>
                  <td>
                    <span className={`badge ${member.hasOutstandingFines ? 'badge-danger' : 'badge-success'}`}>
                      {member.hasOutstandingFines ? 'Pending Fine' : 'No Fine'}
                    </span>
                  </td>
                  <td style={{ fontSize: 13 }}>{member.createdAt ? new Date(member.createdAt).toLocaleDateString() : '—'}</td>
                  {isAdmin && (
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(member)}>
                          <Edit2 size={13} />
                        </button>
                        {member.username !== user?.username && (
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(member)}>
                            <Trash2 size={13} />
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && closeModal()}>
          <div className="modal">
            <div className="modal-header">
              <h2 style={{ fontSize: 17, fontWeight: 700 }}>{editMember ? 'Update User' : 'Add New User'}</h2>
              <button onClick={closeModal} style={{ background: 'none', color: '#64748b', padding: 4 }}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleSave}>
              <div className="modal-body">
                <div className="grid-2">
                  <div className="form-group">
                    <label className="form-label">Username *</label>
                    <input className="form-control" value={form.username} onChange={e => setForm({ ...form, username: e.target.value })} required />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Email *</label>
                    <input className="form-control" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required />
                  </div>
                </div>

                {!editMember && (
                  <div className="form-group">
                    <label className="form-label">Password *</label>
                    <input className="form-control" type="password" minLength={6} value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} required />
                  </div>
                )}

                <div className="form-group">
                  <label className="form-label">Roles *</label>
                  <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                    {['ADMIN', 'LIBRARIAN', 'USER'].map(role => (
                      <label key={role} style={{ display: 'flex', gap: 6, alignItems: 'center', fontSize: 14 }}>
                        <input
                          type="checkbox"
                          checked={form.roles.includes(role)}
                          onChange={() => toggleRole(role)}
                        />
                        {role}
                      </label>
                    ))}
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Status</label>
                  <select className="form-control" value={form.enabled ? 'ACTIVE' : 'DISABLED'} onChange={e => setForm({ ...form, enabled: e.target.value === 'ACTIVE' })}>
                    <option value="ACTIVE">Active</option>
                    <option value="DISABLED">Disabled</option>
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving...' : editMember ? 'Update User' : 'Create User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
