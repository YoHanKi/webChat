<template>
  <div class="space-y-6">
    <AdminSearchBar :filters="filters" @search="onSearch"/>
    <div class="flex justify-end">
      <button class="bg-sky-600 hover:bg-sky-700 text-white px-4 py-2 rounded-md shadow-sm text-sm font-medium transition-colors" @click="onAdd">
        <PlusIcon class="w-5 h-5 mr-2 inline-block"/>
        채팅 추가
      </button>
    </div>
    <AdminTable
        :columns="adminColumns.columns.chat"
        :rows="chats"
        @edit="onEdit"
        @delete="onDelete"
    />
    <AdminPagination :page="page" :total="total" @change="onPageChange"/>

    <!-- 추가/수정 모달 팝업 -->
    <AdminModal
      v-model="showModal"
      :title="modalTitle"
      :confirm-text="isEditing ? '수정' : '추가'"
      @confirm="saveChat"
    >
      <AdminChatForm
        :initial-data="formData"
        @update:form="updateFormData"
      />
    </AdminModal>
    
    <!-- 삭제 확인 모달 -->
    <AdminModal
      v-model="showDeleteModal"
      title="채팅 삭제"
      confirm-text="삭제"
      @confirm="confirmDelete"
    >
      <div class="text-center py-4">
        <ExclamationTriangleIcon class="w-12 h-12 mx-auto text-yellow-500 mb-4" />
        <p class="text-lg font-medium text-gray-900 mb-2">정말로 이 채팅을 삭제하시겠습니까?</p>
        <p class="text-sm text-gray-500">
          ID {{ chatToDelete?.id || ''}} 채팅을 삭제하면 영구적으로 제거됩니다.
          <br>이 작업은 되돌릴 수 없습니다.
        </p>
      </div>
    </AdminModal>
  </div>
</template>

<script setup>
import {ref, computed, onMounted } from 'vue' // onMounted 추가
import AdminSearchBar from '../AdminSearchBar.vue'
import AdminTable from '../AdminTable.vue'
import AdminPagination from '../AdminPagination.vue'
import AdminModal from '../modal/AdminModal.vue'
import AdminChatForm from '../form/AdminChatForm.vue'
import {PlusIcon, ExclamationTriangleIcon} from "@heroicons/vue/24/outline";
import { useAdminColumns } from '~/composables/useAdminColumns';
import { useFetch, useRuntimeConfig } from '#app'; // useFetch 및 useRuntimeConfig 추가

const adminColumns = useAdminColumns();
const config = useRuntimeConfig();
const API_BASE_URL = config.public.apiURL;

const filters = ref({roomId: '', sender: '', date: '', keyword: ''})
const chats = ref([])
const page = ref(1)
const total = ref(0)
const itemsPerPage = 10; // 페이지 당 항목 수

