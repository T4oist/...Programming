import { createRouter, createWebHistory } from 'vue-router'
import { tokenValid } from '../utils/auth'

/**
 * 路由：
 * - /admin/** 管理端，需登录（路由守卫）
 * - /login    管理员登录
 * - /a/:id、/a/:id/signup、/checkin/:id 公开页，免登录
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('../views/admin/LoginView.vue') },

    // 管理端
    {
      path: '/admin',
      component: () => import('../views/admin/LayoutView.vue'),
      redirect: '/admin/activities',
      children: [
        {
          path: 'activities',
          component: () => import('../views/admin/ActivityListView.vue')
        },
        {
          path: 'activities/new',
          component: () => import('../views/admin/ActivityEditView.vue')
        },
        {
          path: 'activities/:id/edit',
          component: () => import('../views/admin/ActivityEditView.vue')
        },
        {
          path: 'activities/:id',
          component: () => import('../views/admin/ActivityDetailView.vue')
        },
        {
          path: 'checkin/:id',
          component: () => import('../views/admin/CheckinBoardView.vue')
        },
        {
          path: 'profile',
          component: () => import('../views/admin/ProfileView.vue')
        }
      ]
    },

    // 公开页（手机优先）
    { path: '/a/:id', component: () => import('../views/public/PromoView.vue') },
    { path: '/a/:id/signup', component: () => import('../views/public/SignupView.vue') },
    { path: '/checkin/:id', component: () => import('../views/public/CheckinView.vue') },

    { path: '/:pathMatch(.*)*', redirect: '/login' }
  ]
})

// 登录守卫
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const valid = tokenValid(token)
  // 无效 token 视为未登录，顺手清除，避免残留
  if (token && !valid) {
    localStorage.removeItem('token')
    localStorage.removeItem('adminName')
  }
  if (to.path.startsWith('/admin') && !valid) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  // 已登录用户无需再访问登录页
  if (to.path === '/login' && valid) {
    return { path: '/admin/activities' }
  }
})

export default router
