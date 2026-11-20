import http from './index'

// ---------------- 管理员账号 ----------------
export const login = (data) => http.post('/admin/auth/login', data)
export const getProfile = () => http.get('/admin/auth/profile')
export const updateProfile = (data) => http.put('/admin/auth/profile', data)
export const changePassword = (data) => http.put('/admin/auth/password', data)

// ---------------- 活动管理（管理端） ----------------
export const listActivities = (params) => http.get('/admin/activities', { params })
export const createActivity = (data) => http.post('/admin/activities', data)
export const getActivity = (id) => http.get(`/admin/activities/${id}`)
export const updateActivity = (id, data) => http.put(`/admin/activities/${id}`, data)
export const deleteActivity = (id) => http.delete(`/admin/activities/${id}`)
export const publishActivity = (id) => http.post(`/admin/activities/${id}/publish`)
export const getEnrollments = (id) => http.get(`/admin/activities/${id}/enrollments`)
export const getStats = (id) => http.get(`/admin/activities/${id}/stats`)
export const getCheckinToken = (id) => http.get(`/admin/activities/${id}/checkin-token`)

// ---------------- 公开接口（免登录） ----------------
export const getPublicColleges = () => http.get('/public/colleges')
export const getPublicActivity = (id) => http.get(`/public/activities/${id}`)
export const signup = (id, data) => http.post(`/public/activities/${id}/signup`, data)
export const checkin = (data) => http.post('/public/checkin', data)
