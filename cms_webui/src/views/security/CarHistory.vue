<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">车辆通行记录</h2></div>
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4"><el-input v-model="query.carNumber" placeholder="车牌号" clearable /></el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <DualSelectV2
            v-model="query.gateId"
            :options="gateOptions"
            remote
            :remote-method="handleGateRemoteSearch"
            placeholder="按门岗编号搜索"
            primary-key="gateCode"
            secondary-key="gateName"
          />
        </el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-select v-model="query.carHistoryType" placeholder="类型" clearable>
            <el-option label="车辆入场" :value="0" />
            <el-option label="车辆离场" :value="1" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="7" :xl="7">
          <el-date-picker v-model="query.timeRange" type="datetimerange" value-format="YYYY-MM-DDTHH:mm:ss" start-placeholder="开始时间" end-placeholder="结束时间" class="full-width" />
        </el-col>
        <el-col :span="5" :xs="24" :sm="24" :md="8" :lg="5" :xl="5">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-col>
      </el-row>
    </el-card>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon> 新增记录</el-button>
      <el-button @click="fetchData(true)">刷新</el-button>
      <el-button class="batch-delete-button" type="danger" plain :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" fixed="left" />
        <el-table-column prop="carHistoryCode" label="记录编号" width="200" sortable show-overflow-tooltip />
        <el-table-column prop="carNumber" label="车牌号" width="120" show-overflow-tooltip />
        <el-table-column prop="gateName" label="门岗" width="120" show-overflow-tooltip />
        <el-table-column label="类型" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="[0, 2].includes(row.carHistoryType) ? 'success' : 'warning'" effect="plain">
              {{ carHistoryTypeText(row.carHistoryType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" show-overflow-tooltip />
        <el-table-column prop="carHistoryTime" label="操作时间" width="200" sortable show-overflow-tooltip />
        <el-table-column prop="carHistoryRemark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该通行记录吗？" confirm-button-text="删除" cancel-button-text="取消" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small" @click.stop="() => {}">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50]" :total="total" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <WizardFormDrawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑通行记录' : '新增通行记录'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="wizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
        <el-form-item label="车辆" prop="carId">
          <DualSelectV2
            v-model="form.carId"
            :options="carOptions"
            :filters="carFilters"
            remote
            :remote-method="handleCarRemoteSearch"
            @filter-change="handleCarFilterChange"
            placeholder="按车牌号搜索车辆"
            primary-key="carNumber"
            secondary-key="carCode"
          />
        </el-form-item>
        <el-form-item label="门岗" prop="gateId">
          <DualSelectV2
            v-model="form.gateId"
            :options="gateOptions"
            placeholder="请选择门岗"
            primary-key="gateName"
            secondary-key="gateCode"
          />
        </el-form-item>
        <el-form-item label="类型" prop="carHistoryType">
          <el-select v-model="form.carHistoryType" placeholder="请选择">
            <el-option label="车辆入场" :value="0" />
            <el-option label="车辆离场" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.carHistoryRemark" type="textarea" :rows="2" /></el-form-item>
        </template>
        <ConfirmInfoTable v-else :rows="confirmRows" />
      </template>
    </WizardFormDrawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCarHistoryPage, addCarHistory, updateCarHistory, deleteCarHistory, batchDeleteCarHistory, getCarPage } from '@/api/car'
import { getGateList } from '@/api/gate'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'
import DualSelectV2 from '@/components/DualSelectV2.vue'
import { useAgentPageControl } from '@/utils/agentControl'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const selectedRows = ref([])
const cars = ref([])
const gates = ref([])
const query = ref({ pageNum: 1, pageSize: 10, carNumber: '', gateId: '', carHistoryType: undefined, timeRange: [] })
const agentQueryKeys = ['carNumber', 'gateId', 'carHistoryType']
const form = ref({})
const wizardSteps = [
  { title: '通行信息', fields: ['carId', 'gateId', 'carHistoryType'] },
  { title: '确认信息', fields: [] }
]
const carFilterValues = ref({ carType: undefined, carStatus: undefined })
const carFilters = computed(() => [
  {
    key: 'carType',
    placeholder: '车辆类型',
    options: Array.from(new Map(cars.value.filter(car => car.carType !== undefined).map(car => [car.carType, {
      label: car.carTypeName || `类型 ${car.carType}`,
      value: car.carType
    }])).values())
  },
  {
    key: 'carStatus',
    placeholder: '车辆状态',
    options: [
      { label: '已入场', value: 0 },
      { label: '已离场', value: 1 }
    ]
  }
])
const confirmRows = computed(() => [
  { label: '车辆', value: valueText(form.value.carId) },
  { label: '门岗', value: valueText(form.value.gateId) },
  { label: '类型', type: 'tag', tagType: [0, 2].includes(form.value.carHistoryType) ? 'success' : 'warning', value: carHistoryTypeText(form.value.carHistoryType) },
  { label: '备注', value: valueText(form.value.carHistoryRemark) }
])
const formRules = {
  carId: [{ required: true, message: '请输入车辆ID', trigger: 'blur' }],
  gateId: [{ required: true, message: '请输入门岗ID', trigger: 'blur' }],
  carHistoryType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}
const carOptions = computed(() => {
  const filtered = cars.value.filter(car => (
    (carFilterValues.value.carType === undefined || car.carType === carFilterValues.value.carType)
    && (carFilterValues.value.carStatus === undefined || car.carStatus === carFilterValues.value.carStatus)
  ))
  return filtered.length
    ? filtered.map(car => ({ value: car.id, label: car.carNumber || String(car.id), carNumber: car.carNumber || String(car.id), carCode: car.carCode || '-' }))
    : [{ value: '__empty_cars__', carNumber: '暂无匹配车辆', carCode: '', disabled: true, empty: true }]
})
const gateOptions = computed(() => gates.value.length ? gates.value.map(gate => ({ value: gate.id, label: gate.gateCode || gate.gateName || String(gate.id), gateCode: gate.gateCode || '-', gateName: gate.gateName || '-' })) : [{ value: '__empty_gates__', label: '暂无门岗', gateCode: '', gateName: '', disabled: true, empty: true }])

function valueText(value) { return value === '' || value === undefined || value === null ? '-' : value }

function carHistoryTypeText(type) {
  return ['车辆入场', '车辆离场'][type] || '未知'
}

async function fetchData(forceRefresh = false) { loading.value = true; try { const p = { ...query.value }; if (p.timeRange?.length === 2) { p.rangeStart = p.timeRange[0]; p.rangeEnd = p.timeRange[1] }; delete p.timeRange; Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] }); const res = await getCarHistoryPage(p, { forceRefresh: !!forceRefresh }); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
async function fetchCars(keyword = '') { const res = await getCarPage({ pageNum: 1, pageSize: 100 }, { forceRefresh: true }); const list = res.data?.records || []; const lower = String(keyword || '').toLowerCase(); cars.value = lower ? list.filter(car => [car.carNumber, car.carCode, car.ownerUid, car.ownerName].some(value => String(value || '').toLowerCase().includes(lower))) : list }
async function fetchGates(keyword = '') { const res = await getGateList({ forceRefresh: true }); const list = res.data || []; const lower = String(keyword || '').toLowerCase(); gates.value = lower ? list.filter(gate => String(gate.gateCode || '').toLowerCase().includes(lower)) : list }
function handleCarRemoteSearch(keyword) { fetchCars(keyword) }
function handleCarFilterChange(filters) { carFilterValues.value = filters }
function handleGateRemoteSearch(keyword) { fetchGates(keyword) }
function resetQuery() { query.value = { pageNum: 1, pageSize: 10, carNumber: '', gateId: '', carHistoryType: undefined, timeRange: [] }; fetchData(true) }
function openAdd() { isEdit.value = false; form.value = { carId: '', gateId: '', carHistoryType: undefined, carHistoryRemark: '' }; dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = { id: row.id, carId: row.carId, gateId: row.gateId, carHistoryType: row.carHistoryType, carHistoryRemark: row.carHistoryRemark }; dialogVisible.value = true }
function handleSelectionChange(rows) { selectedRows.value = rows }
async function handleDelete(row) { await deleteCarHistory(row.id); ElMessage.success('删除成功'); fetchData(true) }
async function handleBatchDelete() { if (!selectedRows.value.length) return; await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条通行记录吗？`, '批量删除', { type: 'warning' }); await batchDeleteCarHistory(selectedRows.value.map(row => row.id)); selectedRows.value = []; ElMessage.success('批量删除成功'); fetchData(true) }
async function handleSubmit() { submitLoading.value = true; try { if (isEdit.value) { await updateCarHistory(form.value); ElMessage.success('更新成功') } else { await addCarHistory(form.value); ElMessage.success('添加成功') }; dialogVisible.value = false; fetchData(true) } finally { submitLoading.value = false } }

useAgentPageControl({
  query,
  queryKeys: agentQueryKeys,
  numericKeys: ['gateId', 'carHistoryType'],
  onSearch: () => fetchData(true),
  onRefresh: () => fetchData(true),
  onOpenAdd: () => openAdd()
})

onMounted(() => { fetchData(); fetchCars(); fetchGates() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.search-card { margin-bottom: 16px; border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.search-card :deep(.el-select), .search-card :deep(.el-date-editor), .search-card :deep(.dual-select-v2) { width: 100%; }
.table-toolbar { margin-bottom: 12px; }
.batch-delete-button { margin-left: auto; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
