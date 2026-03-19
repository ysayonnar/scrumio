import axios from 'axios'

function toSnakeCase(str: string): string {
  return str.replace(/[A-Z]/g, (c) => `_${c.toLowerCase()}`)
}

function toCamelCase(str: string): string {
  return str.replace(/_([a-z])/g, (_, c) => c.toUpperCase())
}

function transformKeys(obj: unknown, fn: (k: string) => string): unknown {
  if (Array.isArray(obj)) return obj.map((v) => transformKeys(v, fn))
  if (obj !== null && typeof obj === 'object') {
    return Object.fromEntries(
      Object.entries(obj).map(([k, v]) => [fn(k), transformKeys(v, fn)])
    )
  }
  return obj
}

const client = axios.create({
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

client.interceptors.request.use((config) => {
  if (config.data) config.data = transformKeys(config.data, toSnakeCase)
  return config
})

client.interceptors.response.use(
  (res) => {
    if (res.data) res.data = transformKeys(res.data, toCamelCase)
    return res
  },
  (error) => {
    if (error.response?.status === 401) window.location.href = '/login'
    if (error.response?.data) {
      error.response.data = transformKeys(error.response.data, toCamelCase)
    }
    return Promise.reject(error)
  },
)

export default client
