<template>
  <div class="page-container">
    <h2 class="page-title">资产类型</h2>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon> 新增类型</el-button>
      <el-button @click="fetchData(true)">刷新</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" row-key="assetTypeId">
        <el-table-column prop="assetTypeId" label="类型ID" width="110" />
        <el-table-column prop="assetTypeName" label="类型名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="assetTypeShort" label="缩写" width="100">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.assetTypeShort }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该资产类型？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <WizardFormDrawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑资产类型' : '新增资产类型'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="steps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
          <el-form-item v-if="isEdit" label="类型ID">
            <el-input :model-value="form.assetTypeId" disabled />
          </el-form-item>
          <el-form-item label="类型名称" prop="assetTypeName">
            <el-input v-model="form.assetTypeName" placeholder="请输入类型名称" />
          </el-form-item>
          <el-form-item label="缩写" prop="assetTypeShort">
            <el-input
              v-model="form.assetTypeShort"
              maxlength="2"
              show-word-limit
              placeholder="最多两位大写字母"
              @input="value => form.assetTypeShort = value.toUpperCase().replace(/[^A-Z]/g, '').slice(0, 2)"
            />
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
import { addAssetType, deleteAssetType, getAssetTypes, updateAssetType } from '@/api/asset'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const form = ref(getDefaultForm())
const steps = [
  { title: '类型信息', fields: ['assetTypeName', 'assetTypeShort'] },
  { title: '确认信息', fields: [] }
]
const formRules = {
  assetTypeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  assetTypeShort: [
    { required: true, message: '请输入类型缩写', trigger: 'blur' },
    { pattern: /^[A-Z]{1,2}$/, message: '缩写必须为一至两位大写英文字母', trigger: 'blur' }
  ]
}
const confirmRows = computed(() => [
  { label: '类型ID', value: isEdit.value ? valueText(form.value.assetTypeId) : '保存时自动生成' },
  { label: '类型名称', value: valueText(form.value.assetTypeName) },
  { label: '缩写', type: 'tag', value: valueText(form.value.assetTypeShort) }
])

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const res = await getAssetTypes({ forceRefresh })
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openAdd() {
  isEdit.value = false
  form.value = getDefaultForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = { assetTypeId: row.assetTypeId, assetTypeName: row.assetTypeName, assetTypeShort: row.assetTypeShort }
  dialogVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateAssetType(form.value)
    } else {
      await addAssetType(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData(true)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteAssetType(row.assetTypeId)
  ElMessage.success('删除成功')
  fetchData(true)
}

function getDefaultForm() {
  return { assetTypeId: null, assetTypeName: '', assetTypeShort: '' }
}

function valueText(value) {
  return value === '' || value === null || value === undefined ? '-' : value
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-title { margin: 0 0 18px; color: var(--vp-c-text-1); font-size: 24px; font-weight: 800; }
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.full-width { width: 100%; }
:deep(.wizard-body .el-input) { width: 100%; }
</style>
