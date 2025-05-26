<template>
  <div class="space-y-6">
    <AdminSearchBar :filters="filters" @search="onSearch"/>
    <div class="flex justify-end">
      <button class="bg-sky-600 hover:bg-sky-700 text-white px-4 py-2 rounded-md shadow-sm text-sm font-medium transition-colors" @click="onAdd">
        <PlusIcon class="w-5 h-5 mr-2 inline-block"/>
        채팅방 추가
      </button>
    </div>
    <AdminTable
        :columns="adminColumns.columns.room"
        :rows="rooms"
        @edit="onEdit"
        @delete="onDelete"
    />
    <AdminPagination :page="page" :total="total" @change="onPageChange"/>

    <!-- 추가/수정 모달 팝업 -->
    <AdminModal
      v-model="showModal"
      :title="modalTitle"
      :confirm-text="isEditing ? '수정' : '추가'"
      @confirm="saveRoom"
    >
      <AdminRoomForm
        :initial-data="formData"
        @update:form="updateFormData"
      />
    </AdminModal>
    
    <!-- 삭제 확인 모달 -->
    <AdminModal
      v-model="showDeleteModal"
      title="채팅방 삭제"
      confirm-text="삭제"
      @confirm="confirmDelete"
    >
      <div class="text-center py-4">
        <ExclamationTriangleIcon class="w-12 h-12 mx-auto text-yellow-500 mb-4" />
        <p class="text-lg font-medium text-gray-900 mb-2">정말로 이 채팅방을 삭제하시겠습니까?</p>
        <p class="text-sm text-gray-500">
          "{{ roomToDelete?.name || ''}}" 채팅방을 삭제하면 관련된 모든 채팅 기록도 함께 삭제될 수 있습니다. (백엔드 정책에 따라 다름)
          <br>이 작업은 되돌릴 수 없습니다.
        </p>
      </div>
    </AdminModal>
  </div>
</template>
<script setup>
import {ref, computed, onMounted} from 'vue' // onMounted 추가
import AdminSearchBar from '../AdminSearchBar.vue'
import AdminTable from '../AdminTable.vue'
import AdminPagination from '../AdminPagination.vue'
import AdminModal from '../modal/AdminModal.vue'
import AdminRoomForm from '../form/AdminRoomForm.vue'
import {PlusIcon, ExclamationTriangleIcon} from "@heroicons/vue/24/outline";
import { useAdminColumns } from '~/composables/useAdminColumns';
import { useFetch, useRuntimeConfig } from '#app'; // useFetch, useRuntimeConfig 추가

const adminColumns = useAdminColumns();
const config = useRuntimeConfig();
const API_BASE_URL = config.public.apiURL;

// SearchRoomRequest DTO에 따라 필터 조정 필요. keyword를 name으로 매핑 가정.
const filters = ref({type: '', date: '', keyword: ''})
const rooms = ref([])
const page = ref(1)
const total = ref(0)
const itemsPerPage = 10; // 페이지 당 항목 수

