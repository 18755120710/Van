<template>
  <footer class="input-panel">
    <div class="input-inner">
      <!-- Agent Thinking Animation -->
      <div class="agent-status-panel" v-if="disableInput">
        <div class="wave-loader">
          <span class="wave-dot delay-1"></span>
          <span class="wave-dot delay-2"></span>
          <span class="wave-dot delay-3"></span>
        </div>
        <span class="agent-thinking-text">AgentScope is executing web automations...</span>
      </div>

      <!-- Sleek Input Box -->
      <div class="input-box-wrapper" :class="{ 'disabled': disableInput }">
        <!-- Model Name Badge -->
        <div v-if="activeModel" class="model-badge" title="当前连接的智能体模型">
          <svg class="model-badge-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polygon points="12 2 2 7 12 12 22 7 12 2" />
            <polyline points="2 17 12 22 22 17" />
            <polyline points="2 12 12 17 22 12" />
          </svg>
          <span class="model-badge-text">{{ activeModel }}</span>
        </div>

        <input
          ref="inputField"
          type="text"
          v-model="newMessage"
          placeholder="Message AgentScope or request browser automations..."
          @keyup.enter="handleSend"
          :disabled="disableInput"
        />
        <button
          v-if="!disableInput"
          @click="handleSend"
          :disabled="!newMessage.trim()"
          class="btn-send"
          title="Send Message"
          type="button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <line x1="22" y1="2" x2="11" y2="13" />
            <polygon points="22 2 15 22 11 13 2 9 22 2" />
          </svg>
        </button>
        <button
          v-else
          @click="handleStop"
          :disabled="stopping"
          class="btn-stop"
          title="Stop Execution"
          type="button"
        >
          <span v-if="stopping" class="stopping-text">停止中...</span>
          <template v-else>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            </svg>
            <span>停止</span>
          </template>
        </button>
      </div>

      <p class="input-disclaimer">
        AgentScope Orchestrator v1.0. Preserves active session tokens and STOMP channels.
      </p>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useStomp } from '~/composables/useStomp'
import { useAgentConfig } from '~/composables/useAgentConfig'

const newMessage = ref('')
const inputField = ref<HTMLInputElement | null>(null)
const activeModel = ref('')

const { disableInput, stopping, sendMessage: sendStompMessage, stopAgent } = useStomp()
const { getModelConfig } = useAgentConfig()

// Pull the currently active model name from backend configuration
onMounted(async () => {
  try {
    const data = await getModelConfig()
    if (data && data.modelName) {
      activeModel.value = data.modelName
    }
  } catch (err) {
    console.warn('Could not fetch active model for input panel badge:', err)
  }
})

const handleSend = () => {
  const text = newMessage.value.trim()
  if (!text || disableInput.value) return
  
  sendStompMessage(text)
  newMessage.value = ''
}

const handleStop = () => {
  stopAgent()
}

const prefillPrompt = (text: string) => {
  newMessage.value = text
  // Focus the input field on next tick
  setTimeout(() => {
    inputField.value?.focus()
  }, 50)
}

// Expose prompt prefill method to parent component
defineExpose({
  prefillPrompt
})
</script>

<style scoped>
.model-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.25rem 0.55rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  margin-left: 0.25rem;
  flex-shrink: 0;
  height: 26px;
  user-select: none;
  animation: badgeFadeIn 0.25s ease-out;
}

.model-badge-icon {
  width: 12px;
  height: 12px;
  color: var(--accent-indigo);
}

.model-badge-text {
  font-size: 0.72rem;
  font-weight: 700;
  color: var(--text-secondary);
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
}

@keyframes badgeFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateX(-4px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateX(0);
  }
}
</style>
