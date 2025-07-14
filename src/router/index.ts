import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      component: () => import('@/views/login/LoginPage.vue'),
      children: [
        {
          path: '/recover',
          component: () => import('@/components/RecoverForm.vue')
        },
        { path: '', component: () => import('@/components/LoginForm.vue') }
      ]
    },
    {
      path: '/',
      component: () => import('@/views/layout/LayoutContainer.vue'),
      children: [
        {
          path: 'assets',
          component: () => import('@/views/myassets/AssetBoard.vue')
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
        },
        {
          path: 'admin/dashboard',
          component: () => import('@/views/admin/DashBoard.vue')
        },
        {
          path: 'message',
          component: () => import('@/views/inbox/index.vue')
        },
        {
          path: 'asset/add',
          component: () => import('@/views/myassets/AddAsset.vue')
        },
        {
          path: 'admin/user/add',
          component: () => import('@/views/admin/AddUser.vue')
        },
        {
          path: 'admin/assets/add',
          component: () => import('@/views/myassets/AddAsset.vue')
        },
        {
          path: 'admin/assets',
          component: () => import('@/views/admin/AllAssets.vue')
        },
        {
          path: 'admin/assets/types',
          component: () => import('@/views/admin/AssetTypes.vue')
        },
        {
          path: 'admin/message',
          component: () => import('@/views/inbox/index.vue')
        },
        {
          path: 'admin/message/template',
          component: () => import('@/views/admin/MessageTemplate.vue')
        },
        {
          path: 'admin/user/detail/',
          component: () => import('@/views/admin/UserDetail.vue')
        },
        {
          path: 'security/verify-mail',
          component: () => import('@/views/user/ResetPassword.vue')
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
  const isLoggedIn = userStore.user !== null
  const isAdmin = isLoggedIn && userStore.user?.admin === true

  if (to.path === '/recover') return true

  if (!isLoggedIn && to.path !== '/login') {
    return '/login'
  }

  if (to.path === '/') {
    if (!isLoggedIn) return '/login'
    return isAdmin ? '/admin/dashboard' : '/assets'
  }

  if (isLoggedIn && to.path === '/login') {
    return isAdmin ? '/admin/dashboard' : '/assets'
  }

  if (!isAdmin && to.path.startsWith('/admin')) {
    return '/assets'
  }
})

export default router
