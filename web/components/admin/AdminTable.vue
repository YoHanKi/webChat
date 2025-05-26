<template>
    <div class="rounded-lg overflow-hidden shadow-md bg-white">
      <table class="min-w-full divide-y divide-slate-200 table-fixed">
        <thead class="bg-slate-50">
          <tr>
            <th v-for="col in columns" :key="col.key" scope="col"
                :class="[
                  'px-6 py-3 text-center text-xs font-medium text-slate-500 uppercase tracking-wider',
                  col.key === 'actions' ? 'w-[120px]' : ''
                ]">
              {{ col.label }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-slate-200">
          <tr v-for="row in rows" :key="row.id" class="hover:bg-slate-50 transition-colors duration-150">
            <td v-for="col in columns" :key="col.key"
                :class="[
                  'px-6 py-4 text-sm text-slate-700',
                  col.key === 'actions' ? 'text-center w-[200px]' : 'overflow-hidden text-ellipsis'
                ]">
              <template v-if="col.key === 'actions'">
                <div class="flex items-center space-x-2">
                  <button
                    class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-sky-500 text-white text-xs font-semibold shadow-sm hover:bg-sky-600 transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-sky-500"
                    @click="$emit('edit', row)"
                  >
                    <PencilIcon class="w-3.5 h-3.5" />
                    수정
                  </button>
                  <button
                    class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md bg-rose-500 text-white text-xs font-semibold shadow-sm hover:bg-rose-600 transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-rose-500"
                    @click="$emit('delete', row)"
                  >
                    <TrashIcon class="w-3.5 h-3.5" />
                    삭제
                  </button>
                </div>
              </template>
              <template v-else>
                {{ row[col.key] }}
              </template>
            </td>
          </tr>
          <tr v-if="rows.length === 0">
            <td :colspan="columns.length" class="text-center text-slate-500 py-10 text-sm">
              <div class="flex flex-col items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-12 h-12 text-slate-400 mb-2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z" />
                </svg>
                데이터가 없습니다.
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>

<script setup>
import { defineProps } from 'vue'
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/outline'

const props = defineProps({
  columns: {
    type: Array,
    required: true
  },
  rows: {
    type: Array,
    required: true
  }
})
</script>
