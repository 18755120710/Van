<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
    <!-- Left collapsible navigation console -->
    <SidebarLeft v-model:collapsed="isSidebarCollapsed" />

    <!-- Center active chat viewbox -->
    <ChatConsole v-model:sidebar-collapsed="isSidebarCollapsed" />

    <!-- Right step logs drawer -->
    <SidebarRight />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useStomp } from '~/composables/useStomp'
import SidebarLeft from '~/components/sidebar/SidebarLeft.vue'
import ChatConsole from '~/components/chat/ChatConsole.vue'
import SidebarRight from '~/components/sidebar/SidebarRight.vue'

const isSidebarCollapsed = ref(false)
const { connect, disconnect, initSession } = useStomp()

// Connect to WebSocket STOMP broker when mounted
onMounted(async () => {
  await initSession()
  connect()
})

// Auto deactivate broker connections before unmounting
onBeforeUnmount(() => {
  disconnect()
})
</script>
