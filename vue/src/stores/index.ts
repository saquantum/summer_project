import { createPinia } from 'pinia'
import persist from 'pinia-plugin-persistedstate'

const pinia = createPinia().use(persist)

export default pinia

export * from './modules/user'
export * from './modules/asset'
export * from './modules/warning'
export * from './modules/template'
export * from './modules/mail'

export const useGlobalLogout = () => {
  const logout = () => {
    // clear store
    localStorage.removeItem('rain-user')
    localStorage.removeItem('rain-assets')
    localStorage.removeItem('rain-warnings')
    localStorage.removeItem('rain-mail')
    localStorage.removeItem('rain-template')

    window.location.href = '/login'
  }

  return { logout }
}
