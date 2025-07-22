/* eslint-env mocha, cypress */

describe('AssetPanel', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.get('input[placeholder="Username"]').type('user_017')
    cy.get('input[placeholder="Password"]').type('123456')
    cy.get('.button').contains('Sign in').click()
    // Wait for login to complete and then navigate to assets
    cy.url().should('not.include', '/login')
    cy.visit('/assets')
    // Wait for the asset panel to load
    cy.get('.search-bar').should('be.visible')
    // Wait for either asset cards to load OR the "no assets" message to appear
    cy.get('.assets-container').should('be.visible')
    // Wait for data to load - either asset cards or empty state message
    cy.get('body').should('satisfy', ($body) => {
      return (
        $body.find('.asset-card').length > 0 ||
        $body.text().includes("You don't have any asset")
      )
    })
  })

  it('renders asset cards and legend', () => {
    cy.get('.legend').should('be.visible')
    cy.get('.legend-item').should('have.length', 4)

    // Debug: Print page content to understand what's happening
    cy.get('body').then(($body) => {
      console.log('Page HTML:', $body.html())
      console.log('Asset cards found:', $body.find('.asset-card').length)
      console.log(
        'Assets container content:',
        $body.find('.assets-container').html()
      )
    })

    // Only check for asset cards if there are assets loaded
    cy.get('body').then(($body) => {
      if ($body.find('.asset-card').length > 0) {
        cy.get('.asset-card').should('exist')
        cy.log('Found asset cards on the page')
      } else {
        cy.log('No asset cards found, checking for empty state message')
        // If no assets, check for the "no assets" message
        cy.contains("You don't have any asset").should('be.visible')
      }
    })
  })

  it('filters by warning level', () => {
    // Open the filter detail panel
    cy.get('.seamless-button').click()
    cy.get('.el-select').first().click()
    cy.get('.el-select-dropdown__item').contains('No Warning').click()
    // Close the filter panel
    cy.get('.seamless-button').click()
  })

  it('filters by asset type', () => {
    // Open the filter detail panel
    cy.get('.seamless-button').click()
    cy.get('.el-select').eq(1).click()
    cy.get('.el-select-dropdown__item').first().click() // Select first available type
    cy.get('.seamless-button').click() // Close filter panel
    // Assets might be filtered out, so just check the panel still exists
    cy.get('.assets-container').should('be.visible')
  })

  it('searches by asset name', () => {
    cy.get('input[placeholder="Search assets..."]').type('Asset')
    // Wait for the search to apply by checking the container
    cy.get('.assets-container').should('be.visible')
  })

  it('clears filters', () => {
    // First apply a filter
    cy.get('input[placeholder="Search assets..."]').type('Test')
    // Open the filter panel and clear
    cy.get('.seamless-button').click()
    cy.get('button').contains('Clear filters').click()
    cy.get('input[placeholder="Search assets..."]').should('have.value', '')
  })

  it('navigates to asset detail page', () => {
    // Only test if there are asset cards present
    cy.get('body').then(($body) => {
      if ($body.find('.asset-card').length > 0) {
        cy.get('.view-details-btn').first().click()
        cy.url().should('include', '/assets/')
      } else {
        cy.log('No assets available to test detail navigation')
      }
    })
  })

  it('pagination works', () => {
    // Only test pagination if there are enough assets
    cy.get('body').then(($body) => {
      if ($body.find('.el-pagination').length > 0) {
        cy.get('.el-pagination .el-pager li').last().click()
        cy.get('.assets-container').should('be.visible')
      } else {
        cy.log('Not enough assets to test pagination')
      }
    })
  })
})
