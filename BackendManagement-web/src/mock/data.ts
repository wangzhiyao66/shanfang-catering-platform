// 演示用种子数据（Mock）。金额一律以「分」为单位，前端展示时 ÷100。
// 真实后端就绪后，将 .env 的 VITE_USE_MOCK 置为 false 即可切换为真实接口，本文件不再参与打包运行。

export interface SpecOption {
  label: string
  priceDelta: number // 相对基础价的加价（分）
}
export interface SpecGroup {
  name: string
  options: SpecOption[]
}
export interface Dish {
  id: number
  name: string
  categoryId: number
  categoryName: string
  price: number // 分
  stock: number
  status: number // 1 上架 0 下架
  image: string
  description: string
  sort: number
  specGroups: SpecGroup[]
}
export interface Category {
  id: number
  name: string
  sort: number
  status: number
}

export const categories: Category[] = [
  { id: 1, name: '热菜', sort: 1, status: 1 },
  { id: 2, name: '凉菜', sort: 2, status: 1 },
  { id: 3, name: '主食', sort: 3, status: 1 },
  { id: 4, name: '汤羹', sort: 4, status: 1 },
  { id: 5, name: '饮品', sort: 5, status: 1 },
  { id: 6, name: '甜点', sort: 6, status: 0 }
]

const img = (seed: number) =>
  `https://picsum.photos/seed/dish${seed}/200/200`

function spec(groups: SpecGroup[]): SpecGroup[] {
  return groups
}

export const dishes: Dish[] = [
  {
    id: 1, name: '宫保鸡丁', categoryId: 1, categoryName: '热菜', price: 3800, stock: 99, status: 1, image: img(1),
    description: '经典川菜，鸡肉鲜嫩花生酥脆', sort: 1, specGroups: spec([
      { name: '份量', options: [{ label: '标准', priceDelta: 0 }, { label: '大份', priceDelta: 800 }] }
    ])
  },
  {
    id: 2, name: '鱼香肉丝', categoryId: 1, categoryName: '热菜', price: 3200, stock: 99, status: 1, image: img(2),
    description: '咸甜酸辣兼备', sort: 2, specGroups: spec([])
  },
  {
    id: 3, name: '红烧肉', categoryId: 1, categoryName: '热菜', price: 4800, stock: 50, status: 1, image: img(3),
    description: '肥而不腻，入口即化', sort: 3, specGroups: spec([
      { name: '份量', options: [{ label: '标准', priceDelta: 0 }, { label: '小份', priceDelta: -1000 }] }
    ])
  },
  {
    id: 4, name: '麻婆豆腐', categoryId: 1, categoryName: '热菜', price: 2600, stock: 99, status: 1, image: img(4),
    description: '麻辣鲜香', sort: 4, specGroups: spec([])
  },
  {
    id: 5, name: '凉拌黄瓜', categoryId: 2, categoryName: '凉菜', price: 1200, stock: 99, status: 1, image: img(5),
    description: '清爽解腻', sort: 1, specGroups: spec([])
  },
  {
    id: 6, name: '口水鸡', categoryId: 2, categoryName: '凉菜', price: 3600, stock: 40, status: 1, image: img(6),
    description: '麻辣鲜香，皮滑肉嫩', sort: 2, specGroups: spec([])
  },
  {
    id: 7, name: '白切鸡', categoryId: 2, categoryName: '凉菜', price: 4200, stock: 30, status: 0, image: img(7),
    description: '原汁原味', sort: 3, specGroups: spec([])
  },
  {
    id: 8, name: '米饭', categoryId: 3, categoryName: '主食', price: 200, stock: 999, status: 1, image: img(8),
    description: '东北珍珠米', sort: 1, specGroups: spec([
      { name: '份量', options: [{ label: '1 碗', priceDelta: 0 }, { label: '2 碗', priceDelta: 200 }] }
    ])
  },
  {
    id: 9, name: '牛肉面', categoryId: 3, categoryName: '主食', price: 2800, stock: 60, status: 1, image: img(9),
    description: '汤鲜面劲', sort: 2, specGroups: spec([
      { name: '辣度', options: [{ label: '微辣', priceDelta: 0 }, { label: '中辣', priceDelta: 0 }, { label: '特辣', priceDelta: 0 }] }
    ])
  },
  {
    id: 10, name: '小笼包', categoryId: 3, categoryName: '主食', price: 1800, stock: 80, status: 1, image: img(10),
    description: '皮薄汁多', sort: 3, specGroups: spec([])
  },
  {
    id: 11, name: '西红柿鸡蛋汤', categoryId: 4, categoryName: '汤羹', price: 1600, stock: 99, status: 1, image: img(11),
    description: '家常暖胃', sort: 1, specGroups: spec([])
  },
  {
    id: 12, name: '酸辣汤', categoryId: 4, categoryName: '汤羹', price: 1800, stock: 99, status: 1, image: img(12),
    description: '开胃爽口', sort: 2, specGroups: spec([])
  },
  {
    id: 13, name: '鲜榨橙汁', categoryId: 5, categoryName: '饮品', price: 1500, stock: 99, status: 1, image: img(13),
    description: '100% 鲜榨', sort: 1, specGroups: spec([
      { name: '规格', options: [{ label: '中杯', priceDelta: 0 }, { label: '大杯', priceDelta: 500 }] }
    ])
  },
  {
    id: 14, name: '可乐', categoryId: 5, categoryName: '饮品', price: 600, stock: 999, status: 1, image: img(14),
    description: '冰镇', sort: 2, specGroups: spec([
      { name: '规格', options: [{ label: '罐装', priceDelta: 0 }, { label: '瓶装', priceDelta: 200 }] }
    ])
  },
  {
    id: 15, name: '酸梅汤', categoryId: 5, categoryName: '饮品', price: 1200, stock: 99, status: 1, image: img(15),
    description: '解腻生津', sort: 3, specGroups: spec([])
  },
  {
    id: 16, name: '提拉米苏', categoryId: 6, categoryName: '甜点', price: 2800, stock: 20, status: 0, image: img(16),
    description: '意式经典', sort: 1, specGroups: spec([])
  },
  {
    id: 17, name: '芒果布丁', categoryId: 6, categoryName: '甜点', price: 1800, stock: 25, status: 0, image: img(17),
    description: '香甜爽滑', sort: 2, specGroups: spec([])
  },
  {
    id: 18, name: '辣子鸡', categoryId: 1, categoryName: '热菜', price: 4600, stock: 45, status: 1, image: img(18),
    description: '香辣酥脆', sort: 5, specGroups: spec([])
  },
  {
    id: 19, name: '回锅肉', categoryId: 1, categoryName: '热菜', price: 3600, stock: 70, status: 1, image: img(19),
    description: '川味家常', sort: 6, specGroups: spec([])
  },
  {
    id: 20, name: '皮蛋豆腐', categoryId: 2, categoryName: '凉菜', price: 1400, stock: 99, status: 1, image: img(20),
    description: '清凉爽口', sort: 4, specGroups: spec([])
  }
]

