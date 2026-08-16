<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 120px" @change="load">
          <el-option label="堂食" :value="1" /><el-option label="外卖" :value="2" /><el-option label="自提" :value="3" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 140px" @change="load">
          <el-option v-for="s in statusMeta" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-input v-model="keyword" placeholder="订单号/客户/手机" style="width: 200px" :prefix-icon="Search" @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }"><el-tag size="small" :type="row.type === 1 ? 'warning' : row.type === 2 ? 'success' : 'info'">{{ typeLabel(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag :type="statusMetaMap[row.status].type" size="small">{{ statusMetaMap[row.status].label }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="tableNo" label="桌台" width="80" />
        <el-table-column prop="customerName" label="客户" width="90" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ yuan(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="150" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-for="a in actionsFor(row.status)" :key="a.to" link :type="a.type" @click="doAction(row, a)">{{ a.label }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="mt" background layout="total, prev, pager, next" :total="total"
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
          <el-descriptions-item label="客户">{{ current.customerName }} {{ current.phone }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ current.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ current.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="current.items" class="mt" size="small" border>
          <el-table-column prop="name" label="菜品" />
          <el-table-column label="规格" width="90"><template #default="{ row }">{{ row.spec || '—' }}</template></el-table-column>
          <el-table-column prop="qty" label="数量" width="60" />
          <el-table-column label="小计" width="90"><template #default="{ row }">¥{{ yuan(row.price * row.qty) }}</template></el-table-column>
        </el-table>
        <div class="total">合计：<b>¥{{ yuan(current.totalAmount) }}</b>（已付 ¥{{ yuan(current.paidAmount) }}）</div>
        <div class="actions mt">
          <el-button v-for="a in actionsFor(current.status)" :key="a.to" :type="a.type" @click="doAction(current, a)">{{ a.label }}</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOrders, updateOrderStatus, refundOrder, acceptOrder, type Order } from '@/api/order'
import { yuan } from '@/utils/format'

const statusMeta = [
  { value: 1, label: '待支付', type: 'info' },
  { value: 2, label: '待接单', type: 'warning' },
  { value: 3, label: '制作中', type: 'primary' },
  { value: 4, label: '待出餐', type: 'warning' },
  { value: 5, label: '已完成', type: 'success' },
  { value: 6, label: '已取消', type: 'info' },
  { value: 7, label: '退款中', type: 'danger' },
  { value: 8, label: '已退款', type: 'info' }
] as const
const statusMetaMap: Record<number, { label: string; type: any }> = Object.fromEntries(statusMeta.map(s => [s.value, s]))

interface Action { label: string; api: 'accept' | 'status' | 'refund'; to?: number; type: any; confirm?: boolean }
function actionsFor(status: number): Action[] {
  switch (status) {
    case 2: return [{ label: '接单', api: 'accept', type: 'primary', confirm: true }, { label: '退款', api: 'refund', type: 'danger', confirm: true }]
    case 3: return [{ label: '出餐', api: 'status', to: 4, type: 'warning', confirm: true }, { label: '退款', api: 'refund', type: 'danger', confirm: true }]
    case 4: return [{ label: '完成', api: 'status', to: 5, type: 'success', confirm: true }, { label: '退款', api: 'refund', type: 'danger', confirm: true }]
    case 7: return [{ label: '同意退款', api: 'refund', type: 'success', confirm: true }]
    default: return []
  }
}
function typeLabel(t: number) { return t === 1 ? '堂食' : t === 2 ? '外卖' : '自提' }

const filterType = ref<number | ''>('')
const filterStatus = ref<number | ''>('')
const keyword = ref('')
const list = ref<Order[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const drawer = ref(false)
const current = ref<Order | null>(null)

async function load() {
  loading.value = true
  try {
    const data = await listOrders({
      page: page.value, size: size.value,
      type: filterType.value === '' ? undefined : filterType.value,
      status: filterStatus.value === '' ? undefined : filterStatus.value,
      keyword: keyword.value || undefined
    })
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
function onPage(p: number) { page.value = p; load() }
function reset() { filterType.value = ''; filterStatus.value = ''; keyword.value = ''; page.value = 1; load() }
function openDetail(row: Order) { current.value = row; drawer.value = true }

async function doAction(row: Order, a: Action) {
  if (a.confirm) {
    try {
      await ElMessageBox.confirm(`确认「${a.label}」？`, '提示', { type: 'warning' })
    } catch {
      return
    }
  }
  if (a.api === 'accept') await acceptOrder(row.id)
  else if (a.api === 'refund') await refundOrder(row.id)
  else await updateOrderStatus(row.id, a.to!)
  ElMessage.success('操作成功')
  drawer.value = false
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.mt { margin-top: 14px; }
.total { text-align: right; margin-top: 12px; font-size: 15px; }
.actions { display: flex; gap: 10px; justify-content: flex-end; }
</style>
