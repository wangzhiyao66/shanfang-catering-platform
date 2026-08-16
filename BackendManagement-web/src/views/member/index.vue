<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="姓名/手机号" style="width: 200px" :prefix-icon="Search" @keyup.enter="load" />
        <el-select v-model="filterLevel" placeholder="全部等级" clearable style="width: 130px" @change="load">
          <el-option label="普通" value="普通" /><el-option label="银卡" value="银卡" />
          <el-option label="金卡" value="金卡" /><el-option label="钻石" value="钻石" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe border @row-click="openDetail">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="会员" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="等级" width="90">
          <template #default="{ row }"><el-tag :type="levelType(row.level)" size="small" effect="dark">{{ row.level }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column label="余额" width="110"><template #default="{ row }">¥{{ yuan(row.balance) }}</template></el-table-column>
        <el-table-column label="累计消费" width="120"><template #default="{ row }">¥{{ yuan(row.totalSpent) }}</template></el-table-column>
        <el-table-column prop="lastVisit" label="最近到店" width="120" />
      </el-table>

      <el-pagination class="mt" background layout="total, prev, pager, next" :total="total"
        :current-page="page" :page-size="size" @current-change="onPage" />
    </el-card>

    <el-drawer v-model="drawer" title="会员详情" size="380px">
      <template v-if="current">
        <div class="profile">
          <el-avatar :size="64">{{ current.name.charAt(0) }}</el-avatar>
          <div class="p-name">{{ current.name }} <el-tag :type="levelType(current.level)" size="small" effect="dark">{{ current.level }}</el-tag></div>
          <div class="p-phone">{{ current.phone }}</div>
        </div>
        <el-descriptions :column="1" border class="mt">
          <el-descriptions-item label="积分">{{ current.points }}</el-descriptions-item>
          <el-descriptions-item label="余额">¥{{ yuan(current.balance) }}</el-descriptions-item>
          <el-descriptions-item label="累计消费">¥{{ yuan(current.totalSpent) }}</el-descriptions-item>
          <el-descriptions-item label="最近到店">{{ current.lastVisit }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listMembers, type Member } from '@/api/member'
import { yuan } from '@/utils/format'

void ElMessage
const keyword = ref('')
const filterLevel = ref('')
const list = ref<Member[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const drawer = ref(false)
const current = ref<Member | null>(null)

function levelType(lv: string): any {
  return lv === '钻石' ? 'danger' : lv === '金卡' ? 'warning' : lv === '银卡' ? 'info' : 'success'
}
async function load() {
  loading.value = true
  try {
    const data = await listMembers({ page: page.value, size: size.value, keyword: keyword.value || undefined, level: filterLevel.value || undefined })
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
function onPage(p: number) { page.value = p; load() }
function openDetail(row: Member) { current.value = row; drawer.value = true }
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.mt { margin-top: 14px; }
.profile { text-align: center; }
.p-name { font-size: 18px; font-weight: 700; margin-top: 8px; }
.p-phone { color: #86909c; font-size: 13px; margin-top: 4px; }
</style>
