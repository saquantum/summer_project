import { useUserStore } from '@/stores'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/layout/BlankLayout.vue'),
      children: [
        {
          path: '/recover',
          component: () => import('@/components/cards/RecoverCard.vue')
        },
        {
          path: '/login',
          component: () => import('@/components/cards/LoginCard.vue')
        }
      ]
    },
    {
      path: '/',
      component: () => import('@/layout/DefaultLayout.vue'),
      children: [
        {
          path: '/assets/my',
          name: 'UserAssetPanel',
          component: () => import('@/views/user/AssetPanel.vue')
        },
        {
          path: '/assets/add',
          name: 'UserAddAsset',
          component: () => import('@/views/asset/AddAsset.vue'),
          meta: { requireAuth: true }
        },
        {
          path: 'admin/users',
          name: 'AdminAllUsers',
          component: () => import('@/views/admin/AllUsers.vue'),
          alias: ['/admin/all-users']
        },
        {
          path: 'admin/user/add',
          name: 'AdminAddUser',
          component: () => import('@/views/admin/AddUser.vue'),
          alias: ['/admin/add-user']
        },
        {
          path: 'admin/users/permission',
          name: 'AdminAccessControl',
          component: () => import('@/views/admin/AccessControl.vue'),
          alias: ['/admin/access-control']
        },
        {
          path: 'admin/user/detail',
          name: 'AdminUserDetail',
          component: () => import('@/views/admin/UserDetail.vue'),
          alias: ['/admin/user/detail/']
        },
        {
          path: '/admin/assets',
          name: 'AdminAllAssets',
          component: () => import('@/views/admin/AllAssets.vue')
        },
        {
          path: '/admin/assets/add',
          name: 'AdminAddAsset',
          component: () => import('@/views/asset/AddAsset.vue')
        },
        {
          path: '/admin/assets/types',
          name: 'AdminAssetTypes',
          component: () => import('@/views/admin/AssetTypes.vue')
        },
        {
          path: 'admin/message/template',
          name: 'AdminMessageTemplate',
          component: () => import('@/views/admin/MessageTemplate.vue')
        },
        {
          path: 'admin/message/send',
          name: 'AdminSendMessage',
          component: () => import('@/views/admin/SendMessage.vue')
        },
        {
          path: 'assets',
          component: () => import('@/views/user/AssetPanel.vue')
        },
        {
          path: 'user/profile',
          component: () => import('@/views/user/UserProfile.vue')
        },
        {
          path: 'assets/:id',
          component: () => import('@/views/asset/AssetDetail.vue')
        },
        {
          path: 'assets/add',
          component: () => import('@/views/asset/AddAsset.vue')
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
          component: () => import('@/views/warning/AllWarnings.vue')
        },
        {
          path: 'warnings',
          component: () => import('@/views/warning/AllWarnings.vue')
        },
        {
          path: 'warnings/:id',
          component: () => import('@/views/warning/WarningDetail.vue')
        },
        {
          path: 'admin/dashboard',
          component: () => import('@/views/admin/DashBoard.vue')
        },
        {
          path: 'message',
          component: () => import('@/views/user/inbox/index.vue')
        },
        {
          path: 'admin/user/add',
          component: () => import('@/views/admin/AddUser.vue')
        },
        {
          path: 'admin/assets/add',
          component: () => import('@/views/asset/AddAsset.vue')
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
          path: 'admin/message/send',
          component: () => import('@/views/admin/SendMessage.vue')
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
        },
        {
          path: 'admin/users/permission',
          component: () => import('@/views/admin/AccessControl.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      component: () => import('@/views/[...all].vue')
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
