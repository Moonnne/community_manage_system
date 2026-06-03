<template>
  <div class="page-container asset-dashboard">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的资产</h2>
        <p class="page-subtitle">查看当前账号拥有的房产、车位等资产信息</p>
      </div>
      <el-input v-model="keyword" placeholder="按资产名搜索" clearable class="search-input" @input="fetchData(true)">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <section class="summary-section">
      <el-card shadow="never" class="summary-card">
        <el-statistic title="资产数量" :value="summary.assetCount || 0" suffix="项" />
      </el-card>
      <el-card shadow="never" class="summary-card warning">
        <el-statistic title="即将过期" :value="summary.expiringCount || 0" suffix="项" />
      </el-card>
      <el-card shadow="never" class="summary-card danger">
        <el-statistic title="已过期" :value="summary.expiredCount || 0" suffix="项" />
      </el-card>
    </section>

    <section class="asset-section">
      <div class="section-title-row">
        <h3>资产卡片</h3>
        <span>共 {{ assets.length }} 项</span>
      </div>
      <div v-if="assets.length > 0" class="asset-card-grid">
        <el-card v-for="asset in assets" :key="asset.id" shadow="never" class="asset-card" @click="openDetail(asset)">
          <el-icon class="asset-watermark"><House /></el-icon>
          <div class="asset-card-top">
            <strong>{{ formatValue(asset.assetCode) }}</strong>
            <el-tag :type="assetStatusType(asset.assetStatus)" effect="plain">{{ assetStatusText(asset.assetStatus) }}</el-tag>
          </div>
          <h3>{{ formatValue(asset.assetName) }}</h3>
          <p class="asset-type">{{ formatValue(asset.assetTypeName) }}</p>
          <div class="asset-detail-grid">
            <span>楼栋</span><strong>{{ formatBuilding(asset) }}</strong>
            <span>楼层</span><strong>{{ formatValue(asset.assetFloor) }}</strong>
            <span>位置</span><strong>{{ formatValue(asset.assetPosition) }}</strong>
            <span>登记</span><strong>{{ formatDate(asset.assetRegTime) }}</strong>
            <span>过期</span><strong>{{ formatDate(asset.assetDueTime, '无过期时间') }}</strong>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无资产" />
    </section>

    <el-dialog v-model="detailVisible" width="760px" class="rounded-dialog" title="资产详情">
      <el-descriptions v-if="currentAsset" :column="2">
        <el-descriptions-item label="资产编号">{{ formatValue(currentAsset.assetCode) }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ formatValue(currentAsset.assetName) }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ formatValue(currentAsset.assetTypeName) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="assetStatusType(currentAsset.assetStatus)" effect="plain">{{ assetStatusText(currentAsset.assetStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="楼栋">{{ formatBuilding(currentAsset) }}</el-descriptions-item>
        <el-descriptions-item label="楼层">{{ formatValue(currentAsset.assetFloor) }}</el-descriptions-item>
        <el-descriptions-item label="具体位置" :span="2">{{ formatValue(currentAsset.assetPosition) }}</el-descriptions-item>
        <el-descriptions-item label="面积">{{ formatValue(currentAsset.assetArea) }}</el-descriptions-item>
        <el-descriptions-item label="业主">{{ formatValue(currentAsset.ownerName) }}</el-descriptions-item>
        <el-descriptions-item label="登记时间">{{ formatValue(currentAsset.assetRegTime) }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">{{ formatValue(currentAsset.assetDueTime) }}</el-descriptions-item>
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

function formatDate(value, emptyText = '-') {
  return value ? String(value).slice(0, 10) : emptyText
}

function formatBuilding(asset) {
  if (!asset) return '-'
  if (asset.buildingCode || asset.buildingName) {
    return `${asset.buildingCode || ''} ${asset.buildingName || ''}`.trim()
  }
  return formatValue(asset.assetBuilding)
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
.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 26px; font-weight: 800; }
.page-subtitle { margin: 8px 0 0; color: var(--vp-c-text-3); font-size: 14px; }
.search-input { width: 320px; }
.summary-section { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 24px; padding-bottom: 24px; border-bottom: 1px solid var(--vp-c-divider); }
.summary-card, .asset-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.summary-card :deep(.el-card__body) { min-height: 118px; display: flex; align-items: center; }
.summary-card.warning { border-color: rgba(245, 158, 11, 0.35); background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), var(--vp-card-bg) 62%); }
.summary-card.danger { border-color: rgba(239, 68, 68, 0.35); background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), var(--vp-card-bg) 62%); }
.section-title-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.section-title-row h3 { margin: 0; color: var(--vp-c-text-1); font-size: 20px; font-weight: 800; }
.section-title-row span { color: var(--vp-c-text-3); font-size: 13px; }
.asset-card-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.asset-card { position: relative; min-height: 270px; overflow: hidden; cursor: pointer; background: linear-gradient(135deg, rgba(16, 185, 129, 0.12), var(--vp-card-bg) 62%); transition: transform 0.2s, box-shadow 0.2s; }
.asset-card:hover { transform: translateY(-2px); box-shadow: var(--el-box-shadow-light); }
.asset-card :deep(.el-card__body) { position: relative; z-index: 1; }
.asset-watermark { position: absolute; right: 22px; bottom: 18px; z-index: 0; color: rgba(16, 185, 129, 0.1); font-size: 104px; }
.asset-card-top { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.asset-card-top strong { color: var(--vp-c-text-1); font-size: 18px; }
.asset-card h3 { margin: 22px 0 8px; color: var(--vp-c-text-1); font-size: 24px; line-height: 1.25; }
.asset-type { margin: 0 0 20px; color: var(--vp-c-text-2); font-size: 15px; }
.asset-detail-grid { display: grid; grid-template-columns: 52px minmax(0, 1fr); gap: 9px 12px; color: var(--vp-c-text-3); font-size: 14px; }
.asset-detail-grid strong { min-width: 0; color: var(--vp-c-text-1); font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
:deep(.rounded-dialog .el-dialog) { border-radius: 16px; }
@media (max-width: 1180px) {
  .asset-card-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 768px) {
  .page-header { flex-direction: column; }
  .search-input { width: 100%; }
  .summary-section, .asset-card-grid { grid-template-columns: 1fr; }
  :deep(.rounded-dialog .el-dialog) { width: calc(100vw - 24px) !important; }
}
</style>
