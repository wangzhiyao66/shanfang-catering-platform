import router from '@/router'
import { getToken } from '@/utils/auth'

const WHITE_LIST = ['/login']

// 全局路由守卫：未登录跳登录页；已登录访问登录页跳首页
router.beforeEach((to) => {
  const token = getToken()
  if (!token && !WHITE_LIST.includes(to.path)) {
    return { path: '/login' }
  }
  if (token && to.path === '/login') {
    return { path: '/' }
  }
  // P1：后端下发可访问路由后，在此按角色动态注册/过滤；Phase0 先放行已配置静态路由
  return true
})
