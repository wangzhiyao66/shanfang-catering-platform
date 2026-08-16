<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索菜品" style="width: 200px" :prefix-icon="Search" @keyup.enter="load" />
        <el-select v-model="filterCat" placeholder="全部分类" clearable style="width: 140px" @change="load">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button :icon="Plus" v-permission="'menu:edit'" @click="openAdd">新增菜品</el-button>
        <el-button :icon="Files" text type="primary" v-permission="'menu:edit'" @click="openCategory">分类管理</el-button>
      </div>

      <el-table :data="pagedList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="70">
          <template #default="{ row }"><el-avatar :size="40" :src="row.image" shape="square" /></template>
        </el-table-column>
        <el-table-column prop="name" label="菜品" min-width="120" />
        <el-table-column label="分类" width="90">
          <template #default="{ row }">{{ catName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ yuan(row.price) }}</template>
        </el-table-column>
        <el-table-column label="规格" width="80">
          <template #default="{ row }">{{ row.specGroups?.length || 0 }} 组</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
            <el-tag v-if="row.isSoldOut === 1" size="small" type="danger" class="ml">售罄</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="!$perms('menu:edit')" @click="onStatus(row, !(row.status === 1))">上/下架</el-button>
            <el-button link type="primary" :disabled="!$perms('menu:edit')" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :disabled="!$perms('menu:edit')" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="mt" background layout="total, prev, pager, next" :total="list.length"
        :current-page="page" :page-size="size" @current-change="onPage" />
    </el-card>

    <!-- 菜品新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="如：宫保鸡丁" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.image" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="上架">
          <el-switch v-model="form.status" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分类管理 -->
    <el-dialog v-model="catVisible" title="分类管理" width="420px">
      <div v-for="c in categories" :key="c.id" class="cat-row">
        <span>{{ c.name }}</span>
        <el-tag size="small" :type="c.status ? 'success' : 'info'">{{ c.status ? '启用' : '停用' }}</el-tag>
      </div>
      <el-divider />
      <el-input v-model="newCat" placeholder="新增分类名称" style="width: 220px" />
      <el-button type="primary" class="ml" @click="addCategoryHandler">添加</el-button>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Plus, Files } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listDishes, addDish, updateDish, deleteDish, listCategories, addCategory,
  type Dish, type DishForm, type Category
} from '@/api/menu'
import { yuan } from '@/utils/format'

const keyword = ref('')
const filterCat = ref<number | undefined>()
const list = ref<Dish[]>([])
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const categories = ref<Category[]>([])

const catMap = computed(() => Object.fromEntries(categories.value.map(c => [c.id, c.name])))
function catName(id?: number) { return (id != null && catMap.value[id]) || '未分类' }

// 客户端筛选 + 分页（真实后端返回裸数组）
const filtered = computed(() => {
  return list.value.filter(d => {
    if (filterCat.value != null && d.categoryId !== filterCat.value) return false
    if (keyword.value && !d.name.includes(keyword.value)) return false
    return true
  })
})
const pagedList = computed(() => filtered.value.slice((page.value - 1) * size.value, page.value * size.value))

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive<DishForm>({ name: '', categoryId: 0, price: 0, status: true, image: '', description: '', sort: 99 })
const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const catVisible = ref(false)
const newCat = ref('')

async function loadCategories() { categories.value = await listCategories() }
async function load() {
  loading.value = true
  try { list.value = await listDishes() }
  finally { loading.value = false }
}
function onPage(p: number) { page.value = p }

async function onStatus(row: Dish, val: boolean) {
  await updateDish({ id: row.id, name: row.name, categoryId: row.categoryId, price: row.price / 100, status: val, image: row.image, description: row.description, sort: row.sort })
  row.status = val ? 1 : 0
  ElMessage.success('已更新状态')
}

function openAdd() {
  dialogTitle.value = '新增菜品'
  Object.assign(form, { id: undefined, name: '', categoryId: categories.value[0]?.id || 0, price: 0, status: true, image: '', description: '', sort: 99 })
  dialogVisible.value = true
}
function openEdit(row: Dish) {
  dialogTitle.value = '编辑菜品'
  Object.assign(form, {
    id: row.id, name: row.name, categoryId: row.categoryId, price: row.price / 100,
    status: row.status === 1, image: row.image, description: row.description, sort: row.sort
  })
  dialogVisible.value = true
}
async function save() {
  await formRef.value.validate()
  if (form.id) await updateDish({ ...form })
  else await addDish({ ...form })
  ElMessage.success('已保存')
  dialogVisible.value = false
  load()
}
async function remove(row: Dish) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '提示', { type: 'warning' })
  await deleteDish(row.id)
  ElMessage.success('已删除')
  load()
}

function openCategory() { catVisible.value = true }
async function addCategoryHandler() {
  if (!newCat.value.trim()) return
  await addCategory(newCat.value.trim())
  newCat.value = ''
  await loadCategories()
  ElMessage.success('已添加分类')
}

onMounted(async () => { await loadCategories(); load() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.mt { margin-top: 14px; justify-content: flex-end; }
.cat-row { display: flex; align-items: center; justify-content: space-between; padding: 6px 0; }
.ml { margin-left: 10px; }
</style>
