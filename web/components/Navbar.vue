<!-- components/Navbar.vue -->
<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function onLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <nav class="bg-white shadow px-6 py-4 flex justify-between items-center">
    <div class="text-2xl font-bold">Chat App</div>
    <div class="flex items-center space-x-4">
      <NuxtLink
          v-if="!auth.user"
          to="/login"
          class="text-gray-700 hover:text-primary transition"
      >로그인</NuxtLink>
      <NuxtLink
          v-if="!auth.user"
          to="/register"
          class="text-gray-700 hover:text-primary transition"
      >회원가입</NuxtLink>
      <div v-else class="flex items-center space-x-2">
        <span class="text-gray-800">안녕하세요, {{ auth.user }}님</span>
        <button
            @click="onLogout"
            class="px-3 py-1 border rounded text-sm text-gray-700 hover:bg-gray-100 transition"
        >로그아웃</button>
      </div>
    </div>
  </nav>
</template>