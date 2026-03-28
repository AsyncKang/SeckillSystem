import axios from 'axios'

const storage = localStorage

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const token = storage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      storage.removeItem('token')
      storage.removeItem('user')
    }
    return Promise.reject(err)
  }
)

export const userApi = {
  register: (data) => api.post('/user/register', data),
  login: (data) => api.post('/user/login', data),
  me: () => api.get('/user/me')
}

export const productApi = {
  listActivities: () => api.get('/product/activities'),
  seckill: (activityId) => api.post(`/product/seckill/${activityId}`)
}

export const productAdminApi = {
  createProduct: (data) => api.post('/product/admin/product', data),
  createActivity: (data) => api.post('/product/admin/activity', data),
  setOnline: (id) => api.put(`/product/admin/activity/${id}/online`),
  listActivities: () => api.get('/product/admin/activities')
}

export const orderApi = {
  myOrders: () => api.get('/order/my')
}

export default api