// 채팅방 목록 가져오기 함수
async function fetchRooms() {
  try {
    const params = new URLSearchParams();
    params.append('page', page.value - 1);
    params.append('size', itemsPerPage);
    if (filters.value.type) params.append('searchType', filters.value.type); // keyword를 title로 검색
    else params.append('searchType', 'ALL'); // keyword가 없으면 빈 문자열로 처리
    if (filters.value.keyword) params.append('searchText', filters.value.keyword); // content 검색 추가 (필요시)
    else params.append('searchText', ''); // content가 없으면 빈 문자열로 처리

    const { data, error } = await useFetch(`${API_BASE_URL}/admin/room/search?${params.toString()}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include' // 쿠키의 JSESSIONID를 자동으로 포함
    });

    if (error.value) {
      console.error('채팅방 목록 조회 실패:', error.value);
      alert('채팅방 목록을 불러오는 데 실패했습니다.');
      rooms.value = [];
      total.value = 0;
    } else if (data.value) {
      rooms.value = data.value.content.map(room => ({
        ...room,
        createdAt: room.createdAt ? new Date(room.createdAt).toLocaleDateString() : '-', // 날짜 형식 변환
        // creator는 SelectRoomForAdminDTO에 있지만, 테이블 컬럼 정의에 따라 표시 여부 결정
      }));
      total.value = data.value.totalElements;
    }
  } catch (e) {
    console.error('채팅방 목록 조회 중 예외 발생:', e);
    alert('채팅방 목록 조회 중 오류가 발생했습니다.');
    rooms.value = [];
    total.value = 0;
  }
}

onMounted(fetchRooms);

function onSearch(newFilters) {
  filters.value.keyword = newFilters.keyword || '';
  page.value = 1; // 검색 시 첫 페이지로 이동
  fetchRooms();
}

// 추가/수정 모달 관련 상태
const showModal = ref(false);
const isEditing = ref(false);
const formData = ref({
  id: null,
  name: '',
  maxCapacity: 10,
  description: ''
});

const modalTitle = computed(() => {
  return isEditing.value ? '채팅방 수정' : '채팅방 추가';
});

function onAdd() {
  isEditing.value = false;
  formData.value = { id: null, name: '', maxCapacity: 10, description: '' };
  showModal.value = true;
}

function onEdit(row) {
  isEditing.value = true;
  // SelectRoomForAdminDTO 필드에 맞게 매핑
  formData.value = {
    id: row.id,
    name: row.name,
    // maxCapacity, description은 SelectRoomForAdminDTO에 없을 수 있음.
    // 상세 조회 API (GET /api/admin/room/{id})를 호출하거나, 목록 DTO에 포함시켜야 함.
    // 여기서는 row에 해당 필드가 있다고 가정하고, 없으면 기본값 사용.
    maxCapacity: row.maxCapacity || 10,
    description: row.description || ''
  };
  showModal.value = true;
}

function updateFormData(data) {
  formData.value = { ...formData.value, ...data };
}

async function saveRoom() {
  try {
    const payload = {
      id: formData.value.id, // 수정 시 id 포함, 추가 시 null 또는 제외 (백엔드 ModifyRoomRequest 구조에 따름)
      name: formData.value.name,
      maxCapacity: formData.value.maxCapacity,
      description: formData.value.description,
    };

    // RoomController는 PUT /api/admin/room 만 제공 (id 유무로 생성/수정 구분 가정)
    const { error } = await useFetch(`${API_BASE_URL}/admin/room`, {
      method: 'PUT',
      body: payload,
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    });

    if (error.value) {
      console.error('채팅방 저장 실패:', error.value);
      alert(`채팅방 ${isEditing.value ? '수정' : '추가'}에 실패했습니다.`);
    } else {
      alert(`채팅방이 성공적으로 ${isEditing.value ? '수정' : '추가'}되었습니다.`);
      showModal.value = false;
      fetchRooms(); // 목록 새로고침
    }
  } catch (e) {
    console.error('채팅방 저장 중 예외 발생:', e);
    alert(`채팅방 ${isEditing.value ? '수정' : '추가'} 중 오류가 발생했습니다.`);
  } finally {
    if (!isEditing.value) {
        formData.value = { id: null, name: '', maxCapacity: 10, description: '' };
    }
  }
}

// 삭제 모달 관련 상태 및 함수
const showDeleteModal = ref(false);
const roomToDelete = ref(null);

function onDelete(row) {
  roomToDelete.value = row;
  showDeleteModal.value = true;
}

async function confirmDelete() {
  if (!roomToDelete.value || !roomToDelete.value.id) return;

  // !!! 중요 !!!
  // RoomController.java에 삭제 API가 없습니다.
  // 아래 코드는 DELETE /api/admin/room/{id} API가 있다고 가정하고 작성되었습니다.
  // 백엔드에 해당 API를 구현해야 정상적으로 동작합니다.
  console.warn('백엔드에 채팅방 삭제 API (DELETE /api/admin/room/{id}) 구현이 필요합니다.');

  try {
    const { error } = await useFetch(`${API_BASE_URL}/admin/room/${roomToDelete.value.id}`, {
      method: 'DELETE',
      credentials: 'include'
    });

    if (error.value) {
      // 404 Not Found 또는 다른 에러가 발생할 가능성이 높음 (API가 없으므로)
      console.error('채팅방 삭제 실패 (API 미구현 가능성 높음):', error.value);
      alert('채팅방 삭제에 실패했습니다. 관리자에게 문의하거나 API 구현을 확인하세요.');
    } else {
      alert('채팅방이 성공적으로 삭제되었습니다. (실제 삭제는 API 구현 후 가능)');
      fetchRooms(); // 목록 새로고침
    }
  } catch (e) {
    console.error('채팅방 삭제 중 예외 발생:', e);
    alert('채팅방 삭제 중 오류가 발생했습니다.');
  } finally {
    showDeleteModal.value = false;
    roomToDelete.value = null;
  }
}

function onPageChange(p) {
  page.value = p;
  fetchRooms();
}
</script>
