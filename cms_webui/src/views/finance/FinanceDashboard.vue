<template>
  <div class="page-container finance-dashboard">
    <div class="page-header">
      <h2 class="page-title">我的账单</h2>
    </div>

    <el-row :gutter="16" class="summary-grid">
      <el-col v-for="item in summaryCards" :key="item.key" :xs="24" :sm="12" :lg="item.lg">
        <el-card shadow="never" class="summary-card">
          <el-statistic
            :title="item.title"
            :value="item.value"
            :precision="item.precision || 0"
            :prefix="item.prefix || ''"
            :suffix="item.suffix || ''"
          />
        </el-card>
      </el-col>
    </el-row>

    <div class="resident-section-title">
      <h3>未结清账单</h3>
      <el-tag effect="plain">部分付款 {{ summary.partiallyPaidCount || 0 }} 笔</el-tag>
    </div>

    <el-empty v-if="!loading && bills.length === 0" description="暂无未结清账单" />
    <el-row v-else :gutter="16" class="bill-card-grid" v-loading="loading">
      <el-col v-for="bill in bills" :key="bill.id" :xs="24" :sm="12" :lg="8">
        <el-card shadow="never" class="bill-card" @click="openBillDetail(bill)">
          <div class="bill-card-header">
            <div>
              <span class="bill-code">{{ formatValue(bill.billCode) }}</span>
              <p>{{ formatValue(bill.assetName) }}</p>
            </div>
            <el-tag :type="billStatusType(bill.billStatus)" effect="plain">
              {{ billStatusText(bill.billStatus) }}
            </el-tag>
          </div>
          <div class="bill-card-amount">￥{{ amountDue(bill) }}</div>
          <div class="bill-card-meta">
            <span>截止时间</span>
            <strong>{{ formatValue(bill.billEndTime) }}</strong>
          </div>
          <div class="bill-countdown" :class="{ overdue: countdownInfo(bill).overdue }">
            {{ countdownInfo(bill).text }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        background
        layout="total, prev, pager, next"
        v-model:current-page="query.pageNum"
        :page-size="query.pageSize"
        :total="total"
        @current-change="fetchBills"
      />
    </div>

    <el-dialog
      v-model="detailVisible"
      width="920px"
      class="bill-detail-card-dialog"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <template #header>
        <div class="detail-card-header">
          <div>
            <p>账单详情</p>
            <h3>{{ formatValue(currentBill?.billCode) }}</h3>
          </div>
          <el-tag v-if="currentBill" :type="billStatusType(currentBill.billStatus)" effect="plain">
            {{ billStatusText(currentBill.billStatus) }}
          </el-tag>
        </div>
      </template>

      <template v-if="currentBill">
        <el-descriptions :column="2" class="bill-detail-desc">
          <el-descriptions-item label="业主">{{ formatValue(currentBill.ownerName) }}</el-descriptions-item>
          <el-descriptions-item label="资产">{{ formatValue(currentBill.assetName) }}</el-descriptions-item>
          <el-descriptions-item label="总金额">￥{{ formatMoney(currentBill.billTotal) }}</el-descriptions-item>
          <el-descriptions-item label="已支付">￥{{ formatMoney(currentBill.billPaid) }}</el-descriptions-item>
          <el-descriptions-item label="待付款">￥{{ amountDue(currentBill) }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatValue(currentBill.billEndTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ formatValue(currentBill.billRemark) }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="details" stripe v-loading="detailLoading" class="bill-detail-table">
          <el-table-column prop="id" label="ID" width="72" />
          <el-table-column prop="billDetailName" label="账目名" min-width="140" />
          <el-table-column prop="billDetailTypeName" label="费用类型" width="120" />
          <el-table-column prop="billDetailQuantity" label="数量" width="80" />
          <el-table-column label="单价" width="100">
            <template #default="{ row }">￥{{ formatMoney(row.billDetailUnitPrice) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{ row }">￥{{ formatMoney(row.billDetailTotal) }}</template>
          </el-table-column>
          <el-table-column label="待付" width="100">
            <template #default="{ row }">￥{{ formatMoney(row.billDetailDue) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="detailStatusType(row.billDetailStatus)" effect="plain">
                {{ detailStatusText(row.billDetailStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="118" fixed="right">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                :disabled="row.billDetailStatus === 0 || currentBill.billStatus === 5"
                @click="handlePayDetail(row)"
              >
                缴纳此项
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="detail-actions">
          <el-link type="warning" :underline="false" @click="handleAppeal">账单申诉</el-link>
          <div>
            <el-button @click="detailVisible = false">关闭</el-button>
            <el-button v-if="canPayCurrentBill" type="primary" :loading="payLoading" @click="handlePayAll">
              缴纳全部
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBill, getBillDetails, getMyBillSummary, getMyUnpaidBills, payBill, payBillDetail } from '@/api/bill'

const router = useRouter()
const loading = ref(false)
const detailLoading = ref(false)
const payLoading = ref(false)
const bills = ref([])
const total = ref(0)
const summary = ref({
  unpaidCount: 0,
  paidCount: 0,
  cancelledCount: 0,
  unpaidAmount: 0,
  overdueCount: 0,
  partiallyPaidCount: 0
})
const detailVisible = ref(false)
const details = ref([])
const currentBill = ref(null)
const query = ref({ pageNum: 1, pageSize: 9 })
const now = ref(Date.now())
let timer = null

const summaryCards = computed(() => [
  { key: 'unpaid', title: '未结清账单', value: summary.value.unpaidCount || 0, suffix: ' 笔', lg: 5 },
  { key: 'paid', title: '已结清账单', value: summary.value.paidCount || 0, suffix: ' 笔', lg: 5 },
  { key: 'cancelled', title: '已取消账单', value: summary.value.cancelledCount || 0, suffix: ' 笔', lg: 5 },
  { key: 'amount', title: '待付款金额', value: Number(summary.value.unpaidAmount || 0), precision: 2, prefix: '￥', lg: 5 },
  { key: 'overdue', title: '逾期账单', value: summary.value.overdueCount || 0, suffix: ' 笔', lg: 4 }
])
const canPayCurrentBill = computed(() => currentBill.value && [1, 2, 3].includes(currentBill.value.billStatus))

function billStatusText(status) {
  return ({ 0: '已完成', 1: '未完成', 2: '部分完成', 3: '已逾期', 4: '已补缴', 5: '已取消' })[status] || '未知'
}

function billStatusType(status) {
  return ({ 0: 'success', 1: 'warning', 2: 'primary', 3: 'danger', 4: 'success', 5: 'info' })[status] || 'info'
}

function detailStatusText(status) {
  return ({ 0: '已结清', 1: '未结清', 2: '部分结清' })[status] || '未知'
}

function detailStatusType(status) {
  return ({ 0: 'success', 1: 'warning', 2: 'primary' })[status] || 'info'
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

function parseTime(value) {
  if (!value) return null
  return new Date(String(value).replace(' ', 'T')).getTime()
}

function countdownInfo(bill) {
  const end = parseTime(bill?.billEndTime)
  if (!end) return { text: '暂无截止时间', overdue: false }
  const diffMinutes = Math.floor((end - now.value) / 60000)
  if (diffMinutes >= 0) {
    const hours = Math.floor(diffMinutes / 60)
    const minutes = diffMinutes % 60
    return { text: `距离截止 ${hours} 小时 ${minutes} 分钟`, overdue: false }
  }
  const overdueMinutes = Math.abs(diffMinutes)
  const hours = Math.floor(overdueMinutes / 60)
  const minutes = overdueMinutes % 60
  return { text: `已逾期 ${hours} 小时 ${minutes} 分钟`, overdue: true }
}

async function fetchSummary(forceRefresh = false) {
  const res = await getMyBillSummary({ forceRefresh: !!forceRefresh })
  summary.value = { ...summary.value, ...(res.data || {}) }
}

async function fetchBills(forceRefresh = false) {
  loading.value = true
  try {
    const res = await getMyUnpaidBills(query.value, { forceRefresh: !!forceRefresh })
    bills.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchData(forceRefresh = false) {
  await Promise.all([fetchSummary(forceRefresh), fetchBills(forceRefresh)])
}

async function refreshCurrentBill() {
  if (!currentBill.value) return
  const [billRes, detailRes] = await Promise.all([
    getBill(currentBill.value.id, { forceRefresh: true }),
    getBillDetails(currentBill.value.id, { forceRefresh: true })
  ])
  currentBill.value = billRes.data
  details.value = detailRes.data || []
}

async function openBillDetail(row) {
  currentBill.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    await refreshCurrentBill()
  } finally {
    detailLoading.value = false
  }
}

async function handlePayDetail(row) {
  await ElMessageBox.confirm('当前为虚拟缴费，确认后该款项将标记为已结清。', '确认缴费', { type: 'warning' })
  payLoading.value = true
  try {
    await payBillDetail({ billId: currentBill.value.id, detailId: row.id })
    ElMessage.success('缴费成功')
    await refreshCurrentBill()
    await fetchData(true)
  } finally {
    payLoading.value = false
  }
}

async function handlePayAll() {
  if (!currentBill.value) return
  await ElMessageBox.confirm('当前为虚拟缴费，确认后该账单将标记为已结清。', '确认缴费', { type: 'warning' })
  payLoading.value = true
  try {
    await payBill(currentBill.value.id)
    ElMessage.success('缴费成功')
    detailVisible.value = false
    await fetchData(true)
  } finally {
    payLoading.value = false
  }
}

function handleAppeal() {
  if (!currentBill.value) return
  router.push({ path: '/task/list', query: { intent: 'bill-appeal', billId: currentBill.value.id } })
}

onMounted(() => {
  fetchData()
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 60000)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 22px; font-weight: 800; }
.summary-grid { row-gap: 16px; margin-bottom: 20px; }
.summary-card, .bill-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.resident-section-title { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin: 6px 0 14px; }
.resident-section-title h3 { margin: 0; color: var(--vp-c-text-1); font-size: 18px; }
.bill-card-grid { row-gap: 16px; }
.bill-card { cursor: pointer; transition: transform 0.18s ease, box-shadow 0.18s ease; }
.bill-card:hover { transform: translateY(-2px); }
.bill-card-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }
.bill-code { display: block; color: var(--vp-c-text-1); font-size: 17px; font-weight: 800; }
.bill-card-header p { margin: 6px 0 0; color: var(--vp-c-text-2); }
.bill-card-amount { margin: 22px 0 18px; color: var(--vp-c-brand-1); font-size: 30px; font-weight: 800; }
.bill-card-meta { display: flex; justify-content: space-between; gap: 12px; color: var(--vp-c-text-3); font-size: 13px; }
.bill-card-meta strong { color: var(--vp-c-text-2); font-weight: 600; }
.bill-countdown { margin-top: 12px; color: var(--vp-c-brand-1); font-size: 13px; font-weight: 700; }
.bill-countdown.overdue { color: var(--el-color-danger); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.detail-card-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.detail-card-header p { margin: 0 0 4px; color: var(--vp-c-brand-1); font-size: 13px; font-weight: 700; }
.detail-card-header h3 { margin: 0; color: var(--vp-c-text-1); font-size: 24px; }
.bill-detail-desc { margin-bottom: 18px; }
.bill-detail-table { margin-top: 12px; }
.detail-actions { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-top: 18px; padding-top: 16px; border-top: 1px solid var(--vp-c-divider); }
:deep(.bill-detail-card-dialog .el-dialog) { border-radius: 16px; }
:deep(.bill-detail-card-dialog .el-dialog__body) { padding-top: 8px; }
@media (max-width: 768px) {
  .resident-section-title, .bill-card-meta, .detail-actions { align-items: flex-start; flex-direction: column; }
  :deep(.bill-detail-card-dialog .el-dialog) { width: calc(100vw - 24px) !important; }
}
</style>
