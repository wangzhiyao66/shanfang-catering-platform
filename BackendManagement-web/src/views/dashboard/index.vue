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
            <el-timeline-item type="warning" :timestamp="`待处理订单 ${stats.pendingOrders} 单`">请及时接单 / 出餐</el-timeline-item>
            <el-timeline-item type="primary" timestamp="外卖订单">M 开头订单需尽快配送</el-timeline-item>
            <el-timeline-item type="success" timestamp="今日营收">¥{{ yuan(stats.todayRevenue) }}</el-timeline-item>
            <el-timeline-item type="info" timestamp="本月营收">¥{{ yuan(stats.monthRevenue) }}</el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import BaseChart from '@/components/BaseChart.vue'
import { getStats, getSalesTrend, getOrderTypeDist, getTopDishes, type DashStats, type TrendPoint, type TypeDist, type TopDish } from '@/api/dashboard'
import { yuan, yuanShort } from '@/utils/format'

const stats = ref<DashStats>({ todayOrders: 0, todayRevenue: 0, pendingOrders: 0, todayMembers: 0, weekRevenue: 0, monthRevenue: 0, avgTicket: 0 })
const trend = ref<TrendPoint[]>([])
const typeDist = ref<TypeDist[]>([])
const topDishes = ref<TopDish[]>([])

const cards = computed(() => [
  { label: '今日订单', value: stats.value.todayOrders, sub: '较昨日 +12%', color: '#ff7a59' },
  { label: '今日营业额', value: '¥' + yuan(stats.value.todayRevenue), sub: '客单价 ¥' + yuan(stats.value.avgTicket), color: '#409eff' },
  { label: '待处理订单', value: stats.value.pendingOrders, sub: '需尽快接单', color: '#e6a23c' },
  { label: '今日新增会员', value: stats.value.todayMembers, sub: '本月活跃良好', color: '#67c23a' }
])

const salesOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['营业额', '订单数'], bottom: 0 },
  grid: { left: 50, right: 20, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: trend.value.map(t => t.date) },
  yAxis: [
    { type: 'value', name: '营业额', axisLabel: { formatter: (v: number) => yuanShort(v) } },
    { type: 'value', name: '订单数' }
  ],
  series: [
    { name: '营业额', type: 'line', smooth: true, areaStyle: {}, data: trend.value.map(t => t.amount), itemStyle: { color: '#ff7a59' } },
    { name: '订单数', type: 'bar', yAxisIndex: 1, data: trend.value.map(t => t.count), itemStyle: { color: '#409eff' } }
  ]
}))

const typeOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 单 ({d}%)' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
    data: typeDist.value.map(t => ({ name: t.label, value: t.value, itemStyle: { color: t.type === 1 ? '#ff7a59' : t.type === 2 ? '#409eff' : '#67c23a' } })),
    label: { formatter: '{b}\n{d}%' }
  }]
}))

const topOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 90, right: 30, top: 10, bottom: 20 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: topDishes.value.map(d => d.name).reverse() },
  series: [{ type: 'bar', data: topDishes.value.map(d => d.count).reverse(), itemStyle: { color: '#ff7a59', borderRadius: [0, 4, 4, 0] }, barWidth: 14 }]
}))

onMounted(async () => {
  const [s, t, d, top] = await Promise.all([getStats(), getSalesTrend(), getOrderTypeDist(), getTopDishes()])
  stats.value = s
  trend.value = t
  typeDist.value = d
  topDishes.value = top
})
</script>

<style scoped>
.stat-card { border-radius: 8px; }
.stat-title { color: #86909c; font-size: 13px; }
.stat-value { font-size: 26px; font-weight: 700; margin: 6px 0; }
.stat-sub { color: #c0c4cc; font-size: 12px; }
.mt { margin-top: 16px; }
</style>
