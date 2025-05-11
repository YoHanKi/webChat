export default defineNuxtRouteMiddleware((to, from) => {
    if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
      if (process.client && sessionStorage.getItem('admin') !== 'true') {
        return navigateTo('/admin/login')
      }
    }
  })
  