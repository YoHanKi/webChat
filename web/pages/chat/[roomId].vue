<template>
  <div class="flex flex-col h-screen bg-gray-100">
    <!-- Header -->
    <header class="bg-[#03C75A] text-white p-4 flex items-center justify-between shadow-md">
      <div class="flex flex-col">
        <h1 class="text-2xl font-bold">{{ room.name }}</h1>
        <span class="text-sm opacity-75">{{ room.description }}</span>
      </div>
      <!-- 방장 수정 버튼 -->
      <button
          v-if="isOwner"
          @click="goToUpdatePage"
          class="bg-white text-[#03C75A] px-4 py-1 rounded-md hover:bg-gray-100 transition text-sm font-semibold"
      >
        방 수정하기
      </button>
    </header>

    <!-- Main content area -->
    <div class="flex flex-1 p-4 gap-4 overflow-hidden">
      <!-- Left side: Streaming area -->
      <div class="w-2/3 h-full">
        <StreamingArea
            :roomId="roomId"
            :isOwner="isOwner"
        />
      </div>

      <!-- Right side: Users list and chat -->
      <div class="w-1/3 h-full flex flex-col gap-4">
        <!-- Users dropdown toggle -->
        <div class="bg-white rounded-lg shadow-md p-2 flex items-center justify-between">
          <span class="font-medium text-gray-700">참가자 목록</span>
          <button
              @click="toggleUsersList"
              class="p-1 hover:bg-gray-100 rounded-full transition-colors"
          >
            <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5 text-gray-600 transition-transform"
                :class="{ 'rotate-180': showUsersList }"
                viewBox="0 0 20 20"
                fill="currentColor"
            >
              <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>

        <!-- Users list (collapsible) -->
        <transition name="slide">
          <UsersList
              v-if="showUsersList"
              :roomId="roomId"
              :isOwner="isOwner"
              :currentUsername="username"
              :maxCapacity="room.maxCapacity || 10"
              ref="usersListRef"
              class="flex-none max-h-[150px] overflow-y-auto bg-white rounded-lg shadow-md"
              @kick-user="onKickUser"
          />
        </transition>

        <!-- Chat area -->
        <div class="flex-1 overflow-hidden bg-white rounded-lg shadow-md">
          <ChatRoom
              :roomId="roomId"
              :username="username"
              ref="chatRoomRef"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { $fetch } from 'ofetch';
import UsersList from '~/components/chat/UsersList.vue';
import ChatRoom from '~/components/chat/ChatRoom.vue';
import StreamingArea from '~/components/chat/StreamingArea.vue';

const route = useRoute();
const router = useRouter();
const roomId = route.params.roomId;
const username = ref('');
const room = ref({ id: roomId, name: '로딩 중...', description: '', maxCapacity: 10, creatorName: null });
const isOwner = ref(false);

// 컴포넌트 참조
const chatRoomRef = ref(null);
const usersListRef = ref(null);

// 유저 목록 표시 상태
const showUsersList = ref(false);

onMounted(async () => {
  try {
    // 세션에서 사용자 이름 가져오기
    const name = sessionStorage.getItem('username');
    if (!name) {
      router.push('/login');
      return;
    }
    username.value = name;

    // 방 상세 정보 가져오기
    await fetchRoomDetails();

  } catch (error) {
    console.error('초기화 오류:', error);
    if (error.response && error.response.status === 401) {
      router.push('/login');
    } else {
      alert('채팅방 정보를 불러오는 중 오류가 발생했습니다.');
      router.push('/');
    }
  }
});

// 유저 목록 토글 함수
function toggleUsersList() {
  showUsersList.value = !showUsersList.value;
}

function onKickUser({ roomId, userId, content }) {
  // WebSocket으로 KICK 메시지 전송
  if (chatRoomRef.value && chatRoomRef.value.sendKick) {
    chatRoomRef.value.sendKick(roomId, userId, content);
  }
}

// 방 상세 정보 가져오기
async function fetchRoomDetails() {
  try {
    const roomData = await $fetch(`/api/room/read/${roomId}`);
    room.value = {
      ...room.value,
      ...roomData
    };

    // 현재 사용자가 방장인지 확인
    isOwner.value = username.value === room.value.creatorName;

  } catch (error) {
    console.error('방 정보를 가져오는데 실패했습니다:', error);
    alert('방 정보를 불러오는 중 오류가 발생했습니다.');
    router.push('/');
  }
}

// 방 수정 페이지로 이동
function goToUpdatePage() {
  router.push(`/room/update/${roomId}`);
}
</script>

<style scoped>
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  max-height: 150px;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>