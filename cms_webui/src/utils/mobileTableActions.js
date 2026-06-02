const MOBILE_QUERY = '(max-width: 768px)'
const LONG_PRESS_DELAY = 560
const MOVE_TOLERANCE = 10
const OPERATION_COLUMN_CLASS_RE = /^el-table_\d+_column_\d+$/

let mediaQuery
let pressTimer
let startPoint
let menuRoot
let tableObserver
let syncFrame

function isMobile() {
  mediaQuery ||= window.matchMedia(MOBILE_QUERY)
  return mediaQuery.matches
}

function getRowFromEvent(event) {
  const target = event.target
  if (!(target instanceof Element)) return null
  return target.closest('.el-table__body-wrapper tbody tr.el-table__row')
}

function isVisible(element) {
  const rect = element.getBoundingClientRect()
  const style = window.getComputedStyle(element)
  return rect.width > 0 && rect.height > 0 && style.visibility !== 'hidden' && style.display !== 'none'
}

function uniqueButtons(buttons) {
  const seen = new Set()
  return buttons.filter((button) => {
    const key = [
      button.textContent?.trim(),
      button.classList.contains('el-button--danger') ? 'danger' : '',
      button.classList.contains('el-button--warning') ? 'warning' : '',
      button.classList.contains('el-button--primary') ? 'primary' : ''
    ].join(':')
    if (seen.has(key)) return false
    seen.add(key)
    return true
  })
}

function getRowActionButtons(row) {
  const table = row.closest('.el-table')
  if (!table) return []

  const body = row.closest('tbody')
  const sourceRows = body ? Array.from(body.querySelectorAll('tr.el-table__row')) : []
  const rowIndex = sourceRows.indexOf(row)
  if (rowIndex < 0) return []

  const wrappers = Array.from(table.querySelectorAll('.el-table__body-wrapper'))
  const buttons = wrappers.flatMap((wrapper) => {
    const rows = Array.from(wrapper.querySelectorAll('tbody tr.el-table__row'))
    const sameRow = rows[rowIndex]
    if (!sameRow) return []

    return Array.from(sameRow.querySelectorAll('.el-button')).filter((button) => {
      const text = button.textContent?.trim()
      return text && !button.classList.contains('is-disabled') && !button.disabled && (isMobile() || isVisible(button))
    })
  })

  return uniqueButtons(buttons)
}

function closeMenu() {
  if (!menuRoot) return
  menuRoot.remove()
  menuRoot = null
}

function getColumnClass(cell) {
  return Array.from(cell.classList).find((className) => OPERATION_COLUMN_CLASS_RE.test(className))
}

function getOperationColumnClasses(table) {
  return Array.from(table.querySelectorAll('.el-table__header-wrapper th'))
    .filter((header) => header.classList.contains('el-table-fixed-column--right'))
    .map(getColumnClass)
    .filter(Boolean)
}

function setOperationColumnCollapsed(table, columnClass, collapsed) {
  Array.from(table.querySelectorAll(`col[name="${columnClass}"]`)).forEach((col) => {
    if (!col.dataset.cmsOriginalWidth) {
      col.dataset.cmsOriginalWidth = col.getAttribute('width') || ''
    }

    if (collapsed) {
      col.setAttribute('width', '0')
      col.style.width = '0px'
    } else {
      const originalWidth = col.dataset.cmsOriginalWidth
      if (originalWidth) col.setAttribute('width', originalWidth)
      else col.removeAttribute('width')
      col.style.width = ''
    }
  })

  Array.from(table.querySelectorAll(`.${columnClass}`)).forEach((cell) => {
    cell.classList.toggle('cms-mobile-operation-cell', collapsed)
  })
}

function syncMobileOperationColumns() {
  const collapsed = isMobile()
  document.querySelectorAll('.table-card .el-table').forEach((table) => {
    getOperationColumnClasses(table).forEach((columnClass) => {
      setOperationColumnCollapsed(table, columnClass, collapsed)
    })
  })
}

