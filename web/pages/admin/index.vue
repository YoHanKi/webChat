<template>
  <AdminLayout>
    <template #default="{ activeTab }">
      <component :is="tabComponent(activeTab)"/>
    </template>
  </AdminLayout>
</template>
<script setup>
import AdminLayout from '~/components/admin/AdminLayout.vue'
import AdminUsers from '~/components/admin/AdminUsers.vue'
import AdminRooms from '~/components/admin/AdminRooms.vue'
import AdminNotices from '~/components/admin/AdminNotices.vue'

function tabComponent(tab) {
  if (tab === 'user') return AdminUsers
  if (tab === 'room') return AdminRooms
  return AdminNotices // 기본값: 공지사항
}
</script>
  