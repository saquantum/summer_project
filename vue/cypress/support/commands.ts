/* eslint-env cypress */
/* global Cypress, cy */

// @ts-expect-error - Adding custom command to Cypress
Cypress.Commands.add('login-session', (username: string, password: string) => {
  cy.session(username, () => {
    cy.visit('/login')
    cy.get('input[placeholder*="username" i]').type(username)
    cy.get('input[placeholder*="password" i]').type(password)
    cy.get('[data-test="loginButton"]').click()
  })
})

// @ts-expect-error - Adding custom command to Cypress
Cypress.Commands.add('login', (username: string, password: string) => {
  cy.visit('/login')
  cy.get('input[placeholder*="username" i]').type(username)
  cy.get('input[placeholder*="password" i]').type(password)
  cy.get('[data-test="loginButton"]').click()
})

// @ts-expect-error - Adding custom command to Cypress
Cypress.Commands.add('resetDB', () => {
  cy.request({
    method: 'POST',
    url: 'api/test/reset',
    headers: {
      'X-Idempotent-Post': 'true'
    }
  })
})
