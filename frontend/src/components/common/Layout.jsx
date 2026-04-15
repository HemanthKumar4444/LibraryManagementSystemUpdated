import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { BookOpen, Users, LayoutDashboard, BookMarked, LogOut } from 'lucide-react'
import { useMemo } from 'react'
import { useAuth } from '../../context/AuthContext.jsx'


export default function Layout() {
  const { user, logout, isManager } = useAuth()
  const navigate = useNavigate()

  const navItems = useMemo(() => {
    const items = [
      { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
      { to: '/books', label: 'Books', icon: BookOpen },
      { to: '/borrowings', label: 'Borrowings', icon: BookMarked },
    ]
    if (isManager) {
      items.splice(2, 0, { to: '/members', label: 'Users', icon: Users })
    }
    return items
  }, [isManager])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div style={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      <aside style={{
        width: 240,
        background: '#1e293b',
        color: 'white',
        display: 'flex',
        flexDirection: 'column',
        flexShrink: 0,
      }}>
        <div style={{ padding: '20px 16px', borderBottom: '1px solid #334155' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{
              width: 36, height: 36, background: '#2563eb',
              borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center'
            }}>
              <BookOpen size={20} color="white" />
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: 14 }}>LibraryMS</div>
            </div>
          </div>
        </div>

        <nav style={{ flex: 1, padding: '12px 8px' }}>
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              style={({ isActive }) => ({
                display: 'flex',
                alignItems: 'center',
                gap: 10,
                padding: '10px 12px',
                borderRadius: 6,
                marginBottom: 2,
                color: isActive ? 'white' : '#94a3b8',
                background: isActive ? '#2563eb' : 'transparent',
                fontWeight: isActive ? 600 : 400,
                fontSize: 14,
              })}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div style={{ padding: '12px 8px', borderTop: '1px solid #334155' }}>
          <div style={{ padding: '10px 12px', marginBottom: 4 }}>
            <div style={{ fontSize: 13, fontWeight: 600 }}>{user?.username}</div>
            <div style={{ fontSize: 11, color: '#94a3b8' }}>{user?.email}</div>
            <div style={{ fontSize: 11, color: '#cbd5e1', marginTop: 4 }}>
              {(user?.roles || []).join(', ')}
            </div>
          </div>
          <button
            onClick={handleLogout}
            style={{
              display: 'flex', alignItems: 'center', gap: 10,
              padding: '10px 12px', borderRadius: 6, width: '100%',
              background: 'transparent', color: '#94a3b8',
              fontSize: 14, border: 'none', cursor: 'pointer',
            }}
          >
            <LogOut size={18} />
            Sign Out
          </button>
        </div>
      </aside>

      <main style={{ flex: 1, overflow: 'auto', background: 'var(--bg)' }}>
        <div style={{ padding: '28px 32px', minHeight: '100%' }}>
          <Outlet />
        </div>
      </main>
    </div>
  )
}
