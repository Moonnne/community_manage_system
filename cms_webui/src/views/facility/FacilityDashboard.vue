<template>
  <div class="page-container facility-dashboard">
    <div class="page-header">
      <h2 class="page-title">公共设施面板</h2>
      <el-button @click="fetchData(true)">
        <el-icon><Refresh /></el-icon>刷新
      </el-button>
    </div>

    <div class="facility-stat-grid">
      <el-card shadow="never" class="facility-stat-card">
        <el-statistic title="登记设施" :value="summary.registeredCount || 0" suffix="项" />
        <p>当前系统登记的公共设施数量</p>
      </el-card>
      <el-card shadow="never" class="facility-stat-card warning">
        <el-statistic title="临检设施" :value="summary.nearInspectionCount || 0" suffix="项" />
        <p>一个月内即将到期的设施</p>
      </el-card>
      <el-card shadow="never" class="facility-stat-card warning">
        <el-statistic title="需要检测" :value="summary.needInspectionCount || 0" suffix="项" />
        <p>检查周期已到期的设施</p>
      </el-card>
      <el-card shadow="never" class="facility-stat-card danger">
        <el-statistic title="异常设施" :value="summary.abnormalCount || 0" suffix="项" />
        <p>过期、损坏或缺失的设施</p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getFacilityDashboard } from '@/api/dashboard'

const summary = ref({})

async function fetchData(forceRefresh = false) {
  const res = await getFacilityDashboard({ forceRefresh })
  summary.value = res.data || {}
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 22px; font-weight: 800; }
.facility-stat-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.facility-stat-card { border: 1px solid var(--vp-c-divider); border-radius: 8px; background: var(--vp-card-bg); }
.facility-stat-card :deep(.el-card__body) { min-height: 138px; }
.facility-stat-card p { margin: 14px 0 0; color: var(--vp-c-text-3); font-size: 13px; }
.facility-stat-card.warning { border-color: rgba(245, 158, 11, 0.35); background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), var(--vp-card-bg) 58%); }
.facility-stat-card.danger { border-color: rgba(239, 68, 68, 0.32); background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), var(--vp-card-bg) 58%); }
@media (max-width: 980px) {
  .facility-stat-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 560px) {
  .page-header { align-items: stretch; flex-direction: column; }
  .page-header .el-button { width: 100%; margin-left: 0; }
  .facility-stat-grid { grid-template-columns: 1fr; }
  .facility-stat-card :deep(.el-card__body) { padding: 18px; }
}
</style>
