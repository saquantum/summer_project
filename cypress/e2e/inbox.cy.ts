/* eslint-env mocha, cypress */

describe('Inbox Page', () => {
  beforeEach(() => {
    cy.resetDB()
    cy.login('user_017', '123456')
    cy.visit('/assets')
    cy.get('[data-test="message-side"]').click()
  })

  it('renders mail list and pagination', () => {
    cy.get('.mail-item').should('have.length.greaterThan', 0)
    cy.get('.el-pagination').should('exist')
  })

  it('filters unread mails', () => {
    cy.get('.el-dropdown .button').first().click() // Open filter dropdown
    cy.contains('Unread').click()
    cy.get('.mail-item').each(($el) => {
      cy.wrap($el).should('have.class', 'unread')
    })
  })

  it('searches mails by keyword', () => {
    cy.get('input[placeholder="Search something..."]').type('Naomi')
    cy.get('.mail-item').each(($el) => {
      cy.wrap($el).find('.mail-title').invoke('text').should('match', /Naomi/i)
    })
  })

  it('sorts mails by subject', () => {
    cy.get('.el-dropdown .button').eq(1).click() // Open sort dropdown
    cy.contains('Subject').click()
    cy.get('.mail-item .mail-title').then(($titles) => {
      const texts = [...$titles].map((el) => el.textContent?.trim() || '')
      const sorted = [...texts].sort((a, b) => a.localeCompare(b))
      expect(texts).to.deep.equal(sorted)
    })
  })

  it('shows mail detail when a mail is clicked', () => {
    cy.get('.mail-item').first().click()
    cy.get('.mail-detail-panel').should('exist')
    cy.get('.mail-detail-panel .mail-title, .mail-detail-panel h3').should(
      'exist'
    )
  })

  it('marks mail as read after clicking', () => {
    cy.get('.mail-item.unread').first().click()
    cy.get('.mail-item').first().should('not.have.class', 'unread')
  })

  it('changes page when pagination is clicked', () => {
    cy.get('.el-pagination .el-pager li').last().click()
    cy.get('.mail-item').should('exist')
  })
})
