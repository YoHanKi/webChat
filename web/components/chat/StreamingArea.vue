<template>
  <div class="bg-black relative rounded-lg shadow-md overflow-hidden h-full flex flex-col">
    <!-- 스트리밍 영역 -->
    <div class="flex-1 flex items-center justify-center">
      <div v-if="!isLive" class="text-center p-6">
        <div class="w-24 h-24 mx-auto mb-4">
          <svg
              class="w-full h-full text-[#03C75A] animate-pulse"
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
          <button
              @click="startScreenShare"
              class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-md shadow-md transition font-bold ml-4">
              화면 공유 시작
          </button>
        </div>
      </div>

      <!-- 실제 스트림 재생될 곳 -->
      <video
          v-else
          ref="videoElement"
          class="w-full h-full object-cover"
          autoplay
          playsinline
      ></video>
    </div>

    <!-- 컨트롤바 -->
    <div class="bg-gradient-to-t from-black/80 to-transparent p-4 absolute bottom-0 w-full">
      <div class="flex justify-between items-center">
        <div class="flex items-center">
          <div class="bg-red-600 rounded-full w-3 h-3 animate-pulse mr-2" v-if="isLive"></div>
          <span class="text-white font-medium">
            {{ isLive ? 'LIVE' : '방송 대기 중' }}
          </span>
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
            <!-- 화면 공유 버튼 (추후 구현) -->
            <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-6 w-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
            >
              <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5v-4m0 4h-4m4 0l-5-5"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount, nextTick } from 'vue';

const props = defineProps({
  roomId:  { type: [String, Number], required: true },
  isOwner: { type: Boolean, default: false }
});

const isLive = ref(false);
const videoElement = ref(null);

let pc = null;
let signalingSocket = null;
let localStream = null;
let snapshotInterval;

const ICE_SERVERS = [
  { urls: 'stun:stun.l.google.com:19302' },
  {
    urls: [
      'turn:localhost:3478?transport=udp',
      'turn:localhost:3478?transport=tcp'
    ],
    username: 'testuser',
    credential: 'testpass'
  }
];

// PeerConnection + 시그널링 소켓 초기화
function initWebRTC() {
  pc = new RTCPeerConnection({ iceServers: ICE_SERVERS });

  pc.onicecandidate = (e) => {
    if (e.candidate && signalingSocket.readyState === WebSocket.OPEN) {
      signalingSocket.send(
          JSON.stringify({
            type: 'ICE',
            roomId: props.roomId,
            candidate: e.candidate
          })
      );
    }
  };

  // 원격 트랙 수신 시 비디오 엘리먼트에 바인딩
  pc.ontrack = (e) => {
    if (videoElement.value) {
      videoElement.value.srcObject = e.streams[0];
    }
  };

  // 시그널링 WS
  signalingSocket = new WebSocket(
      `ws://localhost:8080/ws-signal?roomId=${props.roomId}`
  );
  signalingSocket.onmessage = handleSignalMessage;
}

// 시그널링 메시지 핸들러
async function handleSignalMessage(event) {
  const msg = JSON.parse(event.data);

  if (msg.type === 'OFFER' && !props.isOwner) {
    // 시청자 측: Offer 수신 → Answer 생성
    await pc.setRemoteDescription(msg.sdp);
    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);
    signalingSocket.send(
        JSON.stringify({
          type: 'ANSWER',
          roomId: props.roomId,
          sdp: answer
        })
    );

  } else if (msg.type === 'ANSWER' && props.isOwner) {
    // 방송자 측: Answer 수신
    await pc.setRemoteDescription(msg.sdp);

  } else if (msg.type === 'ICE') {
    // 양쪽 모두 ICE 후보 추가
    try {
      await pc.addIceCandidate(msg.candidate);
    } catch (err) {
      console.error('Failed to add ICE candidate:', err);
    }
  }
}

// 카메라 방송 시작
async function startStream() {
  await startCapture({ video: true, audio: true });
}

// 화면 공유 시작
async function startScreenShare() {
  await startCapture({ video: { cursor: "always" }, audio: false }, true);
}

// 공통 캡처 & WebRTC 초기화
async function startCapture(constraints, isScreen = false) {
  if (!props.isOwner) return;

  try {
    // 1) 화면 또는 카메라 캡처
    localStream = isScreen
        ? await navigator.mediaDevices.getDisplayMedia(constraints)
        : await navigator.mediaDevices.getUserMedia(constraints);

    // 2) 뷰 업데이트
    isLive.value = true;
    await nextTick();
    videoElement.value.srcObject = localStream;

    initWebRTC();

    // 2.5) WebSocket이 OPEN 상태가 될 때까지 기다림
    await new Promise((resolve) => {
      if (signalingSocket.readyState === WebSocket.OPEN) {
        return resolve();
      }
      signalingSocket.addEventListener('open', () => resolve(), { once: true });
    });

    // 3) WebRTC/시그널링 초기화
    localStream.getTracks().forEach(track => pc.addTrack(track, localStream));
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    signalingSocket.send(JSON.stringify({
      type: 'OFFER',
      roomId: props.roomId,
      sdp: offer
    }));

    // 4) 썸네일 캡처 시작
    snapshotInterval = setInterval(captureThumbnail, 20000);
  } catch (err) {
    console.error('캡처 실패:', err);
    alert(isScreen ? '화면 공유에 실패했습니다.' : '카메라 접근에 실패했습니다.');
  }
}

// 방송 종료
function stopStream() {
  if (!isLive.value) return;

  // 미디어 스트림 정지
  if (localStream) {
    localStream.getTracks().forEach((t) => t.stop());
  }
  // Peer 연결 해제
  if (pc) {
    pc.close();
    pc = null;
  }
  // 시그널링 종료
  if (signalingSocket) {
    signalingSocket.close();
    signalingSocket = null;
  }

  isLive.value = false;

  // 캡쳐 종료
  if (snapshotInterval) {
    clearInterval(snapshotInterval);
    snapshotInterval = null;
  }
}

function captureThumbnail() {
  if (!videoElement.value || videoElement.value.readyState < 2) return;
  const canvas = document.createElement('canvas');
  canvas.width  = 320;
  canvas.height = 180;
  const ctx = canvas.getContext('2d');
  ctx.drawImage(videoElement.value, 0, 0, canvas.width, canvas.height);

  // Base64 문자열
  const dataUrl = canvas.toDataURL('image/jpeg', 0.7);

  console.log('Thumbnail captured:', dataUrl);

  // POST to backend
  fetch(`/api/room/${props.roomId}/thumbnail`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ image: dataUrl })
  }).catch(console.error);
}

// 컴포넌트 언마운트 시 정리
onBeforeUnmount(() => {
  stopStream();
  clearInterval(snapshotInterval);
});
</script>

<style scoped>
.streaming-area {
  position: relative;
  background: #000;
}
</style>