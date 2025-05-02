<template>
  <div class="flex items-center justify-center min-h-screen bg-white">
    <div class="flex min-h-full flex-col justify-center px-6 py-12 lg:px-8">

      <div class="sm:mx-auto sm:w-full sm:max-w-sm">
        <h2 class="text-2xl font-bold text-center text-[#03C75A] mb-6">WebChat 로그인</h2>
      </div>

      <div>
        <label class="block text-gray-700 mb-2">닉네임</label>
        <input
            v-model="loginName"
            placeholder="닉네임을 입력하세요"
            class="w-full border border-gray-300 rounded-md px-4 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-[#03C75A]"
        />
        <button
            @click="doLogin"
            class="w-full bg-[#03C75A] hover:bg-[#02af4f] text-white font-semibold px-4 py-2 rounded-md shadow-md transition"
        >
          로그인
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const loginName = ref('');
const router = useRouter();

function doLogin() {
  const name = loginName.value.trim();
  if (!name) {
    alert('닉네임을 입력해주세요.');
    return;
  }
  sessionStorage.setItem('username', name);
  router.push('/');
}
</script>

<style scoped>
</style>