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
import { ref } from 'vue'
import { useStomp } from '~/composables/useStomp'

const newMessage = ref('')
const inputField = ref<HTMLInputElement | null>(null)

const { disableInput, stopping, sendMessage: sendStompMessage, stopAgent } = useStomp()

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
