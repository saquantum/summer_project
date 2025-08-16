describe('All Assets Page', () => {
  beforeEach(() => {
    // Login as admin and visit the page
    cy.login('admin', 'admin')
    cy.visit('/admin/assets')
  })

  it('should display assets in a table with correct warning styles', () => {
    // Check if table is loaded with correct data
    cy.get('.el-table')
      .should('exist')
      .within(() => {
        // Verify first row data and warning style
        cy.contains('ASSET001')
          .parents('tr')
          .should('have.class', 'warning-yellow')
          .within(() => {
            cy.contains('Test Asset 1')
            cy.contains('Tank')
            cy.contains('1000')
          })

        // Verify second row data and warning style
        cy.contains('ASSET002')
          .parents('tr')
          .should('have.class', 'warning-red')
          .within(() => {
            cy.contains('Test Asset 2')
            cy.contains('Pump')
            cy.contains('500')
          })
      })
  })

  it('should support sorting functionality', () => {
    // Click on Name column to sort
    cy.contains('Asset Name').click()

    // Verify the request was made with correct sort parameters
    cy.wait('@getSortedAssets')
      .its('request.url')
      .should('include', 'orderList=asset_name,asc')

    // Click again to sort in descending order
    cy.contains('Asset Name').click()
    cy.wait('@getSortedAssets')
      .its('request.url')
      .should('include', 'orderList=asset_name,desc')
  })

  it('should handle asset deletion', () => {
    // Mock deletion API
    cy.intercept('DELETE', '/api/admin/assets**', {
      statusCode: 200,
      body: { success: true }
    }).as('deleteAsset')

    // Select an asset and click delete
    cy.contains('ASSET001').parents('tr').find('[type="checkbox"]').check()

    cy.get('button').contains('Delete').click()

    // Verify confirmation dialog appears
    cy.get('.el-dialog')
      .should('be.visible')
      .should('contain', 'This will permanently delete this asset')

    // Wait for countdown to finish and button to be enabled
    cy.get('.el-dialog').contains('Confirm').should('not.be.disabled').click()

    // Verify delete request was made
    cy.wait('@deleteAsset')

    // Verify table is refreshed
    cy.wait('@getAssets')
  })

  it('should support pagination', () => {
    // Mock pagination API responses
    cy.intercept('GET', '/api/admin/assets**', (req) => {
      const url = new URL(req.url)
      const offset = url.searchParams.get('offset')
      const limit = url.searchParams.get('limit')

      if (offset === '10' && limit === '10') {
        req.reply({
          statusCode: 200,
          body: {
            data: [
              {
                asset: {
                  id: 'ASSET003',
                  name: 'Test Asset 3'
                  // ... other fields
                },
                warnings: []
              }
            ]
          }
        })
      }
    }).as('getPagedAssets')

    // Update total count for pagination
    cy.intercept('GET', '/api/admin/assets/total**', {
      statusCode: 200,
      body: { data: 15 }
    }).as('getNewTotal')

    // Click on next page
    cy.get('.el-pagination').contains('2').click()

    // Verify correct page request was made
    cy.wait('@getPagedAssets')
      .its('request.url')
      .should('include', 'offset=10')
      .should('include', 'limit=10')

    // Verify new data is displayed
    cy.contains('ASSET003').should('exist')
  })

  it('should display assets in card view on mobile devices', () => {
    // Set viewport to mobile size
    cy.viewport('iphone-6')

    // Verify table is hidden and cards are visible
    cy.get('.el-table').should('not.be.visible')
    cy.get('.asset-list').should('be.visible')

    // Verify asset cards display correct information
    cy.get('.asset-list')
      .find('AssetCard')
      .should('have.length', 2)
      .first()
      .should('contain', 'Test Asset 1')
      .should('contain', 'Tank')
  })

  it('should handle search functionality', () => {
    // Mock search API response
    cy.intercept('GET', '/api/admin/assets**', (req) => {
      const url = new URL(req.url)
      const filters = JSON.parse(
        decodeURIComponent(url.searchParams.get('filters') || '{}')
      )

      if (filters.asset_name?.like === '%Tank%') {
        req.reply({
          statusCode: 200,
          body: {
            data: [
              {
                asset: {
                  id: 'ASSET001',
                  name: 'Test Tank Asset',
                  type: { name: 'Tank' }
                  // ... other fields
                },
                warnings: []
              }
            ]
          }
        })
      }
    }).as('searchAssets')

    // Use the search component
    cy.get('AssetSearch').within(() => {
      cy.get('input').type('Tank')
      cy.get('button').contains('Search').click()
    })

    // Verify search request was made with correct parameters
    cy.wait('@searchAssets')
      .its('request.url')
      .should('include', encodeURIComponent('{"asset_name":{"like":"%Tank%"}}'))

    // Verify filtered results
    cy.contains('Test Tank Asset').should('exist')
  })
})
