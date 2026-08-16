<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="s in cards" :key="s.label">
        <el-card shadow="hover" class="stat-card" :style="{ borderTop: `3px solid ${s.color}` }">
          <div class="stat-title">{{ s.label }}</div>
          <div class="stat-value" :style="{ color: s.color }">{{ s.value }}</div>
          <div class="stat-sub">{{ s.sub }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt">
      <el-col :span="16">
        <el-card shadow="never" header="近 7 日销售趋势">
          <BaseChart :option="salesOption" height="300px" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" header="订单类型分布">
          <BaseChart :option="typeOption" height="300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt">
      <el-col :span="12">
        <el-card shadow="never" header="热销菜品 TOP">
          <BaseChart :option="topOption" height="320px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="待办与提示">
          <el-timeline>
            <el-timeline-item type="warning" :timestamp="`待处理订单 ${data.pendingOrders} 单`">请及时接单 / 出餐</el-timeline-item>
            <el-timeline-item type="primary" timestamp="外卖订单">M 开头订单需尽快配送</el-timeline-item>
            <el-timeline-item type="success" timestamp="今日营收">¥{{ yuan(data.todayRevenue) }}</el-timeline-item>
            <el-timeline-item type="info" timestamp="会员总数">{{ data.totalMembers }} 人</el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import BaseChart from '@/components/BaseChart.vue'
import { getDashboard, type DashboardVO, type TypeCount } from '@/api/dashboard'
import { yuan, yuanShort } from '@/utils/format'

const data = ref<DashboardVO>({
  todayRevenue: 0, todayOrderCount: 0, pendingOrders: 0, totalMembers: 0,
  weekRevenue: [], orderTypeDist: [], topDishes: []
})

const cards = computed(() => [
  { label: '今日订单', value: data.value.todayOrderCount, sub: '实时统计', color: '#ff7a59' },
  { label: '今日营业额', value: '¥' + yuan(data.value.todayRevenue), sub: '单位：元', color: '#409eff' },
  { label: '待处理订单', value: data.value.pendingOrders, sub: '需尽快接单', color: '#e6a23c' },
  { label: '会员总数', value: data.value.totalMembers, sub: '累计注册会员', color: '#67c23a' }
])

function typeLabel(t: number | null): string {
  if (t === 1) return '堂食'
  if (t === 2) return '外卖'
  if (t === 3) return '自提'
  return '其他'
}

const salesOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['营业额', '订单数'], bottom: 0 },
  grid: { left: 50, right: 20, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: data.value.weekRevenue.map(t => t.date) },
  yAxis: [
    { type: 'value', name: '营业额', axisLabel: { formatter: (v: number) => yuanShort(v) } },
    { type: 'value', name: '订单数' }
  ],
  series: [
    { name: '营业额', type: 'line', smooth: true, areaStyle: {}, data: data.value.weekRevenue.map(t => t.amount), itemStyle: { color: '#ff7a59' } },
    { name: '订单数', type: 'bar', yAxisIndex: 1, data: data.value.weekRevenue.map(t => t.orderCount), itemStyle: { color: '#409eff' } }
  ]
}))

const typeOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 单 ({d}%)' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
    data: data.value.orderTypeDist.map((t: TypeCount) => ({
      name: typeLabel(t.type),
      value: t.count,
      itemStyle: { color: t.type === 1 ? '#ff7a59' : t.type === 2 ? '#409eff' : t.type === 3 ? '#67c23a' : '#c0c4cc' }
    })),
    label: { formatter: '{b}\n{d}%' }
  }]
}))

const topOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 90, right: 30, top: 10, bottom: 20 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: data.value.topDishes.map(d => d.dishName).reverse() },
  series: [{ type: 'bar', data: data.value.topDishes.map(d => d.qty).reverse(), itemStyle: { color: '#ff7a59', borderRadius: [0, 4, 4, 0] }, barWidth: 14 }]
}))

onMounted(async () => {
  data.value = await getDashboard()
})
</script>

<style scoped>
.stat-card { border-radius: 8px; }
.stat-title { color: #86909c; font-size: 13px; }
.stat-value { font-size: 26px; font-weight: 700; margin: 6px 0; }
.stat-sub { color: #c0c4cc; font-size: 12px; }
.mt { margin-top: 16px; }
</style>
