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

      <el-table :data="list" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="70">
          <template #default="{ row }"><el-avatar :size="40" :src="row.image" shape="square" /></template>
        </el-table-column>
        <el-table-column prop="name" label="菜品" min-width="120" />
        <el-table-column prop="categoryName" label="分类" width="90" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ yuan(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="规格" width="80">
          <template #default="{ row }">{{ row.specGroups?.length || 0 }} 组</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-switch v-model="row._on" :disabled="!$perms('menu:edit')" @change="(v: boolean) => onStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="!$perms('menu:edit')" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :disabled="!$perms('menu:edit')" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="mt" background layout="total, prev, pager, next" :total="total"
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
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
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
        <el-form-item label="规格组">
          <div class="specs">
            <div v-for="(g, gi) in form.specGroups" :key="gi" class="spec-group">
              <el-input v-model="g.name" placeholder="规格名，如：份量" style="width: 160px" />
              <div v-for="(o, oi) in g.options" :key="oi" class="spec-opt">
                <el-input v-model="o.label" placeholder="选项，如：大份" style="width: 120px" />
                <el-input-number v-model="o.priceDelta" :min="0" :step="100" controls-position="right" /> 分
                <el-button text type="danger" :icon="Close" @click="removeOption(gi, oi)" />
              </div>
              <el-button text type="primary" :icon="Plus" @click="addOption(gi)">添加选项</el-button>
              <el-button text type="danger" :icon="Delete" @click="removeGroup(gi)">删除该规格</el-button>
            </div>
            <el-button :icon="Plus" @click="addGroup">+ 添加规格组</el-button>
          </div>
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
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Files, Close, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listDishes, addDish, updateDish, deleteDish, updateDishStatus, listCategories, addCategory,
  type Dish, type DishForm, type Category, type SpecGroup
} from '@/api/menu'
import { yuan } from '@/utils/format'

const keyword = ref('')
const filterCat = ref<number | undefined>()
const list = ref<Dish[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const categories = ref<Category[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive<DishForm>({ name: '', categoryId: 0, price: 0, stock: 0, status: true, image: '', description: '', sort: 99, specGroups: [] })
const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const catVisible = ref(false)
const newCat = ref('')

async function loadCategories() {
  categories.value = await listCategories()
}
async function load() {
  loading.value = true
  try {
    const data = await listDishes({ page: page.value, size: size.value, keyword: keyword.value || undefined, categoryId: filterCat.value })
    list.value = data.list.map(d => ({ ...d, _on: d.status === 1 }))
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function onPage(p: number) { page.value = p; load() }

async function onStatus(row: any, val: boolean) {
  await updateDishStatus(row.id, val ? 1 : 0)
  row.status = val ? 1 : 0
  ElMessage.success('已更新状态')
}

function openAdd() {
  dialogTitle.value = '新增菜品'
  Object.assign(form, { id: undefined, name: '', categoryId: categories.value[0]?.id || 0, price: 0, stock: 0, status: true, image: '', description: '', sort: 99, specGroups: [] })
  dialogVisible.value = true
}
function openEdit(row: Dish) {
  dialogTitle.value = '编辑菜品'
  Object.assign(form, {
    id: row.id, name: row.name, categoryId: row.categoryId, price: row.price / 100,
    stock: row.stock, status: row.status === 1, image: row.image, description: row.description,
    sort: row.sort, specGroups: JSON.parse(JSON.stringify(row.specGroups || []))
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

function addGroup() { form.specGroups!.push({ name: '', options: [{ label: '', priceDelta: 0 }] }) }
function addOption(gi: number) { form.specGroups![gi].options.push({ label: '', priceDelta: 0 }) }
function removeOption(gi: number, oi: number) { form.specGroups![gi].options.splice(oi, 1) }
function removeGroup(gi: number) { form.specGroups!.splice(gi, 1) }

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
.specs { width: 100%; }
.spec-group { border: 1px dashed #dcdfe6; padding: 10px; border-radius: 6px; margin-bottom: 10px; }
.spec-opt { display: flex; align-items: center; gap: 8px; margin: 6px 0; }
.cat-row { display: flex; align-items: center; justify-content: space-between; padding: 6px 0; }
.ml { margin-left: 10px; }
</style>
