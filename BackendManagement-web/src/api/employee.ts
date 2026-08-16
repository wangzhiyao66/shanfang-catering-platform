import request from '@/utils/request'

export interface Employee {
  id: number
  name: string
  role: string
  phone: string
  status: number // 1 在职 0 离职
}
export interface Role {
  key: string
  name: string
  permissions: string[]
}

/** 员工列表 */
export function listEmployees(): Promise<Employee[]> {
  return request({ url: '/admin/employees', method: 'GET' }) as Promise<Employee[]>
}
/** 角色与权限定义 */
export function listRoles(): Promise<Role[]> {
  return request({ url: '/admin/roles', method: 'GET' }) as Promise<Role[]>
}
