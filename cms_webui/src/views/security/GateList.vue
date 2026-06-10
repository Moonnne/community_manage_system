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
        <el-form-item label="门岗编号" prop="gateCode">
          <el-input v-model="form.gateCode" :disabled="isEdit" maxlength="4" placeholder="至多两位大写字母和两位数字" @input="handleGateCodeInput" />
        </el-form-item>
        <el-form-item label="名称" prop="gateName"><el-input v-model="form.gateName" /></el-form-item>
        <el-form-item label="门岗负责人" prop="gateChiefId">
          <DualSelectV2
            v-model="form.gateChiefId"
            :options="userOptions"
            remote
            :remote-method="handleUserRemoteSearch"
            placeholder="输入以搜索用户"
            primary-key="label"
            secondary-key="userName"
            :filters="userFilterOptions"
            @filter-change="handleUserFilterChange"
          />
        </el-form-item>
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
import { getUserPage } from '@/api/user'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'
import DualSelectV2 from '@/components/DualSelectV2.vue'
import { useAgentPageControl } from '@/utils/agentControl'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const users = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})
const userFilters = ref({ userRole: undefined })
const userFilterOptions = [
  {
    key: 'userRole',
    placeholder: '用户类型',
    options: [
      { label: '系统管理员', value: 0 },
      { label: '物业经理', value: 1 },
      { label: '安保人员', value: 3 }
    ]
  }
]
const wizardSteps = [
  { title: '基础信息', fields: ['gateCode', 'gateName'] },
  { title: '确认信息', fields: [] }
]
const confirmRows = computed(() => [
  { label: '门岗编号', value: valueText(form.value.gateCode) },
  { label: '名称', value: valueText(form.value.gateName) },
  { label: '负责人ID', value: valueText(form.value.gateChiefId) },
  { label: '备注', value: valueText(form.value.gateRemark) }
])
const formRules = {
  gateCode: [
    { required: true, message: '请输入门岗编号', trigger: 'blur' },
    { pattern: /^[A-Z]{1,2}\d{1,2}$/, message: '门岗编号需由至多两位大写字母和至多两位数字组成', trigger: 'blur' }
  ],
  gateName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}
const userOptions = computed(() => users.value.length ? users.value.map(user => ({ value: user.id, label: user.uid || String(user.id), userName: user.userName || '-' })) : [{ value: '__empty_users__', label: '暂无匹配用户', userName: '', disabled: true, empty: true }])
function valueText(value) { return value === '' || value === undefined || value === null ? '-' : value }

async function fetchData(forceRefresh = false) { loading.value = true; try { const res = await getGateList({ forceRefresh: !!forceRefresh }); tableData.value = res.data } finally { loading.value = false } }
async function fetchUsers(keyword = '') { const res = await getUserPage({ pageNum: 1, pageSize: 1000, uid: keyword || undefined }, { forceRefresh: true }); const allowedRoles = userFilters.value.userRole === undefined ? [0, 1, 3] : [userFilters.value.userRole]; users.value = (res.data?.records || []).filter(user => allowedRoles.includes(user.userRole)) }
function handleUserRemoteSearch(keyword) { fetchUsers(keyword) }
function handleUserFilterChange(filters) { userFilters.value = { ...filters }; fetchUsers() }
function handleGateCodeInput(value) { const normalized = String(value || '').toUpperCase().replace(/[^A-Z0-9]/g, ''); const letters = (normalized.match(/[A-Z]/g) || []).join('').slice(0, 2); const digits = (normalized.match(/\d/g) || []).join('').slice(0, 2); form.value.gateCode = letters + digits }
function openAdd() { isEdit.value = false; form.value = getDefaultForm(); dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = mapRowToForm(row); dialogVisible.value = true }
function openAgentEdit(id) { const row = tableData.value.find(item => String(item.id) === String(id)); if (row) openEdit(row) }
function getDefaultForm() { return { gateCode: '', gateName: '', gateChiefId: '', gateRemark: '' } }
function mapRowToForm(row) { return { id: row.id, gateCode: row.gateCode, gateName: row.gateName, gateChiefId: row.gateChiefId, gateRemark: row.gateRemark } }
async function handleSubmit() { submitLoading.value = true; try { if (isEdit.value) { const payload = { ...form.value }; delete payload.gateCode; await updateGate(payload); ElMessage.success('更新成功') } else { await addGate(form.value); ElMessage.success('添加成功') }; dialogVisible.value = false; fetchData(true) } finally { submitLoading.value = false } }
async function handleDelete(row) { await deleteGate(row.id); ElMessage.success('删除成功'); fetchData(true) }

useAgentPageControl({
  onRefresh: () => fetchData(true),
  onOpenAdd: () => openAdd(),
  onOpenEdit: id => openAgentEdit(id)
})

onMounted(() => { fetchData(); fetchUsers() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.table-toolbar { margin-bottom: 12px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
</style>
