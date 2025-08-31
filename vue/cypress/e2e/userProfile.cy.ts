/* eslint-env cypress */

describe('UserProfile', () => {
  beforeEach(() => {
    cy.login('user_017', '123456')
    cy.visit('/assets')
    cy.get('[data-test="my-profile-side"]').click()
  })

  it('renders user table and edit button', () => {
    cy.get('table').should('exist')
    cy.contains('Edit').should('be.visible')
  })

  it('enables edit mode when Edit is clicked', () => {
    cy.contains('Edit').click()
    cy.contains('Cancel').scrollIntoView()
    cy.contains('Cancel').should('be.visible')
    cy.contains('Submit').scrollIntoView()
    cy.contains('Submit').should('be.visible')
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