export interface TableItem {
  id: number
  no: string
  area: string
  seats: number
  status: number // 0 空闲 1 占用 2 待清 3 预订
  currentOrderNo?: string
  remark: string
}

export const tables: TableItem[] = [
  { id: 1, no: 'A01', area: '大厅', seats: 2, status: 1, currentOrderNo: 'T20260815001', remark: '' },
  { id: 2, no: 'A02', area: '大厅', seats: 2, status: 0, remark: '' },
  { id: 3, no: 'A03', area: '大厅', seats: 4, status: 1, currentOrderNo: 'T20260815002', remark: '' },
  { id: 4, no: 'A04', area: '大厅', seats: 4, status: 2, remark: '需更换桌布' },
  { id: 5, no: 'A05', area: '大厅', seats: 6, status: 0, remark: '' },
  { id: 6, no: 'A06', area: '大厅', seats: 6, status: 3, remark: '18:00 王女士预订' },
  { id: 7, no: 'B01', area: '包间', seats: 8, status: 1, currentOrderNo: 'T20260815003', remark: '' },
  { id: 8, no: 'B02', area: '包间', seats: 10, status: 0, remark: '' },
  { id: 9, no: 'B03', area: '包间', seats: 12, status: 1, currentOrderNo: 'T20260815004', remark: '' },
  { id: 10, no: 'B04', area: '包间', seats: 16, status: 2, remark: '' },
  { id: 11, no: 'C01', area: '卡座', seats: 2, status: 0, remark: '' },
  { id: 12, no: 'C02', area: '卡座', seats: 4, status: 1, currentOrderNo: 'T20260815005', remark: '' },
  { id: 13, no: 'C03', area: '卡座', seats: 4, status: 0, remark: '' },
  { id: 14, no: 'C04', area: '卡座', seats: 6, status: 3, remark: '19:30 李先生预订' },
  { id: 15, no: 'C05', area: '卡座', seats: 2, status: 0, remark: '' },
  { id: 16, no: 'C06', area: '卡座', seats: 4, status: 1, currentOrderNo: 'T20260815006', remark: '' }
]

