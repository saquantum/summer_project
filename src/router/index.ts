import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/views/layout/BlankLayout.vue'),
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
      component: () => import('@/views/layout/DefaultLayout.vue'),
      children: [
        {
          path: 'assets',
          component: () => import('@/views/myassets/AssetPanel.vue')
        },
        {
          path: 'user/profile',
          component: () => import('@/views/user/UserProfile.vue')
        },
        {
          path: 'assets/:id',
          component: () => import('@/views/assetdetail/index.vue')
        },
        {
          path: 'assets/add',
          component: () => import('@/views/myassets/AddAsset.vue')
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
          path: 'warnings/:id',
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
          path: 'admin/message/template/manage',
          component: () => import('@/views/admin/AllTemplates.vue')
        },
        {
          path: 'admin/users/permission',
          component: () => import('@/views/admin/AcessControl.vue')
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

// router.beforeEach((to) => {
//   const userStore = useUserStore()
//   const isLoggedIn = userStore.user !== null
//   const isAdmin = isLoggedIn && userStore.user?.admin === true

//   if (to.path === '/recover') return true

//   if (!isLoggedIn && to.path !== '/login') {
//     return '/login'
//   }

//   if (to.path === '/') {
//     if (!isLoggedIn) return '/login'
//     return isAdmin ? '/admin/dashboard' : '/assets'
//   }

//   if (isLoggedIn && to.path === '/login') {
//     return isAdmin ? '/admin/dashboard' : '/assets'
//   }

//   if (!isAdmin && to.path.startsWith('/admin')) {
//     return '/assets'
//   }
// })

export default router
