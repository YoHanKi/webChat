<template>
  <div class="flex flex-col h-full bg-gray-50 rounded-lg shadow-md">
    <!-- Chat messages -->
    <div ref="container" class="flex-1 overflow-y-auto p-4 flex flex-col space-y-3">
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
    </div>

    <!-- Input -->
    <div class="bg-white p-3 shadow-inner flex items-center">
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import defaultAvatar from '~/assets/images/defaultMsgImg.svg';

const props = defineProps({
  roomId: {
    type: [String, Number],
    required: true
  },
  username: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['message-sent']);

const container = ref(null);
const input = ref('');
const messages = ref([]);
let socket = null;

// WebSocket 연결 설정
const setupWebSocket = () => {
  socket = new WebSocket(`ws://localhost:8080/ws-chat?roomId=${props.roomId}`);

  socket.onopen = () => {
    // JOIN 메시지 전송
    socket.send(JSON.stringify({
      type: 'JOIN',
      sender: props.username,
      content: '',
      roomId: props.roomId
    }));
  };

  socket.onmessage = (event) => {
    const chat = JSON.parse(event.data);
    if (chat.roomId === props.roomId) {
      messages.value.push(chat);
      scrollToBottom();
    }
  };

  socket.onerror = (error) => {
    console.error('WebSocket 오류:', error);
  };

  socket.onclose = (event) => {
    console.log('WebSocket 연결 종료:', event);
  };
};

// 스크롤을 맨 아래로 이동
const scrollToBottom = () => {
  nextTick(() => {
    if (container.value) {
      container.value.scrollTop = container.value.scrollHeight;
    }
  });
};

// 메시지 전송
const sendMessage = () => {
  if (!input.value.trim() || !socket) return;

  const chat = {
    type: 'CHAT',
    sender: props.username,
    content: input.value.trim(),
    roomId: props.roomId
  };

  socket.send(JSON.stringify(chat));
  input.value = '';
  emit('message-sent', chat);
};

// 컴포넌트 마운트 시 WebSocket 연결
onMounted(() => {
  setupWebSocket();
});

// 컴포넌트 언마운트 시 WebSocket 연결 해제
onBeforeUnmount(() => {
  if (socket) {
    socket.close();
  }
});

function sendKick(roomId, userId, content) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify({
      type: 'KICK',
      roomId: props.roomId,
      sender: props.username,
      content: content
    }));
  }
}

defineExpose({
  sendSystemMessage: (content) => {
    messages.value.push({
      type: 'SYSTEM',
      content,
      roomId: props.roomId
    });
    scrollToBottom();
  },
  sendKick
});
</script>

<style scoped>
div::-webkit-scrollbar {
  width: 8px;
}

div::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}
</style>
