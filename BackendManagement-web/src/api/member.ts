import request from '@/utils/request'

export interface Member {
  id: number
  name: string
  phone: string
  level: string // 普通/银卡/金卡/钻石
  points: number
  balance: number // 分
  totalSpent: number // 分
  lastVisit: string
}
export interface MemberQuery { page?: number; size?: number; keyword?: string; level?: string }

/** 会员列表（分页 + 筛选） */
export function listMembers(params: MemberQuery): Promise<{ list: Member[]; total: number }> {
  return request({ url: '/admin/members', method: 'GET', params }) as Promise<{ list: Member[]; total: number }>
}
/** 会员详情 */
export function getMember(id: number): Promise<Member> {
  return request({ url: `/admin/members/${id}`, method: 'GET' }) as Promise<Member>
}
