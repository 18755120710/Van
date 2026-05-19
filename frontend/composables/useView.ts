import { ref } from 'vue'

const currentView = ref<'chat' | 'prompt'>('chat')

export const useView = () => {
  const setView = (view: 'chat' | 'prompt') => {
    currentView.value = view
  }
  return {
    currentView,
    setView
  }
}
