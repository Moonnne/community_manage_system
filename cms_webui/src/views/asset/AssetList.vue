<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">{{ title }}</h2></div>
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5"><el-input v-model="query.assetOwnerId" placeholder="业主ID" clearable /></el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-select v-model="query.assetStatus" placeholder="状态" clearable>
            <el-option label="空闲" :value="0" />
            <el-option label="使用中" :value="1" />
            <el-option label="维修中" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5"><el-input v-model="query.assetBuilding" placeholder="楼栋" clearable /></el-col>
        <el-col :span="6" :xs="24" :sm="24" :md="8" :lg="6" :xl="6">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-col>
      </el-row>
    </el-card>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit"><el-icon><Plus /></el-icon> 新增资产</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="assetCode" label="资产编号" width="140" />
        <el-table-column prop="assetName" label="名称" width="120" />
        <el-table-column prop="ownerName" label="业主" width="100" />
        <el-table-column label="类型" width="100">
          <template #default>
            <el-tag effect="plain">{{ assetTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="assetStatusType(row.assetStatus)" effect="plain">
              {{ assetStatusText(row.assetStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assetArea" label="面积" width="80" />
        <el-table-column prop="assetBuilding" label="楼栋" width="80" />
        <el-table-column prop="assetFloor" label="楼层" width="80" />
        <el-table-column prop="assetPosition" label="位置" width="120" show-overflow-tooltip />
        <el-table-column prop="assetRegTime" label="登记时间" width="170" />
        <el-table-column prop="assetRemark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50]" :total="total" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <WizardFormDrawer
      v-model="dialogVisible"
      :title="isEdit ? `编辑${title}` : `新增${title}`"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="wizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
        <el-form-item label="资产编号" prop="assetCode" v-if="!isEdit"><el-input v-model="form.assetCode" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="名称" prop="assetName"><el-input v-model="form.assetName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型"><el-input :model-value="assetTypeName" disabled /></el-form-item></el-col>
        </el-row>
        </template>
        <template v-else-if="activeStep === 1">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="业主ID" prop="assetOwnerId"><el-input v-model.number="form.assetOwnerId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="面积"><el-input v-model.number="form.assetArea" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态" prop="assetStatus">
              <el-select v-model="form.assetStatus" placeholder="请选择">
                <el-option label="空闲" :value="0" />
                <el-option label="使用中" :value="1" />
                <el-option label="维修中" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="楼栋"><el-input v-model="form.assetBuilding" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="楼层"><el-input v-model="form.assetFloor" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="位置"><el-input v-model="form.assetPosition" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.assetRemark" type="textarea" :rows="2" /></el-form-item>
        </template>
        <ConfirmInfoTable v-else :rows="confirmRows" />
      </template>
    </WizardFormDrawer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getAssetPage, addAsset, updateAsset, deleteAsset } from '@/api/asset'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'

const props = defineProps({
  assetType: { type: Number, required: true },
  title: { type: String, required: true }
})

const userStore = useUserStore()
const canEdit = computed(() => [0, 1, 2].includes(userStore.userRole) && userStore.canOperate)
const assetTypeName = computed(() => props.assetType === 1 ? '房产' : '停车位')
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, assetOwnerId: '', assetType: props.assetType, assetStatus: undefined, assetBuilding: '' })
const form = ref({})
const wizardSteps = [
  { title: '基础信息', fields: ['assetCode', 'assetName'] },
  { title: '归属状态', fields: ['assetStatus'] },
  { title: '确认信息', fields: [] }
]
const confirmRows = computed(() => [
  { label: '资产编号', value: valueText(form.value.assetCode) },
  { label: '名称', value: valueText(form.value.assetName) },
  { label: '类型', type: 'tag', value: assetTypeName.value },
  { label: '业主ID', value: valueText(form.value.assetOwnerId) },
  { label: '状态', type: 'tag', tagType: assetStatusType(form.value.assetStatus), value: assetStatusText(form.value.assetStatus) },
  { label: '面积', value: valueText(form.value.assetArea) },
  { label: '楼栋', value: valueText(form.value.assetBuilding) },
  { label: '楼层', value: valueText(form.value.assetFloor) },
  { label: '位置', value: valueText(form.value.assetPosition) },
  { label: '备注', value: valueText(form.value.assetRemark) }
])
const formRules = {
  assetCode: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
  assetName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  assetStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function valueText(value) {
  return value === '' || value === undefined || value === null ? '-' : value
}

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const p = { ...query.value, assetType: props.assetType }
    Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] })
    const res = await getAssetPage(p, { forceRefresh: !!forceRefresh })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.value = { pageNum: 1, pageSize: 10, assetOwnerId: '', assetType: props.assetType, assetStatus: undefined, assetBuilding: '' }
  fetchData(true)
}

function openAdd() {
  isEdit.value = false
  form.value = getDefaultForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = mapRowToForm(row)
  dialogVisible.value = true
}

function getDefaultForm() {
  return { assetCode: '', assetName: '', assetType: props.assetType, assetOwnerId: '', assetStatus: 0, assetArea: '', assetBuilding: '', assetFloor: '', assetPosition: '', assetRemark: '' }
}

function mapRowToForm(row) {
  return { id: row.id, assetName: row.assetName, assetType: props.assetType, assetOwnerId: row.assetOwnerId, assetStatus: row.assetStatus, assetArea: row.assetArea, assetBuilding: row.assetBuilding, assetFloor: row.assetFloor, assetPosition: row.assetPosition, assetRemark: row.assetRemark }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateAsset(form.value)
      ElMessage.success('更新成功')
    } else {
      await addAsset(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData(true)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该资产吗？', '提示', { type: 'warning' })
  await deleteAsset(row.id)
  ElMessage.success('删除成功')
  fetchData(true)
}

function assetStatusText(status) {
  return ['空闲', '使用中', '维修中'][status] || '未知'
}

function assetStatusType(status) {
  return ['success', 'primary', 'warning'][status] || 'info'
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.search-card { margin-bottom: 16px; border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.table-toolbar { margin-bottom: 12px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
