<template>
  <div class="page-container">
    <h2 class="page-title">设施信息</h2>

    <el-card shadow="never" class="filter-card">
      <el-row :gutter="12">
        <el-col :xs="24" :sm="12" :md="6">
          <el-input v-model="query.facilityName" placeholder="设施名称" clearable />
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="query.facilityType" placeholder="设施类型" clearable class="full-width">
            <el-option v-for="type in types" :key="type.facilityTypeId" :label="type.facilityTypeName" :value="type.facilityTypeId" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-select v-model="query.facilityStatus" placeholder="状态" clearable class="full-width">
            <el-option label="正常" :value="0" />
            <el-option label="故障" :value="1" />
            <el-option label="损坏" :value="2" />
            <el-option label="缺失" :value="3" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" class="filter-actions">
          <el-button type="primary" @click="search">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <div class="toolbar">
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon> 添加设施</el-button>
      <el-button @click="fetchData(true)">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="facilityCode" label="设施编号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="facilityName" label="名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.facilityTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="facilityStatusType(row.facilityStatus)" effect="plain">{{ facilityStatusText(row.facilityStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chiefName" label="负责人" min-width="110" show-overflow-tooltip />
        <el-table-column prop="buildingName" label="楼栋" min-width="140" show-overflow-tooltip />
        <el-table-column prop="facilityFloor" label="楼层" width="90" show-overflow-tooltip />
        <el-table-column prop="facilityPosition" label="位置" min-width="140" show-overflow-tooltip />
        <el-table-column prop="facilityCheckTime" label="检查时间" width="170" show-overflow-tooltip />
        <el-table-column prop="facilityExpirationDate" label="检查周期" width="110" show-overflow-tooltip />
        <el-table-column prop="facilityRemark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该设施？" @confirm="handleDelete(row)">
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
      :title="isEdit ? '编辑设施' : '新增设施'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="steps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
          <el-alert v-if="!isEdit" type="info" show-icon :closable="false" class="form-tip">设施编号将由系统保存时自动生成。</el-alert>
          <el-row :gutter="14">
            <el-col :xs="24" :sm="24" v-if="isEdit">
              <el-form-item label="设施编号">
                <el-input :model-value="isEdit ? form.facilityCode : '保存时自动生成'" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24">
              <el-form-item label="名称" prop="facilityName"><el-input v-model="form.facilityName" /></el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24">
              <el-form-item label="类型" prop="facilityType">
                <el-select v-model="form.facilityType" class="full-width">
                  <el-option v-for="type in types" :key="type.facilityTypeId" :label="type.facilityTypeName" :value="type.facilityTypeId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24">
              <el-form-item label="状态" prop="facilityStatus">
                <el-select v-model="form.facilityStatus" class="full-width">
                  <el-option label="正常" :value="0" />
                  <el-option label="故障" :value="1" />
                  <el-option label="损坏" :value="2" />
                  <el-option label="缺失" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24">
              <el-form-item label="负责人">
                <DualSelectV2
                  v-model="form.facilityChiefId"
                  :options="userOptions"
                  remote
                  :remote-method="handleUserRemoteSearch"
                  placeholder="输入以搜索用户"
                  primary-key="label"
                  secondary-key="userName"
                  @change="handleChiefChange"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24">
              <el-form-item label="检查时间">
                <el-date-picker v-model="form.facilityCheckTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="full-width" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24">
              <el-form-item label="检查周期">
                <div class="number-with-unit">
                  <el-input-number v-model="form.facilityExpirationDate" :min="0" controls-position="right" class="full-width" placeholder="月数" />
                  <span class="number-unit">月</span>
                </div>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24">
              <el-form-item label="所有方"><el-input v-model="form.facilityOwner" /></el-form-item>
            </el-col>
          </el-row>
        </template>

        <template v-else-if="activeStep === 1">
          <el-row :gutter="14">
            <el-col :span="24">
              <el-form-item label="名称">
                <el-input v-model="form.facilityName" :placeholder="generatedFacilityName" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24">
              <el-form-item label="楼栋" prop="facilityBuilding">
                <DualSelectV2
                  v-model="form.facilityBuilding"
                  :options="buildingOptions"
                  placeholder="请选择楼栋"
                  primary-key="buildingCode"
                  secondary-key="buildingName"
                  @change="handleBuildingChange"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24">
              <el-form-item label="楼层" prop="facilityFloor">
                <el-select
                  v-model="form.facilityFloor"
                  class="full-width"
                  placeholder="请选择楼层"
                  :disabled="!form.facilityBuilding"
                >
                  <el-option v-for="floor in floorOptions" :key="floor.value" :label="floor.label" :value="floor.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="位置"><el-input v-model="form.facilityPosition" /></el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注"><el-input v-model="form.facilityRemark" type="textarea" :rows="3" /></el-form-item>
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import DualSelectV2 from '@/components/DualSelectV2.vue'
import { addFacility, deleteFacility, getFacilityPage, getFacilityTypes, updateFacility } from '@/api/facility'
import { getBuildingList } from '@/api/building'
import { getUserPage } from '@/api/user'

const loading = ref(false)
const submitLoading = ref(false)
const drawerVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)
const types = ref([])
const buildings = ref([])
const users = ref([])
const query = ref({ pageNum: 1, pageSize: 10, facilityName: '', facilityType: undefined, facilityStatus: undefined })
const form = ref(getDefaultForm())
const buildingOptions = computed(() => buildings.value.map(item => ({
  value: item.id,
  label: item.buildingCode,
  buildingCode: item.buildingCode,
  buildingName: item.buildingName,
  ...item
})))
const selectedBuilding = computed(() => buildings.value.find(item => item.id === form.value.facilityBuilding))
const userOptions = computed(() => {
  const list = users.value.map(user => ({
    value: user.id,
    label: user.uid || String(user.id),
    userName: user.userName || user.uid || String(user.id)
  }))
  return list.length ? list : [{ value: '__empty_users__', label: '暂无匹配用户', userName: '', disabled: true, empty: true }]
})
const floorOptions = computed(() => {
  if (!selectedBuilding.value) return []
  const min = Number(selectedBuilding.value.buildingMinFloor ?? -2)
  const max = Number(selectedBuilding.value.buildingMaxFloor ?? 30)
  const floors = []
  for (let floor = min; floor <= max; floor += 1) {
    if (floor === 0) continue
    floors.push({ value: String(floor), label: floor > 0 ? `F${floor}` : `B${Math.abs(floor)}` })
  }
  return floors
})
const generatedFacilityName = computed(() => {
  const building = selectedBuilding.value?.buildingCode || ''
  const floor = floorOptions.value.find(item => item.value === String(form.value.facilityFloor))?.label || ''
  return [form.value.facilityName, building, floor, form.value.facilityPosition].filter(Boolean).join('-')
})
const steps = [
  { title: '基础信息', fields: ['facilityName', 'facilityType', 'facilityStatus'] },
  { title: '位置配置', fields: ['facilityBuilding', 'facilityFloor'] },
  { title: '确认信息', fields: [] }
]
const formRules = {
  facilityName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  facilityType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  facilityStatus: [{ required: true, message: '请选择状态', trigger: 'change' }],
  facilityBuilding: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  facilityFloor: [{ required: true, message: '请输入楼层', trigger: 'change' }]
}
const confirmRows = computed(() => [
  { label: '设施编号', value: isEdit.value ? valueText(form.value.facilityCode) : '保存时自动生成' },
  { label: '名称', value: valueText(form.value.facilityName) },
  { label: '类型', type: 'tag', value: facilityTypeNameText(form.value.facilityType) },
  { label: '状态', type: 'tag', tagType: facilityStatusType(form.value.facilityStatus), value: facilityStatusText(form.value.facilityStatus) },
  { label: '负责人ID', value: valueText(form.value.facilityChiefId) },
  { label: '检查时间', value: valueText(form.value.facilityCheckTime) },
  { label: '检查周期', value: valueText(form.value.facilityExpirationDate) },
  { label: '楼栋', value: buildingNameText(form.value.facilityBuilding) },
  { label: '楼层', value: valueText(form.value.facilityFloor) },
  { label: '位置', value: valueText(form.value.facilityPosition) },
  { label: '所有方', value: valueText(form.value.facilityOwner) },
  { label: '备注', value: valueText(form.value.facilityRemark) }
])

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const res = await getFacilityPage(query.value, { forceRefresh })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchTypes() {
  const res = await getFacilityTypes()
  types.value = res.data || []
}

async function fetchBuildings() {
  const res = await getBuildingList()
  buildings.value = res.data || []
}

async function fetchUsers(keyword = '') {
  const keywordText = String(keyword || '').trim()
  const requests = keywordText
    ? [
        getUserPage({ pageNum: 1, pageSize: 1000, uid: keywordText }, { forceRefresh: true }),
        getUserPage({ pageNum: 1, pageSize: 1000, userName: keywordText }, { forceRefresh: true })
      ]
    : [getUserPage({ pageNum: 1, pageSize: 1000 }, { forceRefresh: true })]
  const responses = await Promise.all(requests)
  const mergedUsers = responses.flatMap(res => res.data?.records || [])
  users.value = Array.from(new Map(mergedUsers.map(user => [user.id, user])).values())
}

function handleUserRemoteSearch(keyword) {
  fetchUsers(keyword)
}

function handleChiefChange(value) {
  form.value.facilityChiefId = value
}

function search() {
  query.value.pageNum = 1
  fetchData(true)
}

function resetQuery() {
  query.value = { pageNum: 1, pageSize: 10, facilityName: '', facilityType: undefined, facilityStatus: undefined }
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
    const payload = { ...form.value }
    if (isEdit.value) {
      await updateFacility(payload)
    } else {
      delete payload.facilityCode
      await addFacility(payload)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    drawerVisible.value = false
    fetchData(true)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteFacility(row.id)
  ElMessage.success('删除成功')
  fetchData(true)
}

function getDefaultForm() {
  return { facilityCode: '', facilityName: '', facilityType: undefined, facilityChiefId: '', facilityStatus: 0, facilityCheckTime: '', facilityExpirationDate: null, facilityBuilding: undefined, facilityFloor: '', facilityPosition: '', facilityOwner: '', facilityRemark: '' }
}

function mapRowToForm(row) {
  return { id: row.id, facilityCode: row.facilityCode, facilityName: row.facilityName, facilityType: row.facilityType, facilityChiefId: row.facilityChiefId, facilityStatus: row.facilityStatus, facilityCheckTime: row.facilityCheckTime, facilityExpirationDate: row.facilityExpirationDate, facilityBuilding: row.facilityBuilding, facilityFloor: row.facilityFloor, facilityPosition: row.facilityPosition, facilityOwner: row.facilityOwner, facilityRemark: row.facilityRemark }
}

function handleBuildingChange() {
  form.value.facilityFloor = ''
}

function valueText(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function buildingNameText(id) {
  const building = buildings.value.find(item => item.id === id)
  return building ? `${building.buildingCode} ${building.buildingName}` : valueText(id)
}

function facilityTypeNameText(id) {
  return types.value.find(type => type.facilityTypeId === id)?.facilityTypeName || valueText(id)
}

function facilityStatusText(status) {
  return ({ 0: '正常', 1: '故障', 2: '损坏', 3: '缺失' })[status] || '未知'
}

function facilityStatusType(status) {
  return ({ 0: 'success', 1: 'warning', 2: 'danger', 3: 'info' })[status] || 'info'
}

onMounted(() => {
  fetchTypes()
  fetchBuildings()
  fetchUsers()
  fetchData()
})
</script>

<style scoped>
.page-title {
  margin: 0 0 18px;
  color: var(--vp-c-text-1);
  font-size: 24px;
  font-weight: 800;
}

.filter-card,
.table-card {
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-row) {
  row-gap: 12px;
}

.filter-actions {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 10px;
}

.filter-actions .el-button {
  flex: 1 1 0;
  min-width: 0;
  margin-left: 0;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin: 16px 0;
}

.full-width {
  width: 100%;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.form-tip {
  margin-bottom: 16px;
}

.page-container :deep(.wizard-body .el-row) {
  width: 100%;
  margin-right: 0 !important;
  margin-left: 0 !important;
}

.page-container :deep(.wizard-body .el-col) {
  max-width: 100%;
  padding-right: 0 !important;
  padding-left: 0 !important;
}

.page-container :deep(.wizard-body .el-form-item) {
  margin-bottom: 18px;
}

.page-container :deep(.wizard-body .el-select),
.page-container :deep(.wizard-body .el-select-v2),
.page-container :deep(.wizard-body .el-input),
.page-container :deep(.wizard-body .el-input-number),
.page-container :deep(.wizard-body .el-date-editor),
.page-container :deep(.wizard-body .el-textarea) {
  width: 100%;
}

.number-with-unit {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.number-with-unit .el-input-number {
  flex: 1 1 auto;
  min-width: 0;
}

.number-unit {
  flex: 0 0 auto;
  color: var(--vp-c-text-2);
  font-size: 14px;
}

@media (max-width: 768px) {
  .filter-card :deep(.el-card__body),
  .table-card :deep(.el-card__body) {
    padding: 16px 18px;
  }

  .filter-card :deep(.el-row) {
    row-gap: 10px;
  }

  .filter-card :deep(.el-input),
  .filter-card :deep(.el-select),
  .filter-card :deep(.el-select-v2) {
    width: 100%;
  }

  .filter-actions {
    flex-direction: row;
  }

  .filter-actions .el-button {
    width: 100%;
  }

  .toolbar {
    flex-direction: row;
  }

  .toolbar .el-button {
    width: auto;
    margin-left: 0;
  }

  .pagination-wrap {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
