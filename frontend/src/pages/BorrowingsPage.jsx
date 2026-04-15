import { useEffect, useMemo, useState } from 'react'
import { Plus, Search, RefreshCw, Undo2, AlertCircle, X, CalendarDays, CreditCard } from 'lucide-react'
import { borrowingsAPI, booksAPI } from '../services/api.js'
import toast from 'react-hot-toast'
import { useAuth } from '../context/AuthContext.jsx'



const emptyForm = {
  bookId: '',
  quantity: 1,
  notes: '',
}

export default function BorrowingsPage() {
  const { user, isManager } = useAuth()
  const [borrowings, setBorrowings] = useState([])
  const [filtered, setFiltered] = useState([])
  const [books, setBooks] = useState([])
  const [loading, setLoading] = useState(true)
  const [showBorrowModal, setShowBorrowModal] = useState(false)
  const [showDueDateModal, setShowDueDateModal] = useState(false)
  const [saving, setSaving] = useState(false)
  const [updatingDueDate, setUpdatingDueDate] = useState(false)
  const [selectedBorrowing, setSelectedBorrowing] = useState(null)
  const [selectedDueDate, setSelectedDueDate] = useState('')
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [form, setForm] = useState(emptyForm)

  const today = new Date().toISOString().split('T')[0]
  const maxDateObj = new Date()
  maxDateObj.setDate(maxDateObj.getDate() + 7)
  const maxDate = maxDateObj.toISOString().split('T')[0]

  const load = () => {
    setLoading(true)
    Promise.all([borrowingsAPI.getAll(), booksAPI.getAll()])
      .then(([borrowingsRes, booksRes]) => {
        setBorrowings(borrowingsRes.data)
        setFiltered(borrowingsRes.data)
        setBooks(booksRes.data.filter((b) => b.availableCopies > 0))
      })
      .catch(() => toast.error('Failed to load borrowings'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  useEffect(() => {
    let list = borrowings
    if (statusFilter !== 'ALL') list = list.filter(b => b.status === statusFilter)
    if (search.trim()) {
      const q = search.toLowerCase()
      list = list.filter(b =>
        b.bookTitle.toLowerCase().includes(q) ||
        b.username.toLowerCase().includes(q) ||
        b.bookIsbn.toLowerCase().includes(q)
      )
    }
    setFiltered(list)
  }, [search, statusFilter, borrowings])

  const activeCount = useMemo(
    () => borrowings.filter(b => b.status === 'BORROWED').length,
    [borrowings]
  )

  const handleBorrow = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      await borrowingsAPI.borrow({
        bookId: parseInt(form.bookId),
        quantity: Number(form.quantity),
        notes: form.notes || undefined,
      })
      toast.success('Book borrowed successfully. Due date is automatically set to today + 7 days.')
      setShowBorrowModal(false)
      setForm(emptyForm)
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to process borrowing')
    } finally {
      setSaving(false)
    }
  }

  const handleReturn = async (id) => {
    if (!window.confirm('Mark this borrowing as returned?')) return
    try {
      await borrowingsAPI.return(id)
      toast.success('Book returned successfully')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to process return')
    }
  }

  const handleRenew = async (id) => {
    try {
      await borrowingsAPI.renew(id)
      toast.success('Borrowing renewed by 7 days')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to renew')
    }
  }

  const handlePayFine = async (id) => {
    if (!window.confirm('Confirm fine payment for this borrowing?')) return
    try {
      await borrowingsAPI.payFine(id)
      toast.success('Fine paid successfully. You can borrow again now.')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to pay fine')
    }
  }

  const openDueDateModal = (borrowing) => {
    setSelectedBorrowing(borrowing)
    setSelectedDueDate(borrowing.dueDate)
    setShowDueDateModal(true)
  }

  const handleUpdateDueDate = async (e) => {
    e.preventDefault()
    if (!selectedBorrowing) return
    setUpdatingDueDate(true)
    try {
      await borrowingsAPI.updateDueDate(selectedBorrowing.id, selectedDueDate)
      toast.success('Due date updated successfully')
      setShowDueDateModal(false)
      setSelectedBorrowing(null)
      setSelectedDueDate('')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to update due date')
    } finally {
      setUpdatingDueDate(false)
    }
  }

  const statusBadge = (status) => {
    if (status === 'BORROWED') return 'badge-info'
    if (status === 'RETURNED') return 'badge-success'
    if (status === 'OVERDUE') return 'badge-danger'
    return 'badge-warning'
  }

  const canReturnOrRenew = (borrowing) =>
    borrowing.status === 'BORROWED' && (isManager || borrowing.username === user?.username)

  if (loading) {
    return (
      <div className="spinner">
        <div className="spinner-ring" />
      </div>
    )
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Borrowings</h1>
          <p className="page-subtitle">
            {isManager ? `${activeCount} active borrowings in the library` : `Your borrow and return history`}
          </p>
        </div>

        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <div className="search-box">
            <Search size={16} color="#94a3b8" />
            <input
              placeholder={isManager ? 'Search book or user...' : 'Search your books...'}
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
            {search && (
              <X
                size={14}
                color="#94a3b8"
                style={{ cursor: 'pointer' }}
                onClick={() => setSearch('')}
              />
            )}
          </div>

          <select
            className="form-control"
            style={{ width: 140 }}
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
          >
            <option value="ALL">All Status</option>
            <option value="BORROWED">Borrowed</option>
            <option value="RETURNED">Returned</option>
            <option value="OVERDUE">Overdue</option>
          </select>

          <button className="btn btn-primary" onClick={() => setShowBorrowModal(true)}>
            <Plus size={16} /> Borrow Book
          </button>
        </div>
      </div>

      <div className="card">
        {filtered.length === 0 ? (
          <div className="empty-state">
            <AlertCircle size={48} />
            <h3>No borrowings found</h3>
            <p>Use “Borrow Book” to create your first borrowing</p>
          </div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Book</th>
                {isManager && <th>User</th>}
                <th>Quantity</th>
                <th>Borrow Date</th>
                <th>Due Date</th>
                <th>Return Date</th>
                <th>Status</th>
                <th>Fine</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(b => (
                <tr key={b.id}>
                  <td>
                    <div style={{ fontWeight: 600, fontSize: 13 }}>{b.bookTitle}</div>
                    <div style={{ fontSize: 11, color: '#64748b', fontFamily: 'monospace' }}>
                      {b.bookIsbn}
                    </div>
                  </td>

                  {isManager && (
                    <td>
                      <div style={{ fontWeight: 500, fontSize: 13 }}>{b.username}</div>
                      <div style={{ fontSize: 11, color: '#64748b' }}>{b.userEmail}</div>
                    </td>
                  )}

                  <td style={{ fontWeight: 600 }}>{b.quantity}</td>
                  <td style={{ fontSize: 13 }}>{b.borrowDate}</td>
                  <td style={{ fontSize: 13 }}>{b.dueDate}</td>
                  <td style={{ fontSize: 13 }}>{b.returnDate || '—'}</td>
                  <td>
                    <span className={`badge ${statusBadge(b.status)}`}>{b.status}</span>
                  </td>
                  <td style={{ fontSize: 13 }}>
                    {Number(b.fineAmount) > 0 ? (
                      <div>
                        <div style={{ fontWeight: 600 }}>${Number(b.fineAmount).toFixed(2)}</div>
                        <div style={{ fontSize: 11, color: b.finePaid ? '#16a34a' : '#dc2626' }}>
                          {b.finePaid ? 'Paid' : 'Unpaid'}
                        </div>
                      </div>
                    ) : '—'}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                      {canReturnOrRenew(b) && (
                        <>
                          <button
                            className="btn btn-success btn-sm"
                            onClick={() => handleReturn(b.id)}
                            title="Return"
                          >
                            <Undo2 size={13} />
                          </button>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => handleRenew(b.id)}
                            title="Renew"
                          >
                            <RefreshCw size={13} />
                          </button>
                        </>
                      )}

                      {isManager && b.status !== 'RETURNED' && (
                        <button
                          className="btn btn-primary btn-sm"
                          onClick={() => openDueDateModal(b)}
                          title="Update Due Date"
                        >
                          <CalendarDays size={13} />
                        </button>
                      )}

                      {Number(b.fineAmount) > 0 && !b.finePaid && (isManager || b.username === user?.username) && (
                        <button
                          className="btn btn-warning btn-sm"
                          onClick={() => handlePayFine(b.id)}
                          title="Pay Fine"
                        >
                          <CreditCard size={13} />
                        </button>
                      )}

                      {!canReturnOrRenew(b) && !(isManager && b.status !== 'RETURNED') && !(Number(b.fineAmount) > 0 && !b.finePaid && (isManager || b.username === user?.username)) && '—'}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showBorrowModal && (
        <div
          className="modal-overlay"
          onClick={e => e.target === e.currentTarget && setShowBorrowModal(false)}
        >
          <div className="modal">
            <div className="modal-header">
              <h2 style={{ fontSize: 17, fontWeight: 700 }}>Borrow Books</h2>
              <button
                onClick={() => setShowBorrowModal(false)}
                style={{ background: 'none', color: '#64748b', padding: 4 }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleBorrow}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Select Book *</label>
                  <select
                    className="form-control"
                    value={form.bookId}
                    onChange={e => setForm({ ...form, bookId: e.target.value })}
                    required
                  >
                    <option value="">-- Choose a book --</option>
                    {books.map(b => (
                      <option key={b.id} value={b.id}>
                        {b.title} by {b.author} ({b.availableCopies} available)
                      </option>
                    ))}
                  </select>
                </div>

                <div className="grid-2">
                  <div className="form-group">
                    <label className="form-label">Quantity *</label>
                    <input
                      className="form-control"
                      type="number"
                      min="1"
                      value={form.quantity}
                      onChange={e => setForm({ ...form, quantity: e.target.value })}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Due Date</label>
                    <input
                      className="form-control"
                      type="text"
                      value="Automatic: current date + 7 days"
                      disabled
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Notes</label>
                  <textarea
                    className="form-control"
                    rows={2}
                    value={form.notes}
                    onChange={e => setForm({ ...form, notes: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowBorrowModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Processing...' : 'Borrow'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showDueDateModal && selectedBorrowing && (
        <div
          className="modal-overlay"
          onClick={e => e.target === e.currentTarget && setShowDueDateModal(false)}
        >
          <div className="modal">
            <div className="modal-header">
              <h2 style={{ fontSize: 17, fontWeight: 700 }}>Update Due Date</h2>
              <button
                onClick={() => setShowDueDateModal(false)}
                style={{ background: 'none', color: '#64748b', padding: 4 }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleUpdateDueDate}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Book</label>
                  <input className="form-control" type="text" value={selectedBorrowing.bookTitle} disabled />
                </div>
                <div className="form-group">
                  <label className="form-label">Allowed Range</label>
                  <input className="form-control" type="text" value={`${today} to ${maxDate}`} disabled />
                </div>
                <div className="form-group">
                  <label className="form-label">Custom Due Date *</label>
                  <input
                    className="form-control"
                    type="date"
                    min={today}
                    max={maxDate}
                    value={selectedDueDate}
                    onChange={e => setSelectedDueDate(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowDueDateModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={updatingDueDate}>
                  {updatingDueDate ? 'Updating...' : 'Update Due Date'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
