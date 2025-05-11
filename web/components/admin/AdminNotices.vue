<template>
  <div class="space-y-6">
    <AdminSearchBar :filters="filters" @search="onSearch"/>
    <div class="flex justify-end">
      <button class="bg-sky-600 hover:bg-sky-700 text-white px-4 py-2 rounded-md shadow-sm text-sm font-medium transition-colors" @click="onAdd">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 mr-2 inline-block">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
        </svg>
        공지 추가
      </button>
    </div>
    <AdminTable
        :columns="columns"
        :rows="notices"
        @edit="onEdit"
        @delete="onDelete"
    />
    <AdminPagination :page="page" :total="total" @change="onPageChange"/>
  </div>
</template>
<script setup>
import {ref} from 'vue'
import AdminSearchBar from './AdminSearchBar.vue'
import AdminTable from './AdminTable.vue'
import AdminPagination from './AdminPagination.vue'

const filters = ref({type: '', date: '', keyword: ''})
const columns = [
  {key: 'id', label: 'ID'},
  {key: 'title', label: '제목'},
  {key: 'createdAt', label: '등록일'},
  {key: 'actions', label: '관리'}
]
const notices = ref([
  {id: 1, title: '공지사항 예시1', createdAt: '2024-05-01'},
  {id: 2, title: '공지사항 예시2', createdAt: '2024-05-02'}
])
const page = ref(1)
const total = ref(2)

function onSearch(f) { /* 검색 로직 */
}

function onAdd() {
  alert('공지 추가');
}

function onEdit(row) {
  alert('수정: ' + row.title);
}

function onDelete(row) {
  alert('삭제: ' + row.title);
}

function onPageChange(p) {
  page.value = p
}
</script>
  
