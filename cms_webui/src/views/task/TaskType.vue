<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">工单类型</h2></div>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit">
        <el-icon><Plus /></el-icon> 新增类型
      </el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="taskTypeId" label="类型ID" width="100" />
        <el-table-column prop="taskTypeName" label="类型名称" min-width="200" />
        <el-table-column prop="taskTypeRemark" label="备注" min-width="300" />
        <el-table-column label="操作" width="100" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <WizardFormDrawer
      v-model="dialogVisible"
      title="新增工单类型"
      mode="add"
      :steps="wizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
        <el-form-item label="类型名称" prop="taskTypeName">
          <el-input v-model="form.taskTypeName" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.taskTypeRemark" type="textarea" :rows="2" />
        </el-form-item>
        </template>
        <ConfirmInfoTable v-else :rows="confirmRows" />
      </template>
    </WizardFormDrawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getTaskTypes, addTaskType, deleteTaskType } from '@/api/task'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({ taskTypeName: '', taskTypeRemark: '' })
const wizardSteps = [
  { title: '基础信息', fields: ['taskTypeName'] },
  { title: '确认信息', fields: [] }
]
const confirmRows = computed(() => [
  { label: '类型名称', value: valueText(form.value.taskTypeName) },
  { label: '备注', value: valueText(form.value.taskTypeRemark) }
])
const formRules = {
  taskTypeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }]
}
function valueText(value) { return value === '' || value === undefined || value === null ? '-' : value }

async function fetchData(forceRefresh = false) { loading.value = true; try { const res = await getTaskTypes({ forceRefresh: !!forceRefresh }); tableData.value = res.data } finally { loading.value = false } }
function openAdd() { form.value = { taskTypeName: '', taskTypeRemark: '' }; dialogVisible.value = true }
async function handleSubmit() {
  submitLoading.value = true
  try {
    await addTaskType(form.value)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchData(true)
  } finally {
    submitLoading.value = false
  }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除工单类型 ${row.taskTypeName} 吗？`, '提示', { type: 'warning' })
  await deleteTaskType(row.taskTypeId)
  ElMessage.success('删除成功')
  fetchData(true)
}
onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.table-toolbar { margin-bottom: 12px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
</style>
