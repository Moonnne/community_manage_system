<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">门岗管理</h2></div>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit"><el-icon><Plus /></el-icon> 新增门岗</el-button>
      <el-button @click="fetchData(true)">刷新</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" show-overflow-tooltip />
        <el-table-column prop="gateCode" label="门岗编号" width="140" show-overflow-tooltip />
        <el-table-column prop="gateName" label="门岗名称" width="160" show-overflow-tooltip />
        <el-table-column prop="chiefName" label="负责人" width="120" show-overflow-tooltip />
        <el-table-column prop="gateRemark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该门岗吗？" confirm-button-text="删除" cancel-button-text="取消" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small" @click.stop="() => {}">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <WizardFormDrawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑门岗' : '新增门岗'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="wizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
        <el-form-item label="名称" prop="gateName"><el-input v-model="form.gateName" /></el-form-item>
        <el-form-item label="负责人ID" prop="gateChiefId"><el-input v-model.number="form.gateChiefId" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.gateRemark" type="textarea" :rows="2" /></el-form-item>
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
import { getGateList, addGate, updateGate, deleteGate } from '@/api/gate'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})
const wizardSteps = [
  { title: '基础信息', fields: ['gateName'] },
  { title: '确认信息', fields: [] }
]
const confirmRows = computed(() => [
  { label: '名称', value: valueText(form.value.gateName) },
  { label: '负责人ID', value: valueText(form.value.gateChiefId) },
  { label: '备注', value: valueText(form.value.gateRemark) }
])
const formRules = { gateName: [{ required: true, message: '请输入名称', trigger: 'blur' }] }
function valueText(value) { return value === '' || value === undefined || value === null ? '-' : value }

async function fetchData(forceRefresh = false) { loading.value = true; try { const res = await getGateList({ forceRefresh: !!forceRefresh }); tableData.value = res.data } finally { loading.value = false } }
function openAdd() { isEdit.value = false; form.value = getDefaultForm(); dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = mapRowToForm(row); dialogVisible.value = true }
function getDefaultForm() { return { gateName: '', gateChiefId: '', gateRemark: '' } }
function mapRowToForm(row) { return { id: row.id, gateName: row.gateName, gateChiefId: row.gateChiefId, gateRemark: row.gateRemark } }
async function handleSubmit() { submitLoading.value = true; try { if (isEdit.value) { await updateGate(form.value); ElMessage.success('更新成功') } else { await addGate(form.value); ElMessage.success('添加成功') }; dialogVisible.value = false; fetchData(true) } finally { submitLoading.value = false } }
async function handleDelete(row) { await deleteGate(row.id); ElMessage.success('删除成功'); fetchData(true) }
onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.table-toolbar { margin-bottom: 12px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
</style>
