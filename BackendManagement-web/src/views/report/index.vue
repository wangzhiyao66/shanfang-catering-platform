<template>
  <div>
    <el-row :gutter="16" class="mb">
      <el-col :span="6" v-for="s in summaryCards" :key="s.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">{{ s.label }}</div>
          <div class="stat-value" :style="{ color: s.color }">{{ s.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" header="营收 / 订单数趋势">
          <BaseChart :option="revenueOption" height="320px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="客单价趋势">
          <BaseChart :option="avgTicketOption" height="320px" />
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="mt">
      <el-col :span="12">
        <el-card shadow="never" header="菜品销量 TOP">
          <BaseChart :option="dishRankOption" height="340px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="订单类型占比">
          <BaseChart :option="typeShareOption" height="340px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import BaseChart from '@/components/BaseChart.vue'
import { getReport, type ReportVO, type TypeCount } from '@/api/report'
import { yuan, yuanShort } from '@/utils/format'

const report = ref<ReportVO>({ revenueTrend: [], typeDist: [], topDishes: [], summary: { totalRevenue: 0, totalOrders: 0, avgOrderValue: 0, refundAmount: 0 } })

const summaryCards = computed(() => [
  { label: '总营收', value: '¥' + yuan(report.value.summary.totalRevenue), color: '#ff7a59' },
  { label: '总订单数', value: report.value.summary.totalOrders, color: '#409eff' },
  { label: '客单价', value: '¥' + yuan(report.value.summary.avgOrderValue), color: '#67c23a' },
  { label: '退款金额', value: '¥' + yuan(report.value.summary.refundAmount), color: '#e6a23c' }
])

const revenueOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['营收', '订单数'], bottom: 0 },
  grid: { left: 60, right: 50, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: report.value.revenueTrend.map(d => d.date), axisLabel: { rotate: 45 } },
  yAxis: [
    { type: 'value', name: '营收', axisLabel: { formatter: (v: number) => yuanShort(v) } },
    { type: 'value', name: '订单数' }
  ],
  series: [
    { name: '营收', type: 'line', smooth: true, areaStyle: {}, data: report.value.revenueTrend.map(d => d.revenue), itemStyle: { color: '#ff7a59' } },
    { name: '订单数', type: 'bar', yAxisIndex: 1, data: report.value.revenueTrend.map(d => d.orderCount), itemStyle: { color: '#409eff' } }
  ]
}))

const avgTicketOption = computed(() => ({
  tooltip: { trigger: 'axis', formatter: (p: any) => `${p[0].axisValue}<br/>客单价：¥${(p[0].data / 100).toFixed(2)}` },
  grid: { left: 60, right: 20, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: report.value.revenueTrend.map(d => d.date), axisLabel: { rotate: 45 } },
  yAxis: { type: 'value', axisLabel: { formatter: (v: number) => yuanShort(v) } },
  series: [{ type: 'line', smooth: true, data: report.value.revenueTrend.map(d => d.avgAmount), itemStyle: { color: '#409eff' } }]
}))

const dishRankOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 110, right: 30, top: 10, bottom: 20 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: report.value.topDishes.map(d => d.dishName).reverse() },
  series: [{ type: 'bar', data: report.value.topDishes.map(d => d.qty).reverse(), itemStyle: { color: '#67c23a', borderRadius: [0, 4, 4, 0] }, barWidth: 14 }]
}))

function typeLabel(t: number | null): string {
  if (t === 1) return '堂食'
  if (t === 2) return '外卖'
  if (t === 3) return '自提'
  return '其他'
}
const typeShareOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 单 ({d}%)' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
    data: report.value.typeDist.map((t: TypeCount) => ({
      name: typeLabel(t.type),
      value: t.count,
      itemStyle: { color: t.type === 1 ? '#ff7a59' : t.type === 2 ? '#409eff' : t.type === 3 ? '#67c23a' : '#c0c4cc' }
    })),
    label: { formatter: '{b}\n{d}%' }
  }]
}))

onMounted(async () => {
  report.value = await getReport(30)
})
</script>

<style scoped>
.stat-card { border-radius: 8px; }
.stat-title { color: #86909c; font-size: 13px; }
.stat-value { font-size: 24px; font-weight: 700; margin-top: 6px; }
.mt { margin-top: 16px; }
.mb { margin-bottom: 16px; }
</style>
