<template>
  <AdminLayout>
    <template #default="{ activeTab }">
      <component :is="tabComponent(activeTab)"/>
    </template>
  </AdminLayout>
</template>

<script setup>
import AdminLayout from '~/components/admin/AdminLayout.vue'
import AdminUsers from '~/components/admin/view/AdminUsers.vue'
import AdminRooms from '~/components/admin/view/AdminRooms.vue'
import AdminNotices from '~/components/admin/view/AdminNotices.vue'
import AdminChats from '~/components/admin/view/AdminChats.vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '~/stores/auth'

function tabComponent(tab) {
  if (tab === 'user') return AdminUsers
  if (tab === 'room') return AdminRooms
  if (tab === 'chat') return AdminChats
  return AdminNotices // 기본값: 공지사항
}

// 만약 ADMIN이 아니라면 admin/login으로 리다이렉트
const authStore = useAuthStore()
const router = useRouter()
if (!authStore.role || authStore.role !== 'ADMIN') {
  router.push('/admin/login')
}
</script>
  