/* eslint-env mocha, cypress */

describe('login and register', () => {
  beforeEach(() => {
    cy.visit('/login') // Adjust according to your actual route
  })

  it('renders login form and allows input', () => {
    cy.get('.form-title').should('contain', 'SIGN IN')
    cy.get('input[placeholder="Username"]').type('user_010')
    cy.get('input[placeholder="Password"]').type('123456')
    cy.get('.button').contains('Sign in').click()

    cy.url().should('eq', 'http://localhost:5173/assets')
  })

  it('navigates to recover password page when "Forget password?" is clicked', () => {
    cy.contains('Forget password?').click()
    cy.url().should('include', '/recover')
  })

  it('switches to register form when "Register" is clicked', () => {
    cy.contains('Register').click()
    cy.get('.form-title').should('contain', 'SIGN UP')
    cy.get('[data-test="register-email-input"]').type('test@example.com')
    cy.get('[data-test="register-button1"]').click()
    cy.contains('Step 2')
  })

  it('completes the registration process', () => {
    const unique = Date.now()
    cy.contains('Register').click()
    cy.get('[data-test="register-email-input"]').type(
      `test${unique}@example.com`
    )

    cy.get('[data-test="register-button1"]').click()
    cy.get('input[placeholder="Please input username"]').type(`testid${unique}`)
    cy.get('input[placeholder="First name"]').type('Tom')
    cy.get('input[placeholder="Last name"]').type('Jerry')
    cy.get('input[placeholder="Please input your phone number"]').type(
      '1234567890'
    )
    cy.contains('SAVE AND CONTINUE').click()
    cy.get('input[placeholder="Please input password"]').type('123456')
    cy.get('input[placeholder="Please input password again"]').type('123456')
    cy.contains('REGISTER').click()
    // You can add assertions for successful registration or redirection here
    // cy.contains('success')
  })

  it('returns to login form when "Sign in" is clicked on register page', () => {
    cy.contains('Register').click()
    cy.contains('Sign in').click()
    cy.get('.form-title').should('contain', 'SIGN IN')
  })
})
