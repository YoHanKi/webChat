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
    
    <!-- 모달 팝업 -->
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
  </div>
</template>
<script setup>
import {ref, computed} from 'vue'
import AdminSearchBar from '../AdminSearchBar.vue'
import AdminTable from '../AdminTable.vue'
import AdminPagination from '../AdminPagination.vue'
import AdminModal from '../modal/AdminModal.vue'
import AdminUserForm from '../form/AdminUserForm.vue'
import {PlusIcon} from "@heroicons/vue/24/outline/index.js"

const adminColumns = useAdminColumns();

const filters = ref({type: '', date: '', keyword: ''})
const users = ref([
  {id: 1, username: 'admin', role: 'ADMIN', createdAt: '2024-05-01'},
  {id: 2, username: 'user1', role: 'USER', createdAt: '2024-05-02'}
])
const page = ref(1)
const total = ref(2)

// 모달 관련 상태
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

function onSearch(f) { /* 검색 로직 */
}

function onAdd() {
  isEditing.value = false;
  formData.value = { id: null, username: '', password: '', role: 'USER' };
  showModal.value = true;
}

function onEdit(row) {
  isEditing.value = true;
  formData.value = { ...row, password: '' }; // 보안상 패스워드는 빈 값으로
  showModal.value = true;
}

function onDelete(row) {
  alert('삭제: ' + row.username);
}

function updateFormData(data) {
  formData.value = { ...formData.value, ...data };
}

function saveUser() {
  // API 호출 로직을 여기에 구현
  if (isEditing.value) {
    // 수정 로직
    console.log('수정:', formData.value);
  } else {
    // 추가 로직
    console.log('추가:', formData.value);
  }
  
  // 성공 시 폼 초기화 및 목록 새로고침
  formData.value = { id: null, username: '', password: '', role: 'USER' };
}

function onPageChange(p) {
  page.value = p
}
</script>
