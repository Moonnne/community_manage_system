<template>
  <div class="page-container task-dashboard">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的工单</h2>
        <p class="page-subtitle">提交、查看并跟踪物业服务工单</p>
      </div>
      <div class="header-actions">
        <el-input v-model="query.title" placeholder="按标题搜索" clearable @input="fetchData(true)">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="openSubmit"><el-icon><Plus /></el-icon>提交工单</el-button>
      </div>
    </div>

    <section class="task-section">
      <h3>我的工单</h3>
      <div v-if="myTasks.length > 0" class="task-card-list">
        <TaskCard v-for="task in myTasks" :key="task.id" :task="task" @click="openDetail(task, false)" />
      </div>
      <el-empty v-else description="暂无工单" />
    </section>

    <section v-if="canProcess" class="task-section">
      <div class="section-head">
        <h3>处理工单</h3>
        <div class="process-filters">
          <el-select v-model="query.taskType" placeholder="类型" clearable @change="fetchData(true)">
            <el-option v-for="type in taskTypes" :key="type.taskTypeId" :label="type.taskTypeName" :value="type.taskTypeId" />
          </el-select>
          <el-input v-model="query.ownerId" placeholder="发起人ID" clearable @input="fetchData(true)" />
        </div>
      </div>
      <div v-if="processingTasks.length > 0" class="task-card-list">
        <TaskCard v-for="task in processingTasks" :key="task.id" :task="task" process @click="openDetail(task, true)" />
      </div>
      <el-empty v-else description="暂无待处理工单" />
    </section>

    <el-dialog v-model="submitVisible" width="620px" class="rounded-dialog" title="提交工单" :close-on-click-modal="false">
      <el-form :model="submitForm" label-width="82px">
        <el-form-item label="标题" required><el-input v-model="submitForm.taskTitle" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="submitForm.taskType" placeholder="请选择类型" class="full-width">
            <el-option v-for="type in taskTypes" :key="type.taskTypeId" :label="type.taskTypeName" :value="type.taskTypeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" required><el-input v-model="submitForm.taskInfo" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTask">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" width="760px" class="rounded-dialog" title="工单详情">
      <el-descriptions v-if="currentTask" :column="2">
        <el-descriptions-item label="工单编号">{{ formatValue(currentTask.taskCode) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="taskStatusType(currentTask.taskStatus)" effect="plain">{{ taskStatusText(currentTask.taskStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题">{{ formatValue(currentTask.taskTitle) }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ formatValue(currentTask.taskTypeName) }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ formatValue(currentTask.ownerName) }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ formatValue(currentTask.operatorName) }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ formatValue(currentTask.taskInfo) }}</el-descriptions-item>
        <el-descriptions-item label="回执" :span="2">{{ formatValue(currentTask.taskRemark) }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-if="detailProcessMode" class="process-form" label-width="82px">
        <el-form-item label="处理状态">
          <el-select v-model="processForm.status" class="full-width">
            <el-option label="已完成" :value="0" />
            <el-option label="处理中" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理回执">
          <el-input v-model="processForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detailProcessMode" type="primary" @click="submitProcess">保存回执</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { ElCard, ElMessage, ElTag } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { addTask, getTaskTypes, processTask } from '@/api/task'
import { getTaskDashboard } from '@/api/dashboard'

const userStore = useUserStore()
const query = ref({ title: '', taskType: undefined, ownerId: '' })
const taskTypes = ref([])
const myTasks = ref([])
const processingTasks = ref([])
const submitVisible = ref(false)
const detailVisible = ref(false)
const detailProcessMode = ref(false)
const currentTask = ref(null)
const submitForm = ref({ taskTitle: '', taskType: undefined, taskInfo: '' })
const processForm = ref({ status: 2, remark: '' })
const canProcess = computed(() => [0, 1].includes(userStore.userRole))

const TaskCard = defineComponent({
  props: { task: { type: Object, required: true }, process: { type: Boolean, default: false } },
  emits: ['click'],
  setup(props, { emit }) {
    return () => h(ElCard, { shadow: 'never', class: 'task-card', onClick: () => emit('click') }, {
      default: () => [
        h('div', { class: 'task-card-top' }, [
          h('span', null, formatValue(props.task.taskCode)),
          h(ElTag, { type: taskStatusType(props.task.taskStatus), effect: 'plain' }, () => taskStatusText(props.task.taskStatus))
        ]),
        h('h3', null, formatValue(props.task.taskTitle)),
        h('p', null, formatValue(props.task.taskInfo)),
        h('div', { class: 'task-card-meta' }, [
          h('span', null, props.process ? `提交人：${formatValue(props.task.ownerName)}` : `类型：${formatValue(props.task.taskTypeName)}`),
          h('strong', null, formatValue(props.task.taskCreateTime))
        ])
      ]
    })
  }
})

function formatValue(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function taskStatusText(status) {
  return ({ 0: '已完成', 1: '待处理', 2: '处理中' })[status] || '未知'
}

function taskStatusType(status) {
  return ({ 0: 'success', 1: 'warning', 2: 'primary' })[status] || 'info'
}

async function fetchData(forceRefresh = false) {
  const params = {
    title: query.value.title,
    taskType: query.value.taskType,
    ownerId: query.value.ownerId || undefined
  }
  const res = await getTaskDashboard(params, { forceRefresh })
  myTasks.value = res.data?.myTasks || []
  processingTasks.value = res.data?.processingTasks || []
}

async function fetchTypes() {
  const res = await getTaskTypes()
  taskTypes.value = res.data || []
}

function openSubmit() {
  submitForm.value = { taskTitle: '', taskType: undefined, taskInfo: '' }
  submitVisible.value = true
}

async function submitTask() {
  if (!submitForm.value.taskTitle || !submitForm.value.taskType || !submitForm.value.taskInfo) {
    ElMessage.warning('请完善工单信息')
    return
  }
  await addTask(submitForm.value)
  ElMessage.success('工单已提交')
  submitVisible.value = false
  fetchData(true)
}

function openDetail(task, processMode) {
  currentTask.value = task
  detailProcessMode.value = processMode
  processForm.value = { status: task.taskStatus === 1 ? 2 : task.taskStatus, remark: task.taskRemark || '' }
  detailVisible.value = true
}

async function submitProcess() {
  await processTask({
    id: currentTask.value.id,
    status: processForm.value.status,
    operatorId: userStore.userInfo?.id,
    remark: processForm.value.remark
  })
  ElMessage.success('处理回执已保存')
  detailVisible.value = false
  fetchData(true)
}

onMounted(() => {
  fetchTypes()
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header, .section-head, .task-card-top, .task-card-meta { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.page-header { margin-bottom: 18px; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 22px; font-weight: 800; }
.page-subtitle { margin: 8px 0 0; color: var(--vp-c-text-3); font-size: 13px; }
.header-actions { display: flex; gap: 12px; width: min(520px, 100%); }
.task-section { margin-top: 20px; }
.task-section h3 { margin: 0 0 14px; color: var(--vp-c-text-1); font-size: 18px; }
.process-filters { display: flex; gap: 10px; width: min(520px, 100%); }
.task-card-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.task-card { cursor: pointer; border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.task-card h3 { margin: 18px 0 8px; color: var(--vp-c-text-1); font-size: 18px; }
.task-card p { display: -webkit-box; min-height: 42px; margin: 0; overflow: hidden; color: var(--vp-c-text-2); -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.task-card-meta { margin-top: 18px; color: var(--vp-c-text-3); font-size: 13px; }
.task-card-meta strong { color: var(--vp-c-text-1); }
.full-width { width: 100%; }
.process-form { margin-top: 18px; padding-top: 18px; border-top: 1px solid var(--vp-c-divider); }
:deep(.rounded-dialog .el-dialog) { border-radius: 16px; }
@media (max-width: 768px) {
  .page-header, .section-head { flex-direction: column; }
  .header-actions, .process-filters { flex-direction: column; width: 100%; }
  .task-card-list { grid-template-columns: 1fr; }
  :deep(.rounded-dialog .el-dialog) { width: calc(100vw - 24px) !important; }
}
</style>
