<template>
  <div class="space-y-6">
    <AdminSearchBar :filters="filters" @search="onSearch"/>
    <div class="flex justify-end">
      <button class="bg-sky-600 hover:bg-sky-700 text-white px-4 py-2 rounded-md shadow-sm text-sm font-medium transition-colors" @click="onAdd">
        <PlusIcon class="w-5 h-5 mr-2 inline-block"/>
        회원 추가
      </button>
    </div>
    <AdminTable
        :columns="adminColumns.columns.user"
        :rows="users"
        @edit="onEdit"
        @delete="onDelete"
    />
    <AdminPagination :page="page" :total="total" @change="onPageChange"/>
    
    <!-- 추가/수정 모달 팝업 -->
    <AdminModal 
      v-model="showModal"
      :title="modalTitle"
      :confirm-text="isEditing ? '수정' : '추가'"
      @confirm="saveUser"
    >
      <AdminUserForm 
        :initial-data="formData"
        @update:form="updateFormData"
      />
    </AdminModal>
    
    <!-- 삭제 확인 모달 -->
    <AdminModal
      v-model="showDeleteModal"
      title="회원 삭제"
      confirm-text="삭제"
      @confirm="confirmDelete"
    >
      <div class="text-center py-4">
        <ExclamationTriangleIcon class="w-12 h-12 mx-auto text-yellow-500 mb-4" />
        <p class="text-lg font-medium text-gray-900 mb-2">정말로 이 회원을 삭제하시겠습니까?</p>
        <p class="text-sm text-gray-500">
          "{{ userToDelete?.username || ''}}" 회원을 삭제하면 관련된 모든 데이터가 함께 삭제될 수 있습니다. (백엔드 정책에 따라 다름)
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
import AdminUserForm from '../form/AdminUserForm.vue'
import {PlusIcon, ExclamationTriangleIcon} from "@heroicons/vue/24/outline"
import { useAdminColumns } from '~/composables/useAdminColumns';
import { useFetch, useRuntimeConfig } from '#app'; // useFetch, useRuntimeConfig 추가

const adminColumns = useAdminColumns();
const config = useRuntimeConfig();
const API_BASE_URL = config.public.apiURL;

const filters = ref({type: '', date: '', keyword: ''})
const users = ref([])
const page = ref(1)
const total = ref(0)
const itemsPerPage = 10; // 페이지 당 항목 수

