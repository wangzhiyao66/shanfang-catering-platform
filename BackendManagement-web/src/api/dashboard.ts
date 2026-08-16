import request from '@/utils/request'

export interface DashStats {
  todayOrders: number
  todayRevenue: number // 分
  pendingOrders: number
  todayMembers: number
  weekRevenue: number // 分
  monthRevenue: number // 分
  avgTicket: number // 分
}
export interface TrendPoint { date: string; amount: number; count: number }
export interface TypeDist { type: number; label: string; value: number }
export interface TopDish { name: string; count: number; amount: number }

/** 工作台核心指标 */
export function getStats(): Promise<DashStats> {
  return request({ url: '/admin/dashboard/stats', method: 'GET' }) as Promise<DashStats>
}
/** 近 7 日销售趋势 */
export function getSalesTrend(): Promise<TrendPoint[]> {
  return request({ url: '/admin/dashboard/sales-trend', method: 'GET' }) as Promise<TrendPoint[]>
}
/** 订单类型分布 */
export function getOrderTypeDist(): Promise<TypeDist[]> {
  return request({ url: '/admin/dashboard/order-type', method: 'GET' }) as Promise<TypeDist[]>
}
/** 热销菜品 TOP */
export function getTopDishes(): Promise<TopDish[]> {
  return request({ url: '/admin/dashboard/top-dishes', method: 'GET' }) as Promise<TopDish[]>
}
