<template>
  <div class="space-y-6">
    <AdminSearchBar :filters="filters" @search="onSearch"/>
    <div class="flex justify-end">
      <button class="bg-sky-600 hover:bg-sky-700 text-white px-4 py-2 rounded-md shadow-sm text-sm font-medium transition-colors" @click="onAdd">
        <PlusIcon class="w-5 h-5 mr-2 inline-block"/>
        공지 추가
      </button>
    </div>
    <AdminTable
        :columns="adminColumns.columns.notice"
        :rows="notices"
        @edit="onEdit"
        @delete="onDelete"
    />
    <AdminPagination :page="page" :total="total" @change="onPageChange"/>

    <!-- 추가/수정 모달 팝업 -->
    <AdminModal
      v-model="showModal"
      :title="modalTitle"
      :confirm-text="isEditing ? '수정' : '추가'"
      @confirm="saveNotice"
    >
      <AdminNoticeForm
        :initial-data="formData"
        @update:form="updateFormData"
      />
    </AdminModal>
    
    <!-- 삭제 확인 모달 -->
    <AdminModal
      v-model="showDeleteModal"
      title="공지사항 삭제"
      confirm-text="삭제"
      @confirm="confirmDelete"
    >
      <div class="text-center py-4">
        <ExclamationTriangleIcon class="w-12 h-12 mx-auto text-yellow-500 mb-4" />
        <p class="text-lg font-medium text-gray-900 mb-2">정말로 이 공지사항을 삭제하시겠습니까?</p>
        <p class="text-sm text-gray-500">
          "{{ noticeToDelete?.title || ''}}" 공지사항을 삭제하면 영구적으로 제거됩니다.
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
import AdminNoticeForm from '../form/AdminNoticeForm.vue'
import {PlusIcon, ExclamationTriangleIcon} from "@heroicons/vue/24/outline";
import { useAdminColumns } from '~/composables/useAdminColumns';
import { useFetch, useRuntimeConfig } from '#app'; // useFetch 및 useRuntimeConfig 추가

const adminColumns = useAdminColumns();
const config = useRuntimeConfig();
const API_BASE_URL = config.public.apiURL;

// SearchNoticeRequest에는 title, content가 있으나, AdminSearchBar는 keyword 하나로 받음.
// 여기서는 keyword를 title 검색에 사용하고, date는 SearchNoticeRequest에 없으므로 사용 안함.
const filters = ref({title: '', content: '', keyword: ''}) // keyword를 사용하고, fetchNotices에서 title로 매핑
const notices = ref([])
const page = ref(1)
const total = ref(0)
const itemsPerPage = 10; // 페이지 당 항목 수

