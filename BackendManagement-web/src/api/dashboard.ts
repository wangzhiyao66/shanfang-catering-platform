import request from '@/utils/request'

export interface DayPoint { date: string; amount: number; orderCount: number }
export interface TypeCount { type: number | null; count: number }
export interface DishRank { dishName: string; qty: number; amount: number }

/** 工作台视图对象：核心指标 + 近 7 日营收趋势 + 订单类型分布 + 热销 TOP。金额单位为「分」。 */
export interface DashboardVO {
  todayRevenue: number // 今日营收（分）
  todayOrderCount: number // 今日订单数（不含已取消）
  pendingOrders: number // 进行中订单（状态 0~3）
  totalMembers: number // 会员总数
  weekRevenue: DayPoint[] // 近 7 日
  orderTypeDist: TypeCount[] // 订单类型分布
  topDishes: DishRank[] // 热销 TOP5
}

/** 工作台核心指标（单接口聚合，对应后端 GET /api/admin/dashboard） */
export function getDashboard(): Promise<DashboardVO> {
  return request({ url: '/admin/dashboard', method: 'GET' }) as Promise<DashboardVO>
}
