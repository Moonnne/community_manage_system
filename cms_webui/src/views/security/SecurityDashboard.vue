<template>
  <div class="page-container dashboard-page">
    <div class="page-header">
      <h2 class="page-title">社区安全面板</h2>
    </div>

    <div class="stat-grid security-stat-grid">
      <el-card shadow="never" class="stat-card">
        <el-statistic title="今日访客" :value="summary.todayVisitorCount || 0" suffix="人" />
        <p :class="['trend-text', trendClass(summary.visitorChange)]">较昨日 {{ signed(summary.visitorChange) }}%</p>
      </el-card>
      <el-card shadow="never" class="stat-card warning">
        <el-statistic title="滞留访客" :value="summary.stayingVisitorCount || 0" suffix="人" />
        <p class="trend-text">登记进入但未登记离开的访客</p>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <el-statistic title="车辆进出" :value="summary.carFlowCount || 0" suffix="次" />
        <p :class="['trend-text', trendClass(summary.carChange)]">较昨日 {{ signed(summary.carChange) }}%</p>
      </el-card>
    </div>

    <div class="chart-toolbar">
      <div>
        <strong>时间聚焦</strong>
        <span>调整访客与车辆流量的统计图表显示范围</span>
      </div>
      <el-segmented v-model="range" :options="rangeOptions" @change="fetchData(true)" />
    </div>

    <div class="flow-chart-grid">
      <el-card shadow="never" class="panel-card chart-card">
        <template #header>
          <div class="panel-header">
            <strong>访客分时段流量</strong>
            <span>访客通行趋势</span>
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
      <el-card shadow="never" class="panel-card quick-card">
        <template #header><strong>快速变更访客状态</strong></template>
        <div class="quick-card-content">
          <DualSelectV2
            v-model="visitorForm.id"
            :options="visibleVisitorOptions"
            :filters="visitorFilters"
            remote
            :remote-method="handleVisitorSearch"
            primary-key="visitorName"
            secondary-key="visitorCode"
            placeholder="按访客姓名或编号搜索"
            @filter-change="handleVisitorFilterChange"
          />
          <div v-if="visitorActions.length" class="quick-actions" :style="visitorActionStyle">
            <el-button
              v-for="action in visitorActions"
              :key="action.status"
              :type="action.type"
              :plain="action.plain"
              @click="changeVisitorStatus(action.status)"
            >
              {{ action.label }}
            </el-button>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="panel-card quick-card">
        <template #header><strong>快速变更车辆状态</strong></template>
        <div class="quick-card-content">
          <DualSelectV2
            v-model="carForm.id"
            :options="visibleCarOptions"
            :filters="carFilters"
            remote
            :remote-method="handleCarSearch"
            primary-key="carNumber"
            secondary-key="carCode"
            placeholder="按车牌号、业主或车辆编号搜索"
            @filter-change="handleCarFilterChange"
          />
          <el-select v-model="carForm.gateId" placeholder="选择门岗" class="quick-input full-width" clearable>
            <el-option v-for="gate in gateOptions" :key="gate.id" :label="`${gate.gateName}`" :value="gate.id" />
          </el-select>
          <div v-if="selectedCar" class="quick-actions" :style="carActionStyle">
            <el-button type="primary" :disabled="!carForm.gateId" @click="toggleCarStatus">
              {{ selectedCar.carStatus === 0 ? '车辆离场' : '车辆入场' }}
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useTransition } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { getSecurityDashboard } from '@/api/dashboard'
import { getVisitorPage, getVisitorTypes, updateVisitor } from '@/api/visitor'
import { addCarHistory, getCarPage, getCarTypes } from '@/api/car'
import { getGateList } from '@/api/gate'
import DualSelectV2 from '@/components/DualSelectV2.vue'

use([CanvasRenderer, LineChart, GridComponent, LegendComponent, TooltipComponent])

