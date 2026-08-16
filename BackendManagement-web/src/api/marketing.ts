import request from '@/utils/request'

/** 优惠券视图对象：按会员发放，附带会员名与到期日展示字段。金额单位为「分」。 */
export interface CouponVO {
  id: number
  shopId: number
  memberId: number
  memberName: string
  name: string
  value: number // 面额（分）
  threshold: number // 门槛（分）
  status: number // 0未用 1已用 2过期
  startTime?: string
  endTime?: string
  usedAt?: string
  createdAt?: string
  validTo?: string // yyyy-MM-dd 展示用
}

/** 发放优惠券入参：指定会员 + 面额/门槛/有效期天数。金额单位为「分」。 */
export interface IssueCouponDTO {
  memberId: number
  name: string
  value: number // 面额（分）
  threshold: number // 门槛（分），默认 0
  validDays: number // 有效期天数，默认 30
}

/** 已发放优惠券列表 */
export function listCoupons(): Promise<CouponVO[]> {
  return request({ url: '/admin/marketing/coupons', method: 'GET' }) as Promise<CouponVO[]>
}
/** 向指定会员发放优惠券 */
export function issueCoupon(dto: IssueCouponDTO): Promise<CouponVO> {
  return request({ url: '/admin/marketing/coupons', method: 'POST', data: dto }) as Promise<CouponVO>
}
/** 作废优惠券 */
export function deleteCoupon(id: number): Promise<void> {
  return request({ url: `/admin/marketing/coupons/${id}`, method: 'DELETE' }) as Promise<void>
}
