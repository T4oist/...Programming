import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { clearAuth } from '../utils/auth'

/**
 * Axios 实例：
 * - 自动携带管理员 JWT
 * - 统一解析 { code, message, data } 响应
 * - 401 统一跳转登录页（并发去重，避免重复弹窗/导航）
 */
const http = axios.create({
  // 生产可由 VITE_API_BASE_URL 指定；开发默认走 vite 代理的 /api
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    ElMessage.error('请求配置错误')
    return Promise.reject(error)
  }
)

// 401 处理去重标志：并发请求同时过期时只处理一次
let handling401 = false

http.interceptors.response.use(
  (resp) => {
    const body = resp.data
    // 兼容 HTTP 200 + 业务 code 非 0 的情况
    if (body && typeof body === 'object' && 'code' in body && body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body?.data
  },
  (error) => {
    const body = error.response?.data
    if (error.response?.status === 401) {
      if (!handling401) {
        handling401 = true
        clearAuth()
        if (router.currentRoute.value.path !== '/login') {
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath }
          })
        }
        ElMessage.error('登录已过期，请重新登录')
        // 登录成功后由 LoginView 重置该标志；此处兜底 1 秒后释放
        setTimeout(() => { handling401 = false }, 1000)
      }
    } else {
      ElMessage.error(body?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

/** 登录成功后调用，复位 401 去重标志 */
export function reset401Flag() {
  handling401 = false
}

export default http