const range = ref('1d')
const rangeOptions = [
  { label: '1h', value: '1h' },
  { label: '3h', value: '3h' },
  { label: '6h', value: '6h' },
  { label: '1天', value: '1d' },
  { label: '3天', value: '3d' }
]
const summary = ref({ flow: [] })
const visitorOptions = ref([])
const carOptions = ref([])
const visitorTypeOptions = ref([])
const carTypeOptions = ref([])
const gateOptions = ref([])
const visitorKeyword = ref('')
const carKeyword = ref('')
const visitorFilterValues = ref({ visitorType: undefined, visitorStatus: undefined })
const carFilterValues = ref({ carType: undefined, carStatus: undefined })
const visitorForm = ref({ id: '' })
const carForm = ref({ id: '', gateId: '' })
const selectedVisitor = computed(() => visitorOptions.value.find(item => item.id === visitorForm.value.id))
const selectedCar = computed(() => carOptions.value.find(item => item.id === carForm.value.id))
const visitorActions = computed(() => {
  if (selectedVisitor.value?.visitorStatus === '1') {
    return [
      { label: '批准', status: '2', type: 'primary' },
      { label: '驳回', status: '3', type: 'danger', plain: true }
    ]
  }
  if (selectedVisitor.value?.visitorStatus === '2') {
    return [{ label: '结束访问', status: '0', type: 'warning' }]
  }
  return []
})
const visitorActionSource = ref(0)
const carActionSource = ref(0)
const visitorActionProgress = useTransition(visitorActionSource, { duration: 240 })
const carActionProgress = useTransition(carActionSource, { duration: 240 })
const visitorActionStyle = computed(() => actionTransitionStyle(visitorActionProgress.value))
const carActionStyle = computed(() => actionTransitionStyle(carActionProgress.value))
const visitorFilters = computed(() => [
  { key: 'visitorType', placeholder: '访客分类', options: visitorTypeOptions.value.map(item => ({ label: item.visitorTypeName, value: item.visitorTypeId })) },
  { key: 'visitorStatus', placeholder: '访客状态', options: [
    { label: '已结束', value: '0' },
    { label: '待批准', value: '1' },
    { label: '访问中', value: '2' },
    { label: '已驳回', value: '3' }
  ] }
])
const carFilters = computed(() => [
  { key: 'carType', placeholder: '车辆类型', options: carTypeOptions.value.map(item => ({ label: item.carTypeName, value: item.carTypeId })) },
  { key: 'carStatus', placeholder: '车辆状态', options: [
    { label: '已入场', value: 0 },
    { label: '已离场', value: 1 }
  ] }
])
const visibleVisitorOptions = computed(() => {
  const keyword = visitorKeyword.value.trim().toLowerCase()
  const filtered = visitorOptions.value.filter(item => (
    (!keyword || [item.visitorName, item.visitorCode].some(value => String(value || '').toLowerCase().includes(keyword)))
    && (visitorFilterValues.value.visitorType === undefined || item.visitorType === visitorFilterValues.value.visitorType)
    && (visitorFilterValues.value.visitorStatus === undefined || item.visitorStatus === visitorFilterValues.value.visitorStatus)
  ))
  return filtered.length
    ? filtered.map(item => ({ ...item, value: item.id, label: item.visitorName }))
    : [{ value: '__empty_visitors__', label: '暂无匹配访客', disabled: true, empty: true }]
})
const visibleCarOptions = computed(() => {
  const keyword = carKeyword.value.trim().toLowerCase()
  const filtered = carOptions.value.filter(item => (
    (!keyword || [item.carNumber, item.ownerName, item.carCode].some(value => String(value || '').toLowerCase().includes(keyword)))
    && (carFilterValues.value.carType === undefined || item.carType === carFilterValues.value.carType)
    && (carFilterValues.value.carStatus === undefined || item.carStatus === carFilterValues.value.carStatus)
  ))
  return filtered.length
    ? filtered.map(item => ({ ...item, value: item.id, label: item.carNumber }))
    : [{ value: '__empty_cars__', label: '暂无匹配车辆', disabled: true, empty: true }]
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

function actionTransitionStyle(progress) {
  return {
    opacity: progress,
    transform: `translateY(${Math.round((1 - progress) * 8)}px)`
  }
}

async function restartActionTransition(source, visible) {
  source.value = 0
  await nextTick()
  source.value = visible ? 1 : 0
}

function handleVisitorSearch(keyword) {
  visitorKeyword.value = keyword || ''
}

function handleVisitorFilterChange(filters) {
  visitorFilterValues.value = filters
}

function handleCarSearch(keyword) {
  carKeyword.value = keyword || ''
}

function handleCarFilterChange(filters) {
  carFilterValues.value = filters
}

async function fetchData(forceRefresh = false) {
  const [dashboardRes, visitorRes, carRes, visitorTypeRes, carTypeRes, gateRes] = await Promise.all([
    getSecurityDashboard({ range: range.value }, { forceRefresh }),
    getVisitorPage({ pageNum: 1, pageSize: 100 }, { forceRefresh }),
    getCarPage({ pageNum: 1, pageSize: 100 }, { forceRefresh }),
    getVisitorTypes({ forceRefresh }),
    getCarTypes({ forceRefresh }),
    getGateList({ forceRefresh })
  ])
  summary.value = dashboardRes.data || { flow: [] }
  visitorOptions.value = visitorRes.data?.records || []
  carOptions.value = carRes.data?.records || []
  visitorTypeOptions.value = visitorTypeRes.data || []
  carTypeOptions.value = carTypeRes.data || []
  gateOptions.value = gateRes.data || []
}

async function changeVisitorStatus(status) {
  await updateVisitor({ id: visitorForm.value.id, visitorStatus: status })
  ElMessage.success('访客状态已更新')
  visitorForm.value = { id: '' }
  fetchData(true)
}

async function toggleCarStatus() {
  const historyType = selectedCar.value?.carStatus === 0 ? 1 : 0
  await addCarHistory({ carId: carForm.value.id, gateId: carForm.value.gateId, carHistoryType: historyType })
  ElMessage.success('车辆状态已更新')
  carForm.value = { id: '', gateId: '' }
  fetchData(true)
}

watch(() => [visitorForm.value.id, selectedVisitor.value?.visitorStatus], () => {
  restartActionTransition(visitorActionSource, visitorActions.value.length > 0)
})
watch(() => [carForm.value.id, selectedCar.value?.carStatus], () => {
  restartActionTransition(carActionSource, !!selectedCar.value)
})

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 22px; font-weight: 800; }
.chart-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin: 16px 0 0; padding: 14px 16px; border: 1px solid var(--vp-c-divider); border-radius: 8px; background: var(--vp-c-bg-soft); }
.chart-toolbar div { display: flex; flex-direction: column; gap: 4px; }
.chart-toolbar strong { color: var(--vp-c-text-1); font-size: 16px; }
.chart-toolbar span { color: var(--vp-c-text-3); font-size: 13px; }
.chart-toolbar :deep(.el-segmented) {
  --el-segmented-bg-color: var(--vp-c-bg);
  --el-segmented-item-selected-color: #fff;
  --el-segmented-item-selected-bg-color: var(--vp-c-brand-1);
  --el-segmented-item-hover-bg-color: var(--vp-c-brand-soft);
  min-width: 280px;
  padding: 4px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  box-shadow: none;
}
.chart-toolbar :deep(.el-segmented__group) { gap: 2px; }
.chart-toolbar :deep(.el-segmented__item) {
  height: 34px;
  min-width: 48px;
  border-radius: 6px;
  color: var(--vp-c-text-2);
  font-weight: 700;
}
.chart-toolbar :deep(.el-segmented__item.is-selected) { color: #fff; }
.chart-toolbar :deep(.el-segmented__item-label) { line-height: 34px; }
.stat-grid, .quick-grid, .flow-chart-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.quick-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); align-items: stretch; margin-top: 16px; }
.quick-grid > .quick-card { margin-top: 16px; }
.quick-card-content { display: grid; grid-template-rows: auto auto auto; align-content: start; }
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
.quick-actions { display: flex; align-items: center; gap: 8px; margin-top: 12px; min-height: 32px; will-change: opacity, transform; }
.quick-empty { color: var(--vp-c-text-3); font-size: 13px; }
@media (max-width: 768px) {
  .page-header, .panel-header, .chart-toolbar { align-items: flex-start; flex-direction: column; }
  .chart-toolbar :deep(.el-segmented) { min-width: 0; width: 100%; }
  .stat-grid, .quick-grid, .flow-chart-grid { grid-template-columns: 1fr; }
  .flow-chart { height: 280px; }
}
</style>
