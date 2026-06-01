<template>
  <div class="tech-bg-container" ref="containerRef">
    <canvas ref="canvasRef" class="tech-network-canvas"></canvas>
    <!-- 固定在标题上方的混色光晕 -->
    <div class="tech-fixed-glow"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const canvasRef = ref(null)
const containerRef = ref(null)
const isDark = computed(() => document.documentElement.getAttribute('data-theme') === 'dark')

let ctx
let w = 0
let h = 0
let dpr = 1
let nodes = []
let linkStates = new Map()
let explosions = []
let tears = []
let animationFrameId = 0
let lastFrameTime = 0
let isPointerDown = false
let lastPointer = null
let resizeTimer = 0

// 节点网络参数
const nodeDensity = 0.00016
const minNodeCount = 150
const maxNodeCount = 380
const nodeRadius = 1.35
const linkDistance = 128
const linkRecoverSpeed = 0.072
const linkBreakSpeed = 0.32

// 物理模拟参数
const friction = 0.958
const returnStrength = 0.0042
const flowStrength = 0.034
const maxVelocity = 6.6

// 鼠标交互参数
const dragTearRadius = 30
const dragTearFeather = 18
const dragPushStrength = 0.58
const explosionRadius = 128
const explosionStrength = 5.6

const clamp = (value, min, max) => Math.max(min, Math.min(max, value))
const randomBetween = (min, max) => min + Math.random() * (max - min)

class NetworkNode {
  constructor(x, y) {
    this.x = x
    this.y = y
    this.homeX = x
    this.homeY = y
    this.vx = randomBetween(-0.48, 0.48)
    this.vy = randomBetween(-0.48, 0.48)
    this.phase = Math.random() * Math.PI * 2
    this.flowOffset = Math.random() * 1000
    this.breakEnergy = 0
  }

  update(time) {
    // 轻微随机流动：让背景始终像“活的网络”，而不是静止点阵。
    const flowX = Math.cos(time * 0.00082 + this.phase + this.flowOffset) * flowStrength
    const flowY = Math.sin(time * 0.00094 + this.phase * 1.35) * flowStrength

    // 弱回弹：节点被撕开后会缓慢回到自己的活动区域，从而完成重连。
    const homeDx = this.homeX - this.x
    const homeDy = this.homeY - this.y

    this.vx += flowX + homeDx * returnStrength
    this.vy += flowY + homeDy * returnStrength

    this.applyTearForces()
    this.applyExplosionForces()

    this.vx *= friction
    this.vy *= friction

    const speed = Math.hypot(this.vx, this.vy)
    if (speed > maxVelocity) {
      this.vx = (this.vx / speed) * maxVelocity
      this.vy = (this.vy / speed) * maxVelocity
    }

    this.x += this.vx
    this.y += this.vy

    // 允许少量越界再回流，避免边缘显得被裁切。
    if (this.x < -60 || this.x > w + 60) this.vx += (w / 2 - this.x) * 0.002
    if (this.y < -60 || this.y > h + 60) this.vy += (h / 2 - this.y) * 0.002

    this.breakEnergy *= 0.93
  }

  applyTearForces() {
    for (const tear of tears) {
      const { distance, t } = distanceToSegment(this.x, this.y, tear.fromX, tear.fromY, tear.x, tear.y)
      const effectiveRadius = dragTearRadius + dragTearFeather
      if (distance > effectiveRadius) continue

      const coreCut = distance < dragTearRadius ? 1 : 1 - (distance - dragTearRadius) / dragTearFeather
      const pathFade = Math.sin(t * Math.PI) * 0.35 + 0.65
      const influence = coreCut * pathFade * tear.life
      const sideValue = (this.x - tear.x) * tear.normalX + (this.y - tear.y) * tear.normalY
      const side = Math.sign(sideValue) || 1
      const speedBoost = clamp(tear.speed / 26, 0.18, 0.82)

      // 刀切效果只给节点一个很轻的横向分离力，避免像点击爆炸一样大范围散开。
      this.vx += tear.normalX * side * influence * dragPushStrength * speedBoost
      this.vy += tear.normalY * side * influence * dragPushStrength * speedBoost
      this.breakEnergy = Math.max(this.breakEnergy, influence * 0.72)
    }
  }

