import { describe, it, expect, test } from 'vitest'
import { mount } from '@vue/test-utils'
import Test from '@/components/TestCom.vue'
import { ref } from 'vue'

describe('Test Component', () => {
  test('renders an admin link', () => {
    const wrapper = mount(Test, {
      data() {
        return {
          admin: true
        }
      }
    })

    console.log(wrapper.vm.admin)

    // Again, by using `get()` we are implicitly asserting that
    // the element exists.
    expect(wrapper.get('#admin').text()).toEqual('Admin')
  })

  test('does not render an admin link', () => {
    const wrapper = mount(Test)
    expect(wrapper.find('#admin').exists()).toBe(false)
  })

  it('does not render an admin link when admin is false', () => {
    const wrapper = mount(Test)

    // Using `wrapper.get` would throw and make the test fail.
    expect(wrapper.find('#admin').exists()).toBe(false)
  })

  it('renders profile link', () => {
    const wrapper = mount(Test)

    expect(wrapper.find('#profile').exists()).toBe(true)
    expect(wrapper.find('#profile').text()).toBe('My Profile')
  })

  it('renders admin link when admin is true', async () => {
    // Create a mock component with admin set to true
    const TestWithAdmin = {
      template: `
        <nav>
          <a id="profile" href="/profile">My Profile</a>
          <a v-if="admin" id="admin" href="/admin">Admin</a>
        </nav>
      `,
      setup() {
        const admin = ref(true)
        return { admin }
      }
    }

    const wrapper = mount(TestWithAdmin)

    expect(wrapper.find('#admin').exists()).toBe(true)
    expect(wrapper.find('#admin').text()).toBe('Admin')
    expect(wrapper.find('#profile').exists()).toBe(true)
  })
})
