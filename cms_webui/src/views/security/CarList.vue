<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">车辆管理</h2></div>
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5"><el-input v-model="query.ownerId" placeholder="业主ID" clearable /></el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-select v-model="query.carType" placeholder="车辆类型" clearable>
            <el-option v-for="t in types" :key="t.carTypeId" :label="t.carTypeName" :value="t.carTypeId" />
          </el-select>
        </el-col>
        <el-col :span="5" :xs="24" :sm="12" :md="8" :lg="5" :xl="5">
          <el-select v-model="query.carStatus" placeholder="状态" clearable>
            <el-option label="已入场" :value="0" />
            <el-option label="已离场" :value="1" />
          </el-select>
        </el-col>
        <el-col :span="6" :xs="24" :sm="24" :md="8" :lg="6" :xl="6">
          <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon> 搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-col>
      </el-row>
    </el-card>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit"><el-icon><Plus /></el-icon> 登记车辆</el-button>
      <el-button @click="fetchData(true)">刷新</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="carCode" label="车辆编号" width="140" show-overflow-tooltip />
        <el-table-column prop="carNumber" label="车牌号" width="120" show-overflow-tooltip />
        <el-table-column prop="ownerName" label="车主" width="100" show-overflow-tooltip />
        <el-table-column prop="carTypeName" label="类型" width="100" show-overflow-tooltip>
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
            <el-popconfirm title="确定删除该车辆记录吗？" confirm-button-text="删除" cancel-button-text="取消" @confirm="handleDelete(row)">
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
      :title="isEdit ? '编辑车辆' : '登记车辆'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="wizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
        <el-form-item label="车辆编号" prop="carCode" v-if="!isEdit"><el-input v-model="form.carCode" /></el-form-item>
        <el-form-item label="车牌号" prop="carNumber">
          <div class="plate-input">
            <el-select v-model="form.carProvince" placeholder="省份" class="plate-province">
              <el-option v-for="province in provinceOptions" :key="province" :label="province" :value="province" />
            </el-select>
            <el-input v-model="form.carPlateBody" maxlength="7" placeholder="请输入车牌号码" @input="handlePlateBodyInput" />
          </div>
        </el-form-item>
        <el-form-item label="业主ID">
          <DualSelectV2
            v-model="form.ownerId"
            :options="userOptions"
            remote
            :remote-method="handleUserRemoteSearch"
            placeholder="输入以搜索用户"
            primary-key="label"
            secondary-key="userName"
          />
        </el-form-item>
        </template>
        <template v-else-if="activeStep === 1">
        <el-form-item label="类型" prop="carType">
          <el-select v-model="form.carType" placeholder="请选择"><el-option v-for="t in types" :key="t.carTypeId" :label="t.carTypeName" :value="t.carTypeId" /></el-select>
        </el-form-item>
        <el-form-item label="状态" prop="carStatus">
          <el-select v-model="form.carStatus" placeholder="请选择">
            <el-option label="已入场" :value="0" />
            <el-option label="已离场" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="资产ID">
          <DualSelectV2
            v-model="form.assetId"
            :options="assetOptions"
            remote
            :remote-method="handleAssetRemoteSearch"
            placeholder="输入以搜索资产"
            primary-key="label"
            secondary-key="assetCode"
          />
        </el-form-item>
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
import { getCarPage, getCarTypes, addCar, updateCar, deleteCar } from '@/api/car'
import { getUserPage } from '@/api/user'
import { getAssetPage } from '@/api/asset'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'
import DualSelectV2 from '@/components/DualSelectV2.vue'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1, 2, 3].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const types = ref([])
const users = ref([])
const assets = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, ownerId: '', carType: undefined, carStatus: undefined })
const form = ref({})
const wizardSteps = [
  { title: '车辆信息', fields: ['carCode', 'carNumber'] },
  { title: '归属状态', fields: ['carType', 'carStatus'] },
  { title: '确认信息', fields: [] }
]
const confirmRows = computed(() => [
  { label: '车辆编号', value: valueText(form.value.carCode) },
  { label: '车牌号', value: valueText(fullPlateNumber.value) },
  { label: '业主ID', value: valueText(form.value.ownerId) },
  { label: '类型', type: 'tag', value: carTypeNameText(form.value.carType) },
  { label: '状态', type: 'tag', tagType: form.value.carStatus === 0 ? 'success' : 'info', value: carStatusText(form.value.carStatus) },
  { label: '资产ID', value: valueText(form.value.assetId) }
])
const formRules = {
  carCode: [{ required: true, message: '请输入车辆编号', trigger: 'blur' }],
  carNumber: [{ validator: validatePlateNumber, trigger: 'blur' }],
  carType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  carStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
const provinceOptions = ['\u4eac', '\u6d25', '\u6caa', '\u6e1d', '\u5180', '\u8c6b', '\u4e91', '\u8fbd', '\u9ed1', '\u6e58', '\u7696', '\u9c81', '\u65b0', '\u82cf', '\u6d59', '\u8d63', '\u9102', '\u6842', '\u7518', '\u664b', '\u8499', '\u9655', '\u5409', '\u95fd', '\u8d35', '\u7ca4', '\u9752', '\u85cf', '\u5ddd', '\u5b81', '\u743c']
const plateBodyPattern = /^[A-Z][A-Z0-9]{4,6}$/
const fullPlateNumber = computed(() => `${form.value.carProvince || ''}${form.value.carPlateBody || ''}`)
const userOptions = computed(() => users.value.length ? users.value.map(user => ({ value: user.id, label: user.uid || String(user.id), userName: user.userName || '-' })) : [{ value: '__empty_users__', label: '暂无匹配用户', userName: '', disabled: true, empty: true }])
const assetOptions = computed(() => assets.value.length ? assets.value.map(asset => ({ value: asset.id, label: asset.assetName || asset.assetCode || String(asset.id), assetCode: asset.assetCode || '-' })) : [{ value: '__empty_assets__', label: '暂无匹配资产', assetCode: '', disabled: true, empty: true }])

function valueText(value) { return value === '' || value === undefined || value === null ? '-' : value }
function carTypeNameText(id) { return types.value.find(type => type.carTypeId === id)?.carTypeName || valueText(id) }
function carStatusText(status) { return status === 0 ? '已入场' : status === 1 ? '已离场' : '未知' }
function splitPlateNumber(value) {
  const text = String(value || '').toUpperCase()
  const province = provinceOptions.includes(text.charAt(0)) ? text.charAt(0) : ''
  return { province, body: province ? text.slice(1) : text }
}
function handlePlateBodyInput(value) {
  form.value.carPlateBody = String(value || '').toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 7)
  form.value.carNumber = fullPlateNumber.value
}
function validatePlateNumber(rule, value, callback) {
  const plate = fullPlateNumber.value
  if (!form.value.carProvince || !form.value.carPlateBody) {
    callback(new Error('请输入车牌号'))
    return
  }
  if (!plateBodyPattern.test(form.value.carPlateBody)) {
    callback(new Error('请输入正确的车牌号'))
    return
  }
  form.value.carNumber = plate
  callback()
}

