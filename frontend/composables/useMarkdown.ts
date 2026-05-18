import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/vs2015.min.css'

export const useMarkdown = () => {
  const md = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true,
    highlight: (str, lang) => {
      let highlightedCode = ''
      const escapeHtml = md.utils.escapeHtml
      if (lang && hljs.getLanguage(lang)) {
        try {
          highlightedCode = hljs.highlight(str, { language: lang, ignoreIllegals: true }).value
        } catch {
          highlightedCode = escapeHtml(str)
        }
      } else {
        highlightedCode = escapeHtml(str)
      }

      const displayLang = (lang || 'code').toUpperCase()
      return `<div class="code-block-wrapper">
  <div class="code-block-header">
    <div class="mac-dots">
      <span class="mac-dot red"></span>
      <span class="mac-dot yellow"></span>
      <span class="mac-dot green"></span>
    </div>
    <span class="code-lang">${displayLang}</span>
    <div class="code-actions">
      <button class="code-action-btn copy-btn" data-action="copy" type="button">
        <svg xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="display:inline;vertical-align:middle;margin-right:2px;"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
        <span>复制</span>
      </button>
      <button class="code-action-btn fold-btn" data-action="fold" type="button">
        <svg xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="display:inline;vertical-align:middle;margin-right:2px;"><polyline points="6 9 12 15 18 9"/></svg>
        <span>折叠</span>
      </button>
    </div>
  </div>
  <pre class="hljs"><code>${highlightedCode}</code></pre>
</div>`
    }
  })

  const render = (text: string) => {
    return md.render(text || '')
  }

  // Click delegation event handler for Markdown code block buttons
  const handleBlockClick = (event: MouseEvent) => {
    const target = event.target as HTMLElement
    const button = target.closest('.code-action-btn') as HTMLButtonElement | null
    if (!button) return

    const action = button.getAttribute('data-action')
    const wrapper = button.closest('.code-block-wrapper')
    if (!wrapper) return

    if (action === 'copy') {
      const codeEl = wrapper.querySelector('pre code') as HTMLElement | null
      if (!codeEl) return
      
      const text = codeEl.innerText
      navigator.clipboard.writeText(text).then(() => {
        const textSpan = button.querySelector('span')
        if (textSpan) {
          const originalText = textSpan.innerText
          textSpan.innerText = '已复制！'
          button.classList.add('success')
          setTimeout(() => {
            textSpan.innerText = originalText
            button.classList.remove('success')
          }, 2000)
        }
      }).catch(err => {
        console.error('Failed to copy code: ', err)
      })
    } else if (action === 'fold') {
      const preEl = wrapper.querySelector('pre') as HTMLPreElement | null
      if (!preEl) return
      
      const textSpan = button.querySelector('span')
      if (preEl.style.display === 'none') {
        preEl.style.display = 'block'
        if (textSpan) textSpan.innerText = '折叠'
        button.classList.remove('collapsed')
      } else {
        preEl.style.display = 'none'
        if (textSpan) textSpan.innerText = '展开'
        button.classList.add('collapsed')
      }
    }
  }

  return {
    render,
    handleBlockClick
  }
}
