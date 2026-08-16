import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { registerPermission } from './utils/permission'
import { useUserStore } from './store/user'
import './permission'
import './styles/index.css'

const app = createApp(App)

// 全量注册 Element Plus 图标，方便菜单/按钮直接使用
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component as any)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
registerPermission(app)
// 模板内权限判断辅助：$perms('menu:edit')；admin 角色拥有全部权限
app.config.globalProperties.$perms = (perm: string) => useUserStore().hasPermission(perm)
app.mount('#app')
