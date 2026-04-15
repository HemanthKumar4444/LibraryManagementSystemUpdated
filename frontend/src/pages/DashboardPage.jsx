import { useEffect, useState } from 'react'
import { BookOpen, Users, BookMarked, AlertCircle } from 'lucide-react'
import { dashboardAPI } from '../services/api.js'
import toast from 'react-hot-toast'
import { useAuth } from '../context/AuthContext.jsx'


export default function DashboardPage() {
  const { isManager, user } = useAuth()
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    dashboardAPI.getStats()
      .then(res => setStats(res.data))
      .catch(() => toast.error('Failed to load dashboard stats'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="spinner"><div className="spinner-ring" /></div>
  if (!stats) return null

  const cards = isManager
    ? [
        { label: 'Total Books', value: stats.totalBooks, icon: BookOpen, color: '#2563eb', bg: '#eff6ff' },
        { label: 'Active Members', value: stats.activeMembers, icon: Users, color: '#16a34a', bg: '#f0fdf4' },
        { label: 'Active Borrowings', value: stats.activeBorrowings, icon: BookMarked, color: '#d97706', bg: '#fffbeb' },
        { label: 'Overdue', value: stats.overdueBorrowings, icon: AlertCircle, color: '#dc2626', bg: '#fef2f2' },
      ]
    : [
        { label: 'Books Available', value: stats.availableBooks, icon: BookOpen, color: '#2563eb', bg: '#eff6ff' },
        { label: 'My Active Borrowings', value: stats.activeBorrowings, icon: BookMarked, color: '#16a34a', bg: '#f0fdf4' },
        { label: 'My Total Borrowings', value: stats.totalBorrowings, icon: Users, color: '#d97706', bg: '#fffbeb' },
        { label: 'My Overdue', value: stats.overdueBorrowings, icon: AlertCircle, color: '#dc2626', bg: '#fef2f2' },
      ]

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">
            {isManager ? 'Overview of library activity' : `Welcome ${user?.username}, here is your borrowing summary`}
          </p>
        </div>
      </div>

      <div className="grid-4" style={{ marginBottom: 24 }}>
        {cards.map(({ label, value, icon: Icon, color, bg }) => (
          <div key={label} className="stat-card">
            <div className="stat-icon" style={{ background: bg }}>
              <Icon size={22} color={color} />
            </div>
            <div>
              <div className="stat-value">{value}</div>
              <div className="stat-label">{label}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid-2" style={{ gap: 24 }}>
        <div className="card">
          <div className="card-header">
            <h3 style={{ fontWeight: 600, fontSize: 15 }}>
              {isManager ? 'Recent Borrowings' : 'My Recent Borrowings'}
            </h3>
          </div>
          <div>
            {stats.recentBorrowings.length === 0 ? (
              <div style={{ padding: '20px', textAlign: 'center', color: '#94a3b8' }}>No recent borrowings</div>
            ) : (
              stats.recentBorrowings.map((b, i) => (
                <div key={i} style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  padding: '12px 20px', borderBottom: i < stats.recentBorrowings.length - 1 ? '1px solid var(--border)' : 'none'
                }}>
                  <div>
                    <div style={{ fontWeight: 500, fontSize: 13 }}>{b.bookTitle}</div>
                    <div style={{ fontSize: 12, color: '#64748b' }}>
                      {b.username} · Qty {b.quantity}
                    </div>
                  </div>
                  <span className={`badge ${b.status === 'BORROWED' ? 'badge-info' : b.status === 'RETURNED' ? 'badge-success' : 'badge-danger'}`}>
                    {b.status}
                  </span>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 style={{ fontWeight: 600, fontSize: 15 }}>Books by Category</h3>
          </div>
          <div style={{ padding: '8px 0' }}>
            {stats.topCategories.length === 0 ? (
              <div style={{ padding: '20px', textAlign: 'center', color: '#94a3b8' }}>No data</div>
            ) : (
              stats.topCategories.map((cat, i) => (
                <div key={i} style={{ padding: '10px 20px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                    <span style={{ fontSize: 13, fontWeight: 500 }}>{cat.name}</span>
                    <span style={{ fontSize: 13, color: '#64748b' }}>{cat.count} books</span>
                  </div>
                  <div style={{ height: 6, background: '#f1f5f9', borderRadius: 99 }}>
                    <div style={{
                      height: '100%',
                      width: `${Math.min(100, (cat.count / (stats.totalBooks || 1)) * 100)}%`,
                      background: '#2563eb',
                      borderRadius: 99,
                    }} />
                  </div>
                </div>
              ))
            )}
          </div>

          <div style={{ padding: '16px 20px', borderTop: '1px solid var(--border)', display: 'flex', gap: 24 }}>
            <div>
              <div style={{ fontSize: 11, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Available</div>
              <div style={{ fontWeight: 700, fontSize: 20, color: '#16a34a' }}>{stats.availableBooks}</div>
            </div>
            <div>
              <div style={{ fontSize: 11, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Returned</div>
              <div style={{ fontWeight: 700, fontSize: 20 }}>{stats.returnedBorrowings}</div>
            </div>
            <div>
              <div style={{ fontSize: 11, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                {isManager ? 'Total Members' : 'My Role'}
              </div>
              <div style={{ fontWeight: 700, fontSize: 20 }}>{isManager ? stats.totalMembers : 'USER'}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
