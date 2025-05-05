<template>
  <div class="flex items-center justify-center min-h-screen bg-white">
    <div class="flex min-h-full flex-col justify-center px-6 py-12 lg:px-8 w-full max-w-md">

      <div class="sm:mx-auto sm:w-full sm:max-w-sm">
        <h2 class="text-2xl font-bold text-center text-[#03C75A] mb-6">채팅방 수정</h2>
      </div>

      <form @submit.prevent="updateRoom">
        <div class="mb-4">
          <label for="roomName" class="block text-gray-700 mb-2">방 이름</label>
          <input
              id="roomName"
              v-model="roomName"
              type="text"
              required
              placeholder="채팅방 이름을 입력하세요"
              class="w-full border border-gray-300 rounded-md px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#03C75A]"
          />
        </div>

        <div class="mb-4">
          <label for="roomDescription" class="block text-gray-700 mb-2">방 설명</label>
          <textarea
              id="roomDescription"
              v-model="roomDescription"
              rows="3"
              placeholder="채팅방 설명을 입력하세요"
              class="w-full border border-gray-300 rounded-md px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#03C75A]"
          ></textarea>
        </div>

        <div class="mb-6">
          <label for="maxCapacity" class="block text-gray-700 mb-2">최대 인원</label>
          <input
              id="maxCapacity"
              v-model.number="maxCapacity"
              type="number"
              required
              min="2"
              max="100"
              placeholder="최대 인원 (2-100)"
              class="w-full border border-gray-300 rounded-md px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#03C75A]"
          />
        </div>

        <button
            type="submit"
            class="w-full bg-[#03C75A] hover:bg-[#02af4f] text-white font-semibold px-4 py-2 rounded-md shadow-md transition"
        >
          수정하기
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { $fetch } from 'ofetch';

const route = useRoute();
const router = useRouter();
const roomId = route.params.roomId;

const roomName = ref('');
const roomDescription = ref('');
const maxCapacity = ref(2); // 기본값 설정

onMounted(async () => {
  try {
    // 기존 방 정보 가져오기
    const roomData = await $fetch(`/api/room/read/${roomId}`);
    roomName.value = roomData.roomName;
    roomDescription.value = roomData.roomDescription;
    maxCapacity.value = roomData.maxCapacity;
  } catch (error) {
    console.error('방 정보를 불러오는 데 실패했습니다:', error);
    alert('방 정보를 불러올 수 없습니다.');
    router.push('/'); // 오류 발생 시 메인 페이지로 이동
  }
});

async function updateRoom() {
  if (!roomName.value.trim()) {
    alert('방 이름을 입력해주세요.');
    return;
  }
  if (maxCapacity.value < 2 || maxCapacity.value > 100) {
    alert('최대 인원은 2명 이상 100명 이하로 설정해야 합니다.');
    return;
  }

  try {
    await $fetch('/api/room/update', {
      method: 'PUT',
      body: {
        roomId: parseInt(roomId, 10),
        roomName: roomName.value,
        roomDescription: roomDescription.value,
        maxCapacity: maxCapacity.value,
      },
    });
    alert('채팅방 정보가 성공적으로 수정되었습니다.');
    router.push(`/chat/${roomId}`); // 수정 후 해당 채팅방으로 이동
  } catch (error) {
    console.error('채팅방 수정 실패:', error);
    // API 에러 메시지 표시 등 상세 오류 처리
    alert(`채팅방 수정 중 오류가 발생했습니다: ${error.data?.message || error.message}`);
  }
}
</script>

<style scoped>
</style>
