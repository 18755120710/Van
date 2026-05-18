<template>
  <div class="welcome-screen">
    <div class="welcome-hero">
      <div class="welcome-logo">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <polygon points="12 2 2 7 12 12 22 7 12 2" />
          <polyline points="2 17 12 22 22 17" />
          <polyline points="2 12 12 17 22 12" />
        </svg>
      </div>
      <h1>Welcome to AgentScope</h1>
      <p>A high-performance orchestration console designed to supervise local Java web-automation workflows.</p>
    </div>
    
    <div class="welcome-grid">
      <div class="welcome-card" @click="emitPrompt('Search Google for today\'s top news on AI tools')">
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <circle cx="12" cy="12" r="4" />
            <line x1="4.93" y1="4.93" x2="9.17" y2="9.17" />
            <line x1="19.07" y1="4.93" x2="14.83" y2="9.17" />
            <line x1="14.83" y1="14.83" x2="19.07" y2="19.07" />
            <line x1="9.17" y1="14.83" x2="4.93" y2="19.07" />
          </svg>
        </div>
        <h3>Web Crawling</h3>
        <p>Deploy Chrome drivers to navigate and crawl unstructured information dynamically.</p>
        <span class="card-arrow">→</span>
      </div>
      
      <div class="welcome-card" @click="emitPrompt('Analyze my target directories and compile a summary log')">
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
          </svg>
        </div>
        <h3>Data Extraction</h3>
        <p>Process, summarize, and aggregate local files or remote datasets with ease.</p>
        <span class="card-arrow">→</span>
      </div>
      
      <div class="welcome-card" @click="emitPrompt('Deploy standard browser-use tests')">
        <div class="card-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
            <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
            <line x1="6" y1="6" x2="6.01" y2="6" />
            <line x1="6" y1="18" x2="6.01" y2="18" />
          </svg>
        </div>
        <h3>Diagnostic Checks</h3>
        <p>Ensure connection speeds, WebSocket packet round-trips, and broker nodes are stable.</p>
        <span class="card-arrow">→</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Emits select-prompt event to notify parent input box to populate value
const emit = defineEmits<{
  (e: 'select-prompt', text: string): void
}>()

const emitPrompt = (text: string) => {
  emit('select-prompt', text)
}
</script>
