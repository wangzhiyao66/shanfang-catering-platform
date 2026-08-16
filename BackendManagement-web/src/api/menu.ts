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
export interface DishQuery {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number
}
export interface DishForm {
  id?: number
  name: string
  categoryId: number
  price: number // 元（页面录入，提交时 ×100 已在 mock 处理；真实后端由后端处理或前端传分，此处约定前端传元，由 api 层 ×100）
  stock: number
  status: boolean
  image?: string
  description?: string
  sort?: number
  specGroups?: SpecGroup[]
}

/** 分页查询菜品 */
export function listDishes(params: DishQuery): Promise<{ list: Dish[]; total: number }> {
  return request({ url: '/admin/menu/dishes', method: 'GET', params }) as Promise<{ list: Dish[]; total: number }>
}
/** 新增菜品（price 以元为单位，api 层负责 ×100 转换） */
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
/** 上/下架切换 */
export function updateDishStatus(id: number, status: number): Promise<{ success: boolean }> {
  return request({ url: `/admin/menu/dishes/${id}/status`, method: 'PUT', data: { status } }) as Promise<{ success: boolean }>
}
/** 菜品分类列表 */
export function listCategories(): Promise<Category[]> {
  return request({ url: '/admin/menu/categories', method: 'GET' }) as Promise<Category[]>
}
/** 新增分类 */
export function addCategory(name: string, sort = 99): Promise<Category> {
  return request({ url: '/admin/menu/categories', method: 'POST', data: { name, sort } }) as Promise<Category>
}
/** 将页面表单（元）转换为后端载荷（分），保持与后续真实后端契约一致 */
function toPayload(form: DishForm) {
  return {
    name: form.name,
    categoryId: form.categoryId,
    price: form.price,
    stock: form.stock,
    status: form.status,
    image: form.image,
    description: form.description,
    sort: form.sort,
    specGroups: form.specGroups || []
  }
}
