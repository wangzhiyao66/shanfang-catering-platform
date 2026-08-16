<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 120px" @change="applyFilter">
          <el-option label="堂食" :value="1" /><el-option label="外卖" :value="2" /><el-option label="自提" :value="3" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 140px" @change="applyFilter">
          <el-option v-for="s in statusMeta" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-input v-model="keyword" placeholder="订单号" style="width: 200px" :prefix-icon="Search" @keyup.enter="applyFilter" />
        <el-button type="primary" @click="applyFilter">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="pagedList" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }"><el-tag size="small" :type="row.type === 1 ? 'warning' : row.type === 2 ? 'success' : 'info'">{{ typeLabel(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag :type="statusMetaMap[row.status].type" size="small">{{ statusMetaMap[row.status].label }}</el-tag></template>
        </el-table-column>
        <el-table-column label="桌台" width="80">
          <template #default="{ row }">{{ tableNo(row.tableId) || '—' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ yuan(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" min-width="150" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-for="a in actionsFor(row.status)" :key="a.label" link :type="a.type" @click="doAction(row, a)">{{ a.label }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="mt" background layout="total, prev, pager, next" :total="filtered.length"
        :current-page="page" :page-size="size" @current-change="onPage" />
    </el-card>

    <el-drawer v-model="drawer" title="订单详情" size="420px">
      <template v-if="current">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ typeLabel(current.type) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusMetaMap[current.status].type" size="small">{{ statusMetaMap[current.status].label }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="桌台">{{ current.tableNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ current.memberName || '非会员' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ current.createdAt }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="current.items || []" class="mt" size="small" border>
          <el-table-column prop="dishName" label="菜品" />
          <el-table-column label="规格" width="120"><template #default="{ row }">{{ row.specsJson || '—' }}</template></el-table-column>
          <el-table-column prop="qty" label="数量" width="60" />
          <el-table-column label="小计" width="90"><template #default="{ row }">¥{{ yuan((row.unitPrice || 0) * row.qty) }}</template></el-table-column>
        </el-table>
        <div class="total">合计：<b>¥{{ yuan(current.totalAmount) }}</b>（已付 ¥{{ yuan(current.payAmount ?? current.totalAmount) }}）</div>
        <div class="actions mt">
          <el-button v-for="a in actionsFor(current.status)" :key="a.label" :type="a.type" @click="doAction(current, a)">{{ a.label }}</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOrders, getOrder, updateOrderStatus, acceptOrder, cancelOrder, type Order, type OrderDetail } from '@/api/order'
import { listTables, type TableItem } from '@/api/table'
import { yuan } from '@/utils/format'

// 真实后端订单状态机：0待支付 1已支付/待接单 2制作中 3已上菜/待取餐 4已完成 5退款中 6已退款 7退单 9已取消
const statusMeta = [
  { value: 0, label: '待支付', type: 'info' },
  { value: 1, label: '已支付', type: 'warning' },
  { value: 2, label: '制作中', type: 'primary' },
  { value: 3, label: '待取餐', type: 'warning' },
  { value: 4, label: '已完成', type: 'success' },
  { value: 5, label: '退款中', type: 'danger' },
  { value: 6, label: '已退款', type: 'info' },
  { value: 7, label: '退单', type: 'danger' },
  { value: 9, label: '已取消', type: 'info' }
] as const
const statusMetaMap: Record<number, { label: string; type: any }> = Object.fromEntries(statusMeta.map(s => [s.value, s]))

type Api = 'accept' | 'status' | 'cancel'
interface Action { label: string; api: Api; to?: number; type: any; confirm?: boolean }
function actionsFor(status: number): Action[] {
  switch (status) {
    case 0: return [{ label: '取消', api: 'cancel', type: 'info', confirm: true }]
    case 1: return [{ label: '接单', api: 'accept', type: 'primary', confirm: true }, { label: '取消', api: 'cancel', type: 'info', confirm: true }]
    case 2: return [{ label: '出餐', api: 'status', to: 3, type: 'warning', confirm: true }, { label: '退款', api: 'status', to: 5, type: 'danger', confirm: true }, { label: '取消', api: 'cancel', type: 'info', confirm: true }]
    case 3: return [{ label: '完成', api: 'status', to: 4, type: 'success', confirm: true }, { label: '退款', api: 'status', to: 5, type: 'danger', confirm: true }]
    case 4: return [{ label: '退款', api: 'status', to: 5, type: 'danger', confirm: true }]
    default: return []
  }
}
function typeLabel(t: number) { return t === 1 ? '堂食' : t === 2 ? '外卖' : '自提' }

const filterType = ref<number | ''>('')
const filterStatus = ref<number | ''>('')
const keyword = ref('')
const all = ref<Order[]>([])
const filtered = ref<Order[]>([])
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const tableMap = ref<Record<number, string>>({})
function tableNo(id?: number) { return id != null ? (tableMap.value[id] || `桌#${id}`) : '' }

const drawer = ref(false)
const current = ref<OrderDetail | null>(null)

function applyFilter() {
  filtered.value = all.value.filter(o => {
    if (filterType.value !== '' && o.type !== filterType.value) return false
    if (filterStatus.value !== '' && o.status !== filterStatus.value) return false
    if (keyword.value && !o.orderNo.includes(keyword.value)) return false
    return true
  })
  page.value = 1
}
function reset() { filterType.value = ''; filterStatus.value = ''; keyword.value = ''; applyFilter() }
const pagedList = ref<Order[]>([])
function onPage(p: number) {
  page.value = p
  pagedList.value = filtered.value.slice((p - 1) * size.value, p * size.value)
}
async function openDetail(row: Order) {
  current.value = await getOrder(row.id)
  drawer.value = true
}

async function doAction(row: Order, a: Action) {
  if (a.confirm) {
    try { await ElMessageBox.confirm(`确认「${a.label}」？`, '提示', { type: 'warning' }) } catch { return }
  }
  if (a.api === 'accept') await acceptOrder(row.id)
  else if (a.api === 'cancel') await cancelOrder(row.id)
  else await updateOrderStatus(row.id, a.to!)
  ElMessage.success('操作成功')
  drawer.value = false
  await load()
}

async function load() {
  loading.value = true
  try {
    all.value = await listOrders()
    applyFilter()
    pagedList.value = filtered.value.slice(0, size.value)
  } finally { loading.value = false }
}
onMounted(async () => {
  try { tableMap.value = Object.fromEntries((await listTables()).map(t => [t.id, t.tableNo])) } catch { /* 桌台映射失败不阻断订单列表 */ }
  load()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.mt { margin-top: 14px; }
.total { text-align: right; margin-top: 12px; font-size: 15px; }
.actions { display: flex; gap: 10px; justify-content: flex-end; }
</style>