// 사용자 목록 가져오기 함수
async function fetchUsers() {
  try {
    const params = new URLSearchParams();
    params.append('page', page.value - 1);
    params.append('size', itemsPerPage);
    if (filters.value.type) params.append('searchType', filters.value.type); // keyword를 title로 검색
    else params.append('searchType', 'ALL'); // keyword가 없으면 빈 문자열로 처리
    if (filters.value.keyword) params.append('searchText', filters.value.keyword); // content 검색 추가 (필요시)
    else params.append('searchText', ''); // content가 없으면 빈 문자열로 처리

    const { data, error } = await useFetch(`${API_BASE_URL}/admin/user/search?${params.toString()}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include' // 쿠키의 JSESSIONID를 자동으로 포함
    });

    if (error.value) {
      console.error('사용자 목록 조회 실패:', error.value);
      alert('사용자 목록을 불러오는 데 실패했습니다.');
      users.value = [];
      total.value = 0;
    } else if (data.value) {
      users.value = data.value.content.map(user => ({
        ...user,
        // SelectUserForAdminDTO의 createDate는 LocalDateTime이므로 포맷팅
        createdAt: user.createDate ? new Date(user.createDate).toLocaleDateString() : '-',
        // modifiedDate도 필요시 유사하게 처리
      }));
      total.value = data.value.totalElements;
    }
  } catch (e) {
    console.error('사용자 목록 조회 중 예외 발생:', e);
    alert('사용자 목록 조회 중 오류가 발생했습니다.');
    users.value = [];
    total.value = 0;
  }
}

onMounted(fetchUsers);

// 추가/수정 모달 관련 상태
const showModal = ref(false);
const isEditing = ref(false);
const formData = ref({
  id: null,
  username: '',
  password: '',
  role: 'USER'
});

const modalTitle = computed(() => {
  return isEditing.value ? '회원 수정' : '회원 추가';
});

function onSearch(newFilters) {
  filters.value.keyword = newFilters.keyword || '';
  // filters.type, filters.date는 현재 User API 검색 조건에 사용되지 않음
  page.value = 1; // 검색 시 첫 페이지로 이동
  fetchUsers();
}

function onAdd() {
  isEditing.value = false;
  formData.value = { id: null, username: '', password: '', role: 'USER' };
  showModal.value = true;
}

function onEdit(row) {
  isEditing.value = true;
  // SelectUserForAdminDTO에는 password 필드가 없음. 수정 시 password는 빈 값으로 시작.
  formData.value = {
    id: row.id,
    username: row.username,
    password: '', // 수정 시 비밀번호는 새로 입력받거나, 비워두면 변경하지 않도록 백엔드에서 처리해야 하나, 현재 ModifyUserRequest는 password를 필수로 요구함.
    role: row.role
  };
  showModal.value = true;
}

function updateFormData(data) {
  formData.value = { ...formData.value, ...data };
}

async function saveUser() {
  if (!isEditing.value) {
    alert('현재 백엔드 API는 사용자 추가 기능을 지원하지 않습니다. 사용자 수정만 가능합니다.');
    // ChatHistoryController에는 POST 메서드가 없으므로, 사용자 추가 API가 백엔드에 구현되어야 함.
    // 만약 POST /api/admin/user/create 와 같은 API가 있다면 아래 로직 사용.
    /*
    try {
      const addPayload = {
        username: formData.value.username,
        password: formData.value.password,
        role: formData.value.role,
      };
      const { error } = await useFetch(`${API_BASE_URL}/api/admin/user/create`, { // 가상의 생성 API 엔드포인트
        method: 'POST',
        body: addPayload,
        headers: { 'Content-Type': 'application/json' }
      });
      if (error.value) {
        console.error('사용자 추가 실패:', error.value);
        alert('사용자 추가에 실패했습니다.');
      } else {
        alert('사용자가 성공적으로 추가되었습니다.');
        showModal.value = false;
        fetchUsers();
      }
    } catch (e) {
      console.error('사용자 추가 중 예외 발생:', e);
      alert('사용자 추가 중 오류가 발생했습니다.');
    }
    */
    return; // 추가 기능 없으므로 여기서 중단
  }

  // 사용자 수정 로직 (POST /api/admin/user/modify)
  if (!formData.value.id) {
    alert('수정할 사용자의 ID가 없습니다.');
    return;
  }
  if (!formData.value.password && isEditing.value) {
    // ModifyUserRequest는 password를 필수로 요구합니다.
    // 실제 운영 환경에서는 사용자에게 비밀번호를 입력하도록 유도하거나,
    // 백엔드 API가 비밀번호 없는 수정을 허용하도록 변경해야 합니다.
    alert('사용자 수정 시 비밀번호는 필수입니다. (현재 API 제약사항)');
    // return; // 또는 비밀번호 입력 필드를 활성화하도록 UI 변경 고려
  }

  try {
    const payload = {
      id: formData.value.id,
      username: formData.value.username,
      password: formData.value.password, // ModifyUserRequest는 password를 필수로 요구
      role: formData.value.role,
    };

    const { error } = await useFetch(`${API_BASE_URL}/api/admin/user/modify`, { // UserAdminController의 modifyUser는 POST 메서드 사용
      method: 'POST',
      body: payload,
      headers: { 'Content-Type': 'application/json' }
    });

    if (error.value) {
      console.error('사용자 수정 실패:', error.value);
      let errorMessage = '사용자 수정에 실패했습니다.';
      if (error.value.data && error.value.data.message) {
        errorMessage += `\n오류: ${error.value.data.message}`;
      }
      alert(errorMessage);
    } else {
      alert('사용자가 성공적으로 수정되었습니다.');
      showModal.value = false;
      fetchUsers(); // 목록 새로고침
    }
  } catch (e) {
    console.error('사용자 수정 중 예외 발생:', e);
    alert('사용자 수정 중 오류가 발생했습니다.');
  } finally {
    // 성공 여부와 관계없이 폼 초기화는 모달 닫힐 때 처리되거나, 여기서 명시적으로 할 수 있음
  }
}

// 삭제 모달 관련 상태 및 함수
const showDeleteModal = ref(false);
const userToDelete = ref(null);

function onDelete(row) {
  userToDelete.value = row;
  showDeleteModal.value = true;
}

async function confirmDelete() {
  if (!userToDelete.value || !userToDelete.value.id) return;

  try {
    // UserAdminController의 deleteUser는 id를 @RequestParam으로 받음
    const { error } = await useFetch(`${API_BASE_URL}/api/admin/user?id=${userToDelete.value.id}`, {
      method: 'DELETE',
    });

    if (error.value) {
      console.error('사용자 삭제 실패:', error.value);
      alert('사용자 삭제에 실패했습니다.');
    } else {
      alert('사용자가 성공적으로 삭제되었습니다.');
      fetchUsers(); // 목록 새로고침
    }
  } catch (e) {
    console.error('사용자 삭제 중 예외 발생:', e);
    alert('사용자 삭제 중 오류가 발생했습니다.');
  } finally {
    showDeleteModal.value = false;
    userToDelete.value = null;
  }
}

function onPageChange(p) {
  page.value = p;
  fetchUsers();
}
</script>
