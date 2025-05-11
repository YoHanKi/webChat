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

    <span class="px-4 py-2 rounded-md text-sm font-medium bg-sky-500 text-white shadow-sm">
      {{ page }}
    </span>

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

</script>
