<template>
  <div class="flex flex-col min-h-screen bg-gray-50">
    <!-- Header -->
    <header class="bg-[#03C75A] text-white p-4 flex flex-col items-center justify-center shadow-md">
      <h1 class="text-2xl font-bold">WebChat Rooms</h1>
    </header>

    <!-- Room list -->
    <main ref="scrollContainer" class="flex-1 p-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
          v-for="room in rooms"
          :key="room.roomId"
          class="bg-white rounded-xl shadow hover:shadow-lg transition cursor-pointer p-6 flex flex-col"
      >
        <h2 class="text-xl font-semibold mb-2 text-gray-800">{{ room.name }}</h2>
        <p class="text-gray-500 mb-1 text-sm">참여 인원: {{ room.currentCapacity }} / {{ room.maxCapacity }}</p>
        <p class="text-gray-500 mb-4 flex-grow">{{ room.description }}</p>
        <NuxtLink
            :to="`/chat/${room.id}`"
            class="mt-auto bg-[#03C75A] text-white px-4 py-2 rounded-md text-center hover:bg-[#02af4f] transition"
        >
          입장하기
        </NuxtLink>
      </div>
    </main>

    <!-- Loading Indicator -->
    <div v-if="loading" class="text-center p-4 text-gray-500">
      로딩 중...
    </div>
    <div v-if="!hasNext && rooms.length > 0" class="text-center p-4 text-gray-400 text-sm">
      더 이상 방이 없습니다.
    </div>
    <div v-if="!loading && rooms.length === 0" class="text-center p-10 text-gray-500">
      개설된 채팅방이 없습니다. 새 방을 만들어보세요!
    </div>
    <div v-if="error" class="text-center p-4 text-red-500">
      오류 발생: {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';

const rooms = ref([]);
const page = ref(0); // 페이지 번호 (0부터 시작)
const size = ref(6); // 한 페이지에 불러올 방 개수 (그리드 3열 기준)
const loading = ref(false);
const hasNext = ref(true); // 다음 페이지 존재 여부
const error = ref(null);
const scrollContainer = ref(null); // 스크롤 감지 대상

const fetchRooms = async () => {
  if (loading.value || !hasNext.value) return; // 이미 로딩 중이거나 다음 페이지 없으면 중단

  loading.value = true;
  error.value = null;

  try {
    // Nuxt 프록시를 통해 /api/room/read 호출
    const response = await fetch(`/api/room/read?page=${page.value}&size=${size.value}`);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json(); // CustomSlice<ResponseReadRoomDTO> 형태

    if (data && data.content) {
      rooms.value.push(...data.content); // 기존 목록에 새 방 추가
      hasNext.value = data.hasNext;      // 다음 페이지 여부 업데이트
      if (hasNext.value) {
        page.value++; // 다음 페이지 번호 증가
      }
    } else {
      hasNext.value = false; // 데이터 형식이 잘못된 경우
    }

  } catch (err) {
    console.error('Error fetching rooms:', err);
    error.value = err.message || '채팅방 목록을 불러오는 중 오류가 발생했습니다.';
    hasNext.value = false; // 오류 발생 시 더 이상 로드 시도 안 함
  } finally {
    loading.value = false;
  }
};

const handleScroll = () => {
  // window 스크롤 기준으로 변경
  const bottomOfWindow = window.innerHeight + window.scrollY >= document.documentElement.offsetHeight - 100; // 하단 100px 전에 로드

  if (bottomOfWindow && !loading.value && hasNext.value) {
    fetchRooms();
  }
};

onMounted(() => {
  fetchRooms(); // 초기 데이터 로드
  window.addEventListener('scroll', handleScroll); // window에 스크롤 이벤트 리스너 추가
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll); // 컴포넌트 제거 시 리스너 제거
});
</script>

<style scoped>
a {
  text-decoration: none;
}
/* 필요한 경우 추가 스타일 */
main {
  min-height: calc(100vh - 150px); /* 헤더/푸터 제외 최소 높이 확보 (값 조정 필요) */
}
</style>
