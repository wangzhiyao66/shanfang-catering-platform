<template>
  <el-container class="layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="aside">
      <div class="logo">{{ appStore.sidebarCollapsed ? '膳' : '膳房·商家后台' }}</div>
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        background-color="#1f2329"
        text-color="#c9cdd4"
        active-text-color="#ff7a59"
        router
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="left">
          <el-icon class="pointer" @click="appStore.toggleSidebar()">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-select v-model="shopStore.currentShopId" size="small" placeholder="门店" style="width: 150px; margin-left: 12px">
            <el-option :value="1" label="总店（演示）" />
          </el-select>
        </div>
        <div class="right">
          <el-dropdown @command="onCommand">
            <span class="user">{{ userStore.name || '管理员' }}<el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { useShopStore } from '@/store/shop'
import { constantRoutes } from '@/router'

const appStore = useAppStore()
const userStore = useUserStore()
const shopStore = useShopStore()
const route = useRoute()

const menus = computed(() => {
  const root = constantRoutes.find((r) => r.path === '/')
  const children = (root?.children || []).filter((c) => c.meta?.title)
  return children.map((c) => ({
    path: '/' + c.path,
    title: c.meta?.title as string,
    icon: (c.meta?.icon as string) || 'Menu'
  }))
})

const activeMenu = computed(() => route.path)

function onCommand(cmd: string) {
  if (cmd === 'logout') userStore.logout()
}
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #1f2329; transition: width 0.2s; overflow: hidden; }
.logo { height: 56px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700; font-size: 18px; letter-spacing: 1px; white-space: nowrap; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #e8eaed; }
.left { display: flex; align-items: center; }
.pointer { cursor: pointer; font-size: 20px; color: #5b6168; }
.right { display: flex; align-items: center; }
.user { cursor: pointer; display: flex; align-items: center; gap: 4px; color: #1f2329; }
.main { background: #f5f6f8; padding: 16px; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
