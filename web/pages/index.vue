<template>
  <div class="flex flex-col">

    <!-- Room list -->
    <main ref="scrollContainer" class="px-2 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
          v-for="room in rooms"
          :key="room.id"
          class="bg-white rounded-xl shadow hover:shadow-lg transition cursor-pointer p-6 flex flex-col h-[350px] ring-2 ring-transparent hover:ring-[#03C75A] relative"
      >
        <!-- 썸네일 or 플레이스홀더 -->
        <div class="mb-4 w-full h-32 rounded overflow-hidden bg-gray-100 relative flex-shrink-0">
          <!-- 이미지가 있으면 보여주고 -->
          <img
              v-if="room.thumbnail"
              :src="room.thumbnail"
              alt="방 썸네일"
              class="w-full h-full object-cover"
              @error="e => e.target.src = '/default-thumb.png'"
          />
          <!-- 없으면 '방송 준비중' 플레이스홀더 -->
          <div
              v-else
              class="absolute inset-0 flex flex-col items-center justify-center bg-black text-white"
          >
            <svg
                class="w-12 h-12 text-[#03C75A] animate-pulse mb-2"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
            >
              <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"
              />
            </svg>
            <p class="text-sm font-bold">방송 준비중입니다</p>
          </div>
        </div>

        <h2 class="text-xl font-semibold mb-2 text-gray-800 line-clamp-1">{{ room.name }}</h2>
        <p class="text-gray-500 mb-1 text-sm flex-shrink-0">
          참여 인원: {{ room.currentCapacity }} / {{ room.maxCapacity }}
        </p>
        <div class="overflow-y-auto flex-grow mb-4">
          <p class="text-gray-500 line-clamp-3">{{ room.description }}</p>
        </div>
        <NuxtLink
            :to="`/chat/${room.id}`"
            class="flex-shrink-0 bg-[#03C75A] text-white px-4 py-2 rounded-md text-center hover:bg-[#02af4f] transition"
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
import { useAuthStore } from "~/stores/auth.js";

const auth = useAuthStore()
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
  // 회원 정보 확인
  auth.validate(sessionStorage.getItem("username"));
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