export interface OrderItem {
  name: string
  price: number
  qty: number
  spec?: string
}
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

export const orders: Order[] = [
  {
    id: 1, orderNo: 'T20260815001', type: 1, status: 3, tableNo: 'A01', customerName: '堂食客', phone: '',
    totalAmount: 9800, paidAmount: 9800, createdAt: '2026-08-15 11:42', remark: '',
    items: [{ name: '宫保鸡丁', price: 3800, qty: 1, spec: '大份' }, { name: '米饭', price: 200, qty: 2 }, { name: '可乐', price: 600, qty: 1, spec: '瓶装' }]
  },
  {
    id: 2, orderNo: 'T20260815002', type: 1, status: 4, tableNo: 'A03', customerName: '堂食客', phone: '',
    totalAmount: 12600, paidAmount: 12600, createdAt: '2026-08-15 11:50', remark: '不要辣',
    items: [{ name: '鱼香肉丝', price: 3200, qty: 1 }, { name: '红烧肉', price: 4800, qty: 1 }, { name: '米饭', price: 200, qty: 3 }, { name: '鲜榨橙汁', price: 1500, qty: 1, spec: '大杯' }]
  },
  {
    id: 3, orderNo: 'T20260815003', type: 1, status: 2, tableNo: 'B01', customerName: '堂食客', phone: '',
    totalAmount: 16400, paidAmount: 16400, createdAt: '2026-08-15 12:01', remark: '',
    items: [{ name: '辣子鸡', price: 4600, qty: 1 }, { name: '回锅肉', price: 3600, qty: 1 }, { name: '牛肉面', price: 2800, qty: 2 }, { name: '酸梅汤', price: 1200, qty: 2 }]
  },
  {
    id: 4, orderNo: 'T20260815004', type: 1, status: 3, tableNo: 'B03', customerName: '堂食客', phone: '',
    totalAmount: 21800, paidAmount: 21800, createdAt: '2026-08-15 12:05', remark: '',
    items: [{ name: '红烧肉', price: 4800, qty: 2 }, { name: '宫保鸡丁', price: 3800, qty: 1 }, { name: '小笼包', price: 1800, qty: 3 }, { name: '鲜榨橙汁', price: 1500, qty: 2 }]
  },
  {
    id: 5, orderNo: 'M20260815007', type: 2, status: 2, tableNo: '', customerName: '张女士', phone: '138****2233',
    totalAmount: 8600, paidAmount: 8600, createdAt: '2026-08-15 12:10', remark: '外卖送到 A 座 1203',
    items: [{ name: '麻婆豆腐', price: 2600, qty: 1 }, { name: '米饭', price: 200, qty: 2 }, { name: '酸辣汤', price: 1800, qty: 1 }, { name: '可乐', price: 600, qty: 1 }]
  },
  {
    id: 6, orderNo: 'T20260815005', type: 1, status: 4, tableNo: 'C02', customerName: '堂食客', phone: '',
    totalAmount: 6400, paidAmount: 6400, createdAt: '2026-08-15 12:12', remark: '',
    items: [{ name: '凉拌黄瓜', price: 1200, qty: 1 }, { name: '牛肉面', price: 2800, qty: 1 }, { name: '皮蛋豆腐', price: 1400, qty: 1 }, { name: '米饭', price: 200, qty: 2 }]
  },
  {
    id: 7, orderNo: 'S20260815008', type: 3, status: 5, tableNo: '', customerName: '王先生', phone: '139****8899',
    totalAmount: 5200, paidAmount: 5200, createdAt: '2026-08-15 12:15', remark: '自提',
    items: [{ name: '小笼包', price: 1800, qty: 2 }, { name: '豆浆', price: 800, qty: 1 }]
  },
  {
    id: 8, orderNo: 'T20260815006', type: 1, status: 5, tableNo: 'C06', customerName: '堂食客', phone: '',
    totalAmount: 7400, paidAmount: 7400, createdAt: '2026-08-15 12:20', remark: '',
    items: [{ name: '回锅肉', price: 3600, qty: 1 }, { name: '米饭', price: 200, qty: 4 }, { name: '可乐', price: 600, qty: 1, spec: '瓶装' }]
  },
  {
    id: 9, orderNo: 'M20260815009', type: 2, status: 7, tableNo: '', customerName: '李女士', phone: '137****6677',
    totalAmount: 9900, paidAmount: 9900, createdAt: '2026-08-15 12:25', remark: '申请退款：送太慢',
    items: [{ name: '鱼香肉丝', price: 3200, qty: 1 }, { name: '红烧肉', price: 4800, qty: 1 }, { name: '米饭', price: 200, qty: 3 }, { name: '酸梅汤', price: 1200, qty: 1 }]
  },
  {
    id: 10, orderNo: 'T20260815010', type: 1, status: 1, tableNo: 'A02', customerName: '堂食客', phone: '',
    totalAmount: 4200, paidAmount: 0, createdAt: '2026-08-15 12:30', remark: '',
    items: [{ name: '白切鸡', price: 4200, qty: 1 }]
  },
  {
    id: 11, orderNo: 'T20260815011', type: 1, status: 6, tableNo: 'A05', customerName: '堂食客', phone: '',
    totalAmount: 5800, paidAmount: 0, createdAt: '2026-08-15 12:35', remark: '顾客取消',
    items: [{ name: '宫保鸡丁', price: 3800, qty: 1 }, { name: '米饭', price: 200, qty: 2 }, { name: '可乐', price: 600, qty: 1 }]
  },
  {
    id: 12, orderNo: 'M20260815012', type: 2, status: 3, tableNo: '', customerName: '赵先生', phone: '136****1122',
    totalAmount: 7600, paidAmount: 7600, createdAt: '2026-08-15 12:40', remark: '',
    items: [{ name: '麻婆豆腐', price: 2600, qty: 1 }, { name: '牛肉面', price: 2800, qty: 1 }, { name: '小笼包', price: 1800, qty: 1 }, { name: '鲜榨橙汁', price: 1500, qty: 1 }]
  },
  {
    id: 13, orderNo: 'T20260815013', type: 1, status: 2, tableNo: 'C03', customerName: '堂食客', phone: '',
    totalAmount: 11200, paidAmount: 11200, createdAt: '2026-08-15 12:45', remark: '',
    items: [{ name: '辣子鸡', price: 4600, qty: 1 }, { name: '回锅肉', price: 3600, qty: 1 }, { name: '米饭', price: 200, qty: 4 }, { name: '酸梅汤', price: 1200, qty: 1 }]
  },
  {
    id: 14, orderNo: 'S20260815014', type: 3, status: 4, tableNo: '', customerName: '孙女士', phone: '135****3344',
    totalAmount: 6400, paidAmount: 6400, createdAt: '2026-08-15 12:50', remark: '',
    items: [{ name: '红烧肉', price: 4800, qty: 1 }, { name: '米饭', price: 200, qty: 4 }, { name: '可乐', price: 600, qty: 1 }]
  },
  {
    id: 15, orderNo: 'T20260815015', type: 1, status: 5, tableNo: 'B02', customerName: '堂食客', phone: '',
    totalAmount: 13200, paidAmount: 13200, createdAt: '2026-08-15 12:55', remark: '',
    items: [{ name: '宫保鸡丁', price: 3800, qty: 1, spec: '大份' }, { name: '鱼香肉丝', price: 3200, qty: 1 }, { name: '小笼包', price: 1800, qty: 2 }, { name: '鲜榨橙汁', price: 1500, qty: 2, spec: '大杯' }]
  }
]

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

