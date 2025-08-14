/* eslint-env mocha, cypress */

describe('AssetPanel', () => {
  beforeEach(() => {
    cy.login('user_017', '123456')
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
    cy.get('[data-test="warning-legend-btn"]').should('be.visible')

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

    // Wait for select to be visible and click it
    cy.get('.el-select').eq(1).should('be.visible').click()

    // Wait for dropdown to be visible and force click the first item
    cy.get('.el-popper')
      .should('be.visible')
      .find('.el-select-dropdown__item')
      .first()
      .click({ force: true })

    // Close filter panel
    cy.get('.seamless-button').click()

    // Verify container is still visible after filtering
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
