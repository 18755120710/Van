<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
    <!-- Left collapsible navigation console -->
    <SidebarLeft v-model:collapsed="isSidebarCollapsed" />

    <!-- Center active chat viewbox or Settings panel -->
    <ChatConsole v-if="currentView === 'chat'" v-model:sidebar-collapsed="isSidebarCollapsed" />
    <SettingsConsole v-else-if="currentView === 'settings'" v-model:sidebar-collapsed="isSidebarCollapsed" />

    <!-- Right step logs drawer (only visible in chat view) -->
    <SidebarRight v-if="currentView === 'chat'" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useStomp } from '~/composables/useStomp'
import { useView } from '~/composables/useView'
import SidebarLeft from '~/components/sidebar/SidebarLeft.vue'
import ChatConsole from '~/components/chat/ChatConsole.vue'
import SettingsConsole from '~/components/settings/SettingsConsole.vue'
import SidebarRight from '~/components/sidebar/SidebarRight.vue'

const isSidebarCollapsed = ref(false)
const { currentView } = useView()
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
