import { ref } from 'vue'

const currentView = ref<'chat' | 'settings'>('chat')

export const useView = () => {
  const setView = (view: 'chat' | 'settings') => {
    currentView.value = view
  }
  return {
    currentView,
    setView
  }
}