  applyExplosionForces() {
    for (const explosion of explosions) {
      const dx = this.x - explosion.x
      const dy = this.y - explosion.y
      const dist = Math.hypot(dx, dy)
      const waveRadius = explosionRadius * explosion.progress
      const waveWidth = 44
      const waveDist = Math.abs(dist - waveRadius)

      if (dist > explosionRadius || waveDist > waveWidth || dist < 0.001) continue

      const wave = 1 - waveDist / waveWidth
      const fade = 1 - explosion.progress
      const force = wave * fade * explosionStrength

      this.vx += (dx / dist) * force
      this.vy += (dy / dist) * force
      this.breakEnergy = Math.max(this.breakEnergy, wave * fade)
    }
  }

  draw(fillStyle) {
    const pulse = 0.45 + this.breakEnergy * 1.7
    ctx.beginPath()
    ctx.arc(this.x, this.y, nodeRadius + pulse, 0, Math.PI * 2)
    ctx.fillStyle = fillStyle
    ctx.fill()
  }
}

const getPointerPosition = (event) => {
  const source = event.touches?.[0] || event.changedTouches?.[0] || event
  return {
    x: source.clientX,
    y: source.clientY
  }
}

const getLinkKey = (a, b) => (a < b ? `${a}:${b}` : `${b}:${a}`)

const distanceToSegment = (px, py, ax, ay, bx, by) => {
  const abx = bx - ax
  const aby = by - ay
  const lenSq = abx * abx + aby * aby
  if (lenSq < 0.001) return { distance: Math.hypot(px - bx, py - by), t: 1 }

  const t = clamp(((px - ax) * abx + (py - ay) * aby) / lenSq, 0, 1)
  const cx = ax + abx * t
  const cy = ay + aby * t

  return {
    distance: Math.hypot(px - cx, py - cy),
    t
  }
}

const calculateNodeCount = () => {
  const count = Math.round(w * h * nodeDensity)
  return clamp(count, minNodeCount, maxNodeCount)
}

const createNodes = () => {
  const count = calculateNodeCount()
  nodes = Array.from({ length: count }, () => {
    const padding = 24
    return new NetworkNode(
      randomBetween(padding, w - padding),
      randomBetween(padding, h - padding)
    )
  })
  linkStates = new Map()
}

const resize = () => {
  if (!canvasRef.value) return

  dpr = clamp(window.devicePixelRatio || 1, 1, 2)
  w = window.innerWidth
  h = window.innerHeight

  canvasRef.value.width = Math.floor(w * dpr)
  canvasRef.value.height = Math.floor(h * dpr)
  canvasRef.value.style.width = `${w}px`
  canvasRef.value.style.height = `${h}px`

  ctx = canvasRef.value.getContext('2d')
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

  createNodes()
}

const scheduleResize = () => {
  window.clearTimeout(resizeTimer)
  resizeTimer = window.setTimeout(resize, 120)
}

const isLinkCutByTear = (x1, y1, x2, y2) => {
  const mx = (x1 + x2) / 2
  const my = (y1 + y2) / 2

  for (const tear of tears) {
    const { distance } = distanceToSegment(mx, my, tear.fromX, tear.fromY, tear.x, tear.y)
    const cutWidth = dragTearRadius * (0.42 + tear.life * 0.58)
    if (distance < cutWidth) return true
  }

  for (const explosion of explosions) {
    const dist = Math.hypot(mx - explosion.x, my - explosion.y)
    const currentRadius = explosionRadius * explosion.progress
    if (dist < currentRadius && dist > currentRadius - 48) return true
  }

  return false
}

