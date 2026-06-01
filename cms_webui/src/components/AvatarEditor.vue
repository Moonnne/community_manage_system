<template>
  <div class="avatar-editor">
    <button type="button" class="avatar-preview" @click="openPreview">
      <el-avatar :size="size" shape="circle" :src="modelValue" :style="{ backgroundColor: 'var(--vp-c-brand-1)' }">
        {{ initial }}
      </el-avatar>
      <span class="avatar-mask">预览</span>
    </button>

    <div class="avatar-actions">
      <el-button size="small" :icon="Upload" @click="openPicker">修改头像</el-button>
    </div>
    <p v-if="tip" class="avatar-tip">{{ tip }}</p>

    <input ref="fileInputRef" class="file-input" type="file" accept="image/*" @change="handleFileChange" />

    <el-dialog v-model="previewVisible" title="头像预览" width="420px">
      <div class="large-avatar-wrap">
        <el-avatar :size="260" shape="circle" :src="modelValue" :style="{ backgroundColor: 'var(--vp-c-brand-1)' }">
          {{ initial }}
        </el-avatar>
      </div>
    </el-dialog>

    <el-dialog v-model="cropVisible" title="裁切头像" width="680px" :close-on-click-modal="false">
      <div class="crop-layout">
        <div
          ref="cropBoxRef"
          class="crop-box"
          @wheel.prevent="handleWheel"
          @pointerdown.prevent="startPointer"
          @pointermove.prevent="movePointer"
          @pointerup="endPointer"
          @pointercancel="endPointer"
          @pointerleave="endPointer"
        >
          <img
            v-if="localImageUrl"
            class="crop-image"
            :src="localImageUrl"
            :style="imageStyle"
            alt="待裁切头像"
            draggable="false"
            @dragstart.prevent
            @load="handleImageLoad"
          />
          <div class="crop-frame"></div>
        </div>

        <div class="crop-controls">
          <span>缩放</span>
          <el-slider v-model="zoom" :min="1" :max="4" :step="0.01" />
        </div>
      </div>

      <template #footer>
        <el-button @click="cropVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="confirmCrop">上传头像</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { uploadUserAvatar } from '@/api/user'

defineProps({
  modelValue: { type: String, default: '' },
  initial: { type: String, default: 'U' },
  size: { type: Number, default: 128 },
  tip: { type: String, default: '点击头像可预览大图，选择图片后可通过拖拽、缩放进行裁切，上传的文件需小于 10MB。' }
})

const emit = defineEmits(['update:modelValue', 'uploaded'])

const fileInputRef = ref(null)
const cropBoxRef = ref(null)
const sourceFile = ref(null)
const localImageUrl = ref('')
const previewVisible = ref(false)
const cropVisible = ref(false)
const uploading = ref(false)
const zoom = ref(1)
const offset = ref({ x: 0, y: 0 })
const imageSize = ref({ width: 0, height: 0 })
const pointers = new Map()
const gesture = ref({
  mode: '',
  startX: 0,
  startY: 0,
  offsetX: 0,
  offsetY: 0,
  distance: 0,
  zoom: 1
})

const cropMetrics = computed(() => {
  const stageSize = cropBoxRef.value?.clientWidth || 420
  const frameSize = Math.min(340, stageSize - 56)
  const frameOffset = (stageSize - frameSize) / 2
  return { stageSize, frameSize, frameOffset }
})

const imageStyle = computed(() => {
  const { frameSize, stageSize } = cropMetrics.value
  const baseScale = imageSize.value.width && imageSize.value.height
    ? Math.max(frameSize / imageSize.value.width, frameSize / imageSize.value.height)
    : 1
  const width = imageSize.value.width * baseScale * zoom.value
  const height = imageSize.value.height * baseScale * zoom.value
  return {
    width: `${width}px`,
    height: `${height}px`,
    transform: `translate(${stageSize / 2 - width / 2 + offset.value.x}px, ${stageSize / 2 - height / 2 + offset.value.y}px)`
  }
})

watch(zoom, () => {
  offset.value = clampOffset(offset.value)
})

function openPicker() {
  fileInputRef.value?.click()
}

function openPreview() {
  previewVisible.value = true
}

function handleFileChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.size / 1024 / 1024 > 10) {
    ElMessage.error('头像文件不能超过 10MB')
    return
  }
  sourceFile.value = file
  if (localImageUrl.value) URL.revokeObjectURL(localImageUrl.value)
  localImageUrl.value = URL.createObjectURL(file)
  zoom.value = 1
  offset.value = { x: 0, y: 0 }
  pointers.clear()
  cropVisible.value = true
}

function handleImageLoad(event) {
  imageSize.value = {
    width: event.target.naturalWidth,
    height: event.target.naturalHeight
  }
  offset.value = clampOffset(offset.value)
}

function handleWheel(event) {
  const nextZoom = zoom.value + (event.deltaY > 0 ? -0.08 : 0.08)
  setZoom(nextZoom)
}

function startPointer(event) {
  cropBoxRef.value?.setPointerCapture?.(event.pointerId)
  pointers.set(event.pointerId, { x: event.clientX, y: event.clientY })

  if (pointers.size === 1) {
    gesture.value = {
      mode: 'drag',
      startX: event.clientX,
      startY: event.clientY,
      offsetX: offset.value.x,
      offsetY: offset.value.y,
      distance: 0,
      zoom: zoom.value
    }
  } else if (pointers.size === 2) {
    const [a, b] = Array.from(pointers.values())
    gesture.value = {
      mode: 'pinch',
      startX: 0,
      startY: 0,
      offsetX: offset.value.x,
      offsetY: offset.value.y,
      distance: pointerDistance(a, b),
      zoom: zoom.value
    }
  }
}

