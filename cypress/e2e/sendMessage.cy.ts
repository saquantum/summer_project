describe('Send Message Page', () => {
  beforeEach(() => {
    // Login as admin before each test
    cy.login('admin', 'admin')
    cy.visit('/admin/message/send')
  })

  it('should send a message to specific users', () => {
    // Generate a unique message for this test
    const randomMessage = `Test Message ${Date.now()}`

    // Mock the API response with a delay
    cy.intercept('POST', '/api/admin/notify/inbox/all', {
      statusCode: 200,
      delay: 500,
      body: { success: true }
    }).as('sendMessage')

    // Fill in the message form
    cy.get('[data-test="username-input"]').type('user1,user2')
    cy.get('[data-test="duration-input"]').type('60')
    cy.get('[data-test="title-input"]').type(randomMessage)

    // Enter content in the rich text editor
    cy.get('[data-test="editor-container"]')
      .find('.ProseMirror')
      .type(`This is a test content for ${randomMessage}`)

    // Verify the preview updates in real-time
    cy.get('[data-test="message-preview"]').should(
      'contain.text',
      `This is a test content for ${randomMessage}`
    )

    // Verify button is enabled before sending
    cy.get('[data-test="send-button"]').should('not.be.disabled')

    // Send the message and verify loading state
    cy.get('[data-test="send-button"]').click()
    cy.get('[data-test="send-button"]')
      .should('have.class', 'is-loading')
      .and('be.disabled')

    // Wait for API response
    cy.wait('@sendMessage')

    // Verify success notification
    cy.get('.el-message').should('contain.text', 'Message sent')
  })

  it('should send a broadcast message to all users', () => {
    const randomMessage = `Test Message ${Date.now()}`

    // Enable broadcast mode
    cy.get('[data-test="send-to-all-checkbox"]').click()

    // Verify username field becomes optional
    cy.get('[data-test="username-input"]').should(
      'not.have.class',
      'is-required'
    )

    // Fill in broadcast message details
    cy.get('[data-test="title-input"]').type('Broadcast Message')

    cy.window().then((win) => {
      win.editor.commands.setContent(
        `This is a broadcast message ${randomMessage}`
      )
    })

    // Send and verify
    cy.get('[data-test="send-button"]').click()
    cy.get('.el-message').should('contain.text', 'Message sent')
  })

  it('should validate form fields appropriately', () => {
    // Attempt to send with empty fields
    cy.get('[data-test="send-button"]').click()
    cy.get('.el-form-item__error').should('exist')

    // Verify duration field validation
    cy.get('[data-test="duration-input"]').type('-1')
    cy.get('[data-test="duration-input"]').blur()
    cy.get('.el-form-item__error')
      .should('exist')
      .should(
        'contain.text',
        'Please enter a valid duration (positive integer)'
      )
  })

  it('should handle network errors gracefully', () => {
    // Simulate API failure
    cy.intercept('POST', '/api/admin/notify/inbox/all', {
      statusCode: 500,
      body: { error: 'Server error' }
    }).as('sendMessage')

    // Fill in minimum required fields
    cy.get('[data-test="username-input"]').type('user1')
    cy.get('[data-test="title-input"]').type('Test Message')
    cy.get('[data-test="editor-container"]')
      .find('.ProseMirror')
      .type('Test content')

    // Attempt to send
    cy.get('[data-test="send-button"]').click()

    // Verify error handling
    cy.get('.el-message--error').should('contain.text', 'Fail to send message')
    cy.get('[data-test="send-button"]')
      .should('not.be.disabled')
      .should('contain.text', 'Send')
  })
})
