<template>
  <div class="page-container asset-dashboard">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的资产</h2>
        <p class="page-subtitle">查看当前账号关联的房产、车位等资产信息</p>
      </div>
      <el-input v-model="keyword" placeholder="按资产名搜索" clearable class="search-input" @input="fetchData(true)">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <div class="asset-summary-grid">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="资产数量" :value="summary.assetCount || 0" suffix="项" />
      </el-card>
      <el-card shadow="never" class="summary-card warning">
        <el-statistic title="即将过期" :value="summary.expiringCount || 0" suffix="项" />
      </el-card>
      <el-card shadow="never" class="summary-card danger">
        <el-statistic title="已过期" :value="summary.expiredCount || 0" suffix="项" />
      </el-card>
    </div>

    <div v-if="assets.length > 0" class="asset-card-grid">
      <el-card v-for="asset in assets" :key="asset.id" shadow="never" class="asset-card" @click="openDetail(asset)">
        <el-icon class="asset-watermark"><House /></el-icon>
        <div class="asset-card-top">
          <span>{{ formatValue(asset.assetCode) }}</span>
          <el-tag :type="assetStatusType(asset.assetStatus)" effect="plain">{{ assetStatusText(asset.assetStatus) }}</el-tag>
        </div>
        <h3>{{ formatValue(asset.assetName) }}</h3>
        <p>{{ formatValue(asset.assetTypeName) }}</p>
        <div class="asset-card-meta">
          <span>{{ formatValue(asset.assetBuilding) }}栋 {{ formatValue(asset.assetFloor) }}层</span>
          <strong>{{ formatValue(asset.assetDueTime) }}</strong>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无资产" />

    <el-dialog v-model="detailVisible" width="720px" class="rounded-dialog" title="资产详情">
      <el-descriptions v-if="currentAsset" :column="2">
        <el-descriptions-item label="资产编号">{{ formatValue(currentAsset.assetCode) }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ formatValue(currentAsset.assetName) }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ formatValue(currentAsset.assetTypeName) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="assetStatusType(currentAsset.assetStatus)" effect="plain">{{ assetStatusText(currentAsset.assetStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="业主">{{ formatValue(currentAsset.ownerName) }}</el-descriptions-item>
        <el-descriptions-item label="面积">{{ formatValue(currentAsset.assetArea) }}</el-descriptions-item>
        <el-descriptions-item label="位置" :span="2">{{ formatValue(currentAsset.assetPosition) }}</el-descriptions-item>
        <el-descriptions-item label="到期时间" :span="2">{{ formatValue(currentAsset.assetDueTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ formatValue(currentAsset.assetRemark) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getAssetDashboard } from '@/api/dashboard'

const keyword = ref('')
const summary = ref({})
const assets = ref([])
const detailVisible = ref(false)
const currentAsset = ref(null)

function formatValue(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function assetStatusText(status) {
  return ({ 0: '正常', 1: '过期', 2: '未使用', 3: '冻结' })[status] || '未知'
}

function assetStatusType(status) {
  return ({ 0: 'success', 1: 'danger', 2: 'info', 3: 'warning' })[status] || 'info'
}

async function fetchData(forceRefresh = false) {
  const res = await getAssetDashboard({ assetName: keyword.value }, { forceRefresh })
  summary.value = res.data || {}
  assets.value = res.data?.assets || []
}

function openDetail(asset) {
  currentAsset.value = asset
  detailVisible.value = true
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 22px; font-weight: 800; }
.page-subtitle { margin: 8px 0 0; color: var(--vp-c-text-3); font-size: 13px; }
.search-input { width: 320px; }
.asset-summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 18px; }
.summary-card, .asset-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.summary-card.warning { border-color: rgba(245, 158, 11, 0.35); }
.summary-card.danger { border-color: rgba(239, 68, 68, 0.35); }
.asset-card-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.asset-card { position: relative; min-height: 190px; overflow: hidden; cursor: pointer; background: linear-gradient(135deg, rgba(16, 185, 129, 0.12), var(--vp-card-bg) 62%); }
.asset-card :deep(.el-card__body) { position: relative; z-index: 1; }
.asset-watermark { position: absolute; right: 22px; bottom: 18px; z-index: 0; color: rgba(16, 185, 129, 0.12); font-size: 92px; }
.asset-card-top, .asset-card-meta { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.asset-card-top span { color: var(--vp-c-text-3); font-size: 13px; }
.asset-card h3 { margin: 22px 0 8px; color: var(--vp-c-text-1); font-size: 22px; }
.asset-card p { margin: 0; color: var(--vp-c-text-2); }
.asset-card-meta { margin-top: 30px; color: var(--vp-c-text-3); font-size: 13px; }
.asset-card-meta strong { color: var(--vp-c-text-1); }
:deep(.rounded-dialog .el-dialog) { border-radius: 16px; }
@media (max-width: 980px) {
  .asset-card-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 768px) {
  .page-header { flex-direction: column; }
  .search-input { width: 100%; }
  .asset-summary-grid, .asset-card-grid { grid-template-columns: 1fr; }
  :deep(.rounded-dialog .el-dialog) { width: calc(100vw - 24px) !important; }
}
</style>
