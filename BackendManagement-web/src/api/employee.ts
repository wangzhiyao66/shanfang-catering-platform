import request from '@/utils/request'

/** 员工视图对象：附带角色名（roleName）。 */
export interface EmployeeVO {
  id: number
  name: string
  phone: string
  account: string
  roleId: number
  roleName: string
  status: number // 1 在职 0 离职
  createTime?: string
}

/** 角色：permissions 为逗号分隔的权限码（如 dish:manage,order:manage），"*" 表示全部。 */
export interface Role {
  id: number
  shopId: number
  name: string
  permissions: string
  status: number
  createTime?: string
}

/** 员工列表 */
export function listEmployees(): Promise<EmployeeVO[]> {
  return request({ url: '/admin/employees', method: 'GET' }) as Promise<EmployeeVO[]>
}
/** 角色与权限定义 */
export function listRoles(): Promise<Role[]> {
  return request({ url: '/admin/roles', method: 'GET' }) as Promise<Role[]>
}
