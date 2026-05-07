<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">车辆管理</h2></div>
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="5"><el-input v-model="query.ownerId" placeholder="业主ID" clearable /></el-col>
        <el-col :span="5">
          <el-select v-model="query.carType" placeholder="车辆类型" clearable>
            <el-option v-for="t in types" :key="t.carTypeId" :label="t.carTypeName" :value="t.carTypeId" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="query.carStatus" placeholder="状态" clearable>
            <el-option label="已入场" :value="0" />
            <el-option label="已离场" :value="1" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-col>
      </el-row>
    </el-card>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit"><el-icon><Plus /></el-icon> 登记车辆</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="carCode" label="车辆编号" width="140" />
        <el-table-column prop="carNumber" label="车牌号" width="120" />
        <el-table-column prop="ownerName" label="车主" width="100" />
        <el-table-column prop="carTypeName" label="类型" width="100">
          <template #default="{ row }"><el-tag effect="plain">{{ row.carTypeName }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.carStatus === 0 ? 'success' : 'info'" effect="plain">
              {{ row.carStatus === 0 ? '已入场' : '已离场' }}
            </el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑车辆' : '登记车辆'" width="480" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="车辆编号" prop="carCode" v-if="!isEdit"><el-input v-model="form.carCode" /></el-form-item>
        <el-form-item label="车牌号" prop="carNumber"><el-input v-model.number="form.carNumber" /></el-form-item>
        <el-form-item label="业主ID"><el-input v-model.number="form.ownerId" /></el-form-item>
        <el-form-item label="类型" prop="carType">
          <el-select v-model="form.carType" placeholder="请选择"><el-option v-for="t in types" :key="t.carTypeId" :label="t.carTypeName" :value="t.carTypeId" /></el-select>
        </el-form-item>
        <el-form-item label="状态" prop="carStatus">
          <el-select v-model="form.carStatus" placeholder="请选择">
            <el-option label="已入场" :value="0" />
            <el-option label="已离场" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="资产ID"><el-input v-model.number="form.assetId" /></el-form-item>
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
import { getCarPage, getCarTypes, addCar, updateCar, deleteCar } from '@/api/car'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1, 2, 3].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const types = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const query = ref({ pageNum: 1, pageSize: 10, ownerId: '', carType: undefined, carStatus: undefined })
const form = ref({})
const formRules = {
  carCode: [{ required: true, message: '请输入车辆编号', trigger: 'blur' }],
  carNumber: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  carType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  carStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

async function fetchData() { loading.value = true; try { const p = { ...query.value }; Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] }); const res = await getCarPage(p); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
async function fetchTypes() { const res = await getCarTypes(); types.value = res.data }
function resetQuery() { query.value = { pageNum: 1, pageSize: 10, ownerId: '', carType: undefined, carStatus: undefined }; fetchData() }
function openAdd() { isEdit.value = false; form.value = { carCode: '', carNumber: null, ownerId: '', carType: undefined, carStatus: 1, assetId: '' }; dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = { id: row.id, carNumber: row.carNumber, ownerId: row.ownerId, carType: row.carType, carStatus: row.carStatus, assetId: row.assetId }; dialogVisible.value = true }
async function handleSubmit() { await formRef.value.validate(); submitLoading.value = true; try { if (isEdit.value) { await updateCar(form.value); ElMessage.success('更新成功') } else { await addCar(form.value); ElMessage.success('添加成功') }; dialogVisible.value = false; fetchData() } finally { submitLoading.value = false } }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除?', '提示', { type: 'warning' }); await deleteCar(row.id); ElMessage.success('删除成功'); fetchData() }
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
