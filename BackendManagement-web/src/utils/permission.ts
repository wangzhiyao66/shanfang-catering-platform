import type { Directive, App } from 'vue'
import { useUserStore } from '@/store/user'

// 按钮级权限指令：v-permission="'menu:edit'"
// 无权限则直接从 DOM 移除该元素（敏感操作如退菜/改账）
export const permissionDirective: Directive<HTMLElement, string> = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const need = binding.value as string
    if (!userStore.hasPermission(need)) {
      el.parentNode?.removeChild(el)
    }
  }
}

export function registerPermission(app: App): void {
  app.directive('permission', permissionDirective)
}
