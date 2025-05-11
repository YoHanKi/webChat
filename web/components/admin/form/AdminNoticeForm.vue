<template>
  <form @submit.prevent="onSubmit" class="space-y-4">
    <div>
      <label for="title" class="block text-sm font-medium text-gray-700">제목</label>
      <input
          id="title"
          v-model="formData.title"
          type="text"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
          required
      />
    </div>

    <div>
      <label for="content" class="block text-sm font-medium text-gray-700">내용</label>
      <textarea
          id="content"
          v-model="formData.content"
          rows="4"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
          required
      ></textarea>
    </div>
  </form>
</template>

<script setup>
import { defineProps, defineEmits, ref, watch } from 'vue';

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({ title: '', content: '' })
  }
});

const emit = defineEmits(['update:form']);

const formData = ref({ ...props.initialData });

watch(formData, (newVal) => {
  emit('update:form', newVal);
}, { deep: true });

function onSubmit() {
  // 폼 자체 제출은 사용되지 않음 - 모달에서 처리
}
</script>
