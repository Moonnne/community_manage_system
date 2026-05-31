<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">设施信息</h2></div>
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5"><el-input v-model="query.facilityName" placeholder="设施名称" clearable /></el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-select v-model="query.facilityType" placeholder="设施类型" clearable>
            <el-option v-for="t in types" :key="t.facilityTypeId" :label="t.facilityTypeName" :value="t.facilityTypeId" />
          </el-select>
        </el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-select v-model="query.facilityStatus" placeholder="状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="6" :xs="24" :sm="24" :md="8" :lg="6" :xl="6">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-col>
      </el-row>
    </el-card>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit"><el-icon><Plus /></el-icon> 新增设施</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="facilityCode" label="设施编号" width="140" />
        <el-table-column prop="facilityName" label="名称" width="140" />
        <el-table-column prop="facilityTypeName" label="类型" width="100">
          <template #default="{ row }"><el-tag effect="plain">{{ row.facilityTypeName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="chiefName" label="负责人" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.facilityStatus === 1 ? 'success' : row.facilityStatus === 2 ? 'warning' : 'danger'" effect="plain">
              {{ row.facilityStatus === 1 ? '正常' : row.facilityStatus === 2 ? '维修中' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="facilityBuilding" label="楼栋" width="80" />
        <el-table-column prop="facilityFloor" label="楼层" width="80" />
        <el-table-column prop="facilityPosition" label="位置" width="120" show-overflow-tooltip />
        <el-table-column prop="facilityCheckTime" label="检查时间" width="170" />
        <el-table-column prop="facilityRemark" label="备注" min-width="140" show-overflow-tooltip />
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

    <WizardFormDrawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑设施' : '新增设施'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="wizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="名称" prop="facilityName"><el-input v-model="form.facilityName" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="facilityType">
              <el-select v-model="form.facilityType" placeholder="请选择"><el-option v-for="t in types" :key="t.facilityTypeId" :label="t.facilityTypeName" :value="t.facilityTypeId" /></el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="负责人ID"><el-input v-model.number="form.facilityChiefId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="form.facilityStatus"><el-option label="正常" :value="1" /><el-option label="维修中" :value="2" /><el-option label="停用" :value="0" /></el-select>
          </el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="楼栋"><el-input v-model="form.facilityBuilding" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="楼层"><el-input v-model="form.facilityFloor" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="位置"><el-input v-model="form.facilityPosition" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="保质期"><el-input v-model.number="form.facilityExpirationDate" placeholder="月数" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.facilityRemark" type="textarea" :rows="2" /></el-form-item>
    </WizardFormDrawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getFacilityPage, getFacilityTypes, addFacility, updateFacility, deleteFacility } from '@/api/facility'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1, 2].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const types = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, facilityName: '', facilityType: undefined, facilityStatus: undefined })
const form = ref({})
const wizardSteps = [
  { title: '基础信息', fields: ['facilityName', 'facilityType'] },
  { title: '位置状态', fields: [] },
  { title: '备注确认', fields: [] }
]
const formRules = {
  facilityName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  facilityType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

async function fetchData(forceRefresh = false) { loading.value = true; try { const p = { ...query.value }; Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] }); const res = await getFacilityPage(p, { forceRefresh: !!forceRefresh }); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
async function fetchTypes() { const res = await getFacilityTypes(); types.value = res.data }
function resetQuery() { query.value = { pageNum: 1, pageSize: 10, facilityName: '', facilityType: undefined, facilityStatus: undefined }; fetchData(true) }
function openAdd() { isEdit.value = false; form.value = getDefaultForm(); dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = mapRowToForm(row); dialogVisible.value = true }
function getDefaultForm() { return { facilityName: '', facilityType: undefined, facilityChiefId: '', facilityStatus: 1, facilityBuilding: '', facilityFloor: '', facilityPosition: '', facilityExpirationDate: null, facilityRemark: '' } }
function mapRowToForm(row) { return { id: row.id, facilityName: row.facilityName, facilityType: row.facilityType, facilityChiefId: row.facilityChiefId, facilityStatus: row.facilityStatus, facilityBuilding: row.facilityBuilding, facilityFloor: row.facilityFloor, facilityPosition: row.facilityPosition, facilityExpirationDate: row.facilityExpirationDate, facilityRemark: row.facilityRemark } }
async function handleSubmit() { submitLoading.value = true; try { if (isEdit.value) { await updateFacility(form.value); ElMessage.success('更新成功') } else { await addFacility(form.value); ElMessage.success('添加成功') }; dialogVisible.value = false; fetchData(true) } finally { submitLoading.value = false } }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除?', '提示', { type: 'warning' }); await deleteFacility(row.id); ElMessage.success('删除成功'); fetchData(true) }
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
