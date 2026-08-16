import request from '@/utils/request'

export interface TableItem {
  id: number
  shopId: number
  tableNo: string // 真实后端字段为 tableNo（非 no）
  area: string
  seats: number
  status: number // 0 空闲 1 占用 2 待清 3 预订
  qrToken?: string
}
export interface TableQuery { area?: string; status?: number }

/** 桌台列表（真实后端返回裸数组） */
export function listTables(): Promise<TableItem[]> {
  return request({ url: '/admin/tables', method: 'GET' }) as Promise<TableItem[]>
}
/** 改桌台状态：POST /admin/table/{id}/status { status }（后端仅支持此写操作，无完整增删改） */
export function updateTableStatus(id: number, status: number): Promise<{ success: boolean }> {
  return request({ url: `/admin/table/${id}/status`, method: 'POST', data: { status } }) as Promise<{ success: boolean }>
}
