<template>
  <div class="page-container">
    <h2 class="page-title">{{ title }}</h2>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="12">
        <el-col :xs="24" :sm="12" :md="6">
          <el-input v-model="query.assetOwnerId" placeholder="业主ID" clearable />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="query.assetStatus" placeholder="状态" clearable class="full-width">
            <el-option label="正常" :value="0" />
            <el-option label="过期" :value="1" />
            <el-option label="未使用" :value="2" />
            <el-option label="冻结" :value="3" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select-v2
            v-model="query.assetBuilding"
            :options="buildingOptions"
            filterable
            clearable
            placeholder="楼栋"
            class="full-width"
          />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" class="filter-actions">
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <div class="toolbar">
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon> 添加{{ title.replace('管理', '') }}</el-button>
      <el-button @click="fetchData(true)">刷新</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="assetCode" label="资产编号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="assetName" label="名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.assetTypeName || assetTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="assetStatusType(row.assetStatus)" effect="plain">{{ assetStatusText(row.assetStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="业主" min-width="120" show-overflow-tooltip />
        <el-table-column prop="buildingName" label="楼栋" min-width="140" show-overflow-tooltip />
        <el-table-column prop="assetFloor" label="楼层" width="90" show-overflow-tooltip />
        <el-table-column prop="assetPosition" label="位置" min-width="140" show-overflow-tooltip />
        <el-table-column prop="assetRegTime" label="登记时间" width="170" show-overflow-tooltip />
        <el-table-column prop="assetDueTime" label="过期时间" width="170" show-overflow-tooltip />
        <el-table-column prop="assetRemark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该资产？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </el-card>

    <WizardFormDrawer
      v-model="drawerVisible"
      :title="isEdit ? `编辑${title.replace('管理', '')}` : `新增${title.replace('管理', '')}`"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="steps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
          <el-alert v-if="!isEdit" type="info" show-icon :closable="false" class="form-tip" center>
            资产编号将由系统保存时自动生成。
          </el-alert>
          <el-row :gutter="14">
            <el-col :xs="24" :sm="12">
              <el-form-item label="资产编号">
                <el-input :model-value="isEdit ? form.assetCode : '保存时自动生成'" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="名称" prop="assetName">
                <el-input v-model="form.assetName" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="类型">
                <el-input :model-value="assetTypeName" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="业主ID">
                <el-input v-model.number="form.assetOwnerId" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="状态" prop="assetStatus">
                <el-select v-model="form.assetStatus" class="full-width">
                  <el-option label="正常" :value="0" />
                  <el-option label="过期" :value="1" />
                  <el-option label="未使用" :value="2" />
                  <el-option label="冻结" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="面积">
                <el-input-number v-model="form.assetArea" :min="0" controls-position="right" class="full-width" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="登记时间">
                <el-input :model-value="form.assetRegTime || '保存时自动记录'" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="过期时间">
                <el-date-picker v-model="form.assetDueTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="为空代表无过期时间" class="full-width" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <template v-else-if="activeStep === 1">
          <el-row :gutter="14">
            <el-col :xs="24" :sm="12">
              <el-form-item label="楼栋" prop="assetBuilding">
                <el-select-v2
                  v-model="form.assetBuilding"
                  :options="buildingOptions"
                  filterable
                  clearable
                  placeholder="请选择楼栋"
                  class="full-width"
                  @change="handleBuildingChange"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="楼层" prop="assetFloor">
                <el-input-number
                  v-model="floorNumber"
                  :min="selectedBuilding?.buildingMinFloor ?? -99"
                  :max="selectedBuilding?.buildingMaxFloor ?? 99"
                  controls-position="right"
                  class="full-width"
                  :disabled="!form.assetBuilding"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="具体位置">
                <el-input v-model="form.assetPosition" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input v-model="form.assetRemark" type="textarea" :rows="3" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <template v-else>
          <el-table :data="confirmRows" border>
            <el-table-column prop="label" label="字段" width="140" />
            <el-table-column prop="value" label="内容" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag v-if="row.type === 'tag'" :type="row.tagType" effect="plain">{{ row.value }}</el-tag>
                <span v-else>{{ row.value }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </template>
    </WizardFormDrawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import { addAsset, deleteAsset, getAssetPage, updateAsset } from '@/api/asset'
import { getBuildingList } from '@/api/building'

const props = defineProps({
  assetType: { type: Number, required: true },
  title: { type: String, default: '资产管理' }
})

const loading = ref(false)
const submitLoading = ref(false)
const drawerVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)
const buildings = ref([])
const query = ref({ pageNum: 1, pageSize: 10, assetOwnerId: '', assetType: props.assetType, assetStatus: undefined, assetBuilding: undefined })
const form = ref(getDefaultForm())

const assetTypeName = computed(() => props.assetType === 1 ? '房产' : '停车位')
const buildingOptions = computed(() => buildings.value.map(item => ({
  value: item.id,
  label: `${item.buildingCode} ${item.buildingName}`,
  ...item
})))
const selectedBuilding = computed(() => buildings.value.find(item => item.id === form.value.assetBuilding))
const floorNumber = computed({
  get: () => form.value.assetFloor === '' || form.value.assetFloor === null || form.value.assetFloor === undefined ? undefined : Number(form.value.assetFloor),
  set: value => {
    form.value.assetFloor = value === undefined || value === null ? '' : String(value)
  }
})
const steps = [
  { title: '基础信息', fields: ['assetName', 'assetStatus'] },
  { title: '位置配置', fields: ['assetBuilding', 'assetFloor'] },
  { title: '确认信息', fields: [] }
]
const formRules = {
  assetName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  assetStatus: [{ required: true, message: '请选择状态', trigger: 'change' }],
  assetBuilding: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  assetFloor: [{ required: true, message: '请输入楼层', trigger: 'change' }]
}
const confirmRows = computed(() => [
  { label: '资产编号', value: isEdit.value ? valueText(form.value.assetCode) : '保存时自动生成' },
  { label: '名称', value: valueText(form.value.assetName) },
  { label: '类型', type: 'tag', value: assetTypeName.value },
  { label: '业主ID', value: valueText(form.value.assetOwnerId) },
  { label: '状态', type: 'tag', tagType: assetStatusType(form.value.assetStatus), value: assetStatusText(form.value.assetStatus) },
  { label: '面积', value: valueText(form.value.assetArea) },
  { label: '楼栋', value: buildingNameText(form.value.assetBuilding) },
  { label: '楼层', value: valueText(form.value.assetFloor) },
  { label: '具体位置', value: valueText(form.value.assetPosition) },
  { label: '登记时间', value: valueText(form.value.assetRegTime) },
  { label: '过期时间', value: valueText(form.value.assetDueTime) },
  { label: '备注', value: valueText(form.value.assetRemark) }
])

watch(() => props.assetType, () => {
  query.value.assetType = props.assetType
  fetchData(true)
})

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const params = { ...query.value, assetOwnerId: query.value.assetOwnerId || undefined }
    const res = await getAssetPage(params, { forceRefresh })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchBuildings() {
  const res = await getBuildingList()
  buildings.value = res.data || []
}

function search() {
  query.value.pageNum = 1
  fetchData(true)
}

function resetQuery() {
  query.value = { pageNum: 1, pageSize: 10, assetOwnerId: '', assetType: props.assetType, assetStatus: undefined, assetBuilding: undefined }
  fetchData(true)
}

function openAdd() {
  isEdit.value = false
  form.value = getDefaultForm()
  drawerVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = mapRowToForm(row)
  drawerVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    const payload = { ...form.value, assetType: props.assetType }
    if (isEdit.value) {
      await updateAsset(payload)
    } else {
      delete payload.assetCode
      await addAsset(payload)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    drawerVisible.value = false
    fetchData(true)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteAsset(row.id)
  ElMessage.success('删除成功')
  fetchData(true)
}

function getDefaultForm() {
  return {
    assetCode: '',
    assetName: '',
    assetType: props.assetType,
    assetOwnerId: '',
    assetStatus: 0,
    assetArea: null,
    assetBuilding: undefined,
    assetFloor: '',
    assetPosition: '',
    assetRegTime: '',
    assetDueTime: null,
    assetRemark: ''
  }
}

function mapRowToForm(row) {
  return {
    id: row.id,
    assetCode: row.assetCode,
    assetName: row.assetName,
    assetType: props.assetType,
    assetOwnerId: row.assetOwnerId,
    assetStatus: row.assetStatus,
    assetArea: row.assetArea,
    assetBuilding: row.assetBuilding,
    assetFloor: row.assetFloor,
    assetPosition: row.assetPosition,
    assetRegTime: row.assetRegTime,
    assetDueTime: row.assetDueTime,
    assetRemark: row.assetRemark
  }
}

function handleBuildingChange() {
  form.value.assetFloor = ''
}

function valueText(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function buildingNameText(id) {
  const building = buildings.value.find(item => item.id === id)
  return building ? `${building.buildingCode} ${building.buildingName}` : valueText(id)
}

function assetStatusText(status) {
  return ({ 0: '正常', 1: '过期', 2: '未使用', 3: '冻结' })[status] || '未知'
}

function assetStatusType(status) {
  return ({ 0: 'success', 1: 'danger', 2: 'info', 3: 'warning' })[status] || 'info'
}

onMounted(() => {
  fetchBuildings()
  fetchData()
})
</script>

<style scoped>
.page-title { margin: 0 0 18px; color: var(--vp-c-text-1); font-size: 24px; font-weight: 800; }
.filter-card, .table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.filter-actions { display: flex; gap: 8px; }
.toolbar { display: flex; gap: 10px; margin: 16px 0; }
.full-width { width: 100%; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 18px; }
.form-tip { margin-bottom: 16px; }
@media (max-width: 768px) {
  .filter-actions, .toolbar { flex-direction: column; }
  .filter-actions .el-button, .toolbar .el-button { width: 100%; margin-left: 0; }
}
</style>
