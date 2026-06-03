<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">访客管理</h2></div>
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5"><el-input v-model="query.visitorName" placeholder="访客姓名" clearable /></el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5"><el-input v-model="query.ownerId" placeholder="业主ID" clearable /></el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-select v-model="query.visitorType" placeholder="访客类型" clearable>
            <el-option v-for="t in types" :key="t.visitorTypeId" :label="t.visitorTypeName" :value="t.visitorTypeId" />
          </el-select>
        </el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-select v-model="query.visitorStatus" placeholder="状态" clearable>
            <el-option label="待批准" value="1" />
            <el-option label="访问中" value="2" />
            <el-option label="已结束" value="0" />
            <el-option label="已驳回" value="3" />
          </el-select>
        </el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-col>
      </el-row>
    </el-card>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit"><el-icon><Plus /></el-icon> 登记访客</el-button>
      <el-button @click="fetchData(true)">刷新</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="visitorCode" label="访客编号" width="140" show-overflow-tooltip />
        <el-table-column prop="visitorName" label="姓名" width="100" show-overflow-tooltip />
        <el-table-column prop="visitorPhone" label="电话" width="130" show-overflow-tooltip />
        <el-table-column prop="ownerName" label="被访业主" width="100" show-overflow-tooltip />
        <el-table-column prop="visitorTypeName" label="类型" width="100" show-overflow-tooltip>
          <template #default="{ row }"><el-tag effect="plain">{{ row.visitorTypeName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="visitorStatus" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="visitorStatusType(row.visitorStatus)" effect="plain">{{ visitorStatusText(row.visitorStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visitorCreateTime" label="登记时间" width="170" show-overflow-tooltip />
        <el-table-column prop="visitorRemark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该访问记录吗？" confirm-button-text="删除" cancel-button-text="取消" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small" @click.stop="() => {}">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50]" :total="total" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <WizardFormDrawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑访客' : '登记访客'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="isEdit ? editWizardSteps : addWizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
        <el-form-item label="访客姓名" prop="visitorName"><el-input v-model="form.visitorName" /></el-form-item>
        <el-form-item label="访客电话" prop="visitorPhone"><el-input v-model="form.visitorPhone" maxlength="11" placeholder="请输入手机号" clearable @input="handleVisitorPhoneInput" /></el-form-item>
        <el-form-item label="访客类型" prop="visitorType">
          <el-select v-model="form.visitorType" placeholder="请选择"><el-option v-for="t in types" :key="t.visitorTypeId" :label="t.visitorTypeName" :value="t.visitorTypeId" /></el-select>
        </el-form-item>
        </template>
        <template v-else-if="activeStep === 1">
        <el-form-item label="被访业主" prop="ownerId">
          <DualSelectV2
            v-model="form.ownerId"
            :options="userOptions"
            remote
            :remote-method="handleUserRemoteSearch"
            placeholder="输入以搜索用户"
            primary-key="label"
            secondary-key="userName"
            @change="handleOwnerChange"
          />
        </el-form-item>
        <el-form-item label="访问资产">
          <DualSelectV2
            v-model="form.assetId"
            :options="assetOptions"
            remote
            :remote-method="handleAssetRemoteSearch"
            :disabled="!form.ownerId"
            placeholder="选择用户后搜索资产"
            primary-key="label"
            secondary-key="assetCode"
          />
        </el-form-item>
        </template>
        <template v-else-if="isEdit && activeStep === 2">
        <el-form-item label="状态" v-if="isEdit">
          <el-select v-model="form.visitorStatus" placeholder="请选择">
            <el-option label="待批准" value="1" />
            <el-option label="访问中" value="2" />
            <el-option label="已结束" value="0" />
            <el-option label="已驳回" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.visitorRemark" type="textarea" :rows="2" /></el-form-item>
        </template>
        <template v-else-if="!isEdit && activeStep === 2">
          <el-form-item label="备注"><el-input v-model="form.visitorRemark" type="textarea" :rows="2" /></el-form-item>
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
import { getVisitorPage, getVisitorTypes, addVisitor, updateVisitor, deleteVisitor } from '@/api/visitor'
import { getUserPage } from '@/api/user'
import { getAssetPage } from '@/api/asset'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'
import DualSelectV2 from '@/components/DualSelectV2.vue'
import { optionalPhoneRule, sanitizePhone } from '@/utils/validators'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1, 3].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const types = ref([])
const users = ref([])
const assets = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, visitorName: '', ownerId: '', visitorType: undefined, visitorStatus: '' })
const form = ref({})
const addWizardSteps = [
  { title: '访客信息', fields: ['visitorName', 'visitorPhone'] },
  { title: '被访信息', fields: ['ownerId', 'visitorType'] },
  { title: '备注信息', fields: [] },
  { title: '确认信息', fields: [] }
]
const editWizardSteps = [
  { title: '访客信息', fields: ['visitorName', 'visitorPhone'] },
  { title: '被访信息', fields: ['ownerId', 'visitorType'] },
  { title: '状态信息', fields: [] },
  { title: '确认信息', fields: [] }
]
const confirmRows = computed(() => [
  { label: '姓名', value: valueText(form.value.visitorName) },
  { label: '电话', value: valueText(form.value.visitorPhone) },
  { label: '业主ID', value: valueText(form.value.ownerId) },
  { label: '资产ID', value: valueText(form.value.assetId) },
  { label: '类型', type: 'tag', value: visitorTypeNameText(form.value.visitorType) },
  { label: '状态', type: 'tag', tagType: visitorStatusType(form.value.visitorStatus), value: visitorStatusText(form.value.visitorStatus) },
  { label: '备注', value: valueText(form.value.visitorRemark) }
])
const formRules = {
  visitorName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  visitorPhone: [optionalPhoneRule()],
  ownerId: [{ required: true, message: '请输入业主ID', trigger: 'blur' }],
  visitorType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}
