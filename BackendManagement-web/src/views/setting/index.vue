<template>
  <div>
    <el-card shadow="never" header="门店基础设置" style="max-width: 720px">
      <el-form :model="form" label-width="100px" v-loading="loading">
        <el-form-item label="门店名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="门店地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="form.businessHours" placeholder="如 10:00 - 22:00" /></el-form-item>
        <el-form-item label="门店公告"><el-input v-model="form.notice" type="textarea" :rows="2" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save" v-permission="'setting:edit'">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getShopInfo, updateShopInfo, getShopSettings, updateShopSettings } from '@/api/setting'

const form = reactive<{ id: number; name: string; address: string; phone: string; businessHours: string; notice: string }>({
  id: 1, name: '', address: '', phone: '', businessHours: '', notice: ''
})
const loading = ref(false)
const saving = ref(false)

async function load() {
  loading.value = true
  try {
    const [shop, settings] = await Promise.all([getShopInfo(), getShopSettings()])
    form.id = shop.id
    form.name = shop.name || ''
    form.address = settings.address || ''
    form.phone = settings.phone || ''
    form.businessHours = settings.businessHours || ''
    form.notice = settings.notice || ''
  } finally { loading.value = false }
}
async function save() {
  saving.value = true
  try {
    await updateShopInfo({ id: form.id, name: form.name })
    await updateShopSettings({
      address: form.address,
      phone: form.phone,
      businessHours: form.businessHours,
      notice: form.notice
    })
    ElMessage.success('已保存')
  } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
.hint { color: #c0c4cc; font-size: 12px; margin-left: 10px; }
</style>
