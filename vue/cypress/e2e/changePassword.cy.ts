/* eslint-env mocha, cypress */

describe('Change password', () => {
  beforeEach(() => {
    cy.login('user_017', '123456')
    cy.visit('/assets')
    cy.get('[data-test="my-profile-side"]').click()
    cy.contains('Change password').click()

    cy.intercept('**/email/verification*', (req) => {
      console.log('Intercepted:', req)
      req.reply({ code: 200, message: 'success' })
    }).as('verifyCode')
  })

  it('renders code input and send OTP button', () => {
    cy.get('input[placeholder="Please input OTP code"]').should('be.visible')
    cy.contains('Send').should('be.visible')
    cy.contains('Verify').should('be.visible')
  })

  it('sends OTP code and verifies', () => {
    cy.contains('Send').click()
    cy.get('input[placeholder="Please input OTP code"]').type('123456')
    cy.contains('Verify').click()
    // After verification, the reset password form should appear
    cy.get('h1').should('contain', 'Reset your password')
    cy.get('input[placeholder="Please input password"]').should('be.visible')
    cy.get('input[placeholder="Please input password again"]').should(
      'be.visible'
    )
  })

  it('resets password and redirects to login', () => {
    cy.get('input[placeholder="Please input OTP code"]').type('123456')
    cy.contains('Verify').click()
    cy.get('input[placeholder="Please input password"]').type('newpassword')
    cy.get('input[placeholder="Please input password again"]').type(
      'newpassword'
    )
    cy.contains('Confirm').click()
    cy.url().should('include', '/login')
    cy.resetDB()
  })
})
