import request from '@/utils/request'

export interface TrendPoint { date: string; revenue: number; orderCount: number; avgAmount: number }
export interface TypeCount { type: number | null; count: number; amount: number }
export interface DishRank { dishName: string; qty: number; amount: number }
export interface ReportSummary { totalRevenue: number; totalOrders: number; avgOrderValue: number; refundAmount: number }

/** 数据报表视图对象：营收/订单/客单价趋势 + 订单类型占比 + 热销菜品 + 汇总。金额单位为「分」。 */
export interface ReportVO {
  revenueTrend: TrendPoint[] // 每日趋势
  typeDist: TypeCount[] // 订单类型占比
  topDishes: DishRank[] // 热销菜品 TOP10
  summary: ReportSummary // 汇总
}

/** 数据报表（单接口聚合，对应后端 GET /api/admin/report?days=） */
export function getReport(days = 30): Promise<ReportVO> {
  return request({ url: '/admin/report', method: 'GET', params: { days } }) as Promise<ReportVO>
}
