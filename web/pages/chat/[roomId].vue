<template>
  <div class="flex flex-col h-screen bg-gray-50">
    <!-- Header -->
    <header class="bg-[#03C75A] text-white p-4 flex flex-col items-center justify-center shadow-md">
      <h1 class="text-2xl font-bold">{{ room.name }}</h1>
      <span class="text-sm opacity-75">User: {{ username }}</span>
    </header>

    <!-- Chat messages -->
    <main ref="container" class="flex-1 overflow-auto p-6 flex flex-col space-y-4 justify-end">
      <div
          v-for="(msg, idx) in messages"
          :key="idx"
          :class="[
          'max-w-xl px-4 py-2 rounded-lg',
          msg.type !== 'CHAT'
            ? 'bg-gray-200 text-gray-600 italic self-center'
            : msg.sender === username
              ? 'bg-[#03C75A] text-white self-end'
              : 'bg-white text-gray-800 self-start',
        ]"
      >
        <div v-if="msg.type === 'CHAT'">
          <p class="font-semibold mb-1">{{ msg.sender }}</p>
          <p>{{ msg.content }}</p>
        </div>
        <div v-else>
          <p>{{ msg.content }}</p>
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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
// Nuxt 3의 내장 fetch 사용
import { $fetch } from 'ofetch';

const route = useRoute();
const router = useRouter();
const roomId = route.params.roomId;
const messages = ref([]);
const input = ref('');
const username = ref('');
const room = ref({ id: roomId, name: '로딩 중...' });
let socket;
const container = ref(null);

onMounted(async () => {
  try {
    //    이 요청은 브라우저 쿠키(JSESSIONID)를 자동으로 포함합니다.
    const userInfo = await $fetch('/api/user/me', { credentials: 'include' }); // 실제 API 경로로 변경하세요.

    if (!userInfo || !userInfo.username) {
      console.error('사용자 정보를 가져올 수 없습니다. 로그인 상태를 확인하세요.');
      router.push('/login');
      return;
    }
    username.value = userInfo.username; // API로부터 받은 사용자 이름 사용

    room.value.name = `채팅방 ${roomId}`; // 임시 이름

    // 2. 웹소켓 연결 (이제 username.value는 서버에서 확인된 값)
    socket = new WebSocket(`ws://localhost:8080/ws-chat?roomId=${roomId}`);


    socket.onopen = () => {
      // 3. JOIN 메시지에 서버에서 확인된 사용자 이름 사용
      socket.send(JSON.stringify({ type: 'JOIN', sender: username.value, content: '', roomId }));
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
      // 연결 종료 처리 로직
      router.push('/login');
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

function sendMessage() {
  if (!input.value.trim()) return;
  const chat = { type: 'CHAT', sender: username.value, content: input.value.trim(), roomId };
  socket.send(JSON.stringify(chat));
  input.value = '';
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