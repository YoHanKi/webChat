<template>
  <div class="space-y-4">
    <div>
      <label for="roomId" class="block text-sm font-medium text-gray-700 mb-1">채팅방 ID</label>
      <input
        type="number"
        id="roomId"
        v-model.number="form.roomId"
        class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
        placeholder="채팅방 ID를 입력하세요"
      />
    </div>
    
    <div>
      <label for="sender" class="block text-sm font-medium text-gray-700 mb-1">발신자</label>
      <input
        type="text"
        id="sender"
        v-model="form.sender"
        class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
        placeholder="발신자 이름을 입력하세요"
      />
    </div>
    
    <div>
      <label for="content" class="block text-sm font-medium text-gray-700 mb-1">내용</label>
      <textarea
        id="content"
        v-model="form.content"
        rows="4"
        class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-sky-500 focus:border-sky-500 sm:text-sm"
        placeholder="채팅 내용을 입력하세요"
      ></textarea>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({
      id: null,
      roomId: null,
      sender: '',
      content: ''
    })
  }
});

const emit = defineEmits(['update:form']);

const form = ref({
  id: props.initialData.id,
  roomId: props.initialData.roomId,
  sender: props.initialData.sender,
  content: props.initialData.content
});

// initialData가 변경되면 form 상태 업데이트
watch(() => props.initialData, (newData) => {
  form.value = { ...newData };
}, { deep: true });

// form 상태가 변경되면 부모 컴포넌트에 알림
watch(form, (newFormData) => {
  emit('update:form', newFormData);
}, { deep: true });
</script>
