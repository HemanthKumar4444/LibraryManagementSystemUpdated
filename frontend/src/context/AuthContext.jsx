import { createContext, useContext, useState } from 'react'
import { authAPI } from '../services/api.js'




const AuthContext = createContext(undefined)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user')
    return saved ? JSON.parse(saved) : null
  })
  const [token, setToken] = useState(() => localStorage.getItem('token'))

  const persistAuth = (data) => {
    setToken(data.token)
    const userData = { username: data.username, email: data.email, roles: data.roles || [] }
    setUser(userData)
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  const login = async (username, password) => {
    const response = await authAPI.login(username, password)
    persistAuth(response.data)
  }

  const register = async (payload) => {
    const response = await authAPI.register(payload)
    persistAuth(response.data)
  }

  const logout = () => {
    setUser(null)
    setToken(null)
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const roles = user?.roles || []
  const isAdmin = roles.includes('ADMIN')
  const isLibrarian = roles.includes('LIBRARIAN')
  const isManager = isAdmin || isLibrarian
  const isUser = roles.includes('USER')

  return (
    <AuthContext.Provider value={{
      user,
      token,
      login,
      register,
      logout,
      isAuthenticated: !!token,
      isAdmin,
      isLibrarian,
      isManager,
      isUser,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
