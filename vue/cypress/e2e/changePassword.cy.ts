/* eslint-env mocha, cypress */

describe('Change password', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.get('input[placeholder="Username"]').type('user_017')
    cy.get('input[placeholder="Password"]').type('123456')
    cy.get('.button').contains('Sign in').click()
    cy.visit('/user/profile')
    cy.contains('Change password').click()

    cy.intercept('**/email/verification*', (req) => {
      console.log('Intercepted:', req)
      req.reply({ code: 200, message: 'success' })
    }).as('verifyCode')
  })

  afterEach(() => {
    cy.request('POST', 'api/email/password', {
      email: 'finley01@example.org',
      password: '123456'
    })
  })

  it('renders code input and send OTP button', () => {
    cy.get('input[placeholder="Please input OTP code"]').should('be.visible')
    cy.contains('Send OTP code').should('be.visible')
    cy.contains('Next').should('be.visible')
  })

  it('sends OTP code and verifies', () => {
    cy.get('input[placeholder="Please input OTP code"]').type('123456')
    cy.contains('Send OTP code').click()
    cy.contains('Next').click()
    // After verification, the reset password form should appear
    cy.get('h1').should('contain', 'Reset your password')
    cy.get('input[placeholder="Please input password"]').should('be.visible')
    cy.get('input[placeholder="Please input password again"]').should(
      'be.visible'
    )
  })

  it('resets password and redirects to login', () => {
    cy.get('input[placeholder="Please input OTP code"]').type('123456')
    cy.contains('Next').click()
    cy.get('input[placeholder="Please input password"]').type('newpassword')
    cy.get('input[placeholder="Please input password again"]').type(
      'newpassword'
    )
    cy.contains('Confirm').click()
    cy.url().should('include', '/login')
  })
})
