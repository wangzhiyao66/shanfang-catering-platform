<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" v-permission="'marketing:edit'" @click="openAdd">新建优惠券</el-button>
      </div>
      <el-table :data="list" v-loading="loading" stripe border>
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }"><el-tag :type="row.type === 1 ? 'warning' : 'success'" size="small">{{ row.type === 1 ? '满减' : '折扣' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="门槛" width="100"><template #default="{ row }">{{ row.threshold ? '满¥' + yuan(row.threshold) : '无门槛' }}</template></el-table-column>
        <el-table-column label="优惠" width="110">
          <template #default="{ row }">{{ row.type === 1 ? '减¥' + yuan(row.value) : (row.value / 10).toFixed(1) + '折' }}</template>
        </el-table-column>
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">{{ row.validFrom }} ~ {{ row.validTo }}</template>
        </el-table-column>
        <el-table-column label="发放进度" min-width="160">
          <template #default="{ row }">
            <el-progress :percentage="row.total ? Math.round((row.used / row.total) * 100) : 0" :stroke-width="10" />
            <span class="sub">{{ row.used }} / {{ row.total }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="row.status ? 'success' : 'info'" size="small">{{ row.status ? '生效中' : '已停用' }}</el-tag></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建优惠券" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如：满50减10" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">满减</el-radio>
            <el-radio :value="2">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.type === 1 ? '使用门槛(元)' : '门槛(元)'">
          <el-input-number v-model="form.threshold" :min="0" :precision="2" />
          <span class="hint">（0 表示无门槛）</span>
        </el-form-item>
        <el-form-item :label="form.type === 1 ? '减免(元)' : '折扣(如85=8.5折)'">
          <el-input-number v-model="form.value" :min="form.type === 1 ? 0 : 10" :max="form.type === 1 ? 9999 : 99" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker v-model="validRange" type="daterange" value-format="YYYY-MM-DD" range-separator="~" start-placeholder="开始" end-placeholder="结束" />
        </el-form-item>
        <el-form-item label="发放总量"><el-input-number v-model="form.total" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listCoupons, createCoupon, type Coupon, type CouponForm } from '@/api/marketing'
import { yuan } from '@/utils/format'

const list = ref<Coupon[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const validRange = ref<string[]>([])
const form = reactive<CouponForm>({ name: '', type: 1, threshold: 0, value: 10, validFrom: '', validTo: '', total: 100 })

async function load() {
  loading.value = true
  try { list.value = await listCoupons() } finally { loading.value = false }
}
function openAdd() {
  Object.assign(form, { name: '', type: 1, threshold: 0, value: 10, validFrom: '', validTo: '', total: 100 })
  validRange.value = []
  dialogVisible.value = true
}
async function save() {
  if (!form.name.trim()) return ElMessage.warning('请输入名称')
  if (!validRange.value.length) return ElMessage.warning('请选择有效期')
  form.validFrom = validRange.value[0]
  form.validTo = validRange.value[1]
  await createCoupon({ ...form })
  ElMessage.success('已创建优惠券')
  dialogVisible.value = false
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 14px; }
.sub { font-size: 12px; color: #c0c4cc; margin-left: 8px; }
.hint { color: #c0c4cc; font-size: 12px; margin-left: 8px; }
</style>
