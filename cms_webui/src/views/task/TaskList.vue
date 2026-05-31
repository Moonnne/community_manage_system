<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">工单列表</h2></div>
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4"><el-input v-model="query.ownerId" placeholder="提交人ID" clearable /></el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4"><el-input v-model="query.taskOperatorId" placeholder="处理人ID" clearable /></el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-select v-model="query.taskType" placeholder="工单类型" clearable>
            <el-option v-for="t in types" :key="t.taskTypeId" :label="t.taskTypeName" :value="t.taskTypeId" />
          </el-select>
        </el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-select v-model="query.taskStatus" placeholder="状态" clearable>
            <el-option label="待处理" :value="1" />
            <el-option label="受理中" :value="2" />
            <el-option label="已完成" :value="0" />
            <el-option label="已驳回" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-col>
        <el-col :span="6" :xs="24" :sm="24" :md="8" :lg="6" :xl="6">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-col>
      </el-row>
    </el-card>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="userStore.canOperate"><el-icon><Plus /></el-icon> 提交工单</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="taskCode" label="工单编号" width="140" />
        <el-table-column prop="taskTitle" label="标题" width="160" show-overflow-tooltip />
        <el-table-column prop="taskTypeName" label="类型" width="100">
          <template #default="{ row }"><el-tag effect="plain">{{ row.taskTypeName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="ownerName" label="提交人" width="100" />
        <el-table-column prop="operatorName" label="处理人" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="taskStatusType(row.taskStatus)" effect="plain">{{ taskStatusText(row.taskStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskCreateTime" label="创建时间" width="170" />
        <el-table-column prop="taskInfo" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="taskRemark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)" v-if="canManage">编辑</el-button>
            <el-button link type="primary" size="small" @click="openProcess(row)" v-if="canManage && [1, 2].includes(row.taskStatus)">处理</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="canManage">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50]" :total="total" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <WizardFormDrawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑工单' : '提交工单'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="wizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
        <el-form-item label="标题" prop="taskTitle"><el-input v-model="form.taskTitle" /></el-form-item>
        <el-form-item label="类型" prop="taskType">
          <el-select v-model="form.taskType" placeholder="请选择"><el-option v-for="t in types" :key="t.taskTypeId" :label="t.taskTypeName" :value="t.taskTypeId" /></el-select>
        </el-form-item>
        <el-form-item label="描述" prop="taskInfo"><el-input v-model="form.taskInfo" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.taskRemark" type="textarea" :rows="2" /></el-form-item>
    </WizardFormDrawer>

    <!-- Process Dialog -->
    <el-dialog v-model="processVisible" title="处理工单" width="480" :close-on-click-modal="false">
      <el-form ref="processFormRef" :model="processForm" label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="processForm.status" placeholder="请选择">
            <el-option label="受理中" :value="2" />
            <el-option label="已完成" :value="0" />
            <el-option label="已驳回" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人ID"><el-input v-model.number="processForm.operatorId" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="processForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" :loading="processLoading" @click="handleProcess">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getTaskPage, getTaskTypes, addTask, updateTask, deleteTask, processTask } from '@/api/task'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'

const userStore = useUserStore()
const route = useRoute()
const canManage = computed(() => [0, 1, 2, 3].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const processLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const types = ref([])
const dialogVisible = ref(false)
const processVisible = ref(false)
const isEdit = ref(false)
const processFormRef = ref(null)
const query = ref({ pageNum: 1, pageSize: 10, ownerId: '', taskOperatorId: '', taskType: undefined, taskStatus: undefined })
const form = ref({})
const processForm = ref({})
const wizardSteps = [
  { title: '基础信息', fields: ['taskTitle', 'taskType'] },
  { title: '描述信息', fields: ['taskInfo'] },
  { title: '备注确认', fields: [] }
]
const formRules = {
  taskTitle: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  taskInfo: [{ required: true, message: '请输入描述', trigger: 'blur' }]
}

function taskStatusText(s) { return ({ 0: '已完成', 1: '待处理', 2: '受理中', 3: '已驳回', 4: '已取消' })[s] || '未知' }
function taskStatusType(s) { return ({ 0: 'success', 1: 'warning', 2: 'primary', 3: 'danger', 4: 'info' })[s] || 'info' }

async function fetchData(forceRefresh = false) { loading.value = true; try { const p = { ...query.value }; Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] }); const res = await getTaskPage(p, { forceRefresh: !!forceRefresh }); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
async function fetchTypes() { const res = await getTaskTypes(); types.value = res.data }
function resetQuery() { query.value = { pageNum: 1, pageSize: 10, ownerId: '', taskOperatorId: '', taskType: undefined, taskStatus: undefined }; fetchData(true) }
function openAdd() { isEdit.value = false; form.value = getDefaultForm(); dialogVisible.value = true }
function openForgotPasswordTask() {
  isEdit.value = false
  const accountType = types.value.find(type => type.taskTypeName === '账户') || types.value.find(type => type.taskTypeId === 5)
  form.value = {
    taskTitle: '忘记原密码申请',
    taskType: accountType?.taskTypeId,
    taskInfo: `用户ID：${userStore.userInfo?.id || '-'}\n账户：${userStore.userInfo?.uid || '-'}\n请协助处理原密码遗忘问题。`,
    taskRemark: ''
  }
  dialogVisible.value = true
}
function openEdit(row) { isEdit.value = true; form.value = mapRowToForm(row); dialogVisible.value = true }
function getDefaultForm() { return { taskTitle: '', taskType: undefined, taskInfo: '', taskRemark: '' } }
function mapRowToForm(row) { return { id: row.id, taskTitle: row.taskTitle, taskType: row.taskType, taskInfo: row.taskInfo, taskRemark: row.taskRemark } }
function openProcess(row) { processForm.value = { id: row.id, status: row.taskStatus === 1 ? 2 : row.taskStatus, operatorId: row.taskOperatorId || '', remark: row.taskRemark || '' }; processVisible.value = true }

async function handleSubmit() { submitLoading.value = true; try { if (isEdit.value) { await updateTask(form.value); ElMessage.success('更新成功') } else { await addTask(form.value); ElMessage.success('提交成功') }; dialogVisible.value = false; fetchData(true) } finally { submitLoading.value = false } }
async function handleProcess() { processLoading.value = true; try { await processTask(processForm.value); ElMessage.success('处理成功'); processVisible.value = false; fetchData(true) } finally { processLoading.value = false } }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除?', '提示', { type: 'warning' }); await deleteTask(row.id); ElMessage.success('删除成功'); fetchData(true) }
onMounted(async () => {
  fetchData()
  await fetchTypes()
  if (route.query.intent === 'forgot-password') {
    openForgotPasswordTask()
  }
})
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
