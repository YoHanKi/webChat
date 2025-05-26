<template>
  <div class="flex min-h-screen">
    <AdminSideBar :activeTab="activeTab" @changeTab="changeTab"/>
    <div class="flex-1 p-8">
      <slot :activeTab="activeTab"/>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import AdminSideBar from '~/components/admin/AdminSideBar.vue'

const activeTab = ref('notice') // 기본값: 공지사항
function changeTab(tab) {
  activeTab.value = tab
}
</script>