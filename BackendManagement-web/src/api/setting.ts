import request from '@/utils/request'

export interface ShopInfo {
  id: number
  name: string
  phone: string
  address: string
  businessHours: string
  notice: string
  printer: string
  autoAccept: boolean
}

/** 获取门店基础设置 */
export function getShopInfo(): Promise<ShopInfo> {
  return request({ url: '/admin/shop', method: 'GET' }) as Promise<ShopInfo>
}
/** 保存门店基础设置 */
export function updateShopInfo(info: ShopInfo): Promise<ShopInfo> {
  return request({ url: '/admin/shop', method: 'PUT', data: info }) as Promise<ShopInfo>
}
