<template>
  <div class="flex flex-col h-screen bg-gray-50">
    <!-- Header -->
    <header class="bg-[#03C75A] text-white p-4 flex items-center justify-between shadow-md">
      <div class="flex flex-col items-center">
        <h1 class="text-2xl font-bold">{{ room.name }}</h1>
        <span class="text-sm opacity-75">User: {{ username }}</span>
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

    <!-- Chat messages -->
    <!-- justify-end 제거하여 스크롤이 자연스럽게 위에서부터 쌓이도록 함 -->
    <main ref="container" class="flex-1 overflow-y-auto p-6 flex flex-col space-y-4">
      <div
          v-for="(msg, idx) in messages"
          :key="idx"
          :class="[
            'flex items-start max-w-xl',
            msg.type !== 'CHAT'
              ? 'self-center' // 시스템 메시지는 가운데 정렬 유지
              : msg.sender === username
                ? 'self-end'
                : 'self-start',
          ]"
      >
        <!-- 시스템 메시지가 아닐 경우 프로필 이미지 표시 -->
        <img
            v-if="msg.type === 'CHAT'"
            :src="msg.senderImg || defaultAvatar"
            alt="avatar"
            class="w-8 h-8 rounded-full mr-3"
            :class="{ 'order-last ml-3 mr-0': msg.sender === username }"
        />
        <div
            :class="[
              'px-4 py-2 rounded-lg',
              msg.type !== 'CHAT'
                ? 'bg-gray-200 text-gray-600 italic' // 시스템 메시지 스타일
                : msg.sender === username
                  ? 'bg-[#03C75A] text-white' // 내 메시지
                  : 'bg-white text-gray-800 shadow-sm', // 다른 사람 메시지
            ]"
        >
          <!-- 채팅 메시지 내용 -->
          <div v-if="msg.type === 'CHAT'">
            <p class="font-semibold mb-1" :class="{ 'text-right': msg.sender === username }">{{ msg.sender }}</p>
            <p>{{ msg.content }}</p>
          </div>
          <!-- 시스템 메시지 내용 -->
          <div v-else>
            <p>{{ msg.content }}</p>
          </div>
        </div>
      </div>
    </main>

    <!-- Input -->
    <footer class="bg-white p-4 shadow-inner flex items-center">
      <input
          v-model="input"
          @keyup.enter="sendMessage"
          placeholder="메시지를 입력하세요..."
          class="flex-1 border border-gray-300 rounded-l-xl px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#03C75A]"
      />
      <button
          @click="sendMessage"
          class="bg-[#03C75A] hover:bg-[#02af4f] text-white px-6 py-2 rounded-r-xl shadow-md transition"
      >
        전송
      </button>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// Nuxt 3의 내장 fetch 사용
import { $fetch } from 'ofetch';
// 기본 아바타 이미지 import
import defaultAvatar from '~/assets/images/defaultMsgImg.svg';

const route = useRoute();
const router = useRouter();
const roomId = route.params.roomId;
const messages = ref([]);
const input = ref('');
const username = ref('');
const room = ref({ id: roomId, name: '로딩 중...', creatorName: null }); // ownerId 추가
const isOwner = ref(false); // 방장 여부 상태 추가
let socket;
const container = ref(null);

onMounted(async () => {
  try {
    // 세션에서 가져옴
    const name = sessionStorage.getItem('username');
    if (!name) {
      router.push('/login');
      return;
    }
    username.value = name;

    await fetchRoomDetails();

    // 2. 웹소켓 연결 (이제 username.value는 서버에서 확인된 값)
    socket = new WebSocket(`ws://localhost:8080/ws-chat?roomId=${roomId}`);


    socket.onopen = () => {
      // 3. JOIN 메시지에 서버에서 확인된 사용자 이름 사용
      socket.send(JSON.stringify({ type: 'JOIN', sender: name, content: '', roomId }));
    };

    socket.onmessage = (event) => {
      const chat = JSON.parse(event.data);
      if (chat.roomId === roomId) {
        messages.value.push(chat);
        nextTick(() => {
          if (container.value) {
            container.value.scrollTop = container.value.scrollHeight;
          }
        });
      }
    };

    socket.onerror = (error) => {
      console.error('WebSocket 오류:', error);
      // 오류 처리 로직 (예: 사용자에게 알림)
      router.push('/');
    };

    socket.onclose = (event) => {
      console.log('WebSocket 연결 종료:', event);
    };

  } catch (error) {
    console.error('인증 또는 초기화 오류:', error);
    // $fetch 실패 시 (예: 401 Unauthorized) 로그인 페이지로 리디렉션
    if (error.response && error.response.status === 401) {
      router.push('/login');
    } else {
      // 다른 종류의 오류 처리 (예: 네트워크 오류)
      alert('채팅방 정보를 불러오는 중 오류가 발생했습니다.');
      router.push('/');
    }
  }
});

// 방 상세 정보 가져오는 함수 (API 호출 필요)
async function fetchRoomDetails() {
  try {
    const roomData = await $fetch(`/api/room/read/${roomId}`);
    room.value = { ...room.value, ...roomData }; // API 응답으로 room 정보 업데이트

    // 현재 사용자가 방장인지 확인
    isOwner.value = username.value === room.value.creatorName;

  } catch (error) {
    console.error('방 정보를 가져오는데 실패했습니다:', error);
    // 오류 처리 (예: 사용자에게 알림)
    alert('방 정보를 불러오는 중 오류가 발생했습니다.');
    router.push('/');
  }
}

// 스크롤을 맨 아래로 이동시키는 함수
function scrollToBottom() {
  nextTick(() => {
    if (container.value) {
      container.value.scrollTop = container.value.scrollHeight;
    }
  });
}

function sendMessage() {
  if (!input.value.trim()) return;
  const chat = { type: 'CHAT', sender: username.value, content: input.value.trim(), roomId };
  socket.send(JSON.stringify(chat));
  input.value = '';
}

// 방 수정 페이지로 이동하는 함수
function goToUpdatePage() {
  router.push(`/room/update/${roomId}`);
}

onBeforeUnmount(() => {
  if (socket) socket.close();
});
</script>

<style scoped>
main::-webkit-scrollbar {
  width: 8px;
}
main::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}
</style>