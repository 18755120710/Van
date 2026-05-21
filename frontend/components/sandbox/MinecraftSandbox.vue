<template>
  <div class="mc-sandbox-board">
    <div ref="canvasHost" class="mc-canvas-container">
      <div class="mc-hud">
        <span class="mc-hud-dot" :class="`phase-${sandboxState.phase}`"></span>
        <span>{{ activeRoleName }}</span>
      </div>

      <div class="mc-orbit-hint">拖拽旋转视角</div>

      <div class="mc-bubble-layer">
        <div
          v-if="activeAnchor.visible"
          class="mc-speech-bubble"
          :class="`role-${sandboxState.dialogue.role}`"
          :style="{ left: `${activeAnchor.x}px`, top: `${activeAnchor.y}px` }"
        >
          <strong>{{ activeRoleName }}</strong>
          <span>{{ sandboxState.dialogue.text }}</span>
        </div>
      </div>
    </div>

    <div class="mc-dialogue-console" :class="`phase-${sandboxState.phase}`">
      <div class="mc-dialogue-label">{{ phaseLabel }}</div>
      <div class="mc-dialogue-text">{{ sandboxState.storyText }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import type { ToolResult } from '~/types/chat'
import { getSandboxRoleName, mapToolResultsToMinecraftState } from './sandboxStateMapper'
import type { RoleAnchorMap } from './types'
import { MinecraftSandboxScene } from './useMinecraftScene'

const props = defineProps<{
  toolResults: ToolResult[]
}>()

const canvasHost = ref<HTMLElement | null>(null)
const anchors = ref<RoleAnchorMap>({
  pm: { x: -1000, y: -1000, visible: false },
  planner: { x: -1000, y: -1000, visible: false },
  browser: { x: -1000, y: -1000, visible: false },
  qa: { x: -1000, y: -1000, visible: false },
  ops: { x: -1000, y: -1000, visible: false }
})

let scene: MinecraftSandboxScene | null = null

const sandboxState = computed(() => mapToolResultsToMinecraftState(props.toolResults || []))
const activeRoleName = computed(() => getSandboxRoleName(sandboxState.value.dialogue.role))
const activeAnchor = computed(() => anchors.value[sandboxState.value.dialogue.role])

const phaseLabel = computed(() => {
  switch (sandboxState.value.phase) {
    case 'planning': return 'PLAN'
    case 'handoff': return 'HANDOFF'
    case 'working': return 'WORK'
    case 'reporting': return 'REPORT'
    case 'summary': return 'SUMMARY'
    case 'answer': return 'DONE'
    case 'usage': return 'OPS'
    case 'error': return 'QA'
    case 'stopped': return 'STOP'
    default: return 'IDLE'
  }
})

const mountScene = async () => {
  await nextTick()
  if (!canvasHost.value || scene) return
  scene = new MinecraftSandboxScene(canvasHost.value, sandboxState.value, (nextAnchors) => {
    anchors.value = nextAnchors
  })
}

onMounted(() => {
  mountScene()
})

onUnmounted(() => {
  scene?.dispose()
  scene = null
})

watch(sandboxState, (nextState) => {
  scene?.setState(nextState)
})
</script>
