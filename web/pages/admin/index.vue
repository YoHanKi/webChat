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

function tabComponent(tab) {
  if (tab === 'user') return AdminUsers
  if (tab === 'room') return AdminRooms
  if (tab === 'chat') return AdminChats
  return AdminNotices // 기본값: 공지사항
}
</script>
  