<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-50">
    <form class="bg-white p-8 rounded shadow w-full max-w-sm" @submit.prevent="onLogin">
      <h2 class="text-2xl font-bold text-center mb-6">어드민 로그인</h2>
      <input v-model="form.username" placeholder="아이디" class="w-full mb-3 px-3 py-2 border rounded"/>
      <input v-model="form.password" type="password" placeholder="비밀번호" class="w-full mb-3 px-3 py-2 border rounded"/>
      <button class="w-full bg-[#03C75A] text-white py-2 rounded">로그인</button>
      <p v-if="error" class="text-red-500 text-sm mt-2">{{ error }}</p>
    </form>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useAuthStore } from '~/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const form = reactive({
  username: '',
  password: ''
})
const error = ref('')

async function onLogin() {
  error.value = ''
  try {
    await auth.login(form.username, form.password)
    router.push('/admin')
  } catch (e) {
    error.value = e.message
  }
}
</script>
  