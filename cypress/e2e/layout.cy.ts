/* eslint-env mocha, cypress */

describe('LayoutContainer', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.get('input[placeholder="Username"]').type('user_017')
    cy.get('input[placeholder="Password"]').type('123456')
    cy.get('.button').contains('Sign in').click()
  })

  it('renders sidebar and header', () => {
    cy.get('.el-aside').should('be.visible')
    cy.get('.el-header').should('be.visible')
    cy.get('.el-aside__logo').should('be.visible')
    cy.get('.signout-button').should('be.visible')
  })

  it('signs out and redirects to login', () => {
    cy.get('.signout-button').click()
    cy.url().should('include', '/login')
  })

  it('side menu navigates to profile', () => {
    cy.get('[data-test="my-profile-side"]').click()
    cy.url().should('include', '/user/profile')
  })

  it('opens profile dropdown and navigates to profile', () => {
    cy.get('.el-dropdown__box').click()
    cy.contains('My profile').click()
    cy.url().should('include', '/user/profile')
  })

  it('opens profile dropdown and logs out', () => {
    cy.get('.el-dropdown__box').click()
    cy.contains('Log out').click()
    cy.url().should('include', '/login')
  })

  it('opens profile dropdown and tries to change avatar', () => {
    cy.get('.el-dropdown__box').click()
    cy.contains('Change avatar').should('be.visible')
  })

  it('clicks mail icon and navigates', () => {
    cy.get('.bell').click()
    // Assert navigation, e.g.:
    // cy.url().should('include', '/message')
  })

  it('opens search dialog', () => {
    cy.get('.header-left button').eq(1).click()
    cy.get('.el-dialog, .el-drawer, [role="dialog"]').should('be.visible')
  })
})
