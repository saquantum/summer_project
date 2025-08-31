import { userLogOutService } from '@/api/user'

export const useGlobalLogout = () => {
  const logout = async () => {
    try {
      // clear store and redirect
      localStorage.removeItem('rain-user')
      localStorage.removeItem('rain-assets')
      localStorage.removeItem('rain-warnings')
      localStorage.removeItem('rain-mail')
      localStorage.removeItem('rain-template')
      window.location.href = '/login'

      // send request
      await userLogOutService()
    } catch (e) {
      console.error(e)
    }
  }

  return { logout }
}
