<!-- components/Navbar.vue -->
<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'

const auth = useAuthStore()
const router = useRouter()

onMounted(() => {
  // 페이지가 로드될 때 사용자 정보를 확인합니다.
  auth.init();
})

function onLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <nav class="bg-white shadow px-6 py-4 flex justify-between items-center">
    <NuxtLink to="/" class="text-2xl font-bold items-center text-[#03C75A]">
      <img src="~/assets/images/logo.svg" alt="로고" class="h-8 mr-2" />
    </NuxtLink>
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
        <NuxtLink
            v-if="auth.role === 'ADMIN' || auth.role === 'MANAGER'"
            to="/room/create"
            class="px-3 py-1 border rounded text-sm text-gray-700 hover:bg-gray-100 transition"
            >채팅방 생성</NuxtLink>
      </div>
    </div>
  </nav>
</template>