// Mock 适配层：以 axios adapter 形式注入，复用 request.ts 的拦截器与 { code, data, msg } 契约。
// 仅当 VITE_USE_MOCK === 'true' 时启用；后端就绪后关闭该开关即可无缝切换真实接口。
import type { AxiosAdapter, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import {
  categories, dishes, tables, orders, members, employees, roles, coupons, shopInfo,
  type Dish, type TableItem, type Order, type Member, type Coupon, type SpecGroup
} from './data'

let dishSeq = 100
let tableSeq = 100
let couponSeq = 100
let orderSeq = 100

function parseBody(data: unknown): any {
  if (typeof data === 'string') {
    try { return JSON.parse(data) } catch { return {} }
  }
  return data ?? {}
}

// ---- 派生统计（演示用确定性生成）----
const DAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
function salesTrend7() {
  const base = [62000, 58000, 71000, 69000, 83000, 112000, 105000]
  return DAYS.map((d, i) => ({ date: d, amount: base[i], count: [86, 79, 98, 95, 121, 168, 152][i] }))
}
function revenueTrend30() {
  const arr: { date: string; amount: number }[] = []
  for (let i = 29; i >= 0; i--) {
    const day = new Date(2026, 7, 15)
    day.setDate(day.getDate() - i)
    const md = `${day.getMonth() + 1}/${day.getDate()}`
    const wave = 60000 + Math.round(Math.sin(i / 3) * 22000) + (i % 7 >= 5 ? 30000 : 0)
    arr.push({ date: md, amount: wave })
  }
  return arr
}
function orderTypeDist() {
  const t1 = orders.filter(o => o.type === 1).length
  const t2 = orders.filter(o => o.type === 2).length
  const t3 = orders.filter(o => o.type === 3).length
  return [
    { type: 1, label: '堂食', value: 8 },
    { type: 2, label: '外卖', value: 4 },
    { type: 3, label: '自提', value: 3 }
  ]
}
function topDishes() {
  const map = new Map<string, { name: string; count: number; amount: number }>()
  orders.forEach(o => o.items.forEach(it => {
    const cur = map.get(it.name) || { name: it.name, count: 0, amount: 0 }
    cur.count += it.qty
    cur.amount += it.price * it.qty
    map.set(it.name, cur)
  }))
  return [...map.values()].sort((a, b) => b.count - a.count).slice(0, 8)
}
function dishSalesRank() {
  return topDishes().map(d => ({ name: d.name, count: d.count }))
}
function avgTicket30() {
  const arr: { date: string; value: number }[] = []
  for (let i = 29; i >= 0; i--) {
    const day = new Date(2026, 7, 15)
    day.setDate(day.getDate() - i)
    const md = `${day.getMonth() + 1}/${day.getDate()}`
    arr.push({ date: md, value: 7200 + Math.round(Math.cos(i / 4) * 1800) })
  }
  return arr
}

interface Ctx {
  query: Record<string, any>
  body: any
  params: string[]
}
type Handler = (ctx: Ctx) => any

interface Route { method: string; regex: RegExp; handler: Handler }

const routes: Route[] = [
  // 认证
  { method: 'POST', regex: /^\/api\/admin\/auth\/login$/, handler: () => ({ token: 'mock-token-' + Date.now(), name: '演示管理员', roles: ['admin'] }) },

  // 工作台
  { method: 'GET', regex: /^\/api\/admin\/dashboard\/stats$/, handler: () => ({
    todayOrders: 86, todayRevenue: 685000, pendingOrders: 7, todayMembers: 23,
    weekRevenue: 5680000, monthRevenue: 21800000, avgTicket: 7965
  }) },
  { method: 'GET', regex: /^\/api\/admin\/dashboard\/sales-trend$/, handler: () => salesTrend7() },
  { method: 'GET', regex: /^\/api\/admin\/dashboard\/order-type$/, handler: () => orderTypeDist() },
  { method: 'GET', regex: /^\/api\/admin\/dashboard\/top-dishes$/, handler: () => topDishes() },

  // 菜品
  { method: 'GET', regex: /^\/api\/admin\/menu\/dishes$/, handler: ({ query }) => {
    let list = [...dishes]
    if (query.keyword) list = list.filter(d => d.name.includes(query.keyword))
    if (query.categoryId) list = list.filter(d => d.categoryId === Number(query.categoryId))
    const total = list.length
    const page = Number(query.page) || 1
    const size = Number(query.size) || 10
    return { list: list.slice((page - 1) * size, page * size), total }
  } },
  { method: 'POST', regex: /^\/api\/admin\/menu\/dishes$/, handler: ({ body }) => {
    const cat = categories.find(c => c.id === Number(body.categoryId))
    const dish: Dish = { id: ++dishSeq, name: body.name, categoryId: Number(body.categoryId), categoryName: cat?.name || '', price: Number(body.price) * 100, stock: Number(body.stock) || 0, status: body.status === false ? 0 : 1, image: body.image || 'https://picsum.photos/seed/new/200/200', description: body.description || '', sort: Number(body.sort) || 99, specGroups: (body.specGroups || []) as SpecGroup[] }
    dishes.unshift(dish)
    return dish
  } },
  { method: 'PUT', regex: /^\/api\/admin\/menu\/dishes\/(\d+)$/, handler: ({ params, body }) => {
    const d = dishes.find(x => x.id === Number(params[0]))
    if (!d) return null
    Object.assign(d, {
      name: body.name ?? d.name,
      categoryId: body.categoryId ? Number(body.categoryId) : d.categoryId,
      categoryName: body.categoryId ? (categories.find(c => c.id === Number(body.categoryId))?.name || d.categoryName) : d.categoryName,
      price: body.price != null ? Number(body.price) * 100 : d.price,
      stock: body.stock != null ? Number(body.stock) : d.stock,
      status: body.status != null ? (body.status === false ? 0 : 1) : d.status,
      description: body.description ?? d.description,
      sort: body.sort != null ? Number(body.sort) : d.sort,
      specGroups: body.specGroups ?? d.specGroups
    })
    return d
  } },
  { method: 'PUT', regex: /^\/api\/admin\/menu\/dishes\/(\d+)\/status$/, handler: ({ params, body }) => {
    const d = dishes.find(x => x.id === Number(params[0]))
    if (d) d.status = Number(body.status)
    return { success: true }
  } },
  { method: 'DELETE', regex: /^\/api\/admin\/menu\/dishes\/(\d+)$/, handler: ({ params }) => {
    const i = dishes.findIndex(x => x.id === Number(params[0]))
    if (i >= 0) dishes.splice(i, 1)
    return { success: true }
  } },
  { method: 'GET', regex: /^\/api\/admin\/menu\/categories$/, handler: () => [...categories] },
  { method: 'POST', regex: /^\/api\/admin\/menu\/categories$/, handler: ({ body }) => {
    const c = { id: Math.max(...categories.map(x => x.id)) + 1, name: body.name, sort: Number(body.sort) || 99, status: 1 }
    categories.push(c)
    return c
  } },

  // 桌台
  { method: 'GET', regex: /^\/api\/admin\/tables$/, handler: ({ query }) => {
    let list: TableItem[] = [...tables]
    if (query.area) list = list.filter(t => t.area === query.area)
    if (query.status != null && query.status !== '') list = list.filter(t => t.status === Number(query.status))
    return list
  } },
  { method: 'POST', regex: /^\/api\/admin\/tables$/, handler: ({ body }) => {
    const t: TableItem = { id: ++tableSeq, no: body.no, area: body.area, seats: Number(body.seats) || 2, status: 0, remark: body.remark || '' }
    tables.push(t)
    return t
  } },
  { method: 'PUT', regex: /^\/api\/admin\/tables\/(\d+)$/, handler: ({ params, body }) => {
    const t = tables.find(x => x.id === Number(params[0]))
    if (!t) return null
    Object.assign(t, { no: body.no ?? t.no, area: body.area ?? t.area, seats: body.seats != null ? Number(body.seats) : t.seats, status: body.status != null ? Number(body.status) : t.status, remark: body.remark ?? t.remark })
    return t
  } },

  // 订单
  { method: 'GET', regex: /^\/api\/admin\/orders$/, handler: ({ query }) => {
    let list: Order[] = [...orders]
    if (query.type) list = list.filter(o => o.type === Number(query.type))
    if (query.status) list = list.filter(o => o.status === Number(query.status))
    if (query.keyword) list = list.filter(o => o.orderNo.includes(query.keyword) || (o.customerName || '').includes(query.keyword) || (o.phone || '').includes(query.keyword))
    list.sort((a, b) => b.createdAt.localeCompare(a.createdAt))
    const total = list.length
    const page = Number(query.page) || 1
    const size = Number(query.size) || 10
    return { list: list.slice((page - 1) * size, page * size), total }
  } },
  { method: 'GET', regex: /^\/api\/admin\/orders\/(\d+)$/, handler: ({ params }) => orders.find(o => o.id === Number(params[0])) || null },
  // 以下三条路径对齐真实后端 OrderAdminController；mock 内仍用本文件自有的状态枚举(1-8)驱动演示
  { method: 'POST', regex: /^\/api\/admin\/order\/(\d+)\/accept$/, handler: ({ params }) => {
    const o = orders.find(x => x.id === Number(params[0])); if (o) o.status = 3; return { success: true }
  } },
  { method: 'POST', regex: /^\/api\/admin\/order\/(\d+)\/status$/, handler: ({ params, body }) => {
    const o = orders.find(x => x.id === Number(params[0])); if (o) o.status = Number(body.status); return { success: true }
  } },
  { method: 'POST', regex: /^\/api\/admin\/order\/(\d+)\/cancel$/, handler: ({ params }) => {
    const o = orders.find(x => x.id === Number(params[0])); if (o) o.status = 6; return { success: true }
  } },
  { method: 'PUT', regex: /^\/api\/admin\/orders\/(\d+)\/refund$/, handler: ({ params }) => {
    const o = orders.find(x => x.id === Number(params[0])); if (o) o.status = 8
    return { success: true }
  } },

  // 会员
  { method: 'GET', regex: /^\/api\/admin\/members$/, handler: ({ query }) => {
    let list: Member[] = [...members]
    if (query.keyword) list = list.filter(m => m.name.includes(query.keyword) || m.phone.includes(query.keyword))
    if (query.level) list = list.filter(m => m.level === query.level)
    const total = list.length
    const page = Number(query.page) || 1
    const size = Number(query.size) || 10
    return { list: list.slice((page - 1) * size, page * size), total }
  } },
  { method: 'GET', regex: /^\/api\/admin\/members\/(\d+)$/, handler: ({ params }) => members.find(m => m.id === Number(params[0])) || null },

  // 报表
  { method: 'GET', regex: /^\/api\/admin\/reports\/revenue$/, handler: () => revenueTrend30() },
  { method: 'GET', regex: /^\/api\/admin\/reports\/dish-rank$/, handler: () => dishSalesRank() },
  { method: 'GET', regex: /^\/api\/admin\/reports\/order-type$/, handler: () => orderTypeDist() },
  { method: 'GET', regex: /^\/api\/admin\/reports\/avg-ticket$/, handler: () => avgTicket30() },

  // 员工 / 角色
  { method: 'GET', regex: /^\/api\/admin\/employees$/, handler: () => [...employees] },
  { method: 'GET', regex: /^\/api\/admin\/roles$/, handler: () => roles },

  // 营销
  { method: 'GET', regex: /^\/api\/admin\/coupons$/, handler: () => [...coupons] },
  { method: 'POST', regex: /^\/api\/admin\/coupons$/, handler: ({ body }) => {
    const c: Coupon = { id: ++couponSeq, name: body.name, type: Number(body.type), threshold: Number(body.threshold) * 100, value: Number(body.value) * (Number(body.type) === 1 ? 100 : 1), validFrom: body.validFrom, validTo: body.validTo, total: Number(body.total) || 0, used: 0, status: 1 }
    coupons.unshift(c)
    return c
  } },

  // 设置
  { method: 'GET', regex: /^\/api\/admin\/shop$/, handler: () => ({ ...shopInfo }) },
  { method: 'PUT', regex: /^\/api\/admin\/shop$/, handler: ({ body }) => Object.assign(shopInfo, body) }
]

export const mockAdapter: AxiosAdapter = (config: InternalAxiosRequestConfig) => {
  return new Promise((resolve) => {
    const baseURL = (config.baseURL as string) || ''
    const url = (config.url as string) || ''
    const fullPath = (baseURL + url).replace(/\?.*$/, '')
    const method = (config.method || 'get').toUpperCase()
    const query = (config.params as Record<string, any>) || {}
    const body = parseBody(config.data)
    const route = routes.find(r => r.method === method && r.regex.test(fullPath))
    let code = 0
    let msg = 'ok'
    let payload: any = null
    if (!route) {
      code = 404
      msg = `mock: 未匹配接口 ${method} ${fullPath}`
    } else {
      const m = fullPath.match(route.regex)
      payload = route.handler({ query, body, params: m ? m.slice(1) : [] })
    }
    const response: AxiosResponse = {
      data: { code, data: payload, msg },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
      request: {}
    }
    setTimeout(() => resolve(response), 180)
  })
}
