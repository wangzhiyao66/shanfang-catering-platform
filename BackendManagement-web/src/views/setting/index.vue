<template>
  <div>
    <el-card shadow="never" header="门店基础设置" style="max-width: 720px">
      <el-form :model="form" label-width="100px" v-loading="loading">
        <el-form-item label="门店名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="门店地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="form.businessHours" placeholder="如 10:00 - 22:00" /></el-form-item>
        <el-form-item label="门店公告"><el-input v-model="form.notice" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="打印机"><el-input v-model="form.printer" /></el-form-item>
        <el-form-item label="自动接单">
          <el-switch v-model="form.autoAccept" />
          <span class="hint">开启后外卖/自提订单自动接单并推送后厨</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save" v-permission="'setting:edit'">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getShopInfo, updateShopInfo, type ShopInfo } from '@/api/setting'

const form = reactive<ShopInfo>({ id: 1, name: '', phone: '', address: '', businessHours: '', notice: '', printer: '', autoAccept: true })
const loading = ref(false)

async function load() {
  loading.value = true
  try { Object.assign(form, await getShopInfo()) } finally { loading.value = false }
}
async function save() {
  await updateShopInfo({ ...form })
  ElMessage.success('已保存')
}
onMounted(load)
</script>

<style scoped>
.hint { color: #c0c4cc; font-size: 12px; margin-left: 10px; }
</style>
