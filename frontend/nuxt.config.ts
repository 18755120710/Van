// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  ssr: false, // Single Page App (SPA) mode for real-time WebSocket dashboard stability
  srcDir: '.', // Explicitly use root as source directory to avoid Nuxt 4 folder heuristics
  css: [
    '~/assets/css/main.css'
  ],
  future: {
    compatibilityVersion: 4
  },
  experimental: {
    viteEnvironmentApi: true
  },
  nitro: {
    devProxy: {
      '/conversations': {
        target: 'http://localhost:18081/conversations',
        changeOrigin: true
      },
      '/agent-config': {
        target: 'http://localhost:18081/agent-config',
        changeOrigin: true
      }
    }
  }
})