async function fetchData(forceRefresh = false) { loading.value = true; try { const p = { ...query.value }; Object.keys(p).forEach(k => { if (p[k] === '' || p[k] === undefined) delete p[k] }); const res = await getCarPage(p, { forceRefresh: !!forceRefresh }); tableData.value = res.data.records; total.value = res.data.total } finally { loading.value = false } }
async function fetchTypes() { const res = await getCarTypes(); types.value = res.data }
async function fetchUsers(keyword = '') { const res = await getUserPage({ pageNum: 1, pageSize: 100, uid: keyword || undefined }, { forceRefresh: true }); users.value = res.data?.records || [] }
async function fetchAssets(keyword = '') { const res = await getAssetPage({ pageNum: 1, pageSize: 100, assetName: keyword || undefined }, { forceRefresh: true }); assets.value = res.data?.records || [] }
function handleUserRemoteSearch(keyword) { fetchUsers(keyword) }
function handleAssetRemoteSearch(keyword) { fetchAssets(keyword) }
function resetQuery() { query.value = { pageNum: 1, pageSize: 10, ownerId: '', carType: undefined, carStatus: undefined }; fetchData(true) }
function openAdd() { isEdit.value = false; form.value = getDefaultForm(); dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = mapRowToForm(row); dialogVisible.value = true }
function getDefaultForm() { return { carCode: '', carNumber: '', carProvince: '', carPlateBody: '', ownerId: '', carType: undefined, carStatus: 1, assetId: '' } }
function mapRowToForm(row) { const plate = splitPlateNumber(row.carNumber); return { id: row.id, carNumber: row.carNumber, carProvince: plate.province, carPlateBody: plate.body, ownerId: row.ownerId, carType: row.carType, carStatus: row.carStatus, assetId: row.assetId } }
async function handleSubmit() { submitLoading.value = true; try { const payload = { ...form.value, carNumber: fullPlateNumber.value }; delete payload.carProvince; delete payload.carPlateBody; if (isEdit.value) { await updateCar(payload); ElMessage.success('更新成功') } else { await addCar(payload); ElMessage.success('添加成功') }; dialogVisible.value = false; fetchData(true) } finally { submitLoading.value = false } }
async function handleDelete(row) { await deleteCar(row.id); ElMessage.success('删除成功'); fetchData(true) }
onMounted(() => { fetchData(); fetchTypes(); fetchUsers(); fetchAssets() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.search-card { margin-bottom: 16px; border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.table-toolbar { margin-bottom: 12px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.plate-input { display: grid; grid-template-columns: 96px minmax(0, 1fr); gap: 8px; width: 100%; }
.plate-province { width: 96px; }
@media (max-width: 768px) {
  .plate-input { grid-template-columns: 84px minmax(0, 1fr); }
  .plate-province { width: 84px; }
}
</style>