// 채팅 목록 가져오기 함수
async function fetchChats() {
  try {
    const params = new URLSearchParams();
    params.append('page', page.value - 1);
    params.append('size', itemsPerPage);
    if (filters.value.roomId) params.append('roomId', filters.value.roomId);
    if (filters.value.sender) params.append('sender', filters.value.sender);

    const { data, error } = await useFetch(`${API_BASE_URL}/admin/chat/history/search?${params.toString()}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include' // 쿠키의 JSESSIONID를 자동으로 포함
    });

    if (error.value) {
      console.error('채팅 목록 조회 실패:', error.value);
      alert('채팅 목록을 불러오는 데 실패했습니다.');
      chats.value = [];
      total.value = 0;
    } else if (data.value) {
      chats.value = data.value.content.map(chat => ({
        ...chat,
        // roomName은 SelectHistoryMessageForAdminDTO에 없으므로, roomId로 대체하거나 백엔드 수정 필요
        roomName: chat.roomId, // 또는 chat.room?.name 등으로 백엔드 응답에 따라 조정
      }));
      total.value = data.value.totalElements;
    }
  } catch (e) {
    console.error('채팅 목록 조회 중 예외 발생:', e);
    alert('채팅 목록 조회 중 오류가 발생했습니다.');
    chats.value = [];
    total.value = 0;
  }
}

onMounted(fetchChats);

function onSearch(newFilters) {
  filters.value = { ...newFilters };
  page.value = 1; // 검색 시 첫 페이지로 이동
  fetchChats();
}

// 추가/수정 모달 관련 상태
const showModal = ref(false);
const isEditing = ref(false);
const formData = ref({
  id: null,
  roomId: null,
  sender: '',
  content: ''
});

const modalTitle = computed(() => {
  return isEditing.value ? '채팅 수정' : '채팅 추가';
});

function onAdd() {
  isEditing.value = false;
  formData.value = { id: null, roomId: null, sender: '', content: '' };
  showModal.value = true;
}

function onEdit(row) {
  isEditing.value = true;
  // SelectHistoryMessageForAdminDTO 필드에 맞게 매핑
  formData.value = {
    id: row.id,
    roomId: row.roomId,
    sender: row.sender,
    content: row.content
  };
  showModal.value = true;
}

function updateFormData(data) {
  formData.value = { ...formData.value, ...data };
}

async function saveChat() {
  try {
    let response;
    const payload = {
      id: formData.value.id, // 수정 시에만 id 포함
      roomId: formData.value.roomId,
      sender: formData.value.sender,
      content: formData.value.content,
    };

    if (isEditing.value) {
      // 수정 로직 (PUT /admin/chat/history)
      // ModifyHistoryMessageRequest DTO에 맞게 id는 경로 변수가 아닌 body에 포함될 수 있음.
      // ChatHistoryController의 updateChatHistory는 ModifyHistoryMessageRequest를 받음.
      // ModifyHistoryMessageRequest는 id, roomId, sender, content를 가질 것으로 예상.
      response = await useFetch(`${API_BASE_URL}/admin/chat/history`, {
        method: 'PUT',
        body: payload,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
      });
    } else {
      // 추가 로직 (POST /admin/chat/history - 가정)
      // 백엔드에 해당 API 구현 필요
      // ChatHistoryController에는 현재 POST 메서드가 없음.
      // 만약 추가 API가 있다면, id는 payload에서 제외해야 할 수 있음.
      const addPayload = { ...payload };
      delete addPayload.id; // 추가 시에는 id를 보내지 않음
      response = await useFetch(`${API_BASE_URL}/admin/chat/history`, {
        method: 'POST',
        body: addPayload,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
      });
    }

    if (response.error.value) {
      console.error('채팅 저장 실패:', response.error.value);
      alert(`채팅 ${isEditing.value ? '수정' : '추가'}에 실패했습니다.`);
    } else {
      alert(`채팅이 성공적으로 ${isEditing.value ? '수정' : '추가'}되었습니다.`);
      showModal.value = false; // 모달 닫기
      fetchChats(); // 목록 새로고침
    }
  } catch (e) {
    console.error('채팅 저장 중 예외 발생:', e);
    alert(`채팅 ${isEditing.value ? '수정' : '추가'} 중 오류가 발생했습니다.`);
  } finally {
    // 성공 여부와 관계없이 폼 초기화는 모달 닫힐 때 처리되거나, 여기서 명시적으로 할 수 있음
    // formData.value = { id: null, roomId: null, sender: '', content: '' };
  }
}

// 삭제 모달 관련 상태 및 함수
const showDeleteModal = ref(false);
const chatToDelete = ref(null);

function onDelete(row) {
  chatToDelete.value = row;
  showDeleteModal.value = true;
}

async function confirmDelete() {
  if (!chatToDelete.value || !chatToDelete.value.id) return;

  try {
    const { error } = await useFetch(`${API_BASE_URL}/admin/chat/history/${chatToDelete.value.id}`, {
      method: 'DELETE',
      credentials: 'include'
    });

    if (error.value) {
      console.error('채팅 삭제 실패:', error.value);
      alert('채팅 삭제에 실패했습니다.');
    } else {
      alert('채팅이 성공적으로 삭제되었습니다.');
      fetchChats(); // 목록 새로고침
    }
  } catch (e) {
    console.error('채팅 삭제 중 예외 발생:', e);
    alert('채팅 삭제 중 오류가 발생했습니다.');
  } finally {
    showDeleteModal.value = false;
    chatToDelete.value = null;
  }
}

function onPageChange(p) {
  page.value = p;
  fetchChats();
}
</script>

<style scoped>
/* 필요한 스타일이 있다면 여기에 추가 */
</style>
