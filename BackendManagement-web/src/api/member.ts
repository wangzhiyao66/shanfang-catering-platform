import request from '@/utils/request'

/** 会员实体（后台列表，对应后端 GET /api/admin/members） */
export interface Member {
  id: number
  shopId: number
  openid?: string
  phone?: string
  nickname?: string
  avatar?: string
  levelId?: number
  points?: number
  balance?: number // 储值余额（分）
  isBlocked?: number
  lastActiveAt?: string
}

/** 会员列表 */
export function listMembers(): Promise<Member[]> {
  return request({ url: '/admin/members', method: 'GET' }) as Promise<Member[]>
}

/** 会员等级：discount 折扣（0.90 表示 9 折），threshold 升级消费门槛（分）。 */
export interface MemberLevel {
  id: number
  shopId: number
  name: string
  discount?: number
  threshold?: number
}

/** 会员等级列表 */
export function listMemberLevels(): Promise<MemberLevel[]> {
  return request({ url: '/admin/member/levels', method: 'GET' }) as Promise<MemberLevel[]>
}
