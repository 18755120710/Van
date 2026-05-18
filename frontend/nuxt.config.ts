// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  ssr: false, // Single Page App (SPA) mode for real-time WebSocket dashboard stability
  css: [
    '~/assets/css/main.css'
  ],
  future: {
    compatibilityVersion: 3
  }
})