export const members: Member[] = [
  { id: 1, name: '张伟', phone: '138****2233', level: '金卡', points: 3200, balance: 50000, totalSpent: 186000, lastVisit: '2026-08-15' },
  { id: 2, name: '李娜', phone: '137****6677', level: '银卡', points: 1200, balance: 20000, totalSpent: 76000, lastVisit: '2026-08-14' },
  { id: 3, name: '王芳', phone: '139****8899', level: '钻石', points: 8900, balance: 120000, totalSpent: 520000, lastVisit: '2026-08-15' },
  { id: 4, name: '刘强', phone: '136****1122', level: '普通', points: 320, balance: 0, totalSpent: 12800, lastVisit: '2026-08-10' },
  { id: 5, name: '陈静', phone: '135****3344', level: '银卡', points: 2100, balance: 35000, totalSpent: 98000, lastVisit: '2026-08-13' },
  { id: 6, name: '杨洋', phone: '134****5566', level: '金卡', points: 4100, balance: 60000, totalSpent: 230000, lastVisit: '2026-08-15' },
  { id: 7, name: '赵磊', phone: '133****7788', level: '普通', points: 540, balance: 10000, totalSpent: 21000, lastVisit: '2026-08-09' },
  { id: 8, name: '孙悦', phone: '132****9900', level: '钻石', points: 10200, balance: 150000, totalSpent: 610000, lastVisit: '2026-08-15' },
  { id: 9, name: '周敏', phone: '131****2211', level: '银卡', points: 1800, balance: 28000, totalSpent: 88000, lastVisit: '2026-08-12' },
  { id: 10, name: '吴昊', phone: '130****4433', level: '普通', points: 80, balance: 0, totalSpent: 6400, lastVisit: '2026-08-08' },
  { id: 11, name: '郑爽', phone: '159****6655', level: '金卡', points: 3600, balance: 45000, totalSpent: 195000, lastVisit: '2026-08-14' },
  { id: 12, name: '冯涛', phone: '158****8877', level: '银卡', points: 2400, balance: 32000, totalSpent: 105000, lastVisit: '2026-08-15' }
]

