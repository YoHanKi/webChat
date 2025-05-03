// https://nuxt.com/docs/api/configuration/nuxt-config
import tailwindcss from "@tailwindcss/vite";

export default defineNuxtConfig({
  ssr: true,
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },
  css: ['~/assets/css/main.css'],

  runtimeConfig: {
    public: {
      apiURL: process.env.API_URL || 'http://localhost:8080',
    }
  },

  // Nuxt 3에서 serverMiddleware 대신 nitro.devProxy
  nitro: {
    devProxy: {
      // REST API 프록시
      '/api': {
        target: process.env.API_URL || 'http://localhost:8080',
        changeOrigin: true,
      },
      // WebSocket 프록시
      '/ws-chat': {
        target: process.env.API_URL || 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
      }
    }
  },

  modules: [
    '@nuxt/eslint',
    '@nuxt/fonts',
    '@nuxt/icon',
    '@nuxt/image',
    '@nuxt/test-utils',
    '@nuxt/ui',
    '@pinia/nuxt'
  ],

  vite: {
    server: {
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
        '/ws-chat': {
          target: 'ws://localhost:8080',
          changeOrigin: true,
          ws: true,
        }
      }
    },
    define: {
      // 코드 내 모든 `global` 식별자를 `window` 로 치환
      global: 'window'
    },
    // 클라이언트 전용 모듈 처리
    ssr: {
      noExternal: ['sockjs-client', '@stomp/stompjs']
    },

    plugins: [
      tailwindcss(),
    ],
  }
})