import { defineStore } from 'pinia'
import { ref } from 'vue'

// 当前门店（租户）：多店时由门店切换器设置，并自动注入 X-Shop-Id 请求头
export const useShopStore = defineStore('shop', () => {
  const currentShopId = ref<number>(1) // 演示门店；多店预留
  function setShop(id: number) {
    currentShopId.value = id
  }
  return { currentShopId, setShop }
})
