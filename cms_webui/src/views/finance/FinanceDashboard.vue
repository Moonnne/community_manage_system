<template>
  <div class="page-container finance-dashboard">
    <div class="page-header">
      <h2 class="page-title">我的账单</h2>
    </div>

    <div class="dashboard-summary">
      <el-card shadow="never" class="amount-summary-card">
        <p class="amount-kicker">待付款金额</p>
        <div class="amount-value">￥{{ formatMoney(animatedUnpaidAmount) }}</div>
        <p class="amount-desc">当前账户仍需处理的账单金额</p>
      </el-card>
    </div>

    <div class="count-summary-grid">
      <el-card v-for="item in countCards" :key="item.key" shadow="never" class="count-summary-card">
        <p>{{ item.title }}</p>
        <strong>{{ item.value }} <span>笔</span></strong>
      </el-card>
    </div>

    <section class="bill-section">
      <div class="resident-section-title">
        <h3>未结清账单</h3>
      </div>
      <el-empty v-if="!loading && visibleUnsettledBills.length === 0" description="暂无未结清账单" />
      <div v-else class="bill-card-grid" v-loading="loading">
        <BillCard v-for="bill in visibleUnsettledBills" :key="bill.id" :bill="bill" @click="openBillDetail(bill)" />
      </div>
    </section>

    <section class="bill-section">
      <div class="resident-section-title">
        <h3>已完成账单</h3>
      </div>
      <el-empty v-if="!loading && visibleCompletedBills.length === 0" description="暂无已完成账单" />
      <div v-else class="bill-card-grid" v-loading="loading">
        <BillCard v-for="bill in visibleCompletedBills" :key="bill.id" :bill="bill" @click="openBillDetail(bill)" />
      </div>
    </section>

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
          <el-descriptions-item>
            <template #label><span class="detail-desc-label">业主</span></template>
            <span class="detail-desc-value">{{ formatValue(currentBill.ownerName) }}</span>
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label><span class="detail-desc-label">资产</span></template>
            <span class="detail-desc-value">{{ formatValue(currentBill.assetName) }}</span>
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label><span class="detail-desc-label">总金额</span></template>
            <span class="detail-desc-value">￥{{ formatMoney(currentBill.billTotal) }}</span>
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label><span class="detail-desc-label">已支付</span></template>
            <span class="detail-desc-value">￥{{ formatMoney(currentBill.billPaid) }}</span>
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label><span class="detail-desc-label">待付款</span></template>
            <span class="detail-desc-value">￥{{ amountDue(currentBill) }}</span>
          </el-descriptions-item>
          <el-descriptions-item>
            <template #label><span class="detail-desc-label">截止时间</span></template>
            <span class="detail-desc-value">{{ formatValue(currentBill.billEndTime) }}</span>
          </el-descriptions-item>
          <el-descriptions-item :span="2">
            <template #label><span class="detail-desc-label">备注</span></template>
            <span class="detail-desc-value">{{ formatValue(currentBill.billRemark) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-table :data="details" stripe v-loading="detailLoading" class="bill-detail-table">
          <el-table-column label="账目名" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <el-tooltip
                v-if="row.billDetailRemark"
                :content="row.billDetailRemark"
                placement="top"
                popper-class="bill-detail-remark-tooltip"
              >
                <span class="remark-tooltip-trigger">{{ formatValue(row.billDetailName) }}</span>
              </el-tooltip>
              <span v-else>{{ formatValue(row.billDetailName) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="billDetailTypeName" label="费用类型" width="140" show-overflow-tooltip />
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
          <el-button type="warning" plain @click="handleAppeal">账单申诉</el-button>
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
import { computed, defineComponent, h, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElButton, ElCard, ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { useTransition } from '@vueuse/core'
import { useUserStore } from '@/stores/user'
import { getBill, getBillDetails, getBillPage, getMyBillSummary, payBill, payBillDetail } from '@/api/bill'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const detailLoading = ref(false)
const payLoading = ref(false)
const allBills = ref([])
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
const animatedUnpaidAmount = ref(0)
const now = ref(Date.now())
const viewportWidth = ref(typeof window === 'undefined' ? 1200 : window.innerWidth)
let timer = null
let stopAmountTransition = null

const completedStatuses = new Set([0, 4, 5])
const displayLimit = computed(() => (viewportWidth.value <= 768 ? 3 : 6))
const completedBills = computed(() => allBills.value.filter((bill) => completedStatuses.has(bill.billStatus)))
const unsettledBills = computed(() => allBills.value.filter((bill) => !completedStatuses.has(bill.billStatus)))
const visibleCompletedBills = computed(() => completedBills.value.slice(0, displayLimit.value))
const visibleUnsettledBills = computed(() => unsettledBills.value.slice(0, displayLimit.value))
const countCards = computed(() => [
  { key: 'unsettled', title: '未结清账单', value: unsettledBills.value.length },
  { key: 'partial', title: '部分付款账单', value: allBills.value.filter((bill) => bill.billStatus === 2).length },
  { key: 'repaid', title: '已补缴账单', value: allBills.value.filter((bill) => bill.billStatus === 4).length },
  { key: 'paid', title: '已完成账单', value: allBills.value.filter((bill) => bill.billStatus === 0).length },
  { key: 'overdue', title: '逾期账单', value: allBills.value.filter((bill) => bill.billStatus === 3).length }
])
const canPayCurrentBill = computed(() => currentBill.value && [1, 2, 3].includes(currentBill.value.billStatus))

const BillCard = defineComponent({
  name: 'DashboardBillCard',
  props: {
    bill: { type: Object, required: true }
  },
  emits: ['click'],
  setup(props, { emit }) {
    return () => h(ElCard, {
      shadow: 'never',
      class: 'bill-card',
      onClick: () => emit('click')
    }, {
      default: () => [
        h('div', { class: 'bill-card-header' }, [
          h('div', null, [
            h('span', { class: 'bill-code' }, formatValue(props.bill.billCode)),
            h('p', null, formatValue(props.bill.assetName))
          ]),
          h(ElTag, { type: billStatusType(props.bill.billStatus), effect: 'plain' }, () => billStatusText(props.bill.billStatus))
        ]),
        h('div', { class: 'bill-card-amount' }, `￥${amountDue(props.bill)}`),
        h('div', { class: 'bill-card-meta' }, [
          h('span', null, '截止时间'),
          h('strong', null, formatValue(props.bill.billEndTime))
        ]),
        h('div', { class: ['bill-countdown', { overdue: countdownInfo(props.bill).overdue }] }, countdownInfo(props.bill).text)
      ]
    })
  }
})

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
  if (completedStatuses.has(bill?.billStatus)) {
    return { text: billStatusText(bill.billStatus), overdue: false }
  }
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
    const params = {
      pageNum: 1,
      pageSize: 1000,
      ownerId: userStore.userInfo?.id
    }
    const res = await getBillPage(params, { forceRefresh: !!forceRefresh })
    allBills.value = res.data.records || []
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

function updateViewportWidth() {
  viewportWidth.value = window.innerWidth
}

function initializeAmountTransition() {
  const transitioned = useTransition(computed(() => Number(summary.value.unpaidAmount || 0)), { duration: 800 })
  stopAmountTransition = watch(transitioned, value => {
    animatedUnpaidAmount.value = Number(value || 0)
  }, { immediate: true })
}

onMounted(() => {
  initializeAmountTransition()
  fetchData()
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 60000)
  window.addEventListener('resize', updateViewportWidth)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
  if (stopAmountTransition) stopAmountTransition()
  window.removeEventListener('resize', updateViewportWidth)
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-title { margin: 0; color: var(--vp-c-text-1); font-size: 22px; font-weight: 800; }
.dashboard-summary {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}
.amount-summary-card {
  grid-column: span 2;
  border: 1px solid rgba(16, 185, 129, 0.24);
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.14), var(--vp-card-bg) 52%, rgba(59, 130, 246, 0.08)) !important;
}
.amount-summary-card :deep(.el-card__body) {
  min-height: 154px;
}
.amount-kicker { margin: 0 0 12px; color: var(--vp-c-brand-1); font-size: 20px; font-weight: 900; }
.amount-value { color: var(--vp-c-text-1); font-size: 48px; line-height: 1; font-weight: 900; }
.amount-desc { margin: 16px 0 0; color: var(--vp-c-text-3); font-size: 14px; }
.count-summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 22px;
}
.count-summary-card {
  min-width: 0;
  border-radius: 8px;
  border: 1px solid var(--vp-c-divider);
}
.count-summary-card p { margin: 0 0 8px; color: var(--vp-c-text-3); font-size: 13px; }
.count-summary-card strong { color: var(--vp-c-text-1); font-size: 24px; line-height: 1; font-weight: 800; }
.count-summary-card span { color: var(--vp-c-text-2); font-size: 15px; font-weight: 600; }
.bill-section { margin-top: 18px; }
.resident-section-title { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin: 6px 0 14px; }
.resident-section-title h3 { margin: 0; color: var(--vp-c-text-1); font-size: 18px; }
.bill-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.bill-card-grid :deep(.bill-card) { cursor: pointer; border-radius: 8px; border: 1px solid var(--vp-c-divider); transition: transform 0.18s ease, box-shadow 0.18s ease; }
.bill-card-grid :deep(.bill-card:hover) { transform: translateY(-2px); }
.bill-card-grid :deep(.bill-card .el-card__body) { display: flex; flex-direction: column; min-height: 202px; padding: 24px 26px; }
.bill-card-grid :deep(.bill-card-header) { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.bill-card-grid :deep(.bill-code) { display: block; color: var(--vp-c-text-1); font-size: 19px; line-height: 1.15; font-weight: 900; }
.bill-card-grid :deep(.bill-card-header p) { margin: 8px 0 0; color: var(--vp-c-text-2); font-size: 15px; line-height: 1.4; }
.bill-card-grid :deep(.bill-card-amount) { margin: 26px 0 24px; color: var(--vp-c-brand-1); font-size: 38px; line-height: 1; font-weight: 900; letter-spacing: 0; }
.bill-card-grid :deep(.bill-card-meta) { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-top: auto; padding-top: 18px; border-top: 1px solid var(--vp-c-divider); color: var(--vp-c-text-3); font-size: 14px; }
.bill-card-grid :deep(.bill-card-meta strong) { color: var(--vp-c-text-1); font-size: 15px; font-weight: 700; }
.bill-card-grid :deep(.bill-countdown) { margin-top: 10px; color: var(--vp-c-brand-1); font-size: 14px; line-height: 1.4; font-weight: 800; }
.bill-card-grid :deep(.bill-countdown.overdue) { color: var(--el-color-danger); }
.detail-card-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.detail-card-header p { margin: 0 0 4px; color: var(--vp-c-brand-1); font-size: 13px; font-weight: 700; }
.detail-card-header h3 { margin: 0; color: var(--vp-c-text-1); font-size: 24px; }
.bill-detail-desc { margin-bottom: 18px; }
.detail-desc-label { color: var(--vp-c-text-2); font-weight: 500; }
.detail-desc-value { color: var(--vp-c-text-1); }
.bill-detail-table { margin-top: 12px; }
.remark-tooltip-trigger { display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; vertical-align: bottom; white-space: nowrap; }
.detail-actions { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-top: 18px; padding-top: 16px; border-top: 1px solid var(--vp-c-divider); }
:global(.bill-detail-remark-tooltip) { max-width: 280px; border-radius: 8px !important; background: rgba(15, 23, 42, 0.92) !important; color: #fff !important; font-size: 12px; line-height: 1.6; }
:global(.bill-detail-remark-tooltip .el-popper__arrow::before) { background: rgba(15, 23, 42, 0.92) !important; }
:deep(.bill-detail-card-dialog .el-dialog) { border-radius: 16px; }
:deep(.bill-detail-card-dialog .el-dialog__body) { padding-top: 8px; }
@media (max-width: 768px) {
  .dashboard-summary,
  .count-summary-grid,
  .bill-card-grid {
    grid-template-columns: 1fr;
  }
  .amount-summary-card {
    grid-column: auto;
  }
  .amount-summary-card :deep(.el-card__body) {
    min-height: 0;
  }
  .amount-value { font-size: 38px; }
  .bill-card-grid :deep(.bill-card-meta) { align-items: flex-start; flex-direction: column; }
  .detail-actions { align-items: center; justify-content: flex-end; flex-wrap: nowrap; }
  .detail-actions > div { display: flex; justify-content: flex-end; gap: 10px; white-space: nowrap; }
  :deep(.bill-detail-card-dialog .el-dialog) { width: calc(100vw - 24px) !important; }
}
</style>
