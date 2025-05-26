<template>
  <div class="flex justify-center items-center mt-6 gap-2">
    <button 
      :disabled="page <= 1" 
      @click="$emit('change', page - 1)"
      class="flex items-center justify-center p-2 rounded-md bg-white text-slate-600 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-sm border border-slate-300"
      aria-label="Previous Page"
    >
      <ChevronLeftIcon class="h-5 w-5" />
    </button>

    <div class="flex items-center gap-2">
      <template v-for="pageNum in displayedPageNumbers" :key="pageNum">
        <button
            @click="$emit('change', pageNum)"
            class="px-4 py-2 rounded-md text-sm font-medium shadow-sm"
            :class="pageNum === page
        ? 'bg-sky-500 text-white'
        : 'bg-white text-slate-600 hover:bg-slate-100 border border-slate-300'"
        >
          {{ pageNum }}
        </button>
      </template>
    </div>

    <button 
      :disabled="page >= maxPage" 
      @click="$emit('change', page + 1)"
      class="flex items-center justify-center p-2 rounded-md bg-white text-slate-600 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-sm border border-slate-300"
      aria-label="Next Page"
    >
      <ChevronRightIcon class="h-5 w-5" />
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/vue/24/solid'

const props = defineProps({
  page: {
    type: Number,
    required: true
  },
  total: {
    type: Number,
    required: true
  }
})

const pageSize = 10 // 페이지 당 항목 수, 필요에 따라 props로 받거나 설정
const maxPage = computed(() => Math.ceil(props.total / pageSize) || 1)

// 현재 페이지를 중앙에 두고 표시할 페이지 계산
const displayedPageNumbers = computed(() => {
  const totalPages = maxPage.value;
  const currentPage = props.page;

  // 양쪽에 표시할 페이지 수
  const pagesOnEachSide = 2;
  const maxPagesToShow = pagesOnEachSide * 2 + 1;

  // 시작 페이지와 끝 페이지 계산
  let startPage = Math.max(1, currentPage - pagesOnEachSide);
  let endPage = Math.min(totalPages, currentPage + pagesOnEachSide);

  // 표시되는 페이지 수가 maxPagesToShow보다 적으면 조정
  if (endPage - startPage + 1 < maxPagesToShow) {
    if (startPage === 1) {
      endPage = Math.min(startPage + maxPagesToShow - 1, totalPages);
    } else if (endPage === totalPages) {
      startPage = Math.max(endPage - maxPagesToShow + 1, 1);
    }
  }

  // 페이지 번호 배열 생성
  const pages = [];
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }
  return pages;
})

</script>
