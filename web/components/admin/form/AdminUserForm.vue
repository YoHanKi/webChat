<template>
  <form @submit.prevent="onSubmit" class="space-y-4">
    <div>
      <label for="username" class="block text-sm font-medium text-gray-700">아이디</label>
      <input
          id="username"
          v-model="formData.username"
          type="text"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
          required
      />
    </div>

    <div v-if="!initialData.id">
      <label for="password" class="block text-sm font-medium text-gray-700">비밀번호</label>
      <input
          id="password"
          v-model="formData.password"
          type="password"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
          required
      />
    </div>

    <div>
      <label for="role" class="block text-sm font-medium text-gray-700">권한</label>
      <select
          id="role"
          v-model="formData.role"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
          required
      >
        <option value="USER">일반 사용자</option>
        <option value="ADMIN">관리자</option>
      </select>
    </div>
  </form>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({ username: '', password: ''})
  }
});

const emit = defineEmits(['update:form']);

const formData = ref({ ...props.initialData });

watch(formData, (newVal) => {
  emit('update:form', newVal);
}, { deep: true });

function onSubmit() {
}
</script>
