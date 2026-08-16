<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="filterArea" placeholder="全部区域" clearable style="width: 130px" @change="load">
          <el-option label="大厅" value="大厅" />
          <el-option label="包间" value="包间" />
          <el-option label="卡座" value="卡座" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 130px" @change="load">
          <el-option v-for="s in statusMeta" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button type="primary" :icon="Plus" v-permission="'table:edit'" @click="openAdd">新增桌台</el-button>
        <span class="legend">
          <el-tag v-for="s in statusMeta" :key="s.value" :type="s.type" size="small" effect="light">{{ s.label }}</el-tag>
        </span>
      </div>

      <el-row :gutter="14" v-loading="loading">
        <el-col :span="6" v-for="t in list" :key="t.id" class="cell">
          <el-card shadow="hover" class="table-card" :body-style="{ padding: '14px' }" @click="openEdit(t)">
            <div class="head">
              <span class="no">{{ t.no }}</span>
              <el-tag :type="statusMetaMap[t.status].type" size="small">{{ statusMetaMap[t.status].label }}</el-tag>
            </div>
            <div class="meta">{{ t.area }} · {{ t.seats }} 座</div>
            <div class="order" v-if="t.currentOrderNo">订单：{{ t.currentOrderNo }}</div>
            <div class="remark" v-else-if="t.remark">{{ t.remark }}</div>
            <div class="remark muted" v-else>点击编辑</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="桌号"><el-input v-model="form.no" placeholder="如 A01" /></el-form-item>
        <el-form-item label="区域">
          <el-select v-model="form.area"><el-option label="大厅" value="大厅" /><el-option label="包间" value="包间" /><el-option label="卡座" value="卡座" /></el-select>
        </el-form-item>
        <el-form-item label="座位数"><el-input-number v-model="form.seats" :min="1" :max="50" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option v-for="s in statusMeta" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listTables, addTable, updateTable, type TableItem, type TableForm } from '@/api/table'

const statusMeta = [
  { value: 0, label: '空闲', type: 'success' },
  { value: 1, label: '占用', type: 'danger' },
  { value: 2, label: '待清', type: 'warning' },
  { value: 3, label: '预订', type: 'info' }
] as const
const statusMetaMap: Record<number, { label: string; type: any }> = Object.fromEntries(statusMeta.map(s => [s.value, s]))

const filterArea = ref('')
const filterStatus = ref<number | string>('')
const list = ref<TableItem[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive<TableForm>({ no: '', area: '大厅', seats: 4, status: 0, remark: '' })

async function load() {
  loading.value = true
  try {
    list.value = await listTables({ area: filterArea.value || undefined, status: filterStatus.value === '' ? undefined : filterStatus.value })
  } finally { loading.value = false }
}
function openAdd() {
  dialogTitle.value = '新增桌台'
  Object.assign(form, { id: undefined, no: '', area: '大厅', seats: 4, status: 0, remark: '' })
  dialogVisible.value = true
}
function openEdit(t: TableItem) {
  dialogTitle.value = '编辑桌台'
  Object.assign(form, { id: t.id, no: t.no, area: t.area, seats: t.seats, status: t.status, remark: t.remark })
  dialogVisible.value = true
}
async function save() {
  if (form.id) await updateTable({ ...form })
  else await addTable({ ...form })
  ElMessage.success('已保存')
  dialogVisible.value = false
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
.legend { display: flex; gap: 6px; margin-left: auto; }
.cell { margin-bottom: 14px; }
.table-card { cursor: pointer; border-radius: 8px; }
.head { display: flex; justify-content: space-between; align-items: center; }
.no { font-size: 20px; font-weight: 700; }
.meta { color: #86909c; font-size: 13px; margin-top: 6px; }
.order { color: #ff7a59; font-size: 13px; margin-top: 6px; }
.remark { font-size: 12px; margin-top: 6px; color: #c0c4cc; }
.muted { color: #c0c4cc; }
</style>