const drawLinks = (lineColor) => {
  ctx.lineWidth = 1

  for (let i = 0; i < nodes.length; i += 1) {
    for (let j = i + 1; j < nodes.length; j += 1) {
      const a = nodes[i]
      const b = nodes[j]
      const dx = b.x - a.x
      const dy = b.y - a.y
      const dist = Math.hypot(dx, dy)
      const key = getLinkKey(i, j)
      const previousAlpha = linkStates.get(key) || 0

      const inRange = dist < linkDistance
      const interactionBroken = isLinkCutByTear(a.x, a.y, b.x, b.y)
      const energyBroken = Math.max(a.breakEnergy, b.breakEnergy) > 0.22
      const shouldShow = inRange && !interactionBroken && !energyBroken
      const targetAlpha = shouldShow ? (1 - dist / linkDistance) * 0.82 : 0
      const speed = targetAlpha > previousAlpha ? linkRecoverSpeed : linkBreakSpeed
      const nextAlpha = previousAlpha + (targetAlpha - previousAlpha) * speed

      if (nextAlpha > 0.015) {
        linkStates.set(key, nextAlpha)
        ctx.beginPath()
        ctx.moveTo(a.x, a.y)
        ctx.lineTo(b.x, b.y)
        ctx.strokeStyle = lineColor(nextAlpha)
        ctx.stroke()
      } else if (previousAlpha) {
        linkStates.delete(key)
      }
    }
  }
}

const drawTearRipples = () => {
  for (const tear of tears) {
    const alpha = tear.life * 0.34

    ctx.beginPath()
    ctx.moveTo(tear.fromX, tear.fromY)
    ctx.lineTo(tear.x, tear.y)
    ctx.strokeStyle = isDark.value
      ? `rgba(125, 211, 252, ${alpha})`
      : `rgba(59, 130, 246, ${alpha})`
    ctx.lineWidth = 1.15
    ctx.stroke()

    ctx.beginPath()
    ctx.moveTo(tear.fromX + tear.normalX * dragTearRadius * 0.52, tear.fromY + tear.normalY * dragTearRadius * 0.52)
    ctx.lineTo(tear.x + tear.normalX * dragTearRadius * 0.52, tear.y + tear.normalY * dragTearRadius * 0.52)
    ctx.moveTo(tear.fromX - tear.normalX * dragTearRadius * 0.52, tear.fromY - tear.normalY * dragTearRadius * 0.52)
    ctx.lineTo(tear.x - tear.normalX * dragTearRadius * 0.52, tear.y - tear.normalY * dragTearRadius * 0.52)
    ctx.strokeStyle = isDark.value
      ? `rgba(45, 212, 191, ${alpha * 0.28})`
      : `rgba(20, 184, 166, ${alpha * 0.24})`
    ctx.lineWidth = 0.8
    ctx.stroke()
  }
}

const drawExplosions = () => {
  for (const explosion of explosions) {
    const radius = explosionRadius * explosion.progress
    const alpha = (1 - explosion.progress) * 0.42

    ctx.beginPath()
    ctx.arc(explosion.x, explosion.y, radius, 0, Math.PI * 2)
    ctx.strokeStyle = isDark.value
      ? `rgba(167, 139, 250, ${alpha})`
      : `rgba(124, 58, 237, ${alpha})`
    ctx.lineWidth = 1.5
    ctx.stroke()

    ctx.beginPath()
    ctx.arc(explosion.x, explosion.y, radius * 0.46, 0, Math.PI * 2)
    ctx.strokeStyle = isDark.value
      ? `rgba(45, 212, 191, ${alpha * 0.65})`
      : `rgba(20, 184, 166, ${alpha * 0.55})`
    ctx.lineWidth = 1
    ctx.stroke()
  }
}

const cleanupInteractions = () => {
  tears = tears
    .map((tear) => ({ ...tear, life: tear.life * 0.8 }))
    .filter((tear) => tear.life > 0.03)

  explosions = explosions
    .map((explosion) => ({ ...explosion, progress: explosion.progress + 0.042 }))
    .filter((explosion) => explosion.progress < 1)
}

