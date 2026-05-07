<template>
  <div class="tech-bg-container" ref="containerRef">
    <canvas ref="canvasRef" class="tech-grid-canvas"></canvas>
    <!-- 固定在标题上方的混色光晕 -->
    <div class="tech-fixed-glow"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const canvasRef = ref(null)
const containerRef = ref(null)
const mouse = ref({ x: -1000, y: -1000 })
const isDark = computed(() => document.documentElement.getAttribute('data-theme') === 'dark')

let ctx, w, h
let dots = []
let animationFrameId

// 物理模拟参数
const spacing = 36
const baseRadius = 1.4
const influenceRadius = 220 // 吸引与环绕的作用半径
const attractStrength = 0.6 // 吸引力度
const orbitStrength = 1.5   // 环绕速度系数
const friction = 0.92      // 阻力 (调高以保持环绕动能)
const spring = 0.04        // 回位弹力 (调低以允许更自由的环绕)

class Dot {
  constructor(x, y) {
    this.x = x
    this.y = y
    this.originX = x
    this.originY = y
    this.vx = 0
    this.vy = 0
    // 随机化每个粒子的环绕特性，形成不同距离的错落感
    this.orbitFactor = 0.5 + Math.random() * 1.5 
  }

  update(mouseX, mouseY) {
    const dx = mouseX - this.x
    const dy = mouseY - this.y
    const dist = Math.sqrt(dx * dx + dy * dy)
    
    let ax = 0
    let ay = 0

    if (dist < influenceRadius) {
      const force = (influenceRadius - dist) / influenceRadius
      
      // 1. 吸引力：向鼠标中心拉扯
      ax += (dx / dist) * force * attractStrength
      ay += (dy / dist) * force * attractStrength

      // 2. 环绕力：垂直于吸引方向的切线力
      // (-dy, dx) 是旋转 90 度的切线向量
      ax += (-dy / dist) * force * orbitStrength * this.orbitFactor
      ay += (dx / dist) * force * orbitStrength * this.orbitFactor
    }

    // 3. 复原力：拉回原始网格位置
    ax += (this.originX - this.x) * spring
    ay += (this.originY - this.y) * spring

    // 物理累加
    this.vx = (this.vx + ax) * friction
    this.vy = (this.vy + ay) * friction

    this.x += this.vx
    this.y += this.vy
  }

  draw(ctx, color) {
    ctx.beginPath()
    ctx.arc(this.x, this.y, baseRadius, 0, Math.PI * 2)
    ctx.fillStyle = color
    ctx.fill()
  }
}

const resize = () => {
  if (!canvasRef.value) return
  w = canvasRef.value.width = window.innerWidth
  h = canvasRef.value.height = window.innerHeight
  
  dots = []
  for (let x = 0; x < w + spacing; x += spacing) {
    for (let y = 0; y < h + spacing; y += spacing) {
      dots.push(new Dot(x, y))
    }
  }
}

const draw = () => {
  if (!ctx) return
  ctx.clearRect(0, 0, w, h)
  
  const dotColor = isDark.value ? 'rgba(148, 163, 184, 0.25)' : 'rgba(107, 114, 128, 0.2)'

  dots.forEach(dot => {
    dot.update(mouse.value.x, mouse.value.y)
    dot.draw(ctx, dotColor)
  })
  
  animationFrameId = requestAnimationFrame(draw)
}

const handleMouseMove = (e) => {
  mouse.value = { x: e.clientX, y: e.clientY }
}

onMounted(() => {
  ctx = canvasRef.value.getContext('2d')
  window.addEventListener('resize', resize)
  window.addEventListener('mousemove', handleMouseMove)
  resize()
  draw()
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
  window.removeEventListener('mousemove', handleMouseMove)
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

.tech-grid-canvas {
  position: absolute;
  inset: 0;
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