const userOptions = computed(() => users.value.length ? users.value.map(user => ({ value: user.id, label: user.uid || String(user.id), userName: user.userName || '-' })) : [{ value: '__empty_users__', label: '暂无匹配用户', userName: '', disabled: true, empty: true }])
const assetOptions = computed(() => assets.value.length ? assets.value.map(asset => ({ value: asset.id, label: asset.assetName || asset.assetCode || String(asset.id), assetCode: asset.assetCode || '-' })) : [{ value: '__empty_assets__', label: '暂无匹配资产', assetCode: '', disabled: true, empty: true }])

function valueText(value) { return value === '' || value === undefined || value === null ? '-' : value }
function visitorTypeNameText(id) { return types.value.find(type => type.visitorTypeId === id)?.visitorTypeName || valueText(id) }

function handleVisitorPhoneInput(value) {
  form.value.visitorPhone = sanitizePhone(value)
}

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const p = { ...query.value }; Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] })
    const res = await getVisitorPage(p, { forceRefresh: !!forceRefresh }); tableData.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}
async function fetchTypes() { const res = await getVisitorTypes(); types.value = res.data }
async function fetchUsers(keyword = '') { const res = await getUserPage({ pageNum: 1, pageSize: 100, uid: keyword || undefined }, { forceRefresh: true }); users.value = res.data?.records || [] }
async function fetchAssets(keyword = '') { const res = await getAssetPage({ pageNum: 1, pageSize: 100, assetOwnerId: form.value.ownerId || undefined, assetName: keyword || undefined }, { forceRefresh: true }); assets.value = res.data?.records || [] }
function handleUserRemoteSearch(keyword) { fetchUsers(keyword) }
function handleAssetRemoteSearch(keyword) { fetchAssets(keyword) }
function handleOwnerChange() { form.value.assetId = ''; fetchAssets() }
function resetQuery() { query.value = { pageNum: 1, pageSize: 10, visitorName: '', ownerId: '', visitorType: undefined, visitorStatus: '' }; fetchData(true) }
function openAdd() { isEdit.value = false; form.value = getDefaultForm(); dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = mapRowToForm(row); dialogVisible.value = true }
function getDefaultForm() { return { visitorName: '', visitorPhone: '', ownerId: '', assetId: '', visitorType: undefined, visitorStatus: 1, visitorRemark: '' } }
function mapRowToForm(row) { return { id: row.id, visitorName: row.visitorName, visitorPhone: row.visitorPhone, ownerId: row.ownerId, assetId: row.assetId, visitorType: row.visitorType, visitorStatus: row.visitorStatus, visitorRemark: row.visitorRemark } }
async function handleSubmit() { submitLoading.value = true; try { if (isEdit.value) { await updateVisitor(form.value); ElMessage.success('更新成功') } else { await addVisitor(form.value); ElMessage.success('添加成功') }; dialogVisible.value = false; fetchData(true) } finally { submitLoading.value = false } }
async function handleDelete(row) { await deleteVisitor(row.id); ElMessage.success('删除成功'); fetchData(true) }
function visitorStatusText(status) { return ({ '0': '已结束', '1': '待批准', '2': '访问中', '3': '已驳回' })[String(status)] || '未知' }
function visitorStatusType(status) { return ({ '0': 'success', '1': 'warning', '2': 'primary', '3': 'danger' })[String(status)] || 'info' }
onMounted(() => { fetchData(); fetchTypes(); fetchUsers() })
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