const draw = (time = 0) => {
  if (!ctx) return

  const delta = lastFrameTime ? clamp(time - lastFrameTime, 8, 32) : 16
  lastFrameTime = time

  ctx.clearRect(0, 0, w, h)

  const nodeColor = isDark.value
    ? 'rgba(203, 213, 225, 0.68)'
    : 'rgba(71, 85, 105, 0.54)'

  const lineColor = (alpha) => isDark.value
    ? `rgba(148, 163, 184, ${alpha * 0.52})`
    : `rgba(71, 85, 105, ${alpha * 0.34})`

  // delta 用于在掉帧时保持运动速度相对稳定。
  const steps = delta > 20 ? 2 : 1
  for (let step = 0; step < steps; step += 1) {
    for (const node of nodes) node.update(time)
  }

  drawLinks(lineColor)
  for (const node of nodes) node.draw(nodeColor)
  drawTearRipples()
  drawExplosions()
  cleanupInteractions()

  animationFrameId = requestAnimationFrame(draw)
}

const createTear = (current, previous) => {
  if (!previous) return

  const dx = current.x - previous.x
  const dy = current.y - previous.y
  const speed = Math.hypot(dx, dy)
  if (speed < 1.5) return

  tears.push({
    fromX: previous.x,
    fromY: previous.y,
    x: current.x,
    y: current.y,
    normalX: -dy / speed,
    normalY: dx / speed,
    speed,
    life: 1
  })

  if (tears.length > 36) tears.shift()
}

const createExplosion = (point) => {
  explosions.push({
    x: point.x,
    y: point.y,
    progress: 0.04
  })

  if (explosions.length > 8) explosions.shift()
}

const handlePointerDown = (event) => {
  isPointerDown = true
  lastPointer = getPointerPosition(event)
}

const handlePointerMove = (event) => {
  const current = getPointerPosition(event)

  // 鼠标不需要按下：悬浮移动时就会像“拖动”一样撕开节点网络。
  // 触摸设备本身没有 hover，因此触摸移动时同样会触发。
  createTear(current, lastPointer)

  lastPointer = current
}

const handlePointerUp = () => {
  isPointerDown = false
}

const handleClick = (event) => {
  createExplosion(getPointerPosition(event))
}

onMounted(() => {
  resize()
  window.addEventListener('resize', scheduleResize)
  window.addEventListener('pointerdown', handlePointerDown)
  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerup', handlePointerUp)
  window.addEventListener('pointercancel', handlePointerUp)
  window.addEventListener('click', handleClick)
  animationFrameId = requestAnimationFrame(draw)
})

onUnmounted(() => {
  window.clearTimeout(resizeTimer)
  window.removeEventListener('resize', scheduleResize)
  window.removeEventListener('pointerdown', handlePointerDown)
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', handlePointerUp)
  window.removeEventListener('pointercancel', handlePointerUp)
  window.removeEventListener('click', handleClick)
  cancelAnimationFrame(animationFrameId)
})
</script>

<style scoped>
.tech-bg-container {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.tech-network-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.tech-fixed-glow {
  position: absolute;
  top: 15%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  height: 400px;
  pointer-events: none;
  background:
    radial-gradient(circle at 40% 50%,
      rgba(122, 92, 255, 0.15) 0%,
      transparent 70%),
    radial-gradient(circle at 60% 50%,
      rgba(16, 185, 129, 0.12) 0%,
      transparent 70%);
  filter: blur(60px);
}

[data-theme='dark'] .tech-fixed-glow {
  background:
    radial-gradient(circle at 40% 50%,
      rgba(122, 92, 255, 0.25) 0%,
      transparent 70%),
    radial-gradient(circle at 60% 50%,
      rgba(16, 185, 129, 0.2) 0%,
      transparent 70%);
}
</style>
