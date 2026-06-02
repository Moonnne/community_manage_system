<template>
  <el-table :data="normalizedRows" stripe style="width: 100%">
    <el-table-column prop="label" label="字段" width="140" show-overflow-tooltip />
    <el-table-column label="内容" min-width="220" show-overflow-tooltip>
      <template #default="{ row }">
        <div v-if="row.type === 'avatar'" class="confirm-avatar-cell">
          <el-avatar
            :size="48"
            :src="row.avatarUrl"
            :style="{ backgroundColor: 'var(--vp-c-brand-1)' }"
          >
            {{ row.initial || 'U' }}
          </el-avatar>
        </div>
        <el-tag v-else-if="row.type === 'tag'" size="small" :type="row.tagType || 'info'">
          {{ row.value }}
        </el-tag>
        <span v-else>{{ row.value }}</span>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  rows: { type: Array, default: () => [] }
})

const normalizedRows = computed(() =>
  props.rows.map(row => ({
    ...row,
    value: row.value === '' || row.value === undefined || row.value === null ? '-' : row.value
  }))
)
</script>

<style scoped>
.confirm-avatar-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>
