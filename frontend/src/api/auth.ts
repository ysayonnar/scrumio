import axios from 'axios'

const authClient = axios.create({
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

export const register = (email: string, password: string) =>
  authClient.post('/registration', { email, password })

export const login = (email: string, password: string) =>
  authClient.post('/login', { email, password })

export const logout = () => authClient.get('/logout')
