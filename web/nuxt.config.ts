// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  ssr: true,
  compatibilityDate: '2024-11-01',
  devtools: { enabled: true },

  modules: [
    '@nuxt/eslint',
    '@nuxt/fonts',
    '@nuxt/icon',
    '@nuxt/image',
    '@nuxt/test-utils',
    '@nuxt/ui',
  ],
  vite: {
    server: {
      proxy: {
        '/api': {
          target: 'http://api:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
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
    }
  }
})