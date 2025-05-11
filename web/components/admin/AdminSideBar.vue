<template>
  <aside class="w-64 bg-white text-gray-800 h-screen flex flex-col shadow-lg border-r border-gray-200">
    <div class="p-5 font-semibold text-xl tracking-wide flex items-center gap-3 border-b border-gray-200 text-gray-700">
      <!-- Heroicon: AdjustmentsHorizontalIcon -->
      <AdjustmentsHorizontalIcon class="w-6 h-6 text-gray-500"/>
      Admin Panel
    </div>
    <nav class="flex-1 mt-4">
      <ul>
        <li :class="tabClass('notice')" @click="$emit('changeTab', 'notice')">
          <!-- Heroicon: BellIcon -->
          <BellIcon class="w-5 h-5 mr-3 flex-shrink-0"/>
          공지사항 관리
        </li>
        <li :class="tabClass('user')" @click="$emit('changeTab', 'user')">
          <!-- Heroicon: UserGroupIcon -->
          <UserGroupIcon class="w-5 h-5 mr-3 flex-shrink-0"/>
          회원 관리
        </li>
        <li :class="tabClass('room')" @click="$emit('changeTab', 'room')">
          <!-- Heroicon: ChatBubbleLeftRightIcon -->
          <ChatBubbleLeftRightIcon class="w-5 h-5 mr-3 flex-shrink-0"/>
          채팅방 관리
        </li>
      </ul>
    </nav>
  </aside>
</template>
<script setup>
import { BellIcon, UserGroupIcon, ChatBubbleLeftRightIcon, AdjustmentsHorizontalIcon } from '@heroicons/vue/24/outline'
const props = defineProps({activeTab: String})

function tabClass(tab) {
  return [
    'flex items-center px-5 py-3.5 cursor-pointer transition-colors duration-150 ease-in-out text-sm font-medium rounded-md mx-2 my-1',
    props.activeTab === tab ? 'bg-sky-500 text-white shadow-sm' : 'text-gray-600 hover:bg-sky-50 hover:text-sky-600'
  ]
}
</script>
