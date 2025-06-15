import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: () => import('@/views/login/LoginPage.vue') },
    {
      path: '/',
      component: () => import('@/views/layout/LayoutContainer.vue'),
      redirect: '/myassets/manage',
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

// router.beforeEach((to) => {
//   const userstore = useUserStore()
//   if (!userstore.token && to.path !== '/login') return '/login'
// })

export default router
