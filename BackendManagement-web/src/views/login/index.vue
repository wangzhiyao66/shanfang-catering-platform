<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <div class="title">膳房·商家后台</div>
      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="submit" @click="onSubmit">登录</el-button>
      </el-form>
      <div class="tip">演示账号：admin / admin123（默认管理员，见后端 application.yml）</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form.username, form.password)
      ElMessage.success('登录成功')
      router.replace('/')
    } catch {
      // 错误信息已由 request 拦截器统一提示
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-wrap { height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #e8543f, #ff7a59); }
.login-card { width: 360px; padding: 8px 8px 16px; }
.title { text-align: center; font-size: 20px; font-weight: 700; color: #e8543f; margin: 12px 0 20px; }
.submit { width: 100%; }
.tip { text-align: center; color: #909399; font-size: 12px; margin-top: 12px; }
</style>
