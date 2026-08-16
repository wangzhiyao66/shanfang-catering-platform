<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" header="近 30 日营业额趋势">
          <BaseChart :option="revenueOption" height="320px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="近 30 日客单价趋势">
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
import { getRevenueTrend, getDishRank, getOrderTypeShare, getAvgTicket, type TrendPoint, type RankItem, type TypeDist } from '@/api/report'
import { yuanShort } from '@/utils/format'

const revenue = ref<TrendPoint[]>([])
const avgTicket = ref<TrendPoint[]>([])
const dishRank = ref<RankItem[]>([])
const typeShare = ref<TypeDist[]>([])

const revenueOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 60, right: 20, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: revenue.value.map(d => d.date), axisLabel: { rotate: 45 } },
  yAxis: { type: 'value', axisLabel: { formatter: (v: number) => yuanShort(v) } },
  series: [{ type: 'line', smooth: true, data: revenue.value.map(d => d.amount), areaStyle: {}, itemStyle: { color: '#ff7a59' } }]
}))

const avgTicketOption = computed(() => ({
  tooltip: { trigger: 'axis', formatter: (p: any) => `${p[0].axisValue}<br/>客单价：¥${(p[0].data / 100).toFixed(2)}` },
  grid: { left: 60, right: 20, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: avgTicket.value.map(d => d.date), axisLabel: { rotate: 45 } },
  yAxis: { type: 'value', axisLabel: { formatter: (v: number) => yuanShort(v) } },
  series: [{ type: 'line', smooth: true, data: avgTicket.value.map(d => d.value), itemStyle: { color: '#409eff' } }]
}))

const dishRankOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 90, right: 30, top: 10, bottom: 20 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: dishRank.value.map(d => d.name).reverse() },
  series: [{ type: 'bar', data: dishRank.value.map(d => d.count).reverse(), itemStyle: { color: '#67c23a', borderRadius: [0, 4, 4, 0] }, barWidth: 14 }]
}))

const typeShareOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 单 ({d}%)' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
    data: typeShare.value.map(t => ({ name: t.label, value: t.value, itemStyle: { color: t.type === 1 ? '#ff7a59' : t.type === 2 ? '#409eff' : '#67c23a' } })),
    label: { formatter: '{b}\n{d}%' }
  }]
}))

onMounted(async () => {
  const [r, a, d, t] = await Promise.all([getRevenueTrend(), getAvgTicket(), getDishRank(), getOrderTypeShare()])
  revenue.value = r; avgTicket.value = a; dishRank.value = d; typeShare.value = t
})
</script>

<style scoped>
.mt { margin-top: 16px; }
</style>
