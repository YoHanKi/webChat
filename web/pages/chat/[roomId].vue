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

const route = useRoute();
const router = useRouter();
const roomId = route.params.roomId;
const messages = ref([]);
const input = ref('');
const username = ref('');
const room = ref({ id: roomId, name: '로딩 중...' });
let socket;
const container = ref(null);

onMounted(() => {
  const name = sessionStorage.getItem('username');
  if (!name) {
    router.push('/login');
    return;
  }
  username.value = name;
  // 방 정보를 API로 가져오는 경우 여기에 호출
  room.value.name = `채팅방 ${roomId}`;

  socket = new WebSocket(`ws://localhost:8080/ws-chat?roomId=${roomId}`);
  socket.onopen = () => {
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