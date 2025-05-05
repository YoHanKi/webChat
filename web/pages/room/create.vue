<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-50">
    <div class="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
      <h2 class="text-2xl font-bold text-center text-[#03C75A]">새 채팅방 만들기</h2>
      <form @submit.prevent="createRoom" class="space-y-4">
        <div>
          <label for="name" class="block text-sm font-medium text-gray-700">방 이름</label>
          <input
            id="name"
            v-model="roomData.name"
            type="text"
            required
            placeholder="채팅방 이름을 입력하세요"
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-[#03C75A] focus:border-[#03C75A]"
          />
        </div>
        <div>
          <label for="description" class="block text-sm font-medium text-gray-700">방 설명</label>
          <textarea
            id="description"
            v-model="roomData.description"
            rows="3"
            placeholder="채팅방 설명을 입력하세요 (선택 사항)"
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-[#03C75A] focus:border-[#03C75A]"
          ></textarea>
        </div>
        <div>
          <label for="maxCapacity" class="block text-sm font-medium text-gray-700">최대 인원</label>
          <input
            id="maxCapacity"
            v-model.number="roomData.maxCapacity"
            type="number"
            required
            min="2"
            placeholder="최소 2명 이상"
            class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-[#03C75A] focus:border-[#03C75A]"
          />
        </div>
        <div>
          <button
            type="submit"
            class="w-full px-4 py-2 font-semibold text-white bg-[#03C75A] rounded-md shadow-md hover:bg-[#02af4f] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#03C75A] transition"
          >
            채팅방 생성
          </button>
        </div>
      </form>
      <p v-if="errorMessage" class="text-sm text-center text-red-600">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const roomData = ref({
  name: '',
  description: '',
  maxCapacity: 10, // 기본값 설정 예시
});
const errorMessage = ref('');

// 컴포넌트 마운트 시 만료 시간 기본값 설정 (예: 현재 시간 + 1시간)
import { onMounted } from 'vue';
onMounted(() => {
  const now = new Date();
  now.setHours(now.getHours() + 1); // 1시간 뒤로 설정
});


const createRoom = async () => {
  const payload = {
    name: roomData.value.name,
    description: roomData.value.description,
    maxCapacity: roomData.value.maxCapacity,
  };

  if (!payload.name || !payload.maxCapacity) {
    errorMessage.value = '방 이름과 최대 인원은 필수 입력 사항입니다.';
    return;
  }

  errorMessage.value = ''; // 이전 에러 메시지 초기화
  try {
    const response = await fetch('/api/room/create', { // Nuxt 프록시 사용 가정
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || '채팅방 생성에 실패했습니다.');
    }

    const createdRoom = await response.json();

    router.push(`/chat/${createdRoom.roomId}`); // 생성된 방 ID가 응답으로 온다고 가정

  } catch (error) {
    console.error('채팅방 생성 오류:', error);
    errorMessage.value = error.message || '채팅방 생성 중 오류가 발생했습니다.';
  }
};
</script>

<style scoped>
/* 필요한 경우 추가 스타일링 */
input[type="datetime-local"]::-webkit-calendar-picker-indicator {
    cursor: pointer;
    filter: invert(0.6) brightness(0.8); /* 아이콘 색상 조정 예시 */
}
</style>
