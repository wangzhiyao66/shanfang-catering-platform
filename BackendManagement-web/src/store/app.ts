import { defineStore } from 'pinia'
import { ref } from 'vue'

// 应用级 UI 状态：侧边栏折叠等
export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
  return { sidebarCollapsed, toggleSidebar }
})
