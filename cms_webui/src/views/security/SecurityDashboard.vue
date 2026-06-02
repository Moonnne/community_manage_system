<template>
  <div class="page-container dashboard-page">
    <div class="page-header">
      <h2 class="page-title">社区安全面板</h2>
      <el-segmented v-model="range" :options="rangeOptions" @change="fetchData(true)" />
    </div>

    <div class="stat-grid security-stat-grid">
      <el-card shadow="never" class="stat-card">
        <el-statistic title="今日访客" :value="summary.todayVisitorCount || 0" suffix="人" />
        <p :class="['trend-text', trendClass(summary.visitorChange)]">较昨日 {{ signed(summary.visitorChange) }}%</p>
      </el-card>
      <el-card shadow="never" class="stat-card warning">
        <el-statistic title="滞留访客" :value="summary.stayingVisitorCount || 0" suffix="人" />
        <p class="trend-text">需关注仍未离开的访客</p>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <el-statistic title="车辆进出" :value="summary.carFlowCount || 0" suffix="次" />
        <p :class="['trend-text', trendClass(summary.carChange)]">较昨日 {{ signed(summary.carChange) }}%</p>
      </el-card>
    </div>

    <div class="flow-chart-grid">
      <el-card shadow="never" class="panel-card chart-card">
        <template #header>
          <div class="panel-header">
            <strong>访客分时段流量</strong>
            <span>访客登记趋势</span>
          </div>
        </template>
        <v-chart class="flow-chart" :option="visitorChartOption" autoresize />
      </el-card>

      <el-card shadow="never" class="panel-card chart-card">
        <template #header>
          <div class="panel-header">
            <strong>车辆分时段流量</strong>
            <span>车辆通行趋势</span>
          </div>
        </template>
        <v-chart class="flow-chart" :option="carChartOption" autoresize />
      </el-card>
    </div>
    <div class="quick-grid">
      <el-card shadow="never" class="panel-card">
        <template #header><strong>快速变更访客状态</strong></template>
        <el-select v-model="visitorForm.id" filterable placeholder="选择访客" class="full-width">
          <el-option
            v-for="item in visitorOptions"
            :key="item.id"
            :label="`${formatValue(item.visitorName)} / ${formatValue(item.visitorCode)}`"
            :value="item.id"
          />
        </el-select>
        <el-select v-model="visitorForm.status" placeholder="访客状态" class="quick-input">
          <el-option label="已结束" value="0" />
          <el-option label="待批准" value="1" />
          <el-option label="访问中" value="2" />
          <el-option label="已驳回" value="3" />
        </el-select>
        <el-button type="primary" :disabled="!visitorForm.id || !visitorForm.status" @click="updateVisitorStatus">保存状态</el-button>
      </el-card>

      <el-card shadow="never" class="panel-card">
        <template #header><strong>快速变更车辆状态</strong></template>
        <el-select
          v-model="carForm.id"
          filterable
          placeholder="按车牌号搜索车辆"
          class="full-width"
          :filter-method="handleCarFilter"
        >
          <el-option
            v-for="item in filteredCarOptions"
            :key="item.id"
            :label="`${formatValue(item.carNumber)} / ${formatValue(item.carCode)}`"
            :value="item.id"
          />
        </el-select>
        <el-select v-model="carForm.status" placeholder="车辆状态" class="quick-input">
          <el-option label="已入场" :value="0" />
          <el-option label="已离场" :value="1" />
        </el-select>
        <el-button type="primary" :disabled="!carForm.id || carForm.status === ''" @click="updateCarStatus">保存状态</el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { getSecurityDashboard } from '@/api/dashboard'
import { getVisitorPage, updateVisitor } from '@/api/visitor'
import { getCarPage, updateCar } from '@/api/car'

use([CanvasRenderer, LineChart, GridComponent, LegendComponent, TooltipComponent])

