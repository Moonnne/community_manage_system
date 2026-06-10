<template>
  <div class="page-container bill-page">
    <div class="page-header">
      <h2 class="page-title">账单管理</h2>
    </div>

    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-input v-model="query.billCode" placeholder="账单编号" clearable />
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-input v-model="query.ownerUid" placeholder="账单所有人" clearable />
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-input v-model="query.createrUid" placeholder="账单创建人" clearable />
        </el-col>
      </el-row>
      <el-row :gutter="16" class="search-second-row">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-select v-model="query.billStatus" placeholder="账单状态" clearable>
            <el-option v-for="item in billStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="24" :md="16" :lg="12">
          <el-date-picker
            v-model="query.timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            start-placeholder="起始时间区间"
            end-placeholder="截止时间区间"
            class="full-width"
          />
        </el-col>
        <el-col :xs="24" :sm="24" :md="24" :lg="6" class="search-actions">
          <el-button type="primary" @click="fetchData(true)"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <div class="table-toolbar bill-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit">
        <el-icon><Plus /></el-icon>新增账单
      </el-button>
      <el-button type="danger" plain :disabled="selectedBills.length === 0" @click="handleBatchDelete" v-if="canEdit">
        <el-icon><Delete /></el-icon>批量删除
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
        style="width: 100%"
        @cell-click="handleTableCellClick"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" fixed="left" />
        <el-table-column prop="billCode" label="账单编号" width="200" sortable show-overflow-tooltip />
        <el-table-column prop="ownerName" label="账单所有人" width="140" show-overflow-tooltip />
        <el-table-column prop="createrName" label="创建人" width="140" show-overflow-tooltip />
        <el-table-column prop="assetName" label="归属资产" width="200" show-overflow-tooltip />
        <el-table-column label="总金额" width="120" sortable show-overflow-tooltip>
          <template #default="{ row }">￥{{ formatMoney(row.billTotal) }}</template>
        </el-table-column>
        <el-table-column label="已支付" width="120" show-overflow-tooltip>
          <template #default="{ row }">￥{{ formatMoney(row.billPaid) }}</template>
        </el-table-column>
        <el-table-column label="待付款" width="120" show-overflow-tooltip>
          <template #default="{ row }">￥{{ amountDue(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="billStatusType(row.billStatus)" effect="plain">{{ billStatusText(row.billStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billStartTime" label="开始时间" width="160" sortable show-overflow-tooltip />
        <el-table-column prop="billEndTime" label="截止时间" width="160" sortable show-overflow-tooltip />
        <el-table-column prop="billRemark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该账单吗？" confirm-button-text="删除" cancel-button-text="取消" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small" @click.stop="() => {}">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <WizardFormDrawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑账单' : '新增账单'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="wizardSteps"
      :model="form"
      :rules="formRules"
      label-width="96px"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
          <el-alert v-if="!isEdit" type="info" show-icon :closable="false" class="form-tip" center>
            账单编号由系统自动生成，将作为账单唯一标识符
          </el-alert>

          <el-form-item label="选择用户" prop="ownerIds">
            <el-select-v2
              v-model="form.ownerIds"
              filterable
              remote
              clearable
              :remote-method="handleUserRemoteSearch"
              :options="visibleUserSelectOptions"
              class="full-width"
              placeholder="输入以搜索用户"
              @change="handleUserChange"
            >
              <template #header>
                <div class="user-select-header" @mousedown.stop @click.stop>
                  <el-select v-model="userFilters.userRole" placeholder="用户类型" clearable @change="reloadUsersByFilter">
                    <el-option
                      v-for="role in userRoleOptions"
                      :key="role.roleId ?? role.userRoleId"
                      :label="role.roleName || role.userRoleName"
                      :value="role.roleId ?? role.userRoleId"
                    />
                  </el-select>
                  <el-select v-model="userFilters.userStatus" placeholder="用户状态" clearable @change="reloadUsersByFilter">
                    <el-option label="正常" :value="0" />
                    <el-option label="冻结" :value="1" />
                    <el-option label="禁用" :value="2" />
                  </el-select>
                </div>
              </template>
              <template #default="{ item }">
                <div v-if="item.empty" class="select-empty-row">
                  {{ item.label }}
                </div>
                <div v-else class="dual-option-row">
                  <strong>{{ item.label }}</strong>
                  <span>{{ item.userName }}</span>
                </div>
              </template>
            </el-select-v2>
          </el-form-item>

          <el-form-item label="选择资产" prop="assetIds">
            <el-select-v2
              v-model="form.assetIds"
              :multiple="!isEdit"
              filterable
              remote
              clearable
              :disabled="normalizedOwnerIds.length === 0"
              :remote-method="handleAssetRemoteSearch"
              :options="visibleAssetSelectOptions"
              class="full-width"
              placeholder="选择用户后即可选择资产"
            >
              <template #header>
                <div class="asset-select-header" @mousedown.stop @click.stop>
                  <el-select v-model="assetFilters.assetType" placeholder="资产类型" clearable>
                    <el-option v-for="type in assetTypeOptions" :key="type.assetTypeId" :label="type.assetTypeName" :value="type.assetTypeId" />
                  </el-select>
                  <el-select v-model="assetFilters.assetStatus" placeholder="资产状态" clearable>
                    <el-option label="正常" :value="0" />
                    <el-option label="过期" :value="1" />
                    <el-option label="未使用" :value="2" />
                    <el-option label="冻结" :value="3" />
                  </el-select>
                  <el-button v-if="!isEdit" plain @click.stop="selectAllVisibleAssets">全选当前结果</el-button>
                </div>
              </template>
              <template #default="{ item }">
                <div v-if="item.empty" class="select-empty-row">
                  {{ item.label }}
                </div>
                <div v-else class="dual-option-row">
                  <strong>{{ item.label }}</strong>
                  <span>{{ item.assetCode }}</span>
                </div>
              </template>
            </el-select-v2>
          </el-form-item>

          <el-form-item label="开始时间" prop="billStartTime">
            <el-date-picker v-model="form.billStartTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="请选择开始时间" class="full-width" />
          </el-form-item>
          <el-form-item label="截止时间" prop="billEndTime">
            <el-date-picker v-model="form.billEndTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="请选择截止时间" class="full-width" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.billRemark" type="textarea" :rows="3" maxlength="200" show-word-limit />
          </el-form-item>
        </template>

        <template v-else-if="activeStep === 1">
          <div class="detail-editor">
            <div class="detail-toolbar">
              <el-input v-model="detailKeyword" placeholder="搜索账目" clearable>
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-button type="primary" @click="addDetailItem"><el-icon><Plus /></el-icon>添加款项</el-button>
            </div>

            <el-collapse v-model="activeDetailPanels" class="detail-collapse">
              <el-collapse-item v-for="(item, index) in filteredDetails" :key="item.localId" :name="item.localId">
                <template #title>
                  <div class="detail-title">
                    <span>{{ item.billDetailName || `账目 ${index + 1}` }}</span>
                    <strong>￥{{ formatMoney(detailTotal(item)) }}</strong>
                  </div>
                </template>
                <el-row :gutter="12">
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="账目名" required>
                      <el-input v-model="item.billDetailName" placeholder="请输入账目名" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="类型" required>
                      <el-select v-model="item.billDetailType" placeholder="请选择类型" filterable class="full-width">
                        <el-option v-for="type in billTypeOptions" :key="type.billTypeId" :label="type.billTypeName" :value="type.billTypeId" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="数量" required>
                      <el-input-number v-model="item.billDetailQuantity" :min="1" :precision="0" controls-position="right" class="full-width" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="单价" required>
                      <el-input-number v-model="item.billDetailUnitPrice" :min="0" :precision="2" controls-position="right" class="full-width">
                        <template #prefix>
                          <span>￥</span>
                        </template>
                      </el-input-number>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24">
                    <el-form-item label="备注">
                      <el-input v-model="item.billDetailRemark" type="textarea" :rows="2" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <div class="detail-item-actions">
                  <el-button type="danger" plain size="small" @click.stop="removeDetailItem(item)">删除款项</el-button>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </template>

        <template v-else>
          <ConfirmInfoTable :rows="confirmRows" />
          <el-table :data="details" stripe class="confirm-detail-table">
            <el-table-column prop="billDetailName" label="账目名" min-width="140" show-overflow-tooltip />
            <el-table-column label="费用类型" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ billTypeName(row.billDetailType) }}</template>
            </el-table-column>
            <el-table-column prop="billDetailQuantity" label="数量" width="80" show-overflow-tooltip />
            <el-table-column label="单价" width="100" show-overflow-tooltip>
              <template #default="{ row }">￥{{ formatMoney(row.billDetailUnitPrice) }}</template>
            </el-table-column>
            <el-table-column label="金额" width="110" show-overflow-tooltip>
              <template #default="{ row }">￥{{ formatMoney(detailTotal(row)) }}</template>
            </el-table-column>
          </el-table>
        </template>
      </template>
    </WizardFormDrawer>

    <el-dialog v-model="detailVisible" title="账单详情" width="820px" class="bill-detail-dialog">
      <template v-if="currentBill">
        <el-descriptions :column="2" class="bill-detail-desc">
          <el-descriptions-item label="账单编号">{{ formatValue(currentBill.billCode) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="billStatusType(currentBill.billStatus)" effect="plain">{{ billStatusText(currentBill.billStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="业主">{{ formatValue(currentBill.ownerName) }}</el-descriptions-item>
          <el-descriptions-item label="资产">{{ formatValue(currentBill.assetName) }}</el-descriptions-item>
          <el-descriptions-item label="总金额">￥{{ formatMoney(currentBill.billTotal) }}</el-descriptions-item>
          <el-descriptions-item label="待付款">￥{{ amountDue(currentBill) }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatValue(currentBill.billEndTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ formatValue(currentBill.billRemark) }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="viewDetails" stripe v-loading="detailLoading" class="bill-detail-table">
          <el-table-column prop="id" label="ID" width="80" show-overflow-tooltip />
          <el-table-column prop="billDetailName" label="账目名" min-width="130" show-overflow-tooltip />
          <el-table-column prop="billDetailTypeName" label="费用类型" width="120" show-overflow-tooltip />
          <el-table-column prop="billDetailQuantity" label="数量" width="80" show-overflow-tooltip />
          <el-table-column label="单价" width="100" show-overflow-tooltip>
            <template #default="{ row }">￥{{ formatMoney(row.billDetailUnitPrice) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="100" show-overflow-tooltip>
            <template #default="{ row }">￥{{ formatMoney(row.billDetailTotal) }}</template>
          </el-table-column>
          <el-table-column label="待付" width="100" show-overflow-tooltip>
            <template #default="{ row }">￥{{ formatMoney(row.billDetailDue) }}</template>
          </el-table-column>
        </el-table>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { addBill, batchDeleteBills, deleteBill, getBill, getBillDetails, getBillPage, getBillTypes, updateBill } from '@/api/bill'
import { getUserPage, getUserRoles } from '@/api/user'
import { getAssetPage, getAssetTypes } from '@/api/asset'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'
import { useAgentPageControl } from '@/utils/agentControl'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1, 2].includes(userStore.userRole) && userStore.canOperate)

const billStatusOptions = [
  { label: '已完成', value: 0 },
  { label: '未完成', value: 1 },
  { label: '部分完成', value: 2 },
  { label: '已逾期', value: 3 },
  { label: '已补缴', value: 4 },
  { label: '已取消', value: 5 }
]

const loading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedBills = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const currentBill = ref(null)
const viewDetails = ref([])
const userOptions = ref([])
const userRoleOptions = ref([])
const assetOptions = ref([])
const assetTypeOptions = ref([])
const billTypeOptions = ref([])
const detailKeyword = ref('')
const activeDetailPanels = ref([])
const details = ref([])

const query = ref({ pageNum: 1, pageSize: 10, billCode: '', ownerUid: '', createrUid: '', billStatus: undefined, timeRange: [] })
const agentQueryKeys = ['billCode', 'ownerUid', 'createrUid', 'billStatus']
const userFilters = ref({ uid: '', userRole: undefined, userStatus: undefined })
const assetFilters = ref({ keyword: '', assetType: undefined, assetStatus: undefined })
const form = ref(getDefaultForm())
const wizardSteps = [
  { title: '账单信息', fields: ['ownerIds', 'assetIds', 'billStartTime', 'billEndTime'] },
  { title: '账单款项', fields: [], validate: validateDetails },
  { title: '确认信息', fields: [] }
]
const formRules = {
  ownerIds: [{ required: true, validator: validateSelected, message: '请选择用户', trigger: 'change' }],
  assetIds: [{ required: true, validator: validateSelected, message: '请选择资产', trigger: 'change' }],
  billStartTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  billEndTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

const normalizedOwnerIds = computed(() => Array.isArray(form.value.ownerIds) ? form.value.ownerIds : (form.value.ownerIds ? [form.value.ownerIds] : []))
const normalizedAssetIds = computed(() => Array.isArray(form.value.assetIds) ? form.value.assetIds : (form.value.assetIds ? [form.value.assetIds] : []))
const userSelectOptions = computed(() => userOptions.value.map(user => ({
  value: user.id,
  label: formatValue(user.uid),
  userName: formatValue(user.userName)
})))
const visibleUserSelectOptions = computed(() => {
  if (userSelectOptions.value.length > 0) return userSelectOptions.value
  return [{ value: '__empty_users__', label: '暂无匹配用户', userName: '', disabled: true, empty: true }]
})
const filteredAssets = computed(() => {
  const keyword = assetFilters.value.keyword.trim().toLowerCase()
  return assetOptions.value.filter(asset => {
    const matchesKeyword = !keyword
      || String(asset.assetName || '').toLowerCase().includes(keyword)
      || String(asset.assetCode || '').toLowerCase().includes(keyword)
    const matchesType = assetFilters.value.assetType === undefined || assetFilters.value.assetType === '' || asset.assetType === assetFilters.value.assetType
    const matchesStatus = assetFilters.value.assetStatus === undefined || assetFilters.value.assetStatus === '' || asset.assetStatus === assetFilters.value.assetStatus
    return matchesKeyword && matchesType && matchesStatus
  })
})
const assetSelectOptions = computed(() => filteredAssets.value.map(asset => ({
  value: asset.id,
  label: formatValue(asset.assetName),
  assetCode: formatValue(asset.assetCode)
})))
const visibleAssetSelectOptions = computed(() => {
  if (assetSelectOptions.value.length > 0) return assetSelectOptions.value
  return [{ value: '__empty_assets__', label: '暂无匹配资产', assetCode: '', disabled: true, empty: true }]
})
const filteredDetails = computed(() => {
  const keyword = detailKeyword.value.trim().toLowerCase()
  if (!keyword) return details.value
  return details.value.filter(item => String(item.billDetailName || '').toLowerCase().includes(keyword))
})
const totalAmount = computed(() => details.value.reduce((sum, item) => sum + detailTotal(item), 0))
const confirmRows = computed(() => [
  { label: '用户', value: selectedUserNames.value },
  { label: '资产', value: selectedAssetNames.value },
  { label: '开始时间', value: formatValue(form.value.billStartTime) },
  { label: '截止时间', value: formatValue(form.value.billEndTime) },
  { label: '备注', value: formatValue(form.value.billRemark) },
  { label: '总金额', value: `￥${formatMoney(totalAmount.value)}` },
  { label: '默认状态', type: 'tag', tagType: 'warning', value: '未完成' }
])
const selectedUserNames = computed(() => userOptions.value
  .filter(item => normalizedOwnerIds.value.includes(item.id))
  .map(item => `${formatValue(item.uid)} / ${formatValue(item.userName)}`)
  .join('，') || '-')
const selectedAssetNames = computed(() => assetOptions.value
  .filter(item => normalizedAssetIds.value.includes(item.id))
  .map(item => formatValue(item.assetName))
  .join('，') || '-')

function validateSelected(_rule, value, callback) {
  const list = Array.isArray(value) ? value : (value ? [value] : [])
  if (list.length === 0) callback(new Error('请选择'))
  else callback()
}

function billStatusText(status) {
  return billStatusOptions.find(item => item.value === status)?.label || '未知'
}

function billStatusType(status) {
  return ({ 0: 'success', 1: 'warning', 2: 'primary', 3: 'danger', 4: 'success', 5: 'info' })[status] || 'info'
}

function formatValue(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

function amountDue(bill) {
  const due = Number(bill?.billTotal || 0) - Number(bill?.billPaid || 0)
  return Math.max(due, 0).toFixed(2)
}

function detailTotal(item) {
  return Number(item.billDetailQuantity || 0) * Number(item.billDetailUnitPrice || 0)
}

function billTypeName(id) {
  return billTypeOptions.value.find(item => item.billTypeId === id)?.billTypeName || '-'
}

function cleanParams(params) {
  const cleaned = { ...params }
  Object.keys(cleaned).forEach(key => {
    if (cleaned[key] === '' || cleaned[key] === undefined || cleaned[key] === null) delete cleaned[key]
  })
  return cleaned
}

function getDefaultForm() {
  return { ownerIds: null, assetIds: [], billStartTime: '', billEndTime: '', billRemark: '' }
}

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const params = cleanParams({ ...query.value })
    if (query.value.timeRange?.length === 2) {
      params.rangeStart = query.value.timeRange[0]
      params.rangeEnd = query.value.timeRange[1]
    }
    delete params.timeRange
    const res = await getBillPage(params, { forceRefresh: !!forceRefresh })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.value = { pageNum: 1, pageSize: 10, billCode: '', ownerUid: '', createrUid: '', billStatus: undefined, timeRange: [] }
  fetchData(true)
}

async function loadUsers() {
  const res = await getUserPage(cleanParams({
    pageNum: 1,
    pageSize: 100,
    uid: userFilters.value.uid,
    userRole: userFilters.value.userRole,
    userStatus: userFilters.value.userStatus
  }), { forceRefresh: true })
  userOptions.value = res.data.records || []
}

function handleUserRemoteSearch(keyword) {
  userFilters.value.uid = keyword
  loadUsers()
}

function reloadUsersByFilter() {
  loadUsers()
}

async function loadRoles() {
  const res = await getUserRoles()
  userRoleOptions.value = res.data || []
}

async function loadBillTypes() {
  const res = await getBillTypes()
  billTypeOptions.value = res.data || []
}

async function loadAssetTypes() {
  const res = await getAssetTypes()
  assetTypeOptions.value = res.data || []
}

async function loadAssets() {
  const ownerIds = normalizedOwnerIds.value
  if (ownerIds.length === 0) {
    assetOptions.value = []
    form.value.assetIds = isEdit.value ? null : []
    return
  }
  const res = await getAssetPage({ pageNum: 1, pageSize: 100, assetOwnerId: ownerIds[0] }, { forceRefresh: true })
  const assets = res.data.records || []
  const seen = new Set()
  assetOptions.value = assets.filter(asset => {
    if (seen.has(asset.id)) return false
    seen.add(asset.id)
    return true
  })
}

function handleAssetRemoteSearch(keyword) {
  assetFilters.value.keyword = keyword || ''
}

function selectAllVisibleAssets() {
  form.value.assetIds = filteredAssets.value.map(item => item.id)
}

async function handleUserChange() {
  form.value.assetIds = isEdit.value ? null : []
  await loadAssets()
}

async function openAdd() {
  isEdit.value = false
  form.value = getDefaultForm()
  details.value = [createDetailItem()]
  activeDetailPanels.value = details.value.map(item => item.localId)
  await Promise.all([loadUsers(), loadBillTypes()])
  dialogVisible.value = true
}

async function openEdit(row) {
  isEdit.value = true
  const billRes = await getBill(row.id, { forceRefresh: true })
  const bill = billRes.data || row
  form.value = {
    id: bill.id,
    ownerIds: bill.ownerId,
    assetIds: bill.assetId,
    billStartTime: bill.billStartTime,
    billEndTime: bill.billEndTime,
    billRemark: bill.billRemark || ''
  }
  await Promise.all([loadUsers(), loadBillTypes(), loadAssets()])
  details.value = (bill.details || []).map(mapDetailToEditor)
  if (details.value.length === 0) details.value = [createDetailItem()]
  activeDetailPanels.value = details.value.map(item => item.localId)
  dialogVisible.value = true
}

function createDetailItem() {
  return {
    localId: `detail-${Date.now()}-${Math.random().toString(36).slice(2)}`,
    id: null,
    billDetailName: '',
    billDetailType: undefined,
    billDetailQuantity: 1,
    billDetailUnitPrice: 0,
    billDetailRemark: ''
  }
}

function mapDetailToEditor(item) {
  return {
    localId: `detail-${item.id}-${Math.random().toString(36).slice(2)}`,
    id: item.id,
    billDetailName: item.billDetailName,
    billDetailType: item.billDetailType,
    billDetailQuantity: item.billDetailQuantity,
    billDetailUnitPrice: item.billDetailUnitPrice,
    billDetailRemark: item.billDetailRemark || ''
  }
}

function addDetailItem() {
  const item = createDetailItem()
  details.value.push(item)
  activeDetailPanels.value.push(item.localId)
}

async function removeDetailItem(item) {
  if (details.value.length <= 1) {
    ElMessage.warning('至少保留一个账单款项')
    return
  }
  await ElMessageBox.confirm('确定删除该账单款项吗？', '提示', { type: 'warning' })
  details.value = details.value.filter(detail => detail.localId !== item.localId)
}

function validateTimeRange() {
  const start = new Date(String(form.value.billStartTime || '').replace(' ', 'T')).getTime()
  const end = new Date(String(form.value.billEndTime || '').replace(' ', 'T')).getTime()
  if (!start || !end || Number.isNaN(start) || Number.isNaN(end)) {
    ElMessage.warning('请选择账单起止时间')
    return false
  }
  if ((end - start) < 60 * 60 * 1000) {
    ElMessage.warning('账单时长不能少于一小时')
    return false
  }
  return true
}

function validateDetails() {
  if (details.value.length === 0) {
    ElMessage.warning('请至少添加一个账单款项')
    return false
  }
  const invalid = details.value.some(item => (
    !String(item.billDetailName || '').trim()
    || item.billDetailType === undefined
    || item.billDetailType === null
    || Number(item.billDetailQuantity) <= 0
    || Number(item.billDetailUnitPrice) <= 0
  ))
  if (invalid) {
    ElMessage.warning('请完善账单款项信息')
    return false
  }
  return true
}

function buildPayload() {
  return {
    id: form.value.id,
    ownerId: normalizedOwnerIds.value[0],
    assetId: isEdit.value ? normalizedAssetIds.value[0] : undefined,
    assetIds: isEdit.value ? undefined : normalizedAssetIds.value,
    billStartTime: form.value.billStartTime,
    billEndTime: form.value.billEndTime,
    billRemark: form.value.billRemark,
    details: details.value.map(item => ({
      id: item.id,
      billDetailName: item.billDetailName,
      billDetailType: item.billDetailType,
      billDetailQuantity: item.billDetailQuantity,
      billDetailUnitPrice: item.billDetailUnitPrice,
      billDetailTotal: detailTotal(item),
      billDetailRemark: item.billDetailRemark
    }))
  }
}

async function handleSubmit() {
  if (!validateTimeRange() || !validateDetails()) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateBill(buildPayload())
      ElMessage.success('更新成功')
    } else {
      await addBill(buildPayload())
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await fetchData(true)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteBill(row.id)
  ElMessage.success('删除成功')
  fetchData(true)
}

async function handleBatchDelete() {
  if (selectedBills.value.length === 0) {
    ElMessage.warning('请选择要删除的账单')
    return
  }
  await ElMessageBox.confirm(`确定删除选中的 ${selectedBills.value.length} 条账单吗？`, '提示', { type: 'warning' })
  await batchDeleteBills(selectedBills.value.map(item => item.id))
  ElMessage.success('批量删除成功')
  selectedBills.value = []
  fetchData(true)
}

function handleSelectionChange(rows) {
  selectedBills.value = rows
}

function handleTableCellClick(row, column) {
  if (column.type === 'selection' || column.label === '操作') return
  openBillDetail(row)
}

async function openBillDetail(row) {
  currentBill.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    const [billRes, detailRes] = await Promise.all([
      getBill(row.id, { forceRefresh: true }),
      getBillDetails(row.id, { forceRefresh: true })
    ])
    currentBill.value = billRes.data || row
    viewDetails.value = detailRes.data || []
  } finally {
    detailLoading.value = false
  }
}

useAgentPageControl({
  query,
  queryKeys: agentQueryKeys,
  numericKeys: ['billStatus'],
  onSearch: () => fetchData(true),
  onRefresh: () => fetchData(true),
  onOpenAdd: () => openAdd()
})

onMounted(async () => {
  await Promise.all([fetchData(), loadUsers(), loadRoles(), loadBillTypes(), loadAssetTypes()])
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 22px; font-weight: 800; }
.search-card, .table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.search-card :deep(.el-date-editor--datetimerange) { width: 100%; max-width: none; }
.search-second-row { margin-top: 12px; }
.search-actions { display: flex; justify-content: flex-end; }
.bill-toolbar { justify-content: space-between; }
.full-width { width: 100%; }
.form-tip { margin-bottom: 16px; }
.selector-panel { width: 100%; }
.selector-actions { display: flex; gap: 8px; margin: 10px 0; }
.user-select-header { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 8px; padding: 8px; border-bottom: 1px solid var(--vp-c-divider); }
.asset-select-header { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) auto; gap: 8px; padding: 8px; border-bottom: 1px solid var(--vp-c-divider); }
.dual-option-row { display: flex; align-items: baseline; gap: 8px; min-width: 0; }
.dual-option-row strong { color: var(--vp-c-text-1); font-weight: 800; }
.dual-option-row span { min-width: 0; overflow: hidden; color: var(--vp-c-text-3); font-size: 13px; font-style: italic; text-overflow: ellipsis; white-space: nowrap; }
.select-empty-row { width: 100%; color: var(--vp-c-text-3); font-size: 13px; text-align: center; cursor: default; }
.detail-toolbar { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 12px; margin-bottom: 14px; }
.detail-collapse { border-top: 0; }
.detail-title { display: flex; align-items: center; justify-content: space-between; width: 100%; padding-right: 12px; font-weight: 700; }
.detail-item-actions { display: flex; justify-content: flex-end; }
.confirm-detail-table { margin-top: 16px; }
.bill-detail-desc { margin-bottom: 16px; }
.bill-detail-table { margin-top: 12px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
@media (max-width: 768px) {
  .search-actions { justify-content: stretch; }
  .search-actions .el-button { flex: 1; }
  .bill-toolbar, .detail-toolbar { justify-content: flex-start; grid-template-columns: 1fr; }
  .user-select-header, .asset-select-header { grid-template-columns: 1fr; }
  :deep(.bill-detail-dialog) { width: calc(100vw - 24px) !important; }
}
</style>
