import { ref } from 'vue'

/** 登录态工具：token 结构/过期校验与安全的重定向目标 */

/** 管理员姓名：共享响应式引用，个人中心改名后布局实时刷新 */
export const adminName = ref(localStorage.getItem('adminName') || '管理员')

/** 更新姓名（同时持久化） */
export function setAdminName(name) {
  const value = name || '管理员'
  adminName.value = value
  localStorage.setItem('adminName', value)
}

/** JWT 是否具备有效结构且未过期（仅前端粗校验，真正鉴权在后端） */
export function tokenValid(token) {
  if (!token || typeof token !== 'string') return false
  const parts = token.split('.')
  if (parts.length !== 3) return false
  try {
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')))
    if (typeof payload.exp !== 'number') return true // 无 exp 声明时交给后端判定
    return payload.exp * 1000 > Date.now()
  } catch {
    return false
  }
}

/** 校验 redirect 目标：只接受站内以 /admin 开头的路径，防范开放重定向 */
export function safeRedirect(value, fallback = '/admin/activities') {
  if (typeof value !== 'string') return fallback
  if (!value.startsWith('/admin') || value.startsWith('//')) return fallback
  return value
}

/** 统一清除登录态 */
export function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('adminName')
  adminName.value = '管理员'
}