const range = ref('1d')
const rangeOptions = [
  { label: '1h', value: '1h' },
  { label: '3h', value: '3h' },
  { label: '6h', value: '6h' },
  { label: '1d', value: '1d' },
  { label: '3d', value: '3d' }
]
const summary = ref({ flow: [] })
const visitorOptions = ref([])
const carOptions = ref([])
const carKeyword = ref('')
const visitorForm = ref({ id: '', status: '' })
const carForm = ref({ id: '', status: '' })
const filteredCarOptions = computed(() => {
  const keyword = carKeyword.value.trim().toLowerCase()
  if (!keyword) return carOptions.value
  return carOptions.value.filter(item => String(item.carNumber || '').toLowerCase().includes(keyword))
})

function createFlowOption(name, color, data) {
  const flow = summary.value.flow || []
  return {
    color: [color],
    tooltip: { trigger: 'axis' },
    grid: { left: 24, right: 18, top: 18, bottom: 28, containLabel: true },
    xAxis: { type: 'category', data: flow.map(item => item.label), boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ name, type: 'line', smooth: true, areaStyle: { opacity: 0.08 }, data }]
  }
}

const visitorChartOption = computed(() => createFlowOption('访客', '#10b981', (summary.value.flow || []).map(item => item.visitorCount)))
const carChartOption = computed(() => createFlowOption('车辆', '#3b82f6', (summary.value.flow || []).map(item => item.carCount)))

function signed(value) {
  const number = Number(value || 0)
  return number > 0 ? `+${number}` : number
}

function trendClass(value) {
  const number = Number(value || 0)
  if (number > 0) return 'up'
  if (number < 0) return 'down'
  return ''
}

function formatValue(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function handleCarFilter(keyword) {
  carKeyword.value = keyword || ''
}

async function fetchData(forceRefresh = false) {
  const [dashboardRes, visitorRes, carRes] = await Promise.all([
    getSecurityDashboard({ range: range.value }, { forceRefresh }),
    getVisitorPage({ pageNum: 1, pageSize: 100 }, { forceRefresh }),
    getCarPage({ pageNum: 1, pageSize: 100 }, { forceRefresh })
  ])
  summary.value = dashboardRes.data || { flow: [] }
  visitorOptions.value = visitorRes.data?.records || []
  carOptions.value = carRes.data?.records || []
}

async function updateVisitorStatus() {
  await updateVisitor({ id: visitorForm.value.id, visitorStatus: visitorForm.value.status })
  ElMessage.success('访客状态已更新')
  visitorForm.value = { id: '', status: '' }
  fetchData(true)
}

async function updateCarStatus() {
  await updateCar({ id: carForm.value.id, carStatus: carForm.value.status })
  ElMessage.success('车辆状态已更新')
  carForm.value = { id: '', status: '' }
  fetchData(true)
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 22px; font-weight: 800; }
.stat-grid, .quick-grid, .flow-chart-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.quick-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); margin-top: 16px; }
.flow-chart-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.stat-card, .panel-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.stat-card :deep(.el-card__body) { min-height: 118px; }
.stat-card.warning { border-color: rgba(245, 158, 11, 0.35); }
.trend-text { margin: 10px 0 0; color: var(--vp-c-text-3); font-size: 13px; }
.trend-text.up { color: var(--el-color-danger); }
.trend-text.down { color: var(--vp-c-brand-1); }
.panel-card { margin-top: 16px; }
.panel-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.panel-header span { color: var(--vp-c-text-3); font-size: 13px; }
.flow-chart { width: 100%; height: 340px; }
.full-width { width: 100%; }
.quick-input { margin: 12px 0; }
@media (max-width: 768px) {
  .page-header, .panel-header { align-items: flex-start; flex-direction: column; }
  .stat-grid, .quick-grid, .flow-chart-grid { grid-template-columns: 1fr; }
  .flow-chart { height: 280px; }
}
</style>
