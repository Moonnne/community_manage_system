<template>
  <el-select-v2
    v-model="innerValue"
    filterable
    :remote="remote"
    :clearable="clearable"
    :multiple="multiple"
    :disabled="disabled"
    :options="options"
    :placeholder="placeholder"
    :remote-method="remoteMethod"
    :class="selectClass"
    @change="value => emit('change', value)"
  >
    <template v-if="filters.length || showSelectAll" #header>
      <div class="dual-select-header" @mousedown.stop @pointerdown.stop @click.stop>
        <el-select
          v-for="filter in filters"
          :key="filter.key"
          v-model="filterModels[filter.key]"
          :placeholder="filter.placeholder"
          clearable
          class="dual-select-filter"
          @mousedown.stop
          @pointerdown.stop
          @click.stop
          @change="handleFilterChange"
        >
          <el-option
            v-for="option in filter.options"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-button v-if="showSelectAll" @click.stop="emit('select-all')">全选当前结果</el-button>
      </div>
    </template>
    <template #default="{ item }">
      <div v-if="item.empty" class="select-empty-row">{{ item.label }}</div>
      <div v-else class="dual-option-row">
        <strong>{{ item[primaryKey] ?? item.label }}</strong>
        <span>{{ item[secondaryKey] ?? item.subtitle ?? item.value }}</span>
      </div>
    </template>
  </el-select-v2>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'

const props = defineProps({
  modelValue: { type: [String, Number, Array, null], default: undefined },
  options: { type: Array, default: () => [] },
  filters: { type: Array, default: () => [] },
  placeholder: { type: String, default: '请选择' },
  primaryKey: { type: String, default: 'label' },
  secondaryKey: { type: String, default: 'subtitle' },
  selectClass: { type: String, default: 'full-width' },
  multiple: { type: Boolean, default: false },
  remote: { type: Boolean, default: false },
  clearable: { type: Boolean, default: true },
  disabled: { type: Boolean, default: false },
  showSelectAll: { type: Boolean, default: false },
  remoteMethod: { type: Function, default: undefined }
})

const emit = defineEmits(['update:modelValue', 'change', 'filter-change', 'select-all'])
const filterModels = reactive({})

watch(
  () => props.filters,
  filters => {
    filters.forEach(filter => {
      if ('value' in filter && filterModels[filter.key] !== filter.value) {
        filterModels[filter.key] = filter.value
      } else if (!(filter.key in filterModels)) {
        filterModels[filter.key] = filter.value ?? undefined
      }
    })
  },
  { immediate: true }
)

const innerValue = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

function handleFilterChange() {
  emit('filter-change', { ...filterModels })
}
</script>

<style scoped>
.dual-select-header {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 8px;
  padding: 8px;
  border-bottom: 1px solid var(--vp-c-divider);
}

.dual-select-filter {
  width: 100%;
}

.dual-option-row {
  display: flex;
  min-width: 0;
  align-items: baseline;
  gap: 8px;
}

.dual-option-row strong {
  color: var(--vp-c-text-1);
  font-weight: 800;
}

.dual-option-row span {
  min-width: 0;
  overflow: hidden;
  color: var(--vp-c-text-3);
  font-size: 13px;
  font-style: italic;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.select-empty-row {
  color: var(--vp-c-text-3);
}
</style>
