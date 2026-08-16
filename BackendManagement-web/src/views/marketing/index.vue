<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" v-permission="'marketing:edit'" @click="openAdd">发放优惠券</el-button>
      </div>
      <el-table :data="list" v-loading="loading" stripe border>
        <el-table-column prop="memberName" label="会员" min-width="120" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.threshold ? 'warning' : 'success'" size="small">{{ row.threshold ? '满减' : '无门槛' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="门槛" width="110">
          <template #default="{ row }">{{ row.threshold ? '满¥' + yuan(row.threshold) : '无门槛' }}</template>
        </el-table-column>
        <el-table-column label="面额" width="110">
          <template #default="{ row }">减¥{{ yuan(row.value) }}</template>
        </el-table-column>
        <el-table-column label="有效期至" width="130">
          <template #default="{ row }">{{ row.validTo || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : row.status === 1 ? 'info' : 'warning'" size="small">
              {{ row.status === 0 ? '未使用' : row.status === 1 ? '已使用' : '已过期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" :disabled="row.status !== 0" @click="remove(row)">作废</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="发放优惠券" width="460px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="会员">
          <el-select v-model="form.memberId" filterable placeholder="选择会员" style="width: 100%">
            <el-option v-for="m in members" :key="m.id" :label="memberLabel(m)" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如：满50减10" /></el-form-item>
        <el-form-item label="门槛(元)">
          <el-input-number v-model="form.thresholdYuan" :min="0" :precision="2" />
          <span class="hint">（0 表示无门槛）</span>
        </el-form-item>
        <el-form-item label="面额(元)">
          <el-input-number v-model="form.valueYuan" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="有效期(天)">
          <el-input-number v-model="form.validDays" :min="1" :max="365" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="save">发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCoupons, issueCoupon, deleteCoupon, type CouponVO, type IssueCouponDTO } from '@/api/marketing'
import { listMembers, type Member } from '@/api/member'
import { yuan } from '@/utils/format'

const list = ref<CouponVO[]>([])
const members = ref<Member[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)

const form = reactive<{ memberId: number | null; name: string; thresholdYuan: number; valueYuan: number; validDays: number }>({
  memberId: null, name: '', thresholdYuan: 0, valueYuan: 10, validDays: 30
})

function memberLabel(m: Member): string {
  return `${m.nickname || '会员'}${m.phone ? '（' + m.phone + '）' : ''}`
}

async function load() {
  loading.value = true
  try { list.value = await listCoupons() } finally { loading.value = false }
}
function openAdd() {
  Object.assign(form, { memberId: null, name: '', thresholdYuan: 0, valueYuan: 10, validDays: 30 })
  dialogVisible.value = true
}
async function save() {
  if (!form.memberId) return ElMessage.warning('请选择会员')
  if (!form.name.trim()) return ElMessage.warning('请输入名称')
  const dto: IssueCouponDTO = {
    memberId: form.memberId,
    name: form.name.trim(),
    value: Math.round(form.valueYuan * 100),
    threshold: Math.round(form.thresholdYuan * 100),
    validDays: form.validDays
  }
  submitting.value = true
  try {
    await issueCoupon(dto)
    ElMessage.success('已发放优惠券')
    dialogVisible.value = false
    load()
  } finally { submitting.value = false }
}
async function remove(row: CouponVO) {
  try { await ElMessageBox.confirm(`确认作废「${row.name}」？`, '提示', { type: 'warning' }) } catch { return }
  await deleteCoupon(row.id)
  ElMessage.success('已作废')
  load()
}
onMounted(async () => {
  load()
  try { members.value = await listMembers() } catch { /* 会员列表失败不阻断优惠券展示 */ }
})
</script>

<style scoped>
.toolbar { margin-bottom: 14px; }
.hint { color: #c0c4cc; font-size: 12px; margin-left: 8px; }
</style>
