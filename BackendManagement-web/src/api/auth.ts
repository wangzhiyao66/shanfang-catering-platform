import request from '@/utils/request'

export interface LoginResult {
  token: string
  name: string
  roles: string[]
}

// POST /api/admin/auth/login
export function login(username: string, password: string): Promise<LoginResult> {
  return request({
    url: '/admin/auth/login',
    method: 'POST',
    data: { username, password }
  }) as Promise<LoginResult>
}
