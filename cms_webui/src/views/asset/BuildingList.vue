<template>
  <div class="page-container">
    <h2 class="page-title">建筑物管理</h2>
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="12">
        <el-col :xs="24" :sm="12" :md="5">
          <el-input v-model="query.buildingCode" placeholder="建筑编号" clearable @keyup.enter="search" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="5">
          <el-input v-model="query.buildingName" placeholder="建筑名称" clearable @keyup.enter="search" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="5">
          <el-input v-model="query.buildingLocation" placeholder="位置" clearable @keyup.enter="search" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="5">
          <el-input v-model="query.buildingRemark" placeholder="备注" clearable @keyup.enter="search" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="4" class="filter-actions">
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
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon> 新增建筑物</el-button>
      <el-button @click="fetchData(true)">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="buildingCode" label="建筑编号" width="140" sortable/>
        <el-table-column prop="buildingName" label="建筑名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="buildingMinFloor" label="最低楼层" width="110" />
        <el-table-column prop="buildingMaxFloor" label="最高楼层" width="110" />
        <el-table-column prop="buildingLocation" label="位置" min-width="180" show-overflow-tooltip />
        <el-table-column prop="buildingRemark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该建筑物？" @confirm="handleDelete(row)">
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
      v-model="dialogVisible"
      :title="isEdit ? '编辑建筑物' : '新增建筑物'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="steps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
          <el-alert v-if="!isEdit" type="info" show-icon :closable="false" class="form-tip" center>建筑编号将由系统保存时自动生成。</el-alert>
          <el-form-item v-if="isEdit" label="建筑编号">
            <el-input :model-value="form.buildingCode" disabled />
          </el-form-item>
          <el-form-item label="建筑名称" prop="buildingName">
            <el-input v-model="form.buildingName" placeholder="请输入建筑名称" />
          </el-form-item>
          <el-form-item label="位置">
            <el-input v-model="form.buildingLocation" placeholder="请输入建筑位置" />
          </el-form-item>
        </template>

        <template v-else-if="activeStep === 1">
          <el-row :gutter="14">
            <el-col :xs="24" :sm="12">
              <el-form-item label="最低楼层" prop="buildingMinFloor">
                <el-input-number
                  v-model="form.buildingMinFloor"
                  controls-position="right"
                  class="full-width"
                  @change="handleMinFloorChange"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="最高楼层" prop="buildingMaxFloor">
                <el-input-number
                  v-model="form.buildingMaxFloor"
                  controls-position="right"
                  class="full-width"
                  @change="handleMaxFloorChange"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注">
            <el-input v-model="form.buildingRemark" type="textarea" :rows="4" placeholder="请输入备注" />
          </el-form-item>
        </template>

        <template v-else>
          <ConfirmInfoTable :rows="confirmRows" />
        </template>
      </template>
    </WizardFormDrawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addBuilding, deleteBuilding, getBuildingPage, updateBuilding } from '@/api/building'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import { useAgentPageControl } from '@/utils/agentControl'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)
const query = ref(getDefaultQuery())
const agentQueryKeys = ['buildingCode', 'buildingName', 'buildingLocation', 'buildingRemark']
const form = ref(getDefaultForm())
const steps = [
  { title: '基础信息', fields: ['buildingName'] },
  { title: '楼层位置', fields: ['buildingMinFloor', 'buildingMaxFloor'] },
  { title: '确认信息', fields: [] }
]
const formRules = {
  buildingName: [{ required: true, message: '请输入建筑名称', trigger: 'blur' }],
  buildingMinFloor: [{ required: true, validator: validateMinFloor, trigger: 'blur' }],
  buildingMaxFloor: [{ required: true, validator: validateMaxFloor, trigger: 'blur' }]
}
const confirmRows = computed(() => [
  { label: '建筑编号', value: isEdit.value ? valueText(form.value.buildingCode) : '保存时自动生成' },
  { label: '建筑名称', value: valueText(form.value.buildingName) },
  { label: '最低楼层', value: valueText(form.value.buildingMinFloor) },
  { label: '最高楼层', value: valueText(form.value.buildingMaxFloor) },
  { label: '位置', value: valueText(form.value.buildingLocation) },
  { label: '备注', value: valueText(form.value.buildingRemark) }
])

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const res = await getBuildingPage(query.value, { forceRefresh })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.value.pageNum = 1
  fetchData(true)
}

function resetQuery() {
  query.value = getDefaultQuery()
  fetchData(true)
}

