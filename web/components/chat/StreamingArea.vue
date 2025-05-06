<template>
  <div class="bg-black relative rounded-lg shadow-md overflow-hidden h-full flex flex-col">
    <!-- 스트리밍 영역 -->
    <div class="flex-1 flex items-center justify-center">
      <div v-if="!isLive" class="text-center p-6">
        <div class="w-24 h-24 mx-auto mb-4">
          <svg class="w-full h-full text-[#03C75A] animate-pulse" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-white mb-2">방송 준비중입니다</h2>
        <p class="text-gray-400">잠시만 기다려주세요</p>

        <div v-if="isOwner" class="mt-8">
          <button
              @click="startStream"
              class="bg-[#03C75A] hover:bg-[#02af4f] text-white px-6 py-3 rounded-md shadow-md transition font-bold"
          >
            방송 시작하기
          </button>
        </div>
      </div>

      <!-- 실제 스트림 재생될 곳 -->
      <video v-else ref="videoElement" class="w-full h-full object-cover" autoplay></video>
    </div>

    <!-- 컨트롤바 -->
    <div class="bg-gradient-to-t from-black/80 to-transparent p-4 absolute bottom-0 w-full">
      <div class="flex justify-between items-center">
        <div class="flex items-center">
          <div class="bg-red-600 rounded-full w-3 h-3 animate-pulse mr-2" v-if="isLive"></div>
          <span class="text-white font-medium">{{ isLive ? 'LIVE' : '방송 대기 중' }}</span>
        </div>

        <div class="flex space-x-3">
          <button
              v-if="isLive && isOwner"
              @click="stopStream"
              class="text-white bg-red-600 hover:bg-red-700 px-3 py-1 rounded-md text-sm"
          >
            방송 종료
          </button>

          <button class="text-white hover:text-gray-300">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15.536a5 5 0 001.414 1.414m2.828 2.828a9 9 0 002.828 2.828" />
            </svg>
          </button>

          <button class="text-white hover:text-gray-300">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5v-4m0 4h-4m4 0l-5-5" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';

const props = defineProps({
  roomId: {
    type: [String, Number],
    required: true
  },
  isOwner: {
    type: Boolean,
    default: false
  }
});

const isLive = ref(false);
const videoElement = ref(null);
let stream = null;

// 방송 시작 (방장만 가능)
const startStream = async () => {
  if (!props.isOwner) return;

  try {
    // 실제 구현에서는 미디어 서버와 연결하는 코드로 대체
    stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });

    if (videoElement.value) {
      videoElement.value.srcObject = stream;
    }

    isLive.value = true;

    // 실제 구현 시 이 부분에서 방송 시작 API 호출
    // await $fetch(`/api/room/stream/start`, {
    //   method: 'POST',
    //   body: { roomId: props.roomId }
    // });

    console.log('방송 시작');
  } catch (error) {
    console.error('방송 시작 실패:', error);
    alert('카메라 또는 마이크에 접근할 수 없습니다.');
  }
};

// 방송 종료 (방장만 가능)
const stopStream = async () => {
  if (!props.isOwner || !isLive.value) return;

  try {
    // 스트림 정지
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
    }

    isLive.value = false;

    // 실제 구현 시 이 부분에서 방송 종료 API 호출
    // await $fetch(`/api/room/stream/stop`, {
    //   method: 'POST',
    //   body: { roomId: props.roomId }
    // });

    console.log('방송 종료');
  } catch (error) {
    console.error('방송 종료 실패:', error);
  }
};

// 컴포넌트 언마운트 시 스트림 정리
onBeforeUnmount(() => {
  if (stream) {
    stream.getTracks().forEach(track => track.stop());
  }
});
</script>
