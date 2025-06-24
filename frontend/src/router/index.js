import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: () => import('@/views/login/LoginPage.vue') },
    {
      path: '/',
      component: () => import('@/views/layout/LayoutContainer.vue'),
      children: [
        {
          path: 'myassets/manage',
          component: () => import('@/views/myassets/AssetsManage.vue')
        },
        {
          path: 'user/profile',
          component: () => import('@/views/user/UserProfile.vue')
        },
        {
          path: 'asset/:id',
          component: () => import('@/views/assetdetail/index.vue')
        },
        {
          path: 'admin/users',
          component: () => import('@/views/admin/AllUsers.vue')
        },
        {
          path: 'admin/assets',
          component: () => import('@/views/admin/AllAssets.vue')
        },
        {
          path: 'admin/warnings',
          component: () => import('@/views/admin/AllWarnings.vue')
        },
        {
          path: 'warning/:id',
          component: () => import('@/views/warning/index.vue')
        }
      ]
    }
  ]
})

/*
Navigation Guards, enable when deploying
1. undefined /true allow
2. false return to from
3. based on path redirecting it
*/

router.beforeEach((to) => {
  const userStore = useUserStore()
  const isLoggedIn = Object.keys(userStore.user).length > 0
  const isAdmin = isLoggedIn && userStore.user.admin === true

  if (!isLoggedIn && to.path !== '/login') {
    return '/login'
  }

  if (to.path === '/') {
    if (!isLoggedIn) return '/login'
    return isAdmin ? '/admin/users' : '/myassets/manage'
  }

  if (isLoggedIn && to.path === '/login') {
    return isAdmin ? '/admin/users' : '/myassets/manage'
  }

  if (!isAdmin && to.path.startsWith('/admin')) {
    return '/myassets/manage'
  }
})

export default router