export interface Employee {
  id: number
  name: string
  role: string // 超级管理员/店长/服务员/后厨
  phone: string
  status: number // 1 在职 0 离职
}
export const employees: Employee[] = [
  { id: 1, name: '老板(超级管理员)', role: '超级管理员', phone: '138****0000', status: 1 },
  { id: 2, name: '刘店长', role: '店长', phone: '138****1111', status: 1 },
  { id: 3, name: '小美', role: '服务员', phone: '138****2222', status: 1 },
  { id: 4, name: '小强', role: '服务员', phone: '138****3333', status: 1 },
  { id: 5, name: '阿杰', role: '后厨', phone: '138****4444', status: 1 },
  { id: 6, name: '老王', role: '后厨', phone: '138****5555', status: 0 },
  { id: 7, name: '小林', role: '服务员', phone: '138****6666', status: 1 },
  { id: 8, name: '阿强', role: '后厨', phone: '138****7777', status: 1 }
]

export interface Role {
  key: string
  name: string
  permissions: string[]
}
export const roles: Role[] = [
  { key: 'super', name: '超级管理员', permissions: ['*'] },
  { key: 'manager', name: '店长', permissions: ['menu:view', 'menu:edit', 'table:view', 'table:edit', 'order:view', 'order:edit', 'member:view', 'report:view', 'employee:view', 'marketing:view', 'setting:view'] },
  { key: 'waiter', name: '服务员', permissions: ['menu:view', 'table:view', 'table:edit', 'order:view', 'order:edit', 'member:view'] },
  { key: 'kitchen', name: '后厨', permissions: ['order:view', 'menu:view'] }
]

export interface Coupon {
  id: number
  name: string
  type: number // 1 满减 2 折扣
  threshold: number // 满（分）
  value: number // 减（分）或折扣（如 85 表示 8.5 折）
  validFrom: string
  validTo: string
  total: number
  used: number
  status: number // 1 生效 0 停用
}
export const coupons: Coupon[] = [
  { id: 1, name: '满50减10', type: 1, threshold: 5000, value: 1000, validFrom: '2026-08-01', validTo: '2026-08-31', total: 1000, used: 328, status: 1 },
  { id: 2, name: '满100减25', type: 1, threshold: 10000, value: 2500, validFrom: '2026-08-01', validTo: '2026-08-31', total: 500, used: 142, status: 1 },
  { id: 3, name: '全场8.5折', type: 2, threshold: 0, value: 85, validFrom: '2026-08-10', validTo: '2026-08-20', total: 200, used: 76, status: 1 },
  { id: 4, name: '新客立减15', type: 1, threshold: 0, value: 1500, validFrom: '2026-08-01', validTo: '2026-09-30', total: 2000, used: 980, status: 1 },
  { id: 5, name: '满200减60', type: 1, threshold: 20000, value: 6000, validFrom: '2026-07-01', validTo: '2026-07-31', total: 200, used: 200, status: 0 },
  { id: 6, name: '下午茶7折', type: 2, threshold: 0, value: 70, validFrom: '2026-08-15', validTo: '2026-08-31', total: 300, used: 12, status: 1 }
]

export const shopInfo = {
  id: 1,
  name: '膳房·中餐（总店）',
  phone: '021-66668888',
  address: '上海市黄浦区南京东路 123 号 1F',
  businessHours: '10:00 - 22:00',
  notice: '欢迎光临，本店支持堂食 / 外卖 / 自提。',
  printer: '前台飞鹅云打印机',
  autoAccept: true
}
