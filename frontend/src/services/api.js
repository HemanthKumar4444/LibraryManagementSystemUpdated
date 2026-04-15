import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

  const api = axios.create({
    baseURL: API_BASE,
    headers: { 'Content-Type': 'application/json' },
  })

  api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  api.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }
  )

  export default api

  export const authAPI = {
    login: (username, password) =>
      api.post('/auth/login', { username, password }),
    register: (payload) =>
      api.post('/auth/register', payload),
    me: () => api.get('/auth/me'),
  }

  export const booksAPI = {
    getAll: () => api.get('/books'),
    getById: (id) => api.get(`/books/${id}`),
    search: (keyword) => api.get(`/books/search?keyword=${keyword}`),
    getCategories: () => api.get('/books/categories'),
    create: (data) => api.post('/books', data),
    update: (id, data) => api.put(`/books/${id}`, data),
    addSingleCopy: (id) => api.post(`/books/${id}/single-copy`),
    addCopies: (id, copies) => api.post(`/books/${id}/copies`, { copies }),
    deleteSingleCopy: (id) => api.delete(`/books/${id}/single-copy`),
    delete: (id) => api.delete(`/books/${id}`),
  }

  export const membersAPI = {
    getAll: () => api.get('/members'),
    getById: (id) => api.get(`/members/${id}`),
    search: (keyword) => api.get(`/members/search?keyword=${keyword}`),
    create: (data) => api.post('/members', data),
    update: (id, data) => api.put(`/members/${id}`, data),
    delete: (id) => api.delete(`/members/${id}`),
  }

  export const borrowingsAPI = {
    getAll: () => api.get('/borrowings'),
    getById: (id) => api.get(`/borrowings/${id}`),
    getOverdue: () => api.get('/borrowings/overdue'),
    borrow: (data) => api.post('/borrowings/borrow', data),
    return: (id) => api.post(`/borrowings/${id}/return`),
    renew: (id) => api.post(`/borrowings/${id}/renew`),
    updateDueDate: (id, dueDate) => api.put(`/borrowings/${id}/due-date`, { dueDate }),
    payFine: (id) => api.post(`/borrowings/${id}/pay-fine`),
  }

  export const dashboardAPI = {
    getStats: () => api.get('/dashboard/stats'),
  }
