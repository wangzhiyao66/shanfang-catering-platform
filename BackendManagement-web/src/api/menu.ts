import request from '@/utils/request'

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
  categoryName?: string // 页面侧根据分类列表映射，后端不返回
  price: number // 分
  status: number // 1 上架 0 下架
  isSoldOut: number // 1 售罄 0 否
  image: string
  description: string
  sort: number
  specGroups?: SpecGroup[]
}
export interface Category {
  id: number
  name: string
  sort: number
  status: number
}
export interface DishQuery {
  keyword?: string
  categoryId?: number
}
export interface DishForm {
  id?: number
  name: string
  categoryId: number
  price: number // 元（页面录入，提交时 ×100 转分）
  status: boolean // true 上架
  image?: string
  description?: string
  sort?: number
  specGroups?: SpecGroup[]
}

/** 菜品列表（真实后端返回裸数组，非分页） */
export function listDishes(params?: DishQuery): Promise<Dish[]> {
  return request({ url: '/admin/menu/dishes', method: 'GET', params }) as Promise<Dish[]>
}
/** 新增菜品（price 以元录入，api 层负责 ×100 转分） */
export function addDish(form: DishForm): Promise<Dish> {
  return request({ url: '/admin/menu/dishes', method: 'POST', data: toPayload(form) }) as Promise<Dish>
}
/** 修改菜品 */
export function updateDish(form: DishForm): Promise<Dish> {
  return request({ url: `/admin/menu/dishes/${form.id}`, method: 'PUT', data: toPayload(form) }) as Promise<Dish>
}
/** 删除菜品 */
export function deleteDish(id: number): Promise<{ success: boolean }> {
  return request({ url: `/admin/menu/dishes/${id}`, method: 'DELETE' }) as Promise<{ success: boolean }>
}
/** 菜品分类列表（admin 暂未提供列表接口，复用客户端分类接口，返回真实数据） */
export function listCategories(): Promise<Category[]> {
  return request({ url: '/client/menu/categories', method: 'GET' }) as Promise<Category[]>
}
/** 新增分类 */
export function addCategory(name: string, sort = 99): Promise<Category> {
  return request({ url: '/admin/menu/categories', method: 'POST', data: { name, sort } }) as Promise<Category>
}
/** 将页面表单（元/布尔）转换为后端载荷（分/1|0） */
function toPayload(form: DishForm) {
  return {
    name: form.name,
    categoryId: form.categoryId,
    price: Math.round(form.price * 100),
    status: form.status ? 1 : 0,
    image: form.image || '',
    description: form.description || '',
    sort: form.sort ?? 99
  }
}
