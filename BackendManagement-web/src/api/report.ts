import request from '@/utils/request'

export interface TrendPoint { date: string; amount: number }
export interface RankItem { name: string; count: number }
export interface TypeDist { type: number; label: string; value: number }

/** 近 30 日营业额趋势（分） */
export function getRevenueTrend(): Promise<TrendPoint[]> {
  return request({ url: '/admin/reports/revenue', method: 'GET' }) as Promise<TrendPoint[]>
}
/** 菜品销量排行 */
export function getDishRank(): Promise<RankItem[]> {
  return request({ url: '/admin/reports/dish-rank', method: 'GET' }) as Promise<RankItem[]>
}
/** 订单类型占比 */
export function getOrderTypeShare(): Promise<TypeDist[]> {
  return request({ url: '/admin/reports/order-type', method: 'GET' }) as Promise<TypeDist[]>
}
/** 近 30 日客单价趋势（分） */
export function getAvgTicket(): Promise<TrendPoint[]> {
  return request({ url: '/admin/reports/avg-ticket', method: 'GET' }) as Promise<TrendPoint[]>
}
