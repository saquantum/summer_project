/**
 * Unified Tiptap Editor styles configuration
 * These styles will be used for both editor display and HTML output
 */

export interface StyleConfig {
  image: Record<string, string | number>
  code: Record<string, string | number>
  pre: Record<string, string | number>
  codeInPre: Record<string, string | number>
  link: Record<string, string | number>
  selection: Record<string, string | number>
  focus: Record<string, string | number>
}

export const editorStyles: StyleConfig = {
  image: {
    display: 'block',
    height: 'auto',
    margin: '1.5rem 0',
    maxWidth: '100%',
    maxHeight: '100%'
  },

  code: {
    background: '#f5f5f5',
    color: '#303133',
    fontFamily: 'Fira Mono, Consolas, Menlo, monospace',
    fontSize: '0.9em',
    padding: '2px 6px',
    borderRadius: '3px'
  },

  pre: {
    background: '#f5f5f5',
    color: '#303133',
    fontFamily: 'Fira Mono, Consolas, Menlo, monospace',
    fontSize: '0.9em',
    padding: '16px',
    overflowX: 'auto',
    margin: '1.2em 0',
    borderRadius: '6px',
    border: '1px solid #e4e7ed'
  },

  codeInPre: {
    background: 'none',
    color: 'inherit',
    fontFamily: 'inherit',
    fontSize: 'inherit',
    padding: '0',
    borderRadius: '0'
  },

  link: {
    color: '#409eff',
    textDecoration: 'underline',
    fontWeight: 'bold'
  },

  selection: {
    background: 'rgba(64, 158, 255, 0.3)'
  },

  focus: {
    outline: 'none'
  }
}

/**
 * Convert style object to CSS string
 */
function styleObjectToCss(styleObj: Record<string, string | number>): string {
  return Object.entries(styleObj)
    .map(([key, value]) => {
      // Convert camelCase to kebab-case
      const cssKey = key.replace(/([A-Z])/g, '-$1').toLowerCase()
      return `${cssKey}: ${value}`
    })
    .join('; ')
}

/**
 * Apply inline styles to HTML content for output
 */
export function addInlineStyle(html: string): string {
  // Apply link styles
  const htmlWithLinkStyle = html.replace(
    /<a\b([^>]*)>/g,
    `<a$1 style="${styleObjectToCss(editorStyles.link)};">`
  )

  // Apply image styles
  const htmlWithImgStyle = htmlWithLinkStyle.replace(
    /<img\b([^>]*)>/g,
    `<img$1 style="${styleObjectToCss(editorStyles.image)};">`
  )

  // Apply pre (code block) styles
  const htmlWithPreStyle = htmlWithImgStyle.replace(
    /<pre\b([^>]*)>/g,
    `<pre$1 style="${styleObjectToCss(editorStyles.pre)};">`
  )

  // Apply code styles within pre blocks
  const htmlWithCodeStyle = htmlWithPreStyle.replace(
    /<pre([^>]*)><code([^>]*)>/g,
    `<pre$1><code$2 style="${styleObjectToCss(editorStyles.codeInPre)};">`
  )

  // Apply inline code styles (not within pre)
  const htmlWithInlineCodeStyle = htmlWithCodeStyle.replace(
    /<code\b(?![^<]*<\/pre>)([^>]*)>/g,
    `<code$1 style="${styleObjectToCss(editorStyles.code)};">`
  )

  return htmlWithInlineCodeStyle
}

/**
 * Generate CSS for editor styling
 */
export function generateEditorCSS(): string {
  return `
.ProseMirror img {
  ${styleObjectToCss(editorStyles.image)}
}

.ProseMirror:focus {
  ${styleObjectToCss(editorStyles.focus)}
}

.ProseMirror ::selection {
  ${styleObjectToCss(editorStyles.selection)}
}

.ProseMirror code {
  ${styleObjectToCss(editorStyles.code)}
}

.ProseMirror pre {
  ${styleObjectToCss(editorStyles.pre)}
}

.ProseMirror pre code {
  ${styleObjectToCss(editorStyles.codeInPre)}
}

.ProseMirror a {
  ${styleObjectToCss(editorStyles.link)}
}
  `.trim()
}
