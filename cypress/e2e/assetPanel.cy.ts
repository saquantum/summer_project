describe('AssetPanel', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.get('input[placeholder="Username"]').type('user_017')
    cy.get('input[placeholder="Password"]').type('123456')
    cy.get('.button').contains('Sign in').click()
  })

  it('renders asset cards and legend', () => {
    cy.get('.legend').should('be.visible')
    cy.get('.asset-card').should('exist')
  })

  it('filters by warning level', () => {
    cy.get('.seamless-button').click()
    cy.get('.el-select').first().click()
    cy.get('.el-select-dropdown__item').contains('No Warning').click()
  })

  it('filters by asset type', () => {
    cy.get('.seamless-button').click()
    cy.get('.el-select').eq(1).click()
    cy.get('.el-select-dropdown__item').contains('Soakaway').click()
    cy.get('.asset-card').should('exist')
  })

  it('searches by asset name', () => {
    cy.get('input').first().type('Soakaway')
    cy.get('.asset-card').should('contain.text', 'Soakaway')
  })

  it('clears filters', () => {
    cy.get('input').first().type('Asset 1')
    cy.contains('Clear').click({ force: true }) // If you have a clear button
    cy.get('input').first().should('have.value', '')
  })

  it('navigates to asset detail page', () => {
    cy.get('.view-details-btn').first().click()
    cy.url().should('include', '/assets/')
  })

  it('pagination works', () => {
    cy.get('.el-pagination .el-pager li').last().click()
    cy.get('.asset-card').should('exist')
  })
})
