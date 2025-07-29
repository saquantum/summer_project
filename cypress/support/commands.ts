/* eslint-env cypress */
/* global Cypress, cy */

// @ts-expect-error - Adding custom command to Cypress
Cypress.Commands.add('login', (username: string, password: string) => {
  cy.session(username, () => {
    cy.visit('/login')
    cy.get('input[placeholder*="username" i]').type(username)
    cy.get('input[placeholder*="password" i]').type(password)
    cy.get('[data-test="loginButton"]').click()
  })
})