function openAdd() {
  isEdit.value = false
  form.value = getDefaultForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

function openAgentEdit(id) {
  const row = tableData.value.find(item => String(item.id) === String(id))
  if (row) openEdit(row)
}

async function handleSubmit() {
  if (!isValidFloor(form.value.buildingMinFloor) || !isValidFloor(form.value.buildingMaxFloor)) {
    ElMessage.warning('楼层不能为 0')
    return
  }
  if (Number(form.value.buildingMinFloor) > Number(form.value.buildingMaxFloor)) {
    ElMessage.warning('最低楼层不能大于最高楼层')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateBuilding(form.value)
    } else {
      await addBuilding(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData(true)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteBuilding(row.id)
  ElMessage.success('删除成功')
  fetchData(true)
}

function getDefaultForm() {
  return { buildingCode: '', buildingName: '', buildingMinFloor: 1, buildingMaxFloor: 1, buildingLocation: '', buildingRemark: '' }
}

function getDefaultQuery() {
  return { pageNum: 1, pageSize: 10, buildingCode: '', buildingName: '', buildingLocation: '', buildingRemark: '' }
}

function valueText(value) {
  return value === '' || value === null || value === undefined ? '-' : value
}

function isValidFloor(value) {
  return value !== null && value !== undefined && Number(value) !== 0
}

function skipZeroFloor(value, oldValue) {
  if (Number(value) !== 0) return value
  return Number(oldValue) < 0 ? 1 : -1
}

function handleMinFloorChange(value, oldValue) {
  const nextValue = skipZeroFloor(value, oldValue)
  form.value.buildingMinFloor = Number(nextValue) > Number(form.value.buildingMaxFloor)
    ? form.value.buildingMaxFloor
    : nextValue
}

function handleMaxFloorChange(value, oldValue) {
  const nextValue = skipZeroFloor(value, oldValue)
  form.value.buildingMaxFloor = Number(nextValue) < Number(form.value.buildingMinFloor)
    ? form.value.buildingMinFloor
    : nextValue
}

function validateMinFloor(rule, value, callback) {
  if (!isValidFloor(value)) {
    callback(new Error('最低楼层不能为 0'))
    return
  }
  if (Number(value) > Number(form.value.buildingMaxFloor)) {
    callback(new Error('最低楼层不能高于最高楼层'))
    return
  }
  callback()
}

function validateMaxFloor(rule, value, callback) {
  if (!isValidFloor(value)) {
    callback(new Error('最高楼层不能为 0'))
    return
  }
  if (Number(value) < Number(form.value.buildingMinFloor)) {
    callback(new Error('最高楼层不能低于最低楼层'))
    return
  }
  callback()
}

useAgentPageControl({
  query,
  queryKeys: agentQueryKeys,
  onSearch: () => fetchData(true),
  onRefresh: () => fetchData(true),
  onOpenAdd: () => openAdd(),
  onOpenEdit: id => openAgentEdit(id)
})

onMounted(() => fetchData())
</script>

<style scoped>
.page-title { margin: 0 0 18px; color: var(--vp-c-text-1); font-size: 24px; font-weight: 800; }
.filter-card, .table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.filter-actions, .toolbar { display: flex; align-items: center; gap: 10px; }
.filter-actions .el-button { flex: 1 1 0; margin-left: 0; }
.toolbar { margin: 16px 0; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 18px; }
.full-width { width: 100%; }
.form-tip { margin-bottom: 18px; }
:deep(.wizard-body .el-row) { width: 100%; margin-left: 0 !important; margin-right: 0 !important; }
:deep(.wizard-body .el-col) { min-width: 0; padding-left: 0 !important; padding-right: 0 !important; }
:deep(.wizard-body .el-input),
:deep(.wizard-body .el-input-number),
:deep(.wizard-body .el-textarea) { width: 100%; }
@media (max-width: 768px) {
  .filter-card :deep(.el-card__body),
  .table-card :deep(.el-card__body) { padding: 16px 18px; }
  .filter-card :deep(.el-row) { row-gap: 10px; }
  .filter-card :deep(.el-input),
  .filter-card :deep(.el-select),
  .filter-card :deep(.el-select-v2) { width: 100%; }
  .filter-actions, .toolbar { flex-direction: row; }
  .filter-actions .el-button { width: 100%; margin-left: 0; }
  .toolbar .el-button { width: auto; margin-left: 0; }
  .pagination-wrap { justify-content: flex-start; overflow-x: auto; }
}
</style>
