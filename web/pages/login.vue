<!-- pages/login.vue -->
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useAuthStore } from '~/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const form = reactive({ username: '', password: '' })
const error = ref('')

async function onSubmit() {
  error.value = ''
  try {
    await auth.login(form.username, form.password)
    router.push('/')
  } catch (e: any) {
    error.value = e.message
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="w-full max-w-md bg-white p-8 rounded-lg shadow">
      <h2 class="text-2xl font-semibold text-center mb-6">로그인</h2>
      <form @submit.prevent="onSubmit" class="space-y-4">
        <div>
          <label for="username" class="block text-sm font-medium mb-1">아이디</label>
          <input
              v-model="form.username"
              id="username"
              type="text"
              required
              class="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div>
          <label for="password" class="block text-sm font-medium mb-1">비밀번호</label>
          <input
              v-model="form.password"
              id="password"
              type="password"
              required
              class="w-full px-3 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
        <button
            type="submit"
            class="w-full bg-[#03C75A] hover:bg-[#02af4f] text-white font-semibold px-4 py-2 rounded-md shadow-md transition cursor-pointer"
        >로그인</button>
      </form>
      <p class="mt-4 text-center text-sm text-gray-600">
        아직 계정이 없으신가요?
        <NuxtLink to="/register" class="text-primary font-medium">회원가입</NuxtLink>
      </p>
    </div>
  </div>
</template>