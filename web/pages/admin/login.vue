<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-50">
    <form class="bg-white p-8 rounded shadow w-full max-w-sm" @submit.prevent="onLogin">
      <h2 class="text-2xl font-bold text-center mb-6">어드민 로그인</h2>
      <input v-model="id" placeholder="아이디" class="w-full mb-3 px-3 py-2 border rounded"/>
      <input v-model="pw" type="password" placeholder="비밀번호" class="w-full mb-3 px-3 py-2 border rounded"/>
      <button class="w-full bg-[#03C75A] text-white py-2 rounded">로그인</button>
      <p v-if="error" class="text-red-500 text-sm mt-2">{{ error }}</p>
    </form>
  </div>
</template>
<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'

const id = ref(''), pw = ref(''), error = ref('')
const router = useRouter()

async function onLogin() {
  // 실제 어드민 인증 로직 필요
  if (id.value === 'admin' && pw.value === 'admin') {
    sessionStorage.setItem('admin', 'true')
    router.push('/admin')
  } else {
    error.value = '아이디 또는 비밀번호가 올바르지 않습니다.'
  }
}
</script>
  