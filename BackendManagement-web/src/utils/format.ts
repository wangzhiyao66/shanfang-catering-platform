// 金额统一以「分」存储/传输；展示时 ÷100。
export function yuan(fen: number | undefined | null): string {
  return ((fen || 0) / 100).toFixed(2)
}
/** 图表轴用的精简金额：满 1 万显示「万」 */
export function yuanShort(fen: number | undefined | null): string {
  const y = (fen || 0) / 100
  if (y >= 10000) return (y / 10000).toFixed(1) + '万'
  return String(Math.round(y))
}
