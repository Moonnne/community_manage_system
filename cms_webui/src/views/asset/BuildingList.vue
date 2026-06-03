<template>
  <div class="page-container">
    <h2 class="page-title">建筑物管理</h2>
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="12">
        <el-col :xs="24" :sm="16" :md="8">
          <el-input v-model="query.keyword" placeholder="按编号、名称、位置搜索" clearable @keyup.enter="search" />
        </el-col>
        <el-col :xs="24" :sm="8" :md="6" class="filter-actions">
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <div class="toolbar">
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon> 新增建筑物</el-button>
      <el-button @click="fetchData(true)">刷新</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="buildingCode" label="建筑编号" width="140" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑建筑物' : '新增建筑物'" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="建筑编号">
          <el-input :model-value="isEdit ? form.buildingCode : '保存时自动生成'" disabled />
        </el-form-item>
        <el-form-item label="建筑名称" prop="buildingName">
          <el-input v-model="form.buildingName" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="最低楼层" prop="buildingMinFloor">
              <el-input-number v-model="form.buildingMinFloor" controls-position="right" class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高楼层" prop="buildingMaxFloor">
              <el-input-number v-model="form.buildingMaxFloor" controls-position="right" class="full-width" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="位置">
          <el-input v-model="form.buildingLocation" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.buildingRemark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addBuilding, deleteBuilding, getBuildingPage, updateBuilding } from '@/api/building'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '' })
const form = ref(getDefaultForm())
const rules = {
  buildingName: [{ required: true, message: '请输入建筑名称', trigger: 'blur' }],
  buildingMinFloor: [{ required: true, message: '请输入最低楼层', trigger: 'blur' }],
  buildingMaxFloor: [{ required: true, message: '请输入最高楼层', trigger: 'blur' }]
}

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
  query.value = { pageNum: 1, pageSize: 10, keyword: '' }
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

async function handleSubmit() {
  await formRef.value?.validate()
  if (form.value.buildingMinFloor > form.value.buildingMaxFloor) {
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

onMounted(() => fetchData())
</script>

<style scoped>
.page-title { margin: 0 0 18px; color: var(--vp-c-text-1); font-size: 24px; font-weight: 800; }
.filter-card, .table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.filter-actions, .toolbar { display: flex; gap: 10px; }
.toolbar { margin: 16px 0; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 18px; }
.full-width { width: 100%; }
@media (max-width: 768px) {
  .filter-actions, .toolbar { flex-direction: column; }
  .filter-actions .el-button, .toolbar .el-button { width: 100%; margin-left: 0; }
}
</style>
