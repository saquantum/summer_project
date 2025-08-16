/* eslint-env mocha, cypress */

describe('RecoverForm', () => {
  beforeEach(() => {
    cy.visit('/recover')
    // In your test, before the code input step:
    cy.intercept('**/email/verification*', (req) => {
      req.reply({ code: 200, message: 'success' })
    }).as('verifyCode')
  })

  it('renders recover form and sends email', () => {
    cy.get('h1').should('contain', 'Recover')
    cy.get('input[placeholder="Enter your email address"]').type(
      'testrecover@example.com'
    )
    cy.contains('Send').click()
    // After sending, the code input should appear
    cy.get('input[placeholder="Enter OTP code"]').should('be.visible')
    cy.contains('Verify').should('be.visible')
  })

  it('verifies code and shows reset password form', () => {
    cy.get('input[placeholder="Enter your email address"]').type(
      'testrecover@example.com'
    )
    cy.contains('Send').click()
    cy.get('input[placeholder="Enter OTP code"]')
      .should('be.visible')
      .type('123456')
    cy.contains('Verify').click()
    // After verification, the reset password form should appear
    cy.get('h1').should('contain', 'Reset your password')
    cy.get('input[placeholder="Please input password"]').should('be.visible')
    cy.get('input[placeholder="Please input password again"]').should(
      'be.visible'
    )
  })

  it('resets password and redirects to login', () => {
    cy.get('input[placeholder="Enter your email address"]').type(
      'testrecover@example.com'
    )
    cy.contains('Send').click()
    cy.get('input[placeholder="Enter OTP code"]').type('123456')
    cy.contains('Verify').click()
    cy.get('input[placeholder="Please input password"]').type('newpassword')
    cy.get('input[placeholder="Please input password again"]').type(
      'newpassword'
    )
    cy.contains('Confirm').click()
    // After reset, should redirect to login page
    cy.url().should('include', '/login')
  })

  it('can go back to login from recover form', () => {
    cy.contains('Back to login').click()
    cy.url().should('include', '/login')
  })
})
