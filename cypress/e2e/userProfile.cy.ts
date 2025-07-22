/* eslint-env cypress */

describe('UserProfile', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.get('input[placeholder="Username"]').type('user_017')
    cy.get('input[placeholder="Password"]').type('123456')
    cy.get('.button').contains('Sign in').click()
    cy.get('[data-test="my-profile-side"]').click()
  })

  it('renders user table and edit button', () => {
    cy.get('table').should('exist')
    cy.contains('Edit').should('be.visible')
  })

  it('enables edit mode when Edit is clicked', () => {
    cy.contains('Edit').click()
    cy.get('button').contains('Cancel').should('be.visible')
    cy.get('button').contains('Submit').should('be.visible')
  })

  it('cancels edit mode', () => {
    cy.contains('Edit').click()
    cy.contains('Cancel').click()
    cy.contains('Edit').should('be.visible')
  })

  it('submits the form', () => {
    cy.contains('Edit').click()
    // Optionally fill in form fields here, e.g.:
    cy.get('[data-test="firstName"]').clear()
    cy.get('[data-test="firstName"]').type('NewName')
    cy.contains('Submit').click()
    // You can assert for a success message or state change if applicable
    cy.contains('updated')
  })

  it('navigates to change password page', () => {
    cy.contains('Change password').click()
    cy.url().should('include', '/security/verify-mail')
  })
})
