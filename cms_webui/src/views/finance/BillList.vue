<template>
  <div class="page-container bill-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">账单管理</h2>
      </div>
    </div>

    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-input v-model="query.billCode" placeholder="账单编号" clearable />
        </el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-input v-model="query.ownerId" placeholder="业主ID" clearable />
        </el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-select v-model="query.billStatus" placeholder="账单状态" clearable>
            <el-option label="已完成" :value="0" />
            <el-option label="未完成" :value="1" />
            <el-option label="部分完成" :value="2" />
            <el-option label="已逾期" :value="3" />
            <el-option label="已补缴" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-col>
        <el-col :span="6" :xs="24" :sm="24" :md="8" :lg="6" :xl="6">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon>搜索</el-button>
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="billCode" label="账单编号" width="140" />
        <el-table-column prop="ownerName" label="业主" width="110" />
        <el-table-column prop="createrName" label="创建人" width="110" />
        <el-table-column prop="assetName" label="资产" width="140" />
        <el-table-column prop="billTotal" label="总金额" width="110" />
        <el-table-column prop="billPaid" label="已支付" width="110" />
        <el-table-column label="待付款" width="110">
          <template #default="{ row }">￥{{ amountDue(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="billStatusType(row.billStatus)" effect="plain">{{ billStatusText(row.billStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billCreateTime" label="创建时间" width="170" />
        <el-table-column prop="billRemark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click.stop="handleDelete(row)">删除</el-button>
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
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
      <template v-if="activeStep === 0">
      <el-form-item label="账单编号" prop="billCode" v-if="!isEdit">
        <el-input v-model="form.billCode" />
      </el-form-item>
      <el-form-item label="业主ID" prop="ownerId" v-if="!isEdit">
        <el-input v-model.number="form.ownerId" />
      </el-form-item>
      <el-form-item label="资产ID" prop="assetId" v-if="!isEdit">
        <el-input v-model.number="form.assetId" />
      </el-form-item>
      <el-form-item label="起始时间">
        <el-date-picker v-model="form.billStartTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择起始时间" />
      </el-form-item>
      <el-form-item label="截止时间">
        <el-date-picker v-model="form.billEndTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择截止时间" />
      </el-form-item>
      </template>
      <template v-else-if="activeStep === 1">
      <el-form-item label="总金额" prop="billTotal">
        <el-input-number v-model="form.billTotal" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="已支付">
        <el-input-number v-model="form.billPaid" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="状态" v-if="isEdit">
        <el-select v-model="form.billStatus" placeholder="请选择">
          <el-option label="已完成" :value="0" />
          <el-option label="未完成" :value="1" />
          <el-option label="部分完成" :value="2" />
          <el-option label="已逾期" :value="3" />
          <el-option label="已补缴" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.billRemark" type="textarea" :rows="3" />
      </el-form-item>
      </template>
      <ConfirmInfoTable v-else :rows="confirmRows" />
      </template>
    </WizardFormDrawer>

    <el-dialog v-model="detailVisible" title="账单详情" width="760px" class="bill-detail-dialog">
      <template v-if="currentBill">
        <el-descriptions :column="2" border class="bill-detail-desc">
          <el-descriptions-item label="账单编号">{{ formatValue(currentBill.billCode) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="billStatusType(currentBill.billStatus)" effect="plain">{{ billStatusText(currentBill.billStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="业主">{{ formatValue(currentBill.ownerName) }}</el-descriptions-item>
          <el-descriptions-item label="资产">{{ formatValue(currentBill.assetName) }}</el-descriptions-item>
          <el-descriptions-item label="总金额">￥{{ formatMoney(currentBill.billTotal) }}</el-descriptions-item>
          <el-descriptions-item label="已支付">￥{{ formatMoney(currentBill.billPaid) }}</el-descriptions-item>
          <el-descriptions-item label="待付款">￥{{ amountDue(currentBill) }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatValue(currentBill.billEndTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ formatValue(currentBill.billRemark) }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="details" stripe style="width: 100%" v-loading="detailLoading" class="bill-detail-table">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="billDetailName" label="账目名" min-width="120" />
          <el-table-column prop="billDetailTypeName" label="费用类型" width="120" />
          <el-table-column prop="billDetailQuantity" label="数量" width="90" />
          <el-table-column prop="billDetailUnitPrice" label="单价" width="100" />
          <el-table-column prop="billDetailTotal" label="金额" width="100" />
          <el-table-column prop="billDetailRemark" label="备注" min-width="160" show-overflow-tooltip />
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
import { addBill, batchDeleteBills, deleteBill, getBillDetails, getBillPage, updateBill } from '@/api/bill'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1, 2].includes(userStore.userRole) && userStore.canOperate)

const loading = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedBills = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const details = ref([])
const currentBill = ref(null)

const query = ref({ pageNum: 1, pageSize: 10, billCode: '', ownerId: '', billStatus: undefined })
const form = ref({})
const wizardSteps = [
  { title: '基础信息', fields: ['billCode', 'ownerId', 'assetId'] },
  { title: '金额状态', fields: ['billTotal'] },
  { title: '确认信息', fields: [] }
]
const confirmRows = computed(() => [
  { label: '账单编号', value: formatValue(form.value.billCode) },
  { label: '业主ID', value: formatValue(form.value.ownerId) },
  { label: '资产ID', value: formatValue(form.value.assetId) },
  { label: '起始时间', value: formatValue(form.value.billStartTime) },
  { label: '截止时间', value: formatValue(form.value.billEndTime) },
  { label: '总金额', value: formatMoney(form.value.billTotal) },
  { label: '已支付', value: formatMoney(form.value.billPaid) },
  { label: '状态', type: 'tag', tagType: billStatusType(form.value.billStatus), value: billStatusText(form.value.billStatus) },
  { label: '备注', value: formatValue(form.value.billRemark) }
])
const formRules = {
  billCode: [{ required: true, message: '请输入账单编号', trigger: 'blur' }],
  ownerId: [{ required: true, message: '请输入业主ID', trigger: 'blur' }],
  assetId: [{ required: true, message: '请输入资产ID', trigger: 'blur' }],
  billTotal: [{ required: true, message: '请输入总金额', trigger: 'blur' }]
}

function billStatusText(status) {
  return ({ 0: '已完成', 1: '未完成', 2: '部分完成', 3: '已逾期', 4: '已补缴', 5: '已取消' })[status] || '未知'
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

function cleanParams(params) {
  const cleaned = { ...params }
  Object.keys(cleaned).forEach(key => {
    if (cleaned[key] === '' || cleaned[key] === undefined || cleaned[key] === null) delete cleaned[key]
  })
  return cleaned
}

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const res = await getBillPage(cleanParams(query.value), { forceRefresh: !!forceRefresh })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.value = { pageNum: 1, pageSize: 10, billCode: '', ownerId: '', billStatus: undefined }
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
  return {
    billCode: '',
    ownerId: '',
    assetId: '',
    billStartTime: '',
    billEndTime: '',
    billTotal: null,
    billPaid: 0,
    billRemark: ''
  }
}

function mapRowToForm(row) {
  return {
    id: row.id,
    billStatus: row.billStatus,
    billStartTime: row.billStartTime,
    billEndTime: row.billEndTime,
    billTotal: row.billTotal,
    billPaid: row.billPaid,
    billRemark: row.billRemark
  }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateBill(form.value)
      ElMessage.success('更新成功')
    } else {
      await addBill(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData(true)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该账单吗？', '提示', { type: 'warning' })
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
    const res = await getBillDetails(row.id)
    details.value = res.data || []
  } finally {
    detailLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-kicker { margin: 0 0 4px; color: var(--vp-c-brand-1); font-size: 13px; font-weight: 700; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 22px; font-weight: 800; }
.search-card, .table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.bill-toolbar { justify-content: space-between; }
.bill-detail-desc { margin-bottom: 16px; }
.bill-detail-table { margin-top: 12px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
@media (max-width: 768px) {
  .bill-toolbar { justify-content: flex-start; }
  :deep(.bill-detail-dialog) { width: calc(100vw - 24px) !important; }
}
</style>
