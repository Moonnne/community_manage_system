<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">通行记录</h2></div>
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="5"><el-input v-model="query.carId" placeholder="车辆ID" clearable /></el-col>
        <el-col :span="5"><el-input v-model="query.gateId" placeholder="门岗ID" clearable /></el-col>
        <el-col :span="5">
          <el-select v-model="query.carHistoryType" placeholder="类型" clearable>
            <el-option label="入场" :value="0" />
            <el-option label="离场" :value="1" />
            <el-option label="访客入场" :value="2" />
            <el-option label="访客离场" :value="3" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-col>
      </el-row>
    </el-card>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon> 新增记录</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="carHistoryCode" label="记录编号" width="140" />
        <el-table-column prop="carNumber" label="车牌号" width="120" />
        <el-table-column prop="gateName" label="门岗" width="120" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="[0, 2].includes(row.carHistoryType) ? 'success' : 'warning'" effect="plain">
              {{ carHistoryTypeText(row.carHistoryType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="carHistoryTime" label="时间" width="170" />
        <el-table-column prop="carHistoryRemark" label="备注" min-width="200" show-overflow-tooltip />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50]" :total="total" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增通行记录" width="480" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="车辆ID" prop="carId"><el-input v-model.number="form.carId" /></el-form-item>
        <el-form-item label="门岗ID" prop="gateId"><el-input v-model.number="form.gateId" /></el-form-item>
        <el-form-item label="类型" prop="carHistoryType">
          <el-select v-model="form.carHistoryType" placeholder="请选择">
            <el-option label="入场" :value="0" />
            <el-option label="离场" :value="1" />
            <el-option label="访客入场" :value="2" />
            <el-option label="访客离场" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.carHistoryRemark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCarHistoryPage, addCarHistory } from '@/api/car'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)
const query = ref({ pageNum: 1, pageSize: 10, carId: '', gateId: '', carHistoryType: undefined })
const form = ref({})
const formRules = {
  carId: [{ required: true, message: '请输入车辆ID', trigger: 'blur' }],
  gateId: [{ required: true, message: '请输入门岗ID', trigger: 'blur' }],
  carHistoryType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

function carHistoryTypeText(type) {
  return ['入场', '离场', '访客入场', '访客离场'][type] || '未知'
}

async function fetchData() { loading.value = true; try { const p = { ...query.value }; Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] }); const res = await getCarHistoryPage(p); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
function resetQuery() { query.value = { pageNum: 1, pageSize: 10, carId: '', gateId: '', carHistoryType: undefined }; fetchData() }
function openAdd() { form.value = { carId: '', gateId: '', carHistoryType: undefined, carHistoryRemark: '' }; dialogVisible.value = true }
async function handleSubmit() { await formRef.value.validate(); submitLoading.value = true; try { await addCarHistory(form.value); ElMessage.success('添加成功'); dialogVisible.value = false; fetchData() } finally { submitLoading.value = false } }
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
