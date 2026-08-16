import request from '@/utils/request'

/** 门店基础信息（对应后端 shop 主表，仅 name/status 可改） */
export interface ShopInfo {
  id: number
  name: string
  status?: number
  createTime?: string
}

/** 获取门店基础设置 */
export function getShopInfo(): Promise<ShopInfo> {
  return request({ url: '/admin/shop', method: 'GET' }) as Promise<ShopInfo>
}
/** 保存门店基础设置（仅更新名称/状态） */
export function updateShopInfo(info: { id: number; name: string; status?: number }): Promise<void> {
  return request({ url: '/admin/shop', method: 'PUT', data: info }) as Promise<void>
}

/** 门店扩展设置（key-value，对应后端 shop_setting 表：address/phone/businessHours/notice/logo 等） */
export function getShopSettings(): Promise<Record<string, string>> {
  return request({ url: '/admin/setting', method: 'GET' }) as Promise<Record<string, string>>
}
/** 批量保存门店扩展设置 */
export function updateShopSettings(map: Record<string, string>): Promise<void> {
  return request({ url: '/admin/setting', method: 'PUT', data: map }) as Promise<void>
}
