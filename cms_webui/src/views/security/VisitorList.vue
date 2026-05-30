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
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="visitorCode" label="访客编号" width="140" />
        <el-table-column prop="visitorName" label="姓名" width="100" />
        <el-table-column prop="visitorPhone" label="电话" width="130" />
        <el-table-column prop="ownerName" label="被访业主" width="100" />
        <el-table-column prop="visitorTypeName" label="类型" width="100">
          <template #default="{ row }"><el-tag effect="plain">{{ row.visitorTypeName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="visitorStatus" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="visitorStatusType(row.visitorStatus)" effect="plain">{{ visitorStatusText(row.visitorStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visitorCreateTime" label="登记时间" width="170" />
        <el-table-column prop="visitorRemark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50]" :total="total" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑访客' : '登记访客'" width="520" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="isEdit ? {} : formRules" label-width="80px">
        <el-form-item label="姓名" prop="visitorName" v-if="!isEdit"><el-input v-model="form.visitorName" /></el-form-item>
        <el-form-item label="电话" prop="visitorPhone" v-if="!isEdit"><el-input v-model="form.visitorPhone" /></el-form-item>
        <el-form-item label="业主ID" prop="ownerId" v-if="!isEdit"><el-input v-model.number="form.ownerId" /></el-form-item>
        <el-form-item label="资产ID" v-if="!isEdit"><el-input v-model.number="form.assetId" /></el-form-item>
        <el-form-item label="类型" prop="visitorType" v-if="!isEdit">
          <el-select v-model="form.visitorType" placeholder="请选择"><el-option v-for="t in types" :key="t.visitorTypeId" :label="t.visitorTypeName" :value="t.visitorTypeId" /></el-select>
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-select v-model="form.visitorStatus" placeholder="请选择">
            <el-option label="待批准" value="1" />
            <el-option label="访问中" value="2" />
            <el-option label="已结束" value="0" />
            <el-option label="已驳回" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.visitorRemark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getVisitorPage, getVisitorTypes, addVisitor, updateVisitor, deleteVisitor } from '@/api/visitor'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1, 3].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const types = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const query = ref({ pageNum: 1, pageSize: 10, visitorName: '', ownerId: '', visitorType: undefined, visitorStatus: '' })
const form = ref({})
const formRules = {
  visitorName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  ownerId: [{ required: true, message: '请输入业主ID', trigger: 'blur' }],
  visitorType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try {
    const p = { ...query.value }; Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] })
    const res = await getVisitorPage(p); tableData.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}
async function fetchTypes() { const res = await getVisitorTypes(); types.value = res.data }
function resetQuery() { query.value = { pageNum: 1, pageSize: 10, visitorName: '', ownerId: '', visitorType: undefined, visitorStatus: '' }; fetchData() }
function openAdd() { isEdit.value = false; form.value = { visitorName: '', visitorPhone: '', ownerId: '', assetId: '', visitorType: undefined, visitorRemark: '' }; dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = { id: row.id, visitorStatus: row.visitorStatus, visitorRemark: row.visitorRemark }; dialogVisible.value = true }
async function handleSubmit() { await formRef.value.validate(); submitLoading.value = true; try { if (isEdit.value) { await updateVisitor(form.value); ElMessage.success('更新成功') } else { await addVisitor(form.value); ElMessage.success('添加成功') }; dialogVisible.value = false; fetchData() } finally { submitLoading.value = false } }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除?', '提示', { type: 'warning' }); await deleteVisitor(row.id); ElMessage.success('删除成功'); fetchData() }
function visitorStatusText(status) { return ({ '0': '已结束', '1': '待批准', '2': '访问中', '3': '已驳回' })[String(status)] || '未知' }
function visitorStatusType(status) { return ({ '0': 'success', '1': 'warning', '2': 'primary', '3': 'danger' })[String(status)] || 'info' }
onMounted(() => { fetchData(); fetchTypes() })
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
