import request from '@/utils/request'

export interface Coupon {
  id: number
  name: string
  type: number // 1 满减 2 折扣
  threshold: number // 满（分）
  value: number // 减（分）或折扣（如 85 = 8.5 折）
  validFrom: string
  validTo: string
  total: number
  used: number
  status: number // 1 生效 0 停用
}
export interface CouponForm {
  name: string
  type: number
  threshold: number // 元
  value: number // 元 或 折扣（如 85 表示 8.5 折）
  validFrom: string
  validTo: string
  total: number
}

/** 优惠券列表 */
export function listCoupons(): Promise<Coupon[]> {
  return request({ url: '/admin/coupons', method: 'GET' }) as Promise<Coupon[]>
}
/** 创建优惠券 */
export function createCoupon(form: CouponForm): Promise<Coupon> {
  return request({ url: '/admin/coupons', method: 'POST', data: form }) as Promise<Coupon>
}
