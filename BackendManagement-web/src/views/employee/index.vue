<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never" header="员工列表">
          <el-table :data="employees" v-loading="loading" stripe border>
            <el-table-column prop="name" label="姓名" width="160" />
            <el-table-column label="角色" width="120">
              <template #default="{ row }"><el-tag :type="roleType(row.roleName)" size="small">{{ row.roleName }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column prop="account" label="账号" width="140" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }"><el-tag :type="row.status ? 'success' : 'info'" size="small">{{ row.status ? '在职' : '离职' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="showPerm(row)">权限</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" header="角色权限矩阵">
          <el-table :data="roles" border>
            <el-table-column prop="name" label="角色" width="120" />
            <el-table-column label="权限点">
              <template #default="{ row }">
                <template v-if="permList(row.permissions).includes('*')">
                  <el-tag type="danger" size="small">全部权限</el-tag>
                </template>
                <template v-else>
                  <el-tag v-for="p in permList(row.permissions)" :key="p" size="small" effect="plain" class="perm">{{ p }}</el-tag>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <el-alert class="mt" type="info" :closable="false" title="权限控制说明">
            按钮级权限通过 v-permission 指令 + $perms() 实现；超级管理员(老板)拥有全部权限，其余角色按上表细粒度控制。
          </el-alert>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="permVisible" title="员工权限详情" width="420px">
      <template v-if="activeEmp">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">{{ activeEmp.name }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ activeEmp.roleName }}</el-descriptions-item>
          <el-descriptions-item label="权限点">
            <el-tag v-for="p in activePerms" :key="p" size="small" effect="plain" class="perm">{{ p }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listEmployees, listRoles, type EmployeeVO, type Role } from '@/api/employee'

const employees = ref<EmployeeVO[]>([])
const roles = ref<Role[]>([])
const loading = ref(false)
const permVisible = ref(false)
const activeEmp = ref<EmployeeVO | null>(null)
const activePerms = ref<string[]>([])

function permList(permissions: string): string[] {
  if (!permissions) return []
  return permissions.split(',').map(s => s.trim()).filter(Boolean)
}
function roleType(role: string): any {
  return role === '超级管理员' ? 'danger' : role === '店长' ? 'warning' : role === '服务员' ? 'success' : 'info'
}
function showPerm(emp: EmployeeVO) {
  activeEmp.value = emp
  const r = roles.value.find(x => x.name === emp.roleName)
  activePerms.value = r ? permList(r.permissions) : []
  permVisible.value = true
}
onMounted(async () => {
  loading.value = true
  try {
    const [e, r] = await Promise.all([listEmployees(), listRoles()])
    employees.value = e; roles.value = r
  } finally { loading.value = false }
})
</script>

<style scoped>
.perm { margin: 2px 4px 2px 0; }
.mt { margin-top: 14px; }
</style>
