import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'
import { login as apiLogin, logout as apiLogout, register as apiRegister } from '../api/auth'

interface AuthContextValue {
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  register: (email: string, password: string) => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return localStorage.getItem('auth') === 'true'
  })

  const login = useCallback(async (email: string, password: string) => {
    await apiLogin(email, password)
    setIsAuthenticated(true)
    localStorage.setItem('auth', 'true')
  }, [])

  const logout = useCallback(async () => {
    await apiLogout()
    setIsAuthenticated(false)
    localStorage.removeItem('auth')
  }, [])

  const register = useCallback(async (email: string, password: string) => {
    await apiRegister(email, password)
  }, [])

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
