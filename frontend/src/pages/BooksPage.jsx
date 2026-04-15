import { useEffect, useState } from 'react'
import { Plus, Search, Edit2, Trash2, BookOpen, X, MinusCircle, PlusCircle, Layers3 } from 'lucide-react'
import { booksAPI } from '../services/api.js'
import toast from 'react-hot-toast'
import { useAuth } from '../context/AuthContext.jsx'


const emptyBook = {
  title: '', author: '', isbn: '', category: '',
  publisher: '', publishedYear: new Date().getFullYear(),
  totalCopies: 1, description: '', status: 'AVAILABLE'
}

export default function BooksPage() {
  const { isManager } = useAuth()
  const [books, setBooks] = useState([])
  const [filtered, setFiltered] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editBook, setEditBook] = useState(null)
  const [form, setForm] = useState(emptyBook)
  const [saving, setSaving] = useState(false)
  const [copiesModalBook, setCopiesModalBook] = useState(null)
  const [copiesToAdd, setCopiesToAdd] = useState(1)

  const load = () => {
    booksAPI.getAll()
      .then(res => { setBooks(res.data); setFiltered(res.data) })
      .catch(() => toast.error('Failed to load books'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  useEffect(() => {
    if (!search.trim()) { setFiltered(books); return }
    const q = search.toLowerCase()
    setFiltered(books.filter(b =>
      b.title.toLowerCase().includes(q) ||
      b.author.toLowerCase().includes(q) ||
      b.isbn.toLowerCase().includes(q) ||
      b.category.toLowerCase().includes(q)
    ))
  }, [search, books])

  const openCreate = () => { setEditBook(null); setForm(emptyBook); setShowModal(true) }
  const openEdit = (book) => { setEditBook(book); setForm({ ...book }); setShowModal(true) }
  const closeModal = () => { setShowModal(false); setEditBook(null) }
  const closeCopiesModal = () => { setCopiesModalBook(null); setCopiesToAdd(1) }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      if (editBook) {
        await booksAPI.update(editBook.id, form)
        toast.success('Book updated successfully')
      } else {
        await booksAPI.create(form)
        toast.success('Book added successfully')
      }
      load()
      closeModal()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save book')
    } finally {
      setSaving(false)
    }
  }


  const handleAddSingleCopy = async (book) => {
    try {
      await booksAPI.addSingleCopy(book.id)
      toast.success('One copy added successfully')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add single copy')
    }
  }

  const handleAddMultipleCopies = async (e) => {
    e.preventDefault()
    if (!copiesModalBook) return

    try {
      await booksAPI.addCopies(copiesModalBook.id, copiesToAdd)
      toast.success(`${copiesToAdd} copie${copiesToAdd > 1 ? 's' : ''} added successfully`)
      load()
      closeCopiesModal()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add copies')
    }
  }

  const handleDeleteSingleCopy = async (book) => {
    if (!window.confirm(`Delete one available copy of "${book.title}"?`)) return
    try {
      await booksAPI.deleteSingleCopy(book.id)
      toast.success('One copy deleted successfully')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete single copy')
    }
  }

  const handleDelete = async (book) => {
    if (!window.confirm(`Delete all copies of "${book.title}" and remove the book completely?`)) return
    try {
      await booksAPI.delete(book.id)
      toast.success('Entire book deleted')
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete book')
    }
  }

  if (loading) return <div className="spinner"><div className="spinner-ring" /></div>

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Books</h1>
          <p className="page-subtitle">
            {isManager ? `${books.length} books in collection. You can add 1 copy, add many copies, remove 1 copy, or delete the whole title.` : 'Browse the catalogue and borrow what you need'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <div className="search-box">
            <Search size={16} color="#94a3b8" />
            <input
              placeholder="Search books..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
            {search && <X size={14} color="#94a3b8" style={{ cursor: 'pointer' }} onClick={() => setSearch('')} />}
          </div>
          {isManager && (
            <button className="btn btn-primary" onClick={openCreate}>
              <Plus size={16} /> Add Book
            </button>
          )}
        </div>
      </div>

      <div className="card">
        {filtered.length === 0 ? (
          <div className="empty-state">
            <BookOpen size={48} />
            <h3>No books found</h3>
            <p>{search ? 'Try a different search term' : 'Add your first book to get started'}</p>
          </div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Title / Author</th>
                <th>ISBN</th>
                <th>Category</th>
                <th>Copies</th>
                <th>Available</th>
                <th>Status</th>
                {isManager && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {filtered.map(book => (
                <tr key={book.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{book.title}</div>
                    <div style={{ fontSize: 12, color: '#64748b' }}>{book.author}</div>
                  </td>
                  <td style={{ fontFamily: 'monospace', fontSize: 12 }}>{book.isbn}</td>
                  <td><span className="badge badge-secondary">{book.category}</span></td>
                  <td>{book.totalCopies}</td>
                  <td style={{ fontWeight: 600, color: book.availableCopies > 0 ? '#16a34a' : '#dc2626' }}>
                    {book.availableCopies}
                  </td>
                  <td>
                    <span className={`badge ${book.availableCopies > 0 ? 'badge-success' : 'badge-danger'}`}>
                      {book.availableCopies > 0 ? 'Available' : 'Unavailable'}
                    </span>
                  </td>
                  {isManager && (
                    <td>
                      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(book)} title="Edit book">
                          <Edit2 size={13} />
                        </button>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleAddSingleCopy(book)}
                          title="Add one individual copy"
                        >
                          <PlusCircle size={13} />
                        </button>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => { setCopiesModalBook(book); setCopiesToAdd(1) }}
                          title="Add any number of copies"
                        >
                          <Layers3 size={13} />
                        </button>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleDeleteSingleCopy(book)}
                          title="Delete one individual available copy"
                          disabled={book.totalCopies <= 1 || book.availableCopies <= 0}
                        >
                          <MinusCircle size={13} />
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(book)} title="Delete all copies and remove book">
                          <Trash2 size={13} />
                        </button>
                      </div>
                      <div style={{ fontSize: 11, color: '#64748b', marginTop: 6 }}>
                        Add 1 / Add many / Delete 1 / Delete all
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {copiesModalBook && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && closeCopiesModal()}>
          <div className="modal">
            <div className="modal-header">
              <h2 style={{ fontSize: 17, fontWeight: 700 }}>Add Copies</h2>
              <button onClick={closeCopiesModal} style={{ background: 'none', color: '#64748b', padding: 4 }}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleAddMultipleCopies}>
              <div className="modal-body">
                <div style={{ marginBottom: 12, color: '#334155' }}>
                  <div style={{ fontWeight: 600 }}>{copiesModalBook.title}</div>
                  <div style={{ fontSize: 12, color: '#64748b' }}>Current total: {copiesModalBook.totalCopies} | Available: {copiesModalBook.availableCopies}</div>
                </div>
                <div className="form-group">
                  <label className="form-label">Number of copies to add *</label>
                  <input
                    className="form-control"
                    type="number"
                    min="1"
                    value={copiesToAdd}
                    onChange={e => setCopiesToAdd(Math.max(1, Number(e.target.value) || 1))}
                    required
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={closeCopiesModal}>Cancel</button>
                <button type="submit" className="btn btn-primary">Add Copies</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && closeModal()}>
          <div className="modal">
            <div className="modal-header">
              <h2 style={{ fontSize: 17, fontWeight: 700 }}>{editBook ? 'Edit Book' : 'Add New Book'}</h2>
              <button onClick={closeModal} style={{ background: 'none', color: '#64748b', padding: 4 }}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleSave}>
              <div className="modal-body">
                <div className="grid-2">
                  <div className="form-group">
                    <label className="form-label">Title *</label>
                    <input className="form-control" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} required />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Author *</label>
                    <input className="form-control" value={form.author} onChange={e => setForm({ ...form, author: e.target.value })} required />
                  </div>
                </div>
                <div className="grid-2">
                  <div className="form-group">
                    <label className="form-label">ISBN *</label>
                    <input className="form-control" value={form.isbn} onChange={e => setForm({ ...form, isbn: e.target.value })} required />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Category *</label>
                    <input className="form-control" value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} required />
                  </div>
                </div>
                <div className="grid-2">
                  <div className="form-group">
                    <label className="form-label">Publisher</label>
                    <input className="form-control" value={form.publisher || ''} onChange={e => setForm({ ...form, publisher: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Published Year</label>
                    <input className="form-control" type="number" value={form.publishedYear || ''} onChange={e => setForm({ ...form, publishedYear: parseInt(e.target.value) })} />
                  </div>
                </div>
                <div className="form-group">
                  <label className="form-label">Total Copies *</label>
                  <input className="form-control" type="number" min="1" value={form.totalCopies} onChange={e => setForm({ ...form, totalCopies: parseInt(e.target.value) })} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Description</label>
                  <textarea className="form-control" rows={3} value={form.description || ''} onChange={e => setForm({ ...form, description: e.target.value })} />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving...' : editBook ? 'Update Book' : 'Add Book'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
