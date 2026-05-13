import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

export const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: true,
  highlight(str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch {
        // fall through
      }
    }
    try {
      return hljs.highlightAuto(str).value
    } catch {
      return md.utils.escapeHtml(str)
    }
  },
})

export function renderMarkdown(content: string): string {
  return md.render(content)
}
