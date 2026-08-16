import request from '@/utils/request'

export interface OrderItem { name: string; price: number; qty: number; spec?: string }
export interface Order {
  id: number
  orderNo: string
  type: number // 1 堂食 2 外卖 3 自提
  status: number // 1 待支付 2 已支付待接单 3 制作中 4 待出餐 5 已完成 6 已取消 7 退款中 8 已退款
  tableNo?: string
  customerName: string
  phone: string
  totalAmount: number // 分
  paidAmount: number
  items: OrderItem[]
  createdAt: string
  remark: string
}
export interface OrderQuery {
  page?: number
  size?: number
  type?: number
  status?: number
  keyword?: string
}

/** 订单列表（分页 + 筛选）。注意：真实后端 /admin/orders 返回裸数组（非分页），接入时需适配分页；当前由 mock 提供分页结构 */
export function listOrders(params: OrderQuery): Promise<{ list: Order[]; total: number }> {
  return request({ url: '/admin/orders', method: 'GET', params }) as Promise<{ list: Order[]; total: number }>
}
/** 订单详情 */
export function getOrder(id: number): Promise<Order> {
  return request({ url: `/admin/orders/${id}`, method: 'GET' }) as Promise<Order>
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
/** 退款（mock 专用：真实后端暂无该端点，需后续补齐） */
export function refundOrder(id: number): Promise<{ success: boolean }> {
  return request({ url: `/admin/orders/${id}/refund`, method: 'PUT' }) as Promise<{ success: boolean }>
}
