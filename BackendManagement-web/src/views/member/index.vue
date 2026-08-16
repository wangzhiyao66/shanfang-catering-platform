<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="昵称/手机号" style="width: 200px" :prefix-icon="Search" @keyup.enter="applyFilter" />
        <el-select v-model="filterLevel" placeholder="全部等级" clearable style="width: 130px" @change="applyFilter">
          <el-option v-for="lv in levels" :key="lv.id" :label="lv.name" :value="lv.id" />
        </el-select>
        <el-button type="primary" @click="applyFilter">查询</el-button>
      </div>

      <el-table :data="pagedList" v-loading="loading" stripe border @row-click="openDetail">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="会员" min-width="100">
          <template #default="{ row }">{{ row.nickname || '匿名' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="等级" width="90">
          <template #default="{ row }"><el-tag :type="levelType(row.levelId)" size="small" effect="dark">{{ levelName(row.levelId) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column label="余额" width="110"><template #default="{ row }">¥{{ yuan(row.balance) }}</template></el-table-column>
        <el-table-column prop="lastActiveAt" label="最近活跃" min-width="160" />
      </el-table>

      <el-pagination class="mt" background layout="total, prev, pager, next" :total="filtered.length"
        :current-page="page" :page-size="size" @current-change="onPage" />
    </el-card>

    <el-drawer v-model="drawer" title="会员详情" size="380px">
      <template v-if="current">
        <div class="profile">
          <el-avatar :size="64">{{ (current.nickname || '匿').charAt(0) }}</el-avatar>
          <div class="p-name">{{ current.nickname || '匿名' }} <el-tag :type="levelType(current.levelId)" size="small" effect="dark">{{ levelName(current.levelId) }}</el-tag></div>
          <div class="p-phone">{{ current.phone || '未绑定' }}</div>
        </div>
        <el-descriptions :column="1" border class="mt">
          <el-descriptions-item label="积分">{{ current.points }}</el-descriptions-item>
          <el-descriptions-item label="余额">¥{{ yuan(current.balance) }}</el-descriptions-item>
          <el-descriptions-item label="最近活跃">{{ current.lastActiveAt }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { listMembers, listMemberLevels, type Member, type MemberLevel } from '@/api/member'
import { yuan } from '@/utils/format'

const keyword = ref('')
const filterLevel = ref<number | ''>('')
const levels = ref<MemberLevel[]>([])
const all = ref<Member[]>([])
const filtered = ref<Member[]>([])
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const drawer = ref(false)
const current = ref<Member | null>(null)

const levelMap = ref<Record<number, MemberLevel>>({})
function levelName(id: number) { return levelMap.value[id]?.name || '普通' }
function levelType(id: number): any {
  const name = levelName(id)
  return name === '金卡' ? 'warning' : name === '银卡' ? 'info' : 'success'
}

function applyFilter() {
  filtered.value = all.value.filter(m => {
    if (filterLevel.value !== '' && m.levelId !== filterLevel.value) return false
    if (keyword.value) {
      const k = keyword.value
      if (!((m.nickname || '').includes(k) || (m.phone || '').includes(k))) return false
    }
    return true
  })
  page.value = 1
  pagedList.value = filtered.value.slice(0, size.value)
}
const pagedList = ref<Member[]>([])
function onPage(p: number) { page.value = p; pagedList.value = filtered.value.slice((p - 1) * size.value, p * size.value) }
function openDetail(row: Member) { current.value = row; drawer.value = true }

async function load() {
  loading.value = true
  try { all.value = await listMembers(); applyFilter() }
  finally { loading.value = false }
}
onMounted(async () => {
  levels.value = await listMemberLevels()
  levelMap.value = Object.fromEntries(levels.value.map(l => [l.id, l]))
  load()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.mt { margin-top: 14px; }
.profile { text-align: center; }
.p-name { font-size: 18px; font-weight: 700; margin-top: 8px; }
.p-phone { color: #86909c; font-size: 13px; margin-top: 4px; }
</style>
