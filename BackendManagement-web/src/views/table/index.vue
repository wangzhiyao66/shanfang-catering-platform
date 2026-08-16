<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="filterArea" placeholder="全部区域" clearable style="width: 130px" @change="applyFilter">
          <el-option label="大厅" value="大厅" />
          <el-option label="包间" value="包间" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 130px" @change="applyFilter">
          <el-option v-for="s in statusMeta" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <span class="legend">
          <el-tag v-for="s in statusMeta" :key="s.value" :type="s.type" size="small" effect="light">{{ s.label }}</el-tag>
        </span>
      </div>

      <el-row :gutter="14" v-loading="loading">
        <el-col :span="6" v-for="t in filtered" :key="t.id" class="cell">
          <el-card shadow="hover" class="table-card" :body-style="{ padding: '14px' }" @click="openStatus(t)">
            <div class="head">
              <span class="no">{{ t.tableNo }}</span>
              <el-tag :type="statusMetaMap[t.status].type" size="small">{{ statusMetaMap[t.status].label }}</el-tag>
            </div>
            <div class="meta">{{ t.area }} · {{ t.seats }} 座</div>
            <div class="remark muted">点击修改状态</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="dialogVisible" title="修改桌台状态" width="360px">
      <p>桌台：<b>{{ active?.tableNo }}</b>（{{ active?.area }} · {{ active?.seats }} 座）</p>
      <el-select v-model="nextStatus" style="width: 100%" placeholder="选择新状态">
        <el-option v-for="s in statusMeta" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listTables, updateTableStatus, type TableItem } from '@/api/table'

const statusMeta = [
  { value: 0, label: '空闲', type: 'success' },
  { value: 1, label: '占用', type: 'danger' },
  { value: 2, label: '待清', type: 'warning' },
  { value: 3, label: '预订', type: 'info' }
] as const
const statusMetaMap: Record<number, { label: string; type: any }> = Object.fromEntries(statusMeta.map(s => [s.value, s]))

const filterArea = ref('')
const filterStatus = ref<number | string>('')
const all = ref<TableItem[]>([])
const filtered = ref<TableItem[]>([])
const loading = ref(false)

function applyFilter() {
  filtered.value = all.value.filter(t => {
    if (filterArea.value && t.area !== filterArea.value) return false
    if (filterStatus.value !== '' && t.status !== Number(filterStatus.value)) return false
    return true
  })
}

const dialogVisible = ref(false)
const active = ref<TableItem | null>(null)
const nextStatus = ref(0)
function openStatus(t: TableItem) { active.value = t; nextStatus.value = t.status; dialogVisible.value = true }
async function save() {
  if (!active.value) return
  await updateTableStatus(active.value.id, nextStatus.value)
  ElMessage.success('状态已更新')
  dialogVisible.value = false
  load()
}

async function load() {
  loading.value = true
  try { all.value = await listTables(); applyFilter() }
  finally { loading.value = false }
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
.remark { font-size: 12px; margin-top: 6px; color: #c0c4cc; }
.muted { color: #c0c4cc; }
</style>
