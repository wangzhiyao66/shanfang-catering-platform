import request from '@/utils/request'

export interface TableItem {
  id: number
  no: string
  area: string
  seats: number
  status: number // 0 空闲 1 占用 2 待清 3 预订
  currentOrderNo?: string
  remark: string
}
export interface TableQuery { area?: string; status?: number | string }
export interface TableForm {
  id?: number
  no: string
  area: string
  seats: number
  status?: number
  remark?: string
}

/** 桌台列表（可按区域/状态筛选） */
export function listTables(params?: TableQuery): Promise<TableItem[]> {
  return request({ url: '/admin/tables', method: 'GET', params }) as Promise<TableItem[]>
}
/** 新增桌台 */
export function addTable(form: TableForm): Promise<TableItem> {
  return request({ url: '/admin/tables', method: 'POST', data: form }) as Promise<TableItem>
}
/** 编辑桌台（含状态变更） */
export function updateTable(form: TableForm): Promise<TableItem> {
  return request({ url: `/admin/tables/${form.id}`, method: 'PUT', data: form }) as Promise<TableItem>
}
