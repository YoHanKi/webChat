<template>
  <form class="p-6 bg-white rounded-lg shadow-md flex items-center space-x-4" @submit.prevent="$emit('search', filters)">
    <div class="flex-grow flex items-center space-x-4">
      <select
        v-model="filters.type"
        class="form-select block w-auto min-w-[150px] pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm rounded-md shadow-sm"
      >
        <option value="">전체 구분</option>
        <option value="notice">공지사항</option>
        <option value="user">회원</option>
        <option value="room">채팅방</option>
      </select>
      <input
        v-model="filters.date"
        type="date"
        class="form-input block w-auto min-w-[180px] pl-3 pr-2 py-2 text-base border-gray-300 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm rounded-md shadow-sm"
      />
      <input
        v-model="filters.keyword"
        type="text"
        placeholder="검색어를 입력하세요"
        class="form-input block w-full pl-3 pr-2 py-2 text-base border-gray-300 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm rounded-md shadow-sm flex-1"
      />
    </div>
    <button
      type="submit"
      class="inline-flex items-center justify-center px-6 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-sky-600 hover:bg-sky-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-sky-500 transition-colors"
    >
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 mr-2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
      </svg>
      검색
    </button>
  </form>
</template>
<script setup>
const props = defineProps({filters: Object})
</script>