function movePointer(event) {
  if (!pointers.has(event.pointerId)) return
  pointers.set(event.pointerId, { x: event.clientX, y: event.clientY })

  if (gesture.value.mode === 'drag' && pointers.size === 1) {
    offset.value = clampOffset({
      x: gesture.value.offsetX + event.clientX - gesture.value.startX,
      y: gesture.value.offsetY + event.clientY - gesture.value.startY
    })
    return
  }

  if (gesture.value.mode === 'pinch' && pointers.size >= 2) {
    const [a, b] = Array.from(pointers.values())
    const ratio = pointerDistance(a, b) / Math.max(gesture.value.distance, 1)
    setZoom(gesture.value.zoom * ratio)
  }
}

function endPointer(event) {
  pointers.delete(event.pointerId)
  if (pointers.size === 0) {
    gesture.value.mode = ''
    return
  }
  if (pointers.size === 1) {
    const [point] = Array.from(pointers.values())
    gesture.value = {
      mode: 'drag',
      startX: point.x,
      startY: point.y,
      offsetX: offset.value.x,
      offsetY: offset.value.y,
      distance: 0,
      zoom: zoom.value
    }
  }
}

async function confirmCrop() {
  if (!sourceFile.value || !localImageUrl.value) return
  uploading.value = true
  try {
    const file = await cropToFile()
    const res = await uploadUserAvatar(file)
    emit('update:modelValue', res.data)
    emit('uploaded', res.data)
    ElMessage.success('头像上传成功')
    cropVisible.value = false
  } finally {
    uploading.value = false
  }
}

function cropToFile() {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => {
      const { stageSize, frameSize, frameOffset } = cropMetrics.value
      const canvas = document.createElement('canvas')
      canvas.width = 512
      canvas.height = 512
      const context = canvas.getContext('2d')
      const baseScale = Math.max(frameSize / image.width, frameSize / image.height)
      const scale = baseScale * zoom.value
      const displayWidth = image.width * scale
      const displayHeight = image.height * scale
      const imageLeft = stageSize / 2 - displayWidth / 2 + offset.value.x
      const imageTop = stageSize / 2 - displayHeight / 2 + offset.value.y
      const ratio = 512 / frameSize
      context.drawImage(
        image,
        (imageLeft - frameOffset) * ratio,
        (imageTop - frameOffset) * ratio,
        displayWidth * ratio,
        displayHeight * ratio
      )
      canvas.toBlob(blob => {
        if (!blob) {
          reject(new Error('头像裁切失败'))
          return
        }
        const filename = (sourceFile.value?.name || 'avatar').replace(/\.[^.]+$/, '.png')
        resolve(new File([blob], filename, { type: 'image/png' }))
      }, 'image/png', 0.92)
    }
    image.onerror = reject
    image.src = localImageUrl.value
  })
}

function pointerDistance(a, b) {
  return Math.hypot(a.x - b.x, a.y - b.y)
}

function setZoom(value) {
  zoom.value = clamp(value, 1, 4)
  offset.value = clampOffset(offset.value)
}

function getDisplaySize(targetZoom = zoom.value) {
  const { frameSize } = cropMetrics.value
  const baseScale = imageSize.value.width && imageSize.value.height
    ? Math.max(frameSize / imageSize.value.width, frameSize / imageSize.value.height)
    : 1
  return {
    width: imageSize.value.width * baseScale * targetZoom,
    height: imageSize.value.height * baseScale * targetZoom
  }
}

function clampOffset(nextOffset) {
  const { frameSize } = cropMetrics.value
  const display = getDisplaySize()
  const maxX = Math.max((display.width - frameSize) / 2, 0)
  const maxY = Math.max((display.height - frameSize) / 2, 0)
  return {
    x: clamp(nextOffset.x, -maxX, maxX),
    y: clamp(nextOffset.y, -maxY, maxY)
  }
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value))
}
</script>

<style scoped>
.avatar-editor {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-preview {
  position: relative;
  display: inline-flex;
  border: 0;
  padding: 0;
  border-radius: 50%;
  background: transparent;
  cursor: zoom-in;
  overflow: hidden;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  background: rgba(0, 0, 0, 0.48);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar-preview:hover .avatar-mask {
  opacity: 1;
}

.avatar-actions {
  display: flex;
  justify-content: center;
}

.avatar-tip {
  max-width: 260px;
  margin: -2px 0 0;
  color: var(--vp-c-text-3);
  font-size: 12px;
  font-style: italic;
  line-height: 1.6;
  text-align: center;
}

.file-input {
  display: none;
}

.large-avatar-wrap {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.crop-layout {
  display: grid;
  gap: 18px;
}

.crop-box {
  position: relative;
  width: min(420px, 78vw);
  aspect-ratio: 1;
  margin: 0 auto;
  overflow: hidden;
  background: #101827;
  cursor: grab;
  touch-action: none;
  user-select: none;
}

.crop-box:active {
  cursor: grabbing;
}

.crop-image {
  position: absolute;
  left: 0;
  top: 0;
  user-select: none;
  pointer-events: none;
  -webkit-user-drag: none;
}

.crop-frame {
  position: absolute;
  pointer-events: none;
  width: min(340px, calc(100% - 56px));
  height: min(340px, calc(100% - 56px));
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border: 3px solid var(--vp-c-brand-1);
  box-shadow:
    0 0 0 9999px rgba(0, 0, 0, 0.62),
    0 0 0 2px rgba(255, 255, 255, 0.9) inset;
}

.crop-controls {
  display: grid;
  grid-template-columns: 46px 1fr;
  align-items: center;
  gap: 12px;
}
</style>