function scheduleOperationColumnSync() {
  if (syncFrame) return
  syncFrame = window.requestAnimationFrame(() => {
    syncFrame = null
    syncMobileOperationColumns()
  })
}

function createMenuItem(button) {
  const item = document.createElement('button')
  item.type = 'button'
  item.textContent = button.textContent.trim()
  item.className = 'mobile-table-action-item'
  if (button.classList.contains('el-button--danger')) item.classList.add('danger')
  if (button.classList.contains('el-button--warning')) item.classList.add('warning')
  item.addEventListener('click', () => {
    closeMenu()
    button.click()
  })
  return item
}

function showMenu(row, clientX, clientY) {
  if (!isMobile()) return
  const actions = getRowActionButtons(row)
  if (actions.length === 0) return

  closeMenu()

  const overlay = document.createElement('div')
  overlay.className = 'mobile-table-action-overlay'

  const menu = document.createElement('div')
  menu.className = 'mobile-table-action-menu'
  actions.forEach((button) => menu.appendChild(createMenuItem(button)))
  overlay.appendChild(menu)
  document.body.appendChild(overlay)
  menuRoot = overlay

  const menuRect = menu.getBoundingClientRect()
  const viewportPadding = 10
  const left = Math.min(Math.max(clientX, viewportPadding), window.innerWidth - menuRect.width - viewportPadding)
  const top = Math.min(Math.max(clientY, viewportPadding), window.innerHeight - menuRect.height - viewportPadding)
  menu.style.left = `${left}px`
  menu.style.top = `${top}px`

  overlay.addEventListener('click', (event) => {
    if (event.target === overlay) closeMenu()
  })
}

function clearPressTimer() {
  if (!pressTimer) return
  window.clearTimeout(pressTimer)
  pressTimer = null
}

function handleContextMenu(event) {
  if (!isMobile()) return
  const row = getRowFromEvent(event)
  if (!row || getRowActionButtons(row).length === 0) return

  event.preventDefault()
  showMenu(row, event.clientX, event.clientY)
}

function handleTouchStart(event) {
  if (!isMobile() || event.touches.length !== 1) return
  const row = getRowFromEvent(event)
  if (!row || getRowActionButtons(row).length === 0) return

  const touch = event.touches[0]
  startPoint = { x: touch.clientX, y: touch.clientY, row }
  clearPressTimer()
  pressTimer = window.setTimeout(() => {
    navigator.vibrate?.(10)
    showMenu(row, touch.clientX, touch.clientY)
    pressTimer = null
  }, LONG_PRESS_DELAY)
}

function handleTouchMove(event) {
  if (!startPoint || event.touches.length !== 1) return
  const touch = event.touches[0]
  const distance = Math.hypot(touch.clientX - startPoint.x, touch.clientY - startPoint.y)
  if (distance > MOVE_TOLERANCE) clearPressTimer()
}

function handleTouchEnd() {
  clearPressTimer()
  startPoint = null
}

export function installMobileTableActions() {
  if (typeof window === 'undefined') return
  if (window.__cmsMobileTableActionsInstalled) return
  window.__cmsMobileTableActionsInstalled = true

  document.addEventListener('contextmenu', handleContextMenu, true)
  document.addEventListener('touchstart', handleTouchStart, { passive: true, capture: true })
  document.addEventListener('touchmove', handleTouchMove, { passive: true, capture: true })
  document.addEventListener('touchend', handleTouchEnd, true)
  document.addEventListener('touchcancel', handleTouchEnd, true)
  window.addEventListener('resize', closeMenu)
  window.addEventListener('resize', scheduleOperationColumnSync)
  window.addEventListener('scroll', closeMenu, true)

  tableObserver = new MutationObserver(scheduleOperationColumnSync)
  tableObserver.observe(document.body, { childList: true, subtree: true })
  scheduleOperationColumnSync()
}
