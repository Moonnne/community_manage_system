<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">账单管理</h2>
    </div>
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="5">
          <el-input v-model="query.billCode" placeholder="账单编号" clearable />
        </el-col>
        <el-col :span="5">
          <el-input v-model="query.ownerId" placeholder="业主ID" clearable />
        </el-col>
        <el-col :span="5">
          <el-select v-model="query.billStatus" placeholder="账单状态" clearable>
            <el-option label="未完成" :value="1" />
            <el-option label="已完成" :value="0" />
            <el-option label="部分完成" :value="2" />
            <el-option label="已逾期" :value="3" />
            <el-option label="已补缴" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-col>
      </el-row>
    </el-card>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit"><el-icon><Plus /></el-icon> 新增账单</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="billCode" label="账单编号" width="140" />
        <el-table-column prop="ownerName" label="业主" width="100" />
        <el-table-column prop="createrName" label="创建人" width="100" />
        <el-table-column prop="assetName" label="资产" width="120" />
        <el-table-column prop="billTotal" label="总金额" width="100" />
        <el-table-column prop="billPaid" label="已支付" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="billStatusType(row.billStatus)" effect="plain">{{ billStatusText(row.billStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billCreateTime" label="创建时间" width="170" />
        <el-table-column prop="billRemark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetails(row)">明细</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)" v-if="canEdit">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="canEdit">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50]" :total="total" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑账单' : '新增账单'" width="560" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="账单编号" prop="billCode" v-if="!isEdit"><el-input v-model="form.billCode" /></el-form-item>
        <el-form-item label="业主ID" prop="ownerId" v-if="!isEdit"><el-input v-model.number="form.ownerId" /></el-form-item>
        <el-form-item label="资产ID" prop="assetId" v-if="!isEdit"><el-input v-model.number="form.assetId" /></el-form-item>
        <el-form-item label="总金额" prop="billTotal"><el-input v-model.number="form.billTotal" /></el-form-item>
        <el-form-item label="已支付"><el-input v-model.number="form.billPaid" /></el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-select v-model="form.billStatus" placeholder="请选择">
            <el-option label="未完成" :value="1" />
            <el-option label="已完成" :value="0" />
            <el-option label="部分完成" :value="2" />
            <el-option label="已逾期" :value="3" />
            <el-option label="已补缴" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.billRemark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="账单明细" width="700">
      <el-table :data="details" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="billDetailTypeName" label="费用类型" width="120" />
        <el-table-column prop="billDetailTotal" label="金额" width="100" />
        <el-table-column prop="billDetailRemark" label="备注" min-width="200" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getBillPage, addBill, updateBill, deleteBill, getBillDetails } from '@/api/bill'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1, 2].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const details = ref([])

const query = ref({ pageNum: 1, pageSize: 10, billCode: '', ownerId: '', billStatus: undefined })
const form = ref({})
const formRules = {
  billCode: [{ required: true, message: '请输入账单编号', trigger: 'blur' }],
  ownerId: [{ required: true, message: '请输入业主ID', trigger: 'blur' }],
  assetId: [{ required: true, message: '请输入资产ID', trigger: 'blur' }],
  billTotal: [{ required: true, message: '请输入总金额', trigger: 'blur' }]
}

function billStatusText(s) { return ({ 0: '已完成', 1: '未完成', 2: '部分完成', 3: '已逾期', 4: '已补缴', 5: '已取消' })[s] || '未知' }
function billStatusType(s) { return ({ 0: 'success', 1: 'warning', 2: 'primary', 3: 'danger', 4: 'success', 5: 'info' })[s] || 'info' }

async function fetchData() {
  loading.value = true
  try {
    const p = { ...query.value }
    Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] })
    const res = await getBillPage(p)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function resetQuery() { query.value = { pageNum: 1, pageSize: 10, billCode: '', ownerId: '', billStatus: undefined }; fetchData() }
function openAdd() { isEdit.value = false; form.value = { billCode: '', ownerId: '', assetId: '', billTotal: null, billPaid: null, billRemark: '' }; dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = { id: row.id, billStatus: row.billStatus, billTotal: row.billTotal, billPaid: row.billPaid, billRemark: row.billRemark }; dialogVisible.value = true }

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) { await updateBill(form.value); ElMessage.success('更新成功') }
    else { await addBill(form.value); ElMessage.success('添加成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该账单?', '提示', { type: 'warning' })
  await deleteBill(row.id); ElMessage.success('删除成功'); fetchData()
}

async function viewDetails(row) {
  const res = await getBillDetails(row.id)
  details.value = res.data
  detailVisible.value = true
}

onMounted(fetchData)
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
