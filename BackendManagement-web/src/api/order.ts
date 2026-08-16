import request from '@/utils/request'

export interface OrderItem { dishName: string; qty: number; unitPrice: number; specsJson?: string; remark?: string }

/** 后台订单详情（聚合主信息 + 菜品明细 + 支付单 + 会员名 + 桌台号），对应后端 OrderAdminDetailVO。金额单位为「分」。 */
export interface OrderDetail {
  id: number
  orderNo: string
  type: number // 1 堂食 2 外卖 3 自提
  status: number
  peopleCount?: number
  totalAmount: number // 分
  discountAmount?: number
  payAmount?: number
  memberName?: string
  tableNo?: string
  paidAt?: string
  createdAt: string
  items?: OrderItem[]
}
export interface Order {
  id: number
  orderNo: string
  type: number // 1 堂食 2 外卖 3 自提
  status: number // 0待支付 1已支付/待接单 2制作中 3已上菜/待取餐 4已完成 5退款中 6已退款 7退单 9已取消
  tableId?: number
  tableNo?: string
  customerName?: string
  phone?: string
  totalAmount: number // 分
  discountAmount?: number
  payAmount?: number
  items?: OrderItem[]
  createdAt: string
  remark?: string
}
export interface OrderQuery {
  type?: number
  status?: number
  keyword?: string
}

/** 订单列表（真实后端返回裸数组；status 可作为服务端筛选参数，type/keyword 由页面端再筛） */
export function listOrders(params?: OrderQuery): Promise<Order[]> {
  return request({ url: '/admin/orders', method: 'GET', params }) as Promise<Order[]>
}
/** 订单详情（真实后端聚合接口，含菜品明细/会员名/桌台号/支付单） */
export function getOrder(id: number): Promise<OrderDetail> {
  return request({ url: `/admin/order/${id}`, method: 'GET' }) as Promise<OrderDetail>
}
/** 接单：POST /admin/order/{id}/accept（已支付/待接单 → 制作中） */
export function acceptOrder(id: number): Promise<{ success: boolean }> {
  return request({ url: `/admin/order/${id}/accept`, method: 'POST' }) as Promise<{ success: boolean }>
}
/** 推进订单状态：POST /admin/order/{id}/status { status } */
export function updateOrderStatus(id: number, status: number): Promise<{ success: boolean }> {
  return request({ url: `/admin/order/${id}/status`, method: 'POST', data: { status } }) as Promise<{ success: boolean }>
}
/** 取消订单：POST /admin/order/{id}/cancel */
export function cancelOrder(id: number): Promise<{ success: boolean }> {
  return request({ url: `/admin/order/${id}/cancel`, method: 'POST' }) as Promise<{ success: boolean }>
}
