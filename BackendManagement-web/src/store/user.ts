import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, type LoginResult } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

// 用户状态：登录态、姓名、角色（用于 RBAC）
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken())
  const name = ref<string>('')
  const roles = ref<string[]>([])

  async function login(username: string, password: string) {
    const data: LoginResult = await loginApi(username, password)
    token.value = data.token
    name.value = data.name
    roles.value = data.roles || []
    setToken(data.token)
  }

  function logout() {
    removeToken()
    token.value = ''
    name.value = ''
    roles.value = []
    window.location.href = '/login'
  }

  // 权限判断：admin 拥有全部；否则需命中具体权限点
  function hasPermission(perm: string): boolean {
    if (roles.value.includes('admin')) return true
    return roles.value.includes(perm)
  }

  return { token, name, roles, login, logout, hasPermission }
})
