import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'

// 静态路由；P1 将由后端下发角色可访问路由后做动态注册
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '工作台', icon: 'Odometer' } },
      { path: 'menu', name: 'Menu', component: () => import('@/views/menu/index.vue'), meta: { title: '菜品管理', icon: 'Food', permission: 'menu:view' } },
      { path: 'table', name: 'Table', component: () => import('@/views/table/index.vue'), meta: { title: '桌台管理', icon: 'Grid', permission: 'table:view' } },
      { path: 'order', name: 'Order', component: () => import('@/views/order/index.vue'), meta: { title: '订单管理', icon: 'List', permission: 'order:view' } },
      { path: 'member', name: 'Member', component: () => import('@/views/member/index.vue'), meta: { title: '会员管理', icon: 'User', permission: 'member:view' } },
      { path: 'report', name: 'Report', component: () => import('@/views/report/index.vue'), meta: { title: '数据报表', icon: 'DataLine', permission: 'report:view' } },
      { path: 'employee', name: 'Employee', component: () => import('@/views/employee/index.vue'), meta: { title: '员工权限', icon: 'Avatar', permission: 'employee:view' } },
      { path: 'marketing', name: 'Marketing', component: () => import('@/views/marketing/index.vue'), meta: { title: '营销工具', icon: 'Present', permission: 'marketing:view' } },
      { path: 'setting', name: 'Setting', component: () => import('@/views/setting/index.vue'), meta: { title: '基础设置', icon: 'Setting', permission: 'setting:view' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

export default router
