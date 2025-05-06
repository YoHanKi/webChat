<template>
  <div class="bg-white rounded-lg shadow-md p-4 mb-4">
    <div class="flex justify-between items-center mb-3">
      <h3 class="font-bold text-gray-700">접속자 목록 ({{ users.length }})</h3>
      <span class="text-sm text-gray-500">최대 {{ maxCapacity }}명</span>
    </div>

    <div class="space-y-2 overflow-y-auto">
      <div
          v-for="user in users"
          :key="user.userId"
          class="flex items-center justify-between py-2 border-b border-gray-100"
      >
        <div class="flex items-center">
          <img
              :src="user.userImageUrl || defaultAvatar"
              :alt="user.userName"
              class="w-8 h-8 rounded-full mr-3"
          />
          <div>
            <div class="flex items-center">
              <span class="font-medium text-gray-800">{{ user.userName }}</span>
              <span
                  v-if="isOwner && currentUsername === user.userName"
                  class="ml-2 px-2 py-0.5 bg-[#03C75A] text-white text-xs rounded-full"
              >
                방장
              </span>
            </div>
            <span class="text-xs text-gray-500">{{ user.userStatus === 'ACTIVE' ? '접속 중' : '자리 비움' }}</span>
          </div>
        </div>

        <!-- 방장만 다른 사용자 강퇴 버튼 표시 -->
        <button
            v-if="isOwner && currentUsername !== user.userName"
            @click="kickUser(user)"
            class="text-xs text-red-500 hover:text-red-700"
        >
          강퇴
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { $fetch } from 'ofetch';
import defaultAvatar from '~/assets/images/defaultMsgImg.svg';

const props = defineProps({
  roomId: {
    type: [String, Number],
    required: true
  },
  isOwner: {
    type: Boolean,
    default: false
  },
  currentUsername: {
    type: String,
    required: true
  },
  maxCapacity: {
    type: Number,
    default: 10
  }
});

const users = ref([]);
const emit = defineEmits(['kick-user']);

// 사용자 목록 가져오기
const fetchUsers = async () => {
  try {
    const response = await $fetch(`/api/room/list/${props.roomId}`);
    users.value = response.map(user => ({
      ...user,
      userImageUrl: user.userImageUrl || null,
      userStatus: user.userStatus || 'ACTIVE'
    }));
  } catch (error) {
    console.error('사용자 목록을 가져오는데 실패했습니다:', error);
  }
};

// 사용자 강퇴
const kickUser = async (user) => {
  try {
    if (confirm(`정말로 ${user.userName}님을 강퇴하시겠습니까?`)) {
      // await $fetch(`/api/room/kick`, {
      //   method: 'POST',
      //   body: { roomId: props.roomId, userId: user.userId }
      // });

      // 강퇴 이벤트 발생 - 부모 컴포넌트에서 처리
      emit('kick-user', { roomId: props.roomId, userId: props.userId, content: user.userName });

      alert(`${user.userName}님을 강퇴했습니다.`);
    }
  } catch (error) {
    console.error('사용자 강퇴에 실패했습니다:', error);
    alert('사용자 강퇴에 실패했습니다.');
  }
};

// 컴포넌트 마운트 시 사용자 목록 가져오기
onMounted(() => {
  fetchUsers();

  // 실제 구현에서는 주기적으로 업데이트하거나 WebSocket으로 실시간 업데이트하는 것이 좋음
  const interval = setInterval(fetchUsers, 10000); // 10초마다 갱신

  // 컴포넌트 언마운트 시 인터벌 제거
  onBeforeUnmount(() => {
    clearInterval(interval);
  });
});

// roomId가 변경되면 사용자 목록 다시 가져오기
watch(() => props.roomId, fetchUsers);

defineExpose({
  refreshUsers: fetchUsers
});
</script>
