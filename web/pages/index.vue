<template>
  <div class="flex flex-col min-h-screen bg-gray-50">
    <!-- Header -->
    <header class="bg-[#03C75A] text-white p-4 flex flex-col items-center justify-center shadow-md">
      <h1 class="text-2xl font-bold">WebChat Rooms</h1>
      <NuxtLink to="/login" class="underline hover:text-gray-200">로그인/프로필</NuxtLink>
    </header>

    <!-- Room list -->
    <main class="flex-1 p-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
          v-for="room in rooms"
          :key="room.id"
          class="bg-white rounded-xl shadow hover:shadow-lg transition cursor-pointer p-6 flex flex-col"
      >
        <h2 class="text-xl font-semibold mb-2 text-gray-800">{{ room.name }}</h2>
        <p class="text-gray-500 mb-4">{{ room.description }}</p>
        <NuxtLink
            :to="`/chat/${room.id}`"
            class="mt-auto bg-[#03C75A] text-white px-4 py-2 rounded-md text-center hover:bg-[#02af4f] transition"
        >
          입장하기
        </NuxtLink>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 실제 환경에서는 API 호출로 방 목록을 받아옵니다
const rooms = ref([]);

onMounted(() => {
  rooms.value = [
    { id: 'room1', name: '일반 채팅방', description: '자유롭게 대화하는 공간입니다.' },
    { id: 'room2', name: '주제별 토론방', description: '특정 주제에 대해 토론해 보세요.' },
    { id: 'room3', name: '프로젝트 팀방', description: '팀원들과 협업을 위한 방입니다.' },
  ];
});
</script>

<style scoped>
a {
  text-decoration: none;
}
</style>