// 공지사항 목록 가져오기 함수
async function fetchNotices() {
  try {
    const params = new URLSearchParams();
    params.append('page', page.value - 1);
    params.append('size', itemsPerPage);
    if (filters.value.keyword) params.append('searchType', filters.value.title); // keyword를 title로 검색
    else params.append('searchType', 'ALL'); // keyword가 없으면 빈 문자열로 처리
    if (filters.value.content) params.append('searchText', filters.value.content); // content 검색 추가 (필요시)
    else params.append('searchText', ''); // content가 없으면 빈 문자열로 처리

    const { data, error } = await useFetch(`${API_BASE_URL}/admin/notice/search?${params.toString()}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include' // 쿠키의 JSESSIONID를 자동으로 포함
    });

    if (error.value) {
      console.error('공지사항 목록 조회 실패:', error.value);
      alert('공지사항 목록을 불러오는 데 실패했습니다.');
      notices.value = [];
      total.value = 0;
    } else if (data.value) {
      notices.value = data.value.content.map(notice => ({
        ...notice,
        createdAt: notice.createdAt ? new Date(notice.createdAt).toLocaleDateString() : '-', // 날짜 형식 변환
      }));
      total.value = data.value.totalElements;
    }
  } catch (e) {
    console.error('공지사항 목록 조회 중 예외 발생:', e);
    alert('공지사항 목록 조회 중 오류가 발생했습니다.');
    notices.value = [];
    total.value = 0;
  }
}

onMounted(fetchNotices);

function onSearch(newFilters) {
  // AdminSearchBar는 type, date, keyword를 포함한 객체를 전달
  // 여기서는 keyword만 사용
  filters.value.keyword = newFilters.keyword || '';
  page.value = 1; // 검색 시 첫 페이지로 이동
  fetchNotices();
}

// 추가/수정 모달 관련 상태
const showModal = ref(false);
const isEditing = ref(false);
const formData = ref({
  id: null,
  title: '',
  content: ''
});

const modalTitle = computed(() => {
  return isEditing.value ? '공지사항 수정' : '공지사항 추가';
});

function onAdd() {
  isEditing.value = false;
  formData.value = { id: null, title: '', content: '' };
  showModal.value = true;
}

function onEdit(row) {
  isEditing.value = true;
  // API에서 받은 데이터로 폼 채우기 (날짜 형식은 이미 변환됨)
  formData.value = {
    id: row.id,
    title: row.title,
    // content는 목록에 없을 수 있으므로, 상세 조회 API가 있다면 거기서 가져오거나,
    // 목록 DTO에 content가 포함되어 있다면 row.content 사용
    // 현재 SelectNoticeForAdminDTO에는 content가 없으므로, 수정 시 불러오거나 입력받아야 함.
    // 여기서는 임시로 '내용을 입력하세요' 또는 row에서 가져오도록 처리 (백엔드 DTO 확인 필요)
    content: row.content || '' // SelectNoticeForAdminDTO에 content가 있다면 row.content 사용
  };
  showModal.value = true;
}

function updateFormData(data) {
  formData.value = { ...formData.value, ...data };
}

async function saveNotice() {
  try {
    const payload = {
      id: formData.value.id, // 수정 시에만 id 포함 (백엔드에서 id 없으면 생성으로 처리 가정)
      title: formData.value.title,
      content: formData.value.content,
    };

    // NoticeAdminController에는 POST가 없고 PUT만 있음.
    // PUT /api/admin/notice 로 요청 (id 유무에 따라 백엔드에서 생성/수정 처리 가정)
    const { error } = await useFetch(`${API_BASE_URL}/api/admin/notice`, {
      method: 'PUT',
      body: payload,
      headers: { 'Content-Type': 'application/json' }
    });

    if (error.value) {
      console.error('공지사항 저장 실패:', error.value);
      alert(`공지사항 ${isEditing.value ? '수정' : '추가'}에 실패했습니다.`);
    } else {
      alert(`공지사항이 성공적으로 ${isEditing.value ? '수정' : '추가'}되었습니다.`);
      showModal.value = false;
      fetchNotices(); // 목록 새로고침
    }
  } catch (e) {
    console.error('공지사항 저장 중 예외 발생:', e);
    alert(`공지사항 ${isEditing.value ? '수정' : '추가'} 중 오류가 발생했습니다.`);
  } finally {
    if (!isEditing.value) {
        formData.value = { id: null, title: '', content: '' };
    }
  }
}

// 삭제 모달 관련 상태 및 함수
const showDeleteModal = ref(false);
const noticeToDelete = ref(null);

function onDelete(row) {
  noticeToDelete.value = row;
  showDeleteModal.value = true;
}

async function confirmDelete() {
  if (!noticeToDelete.value || !noticeToDelete.value.id) return;

  try {
    const { error } = await useFetch(`${API_BASE_URL}/api/admin/notice/${noticeToDelete.value.id}`, {
      method: 'DELETE',
    });

    if (error.value) {
      console.error('공지사항 삭제 실패:', error.value);
      alert('공지사항 삭제에 실패했습니다.');
    } else {
      alert('공지사항이 성공적으로 삭제되었습니다.');
      fetchNotices(); // 목록 새로고침
    }
  } catch (e) {
    console.error('공지사항 삭제 중 예외 발생:', e);
    alert('공지사항 삭제 중 오류가 발생했습니다.');
  } finally {
    showDeleteModal.value = false;
    noticeToDelete.value = null;
  }
}

function onPageChange(p) {
  page.value = p;
  fetchNotices();
}
</script>
