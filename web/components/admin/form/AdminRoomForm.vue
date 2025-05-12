<template>
  <form @submit.prevent="onSubmit" class="space-y-4">
    <div>
      <label for="name" class="block text-sm font-medium text-gray-700">방 이름</label>
      <input
          id="name"
          v-model="formData.name"
          type="text"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
          required
      />
    </div>

    <div>
      <label for="maxCapacity" class="block text-sm font-medium text-gray-700">최대 인원</label>
      <input
          id="maxCapacity"
          v-model.number="formData.maxCapacity"
          type="number"
          min="2"
          max="100"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
          required
      />
    </div>

    <div>
      <label for="description" class="block text-sm font-medium text-gray-700">설명</label>
      <textarea
          id="description"
          v-model="formData.description"
          rows="3"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
      ></textarea>
    </div>
  </form>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({ name: '', maxCapacity: 10, description: '' })
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
