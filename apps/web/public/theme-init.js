// Aplica o tema antes da primeira pintura (evita piscar o tema errado).
// Arquivo separado, e não inline, para a Content-Security-Policy não precisar de 'unsafe-inline'.
// Sem escolha salva, segue o sistema; sem preferência do sistema, fica o claro.
;(function () {
  var stored = null
  try {
    stored = localStorage.getItem('theme')
  } catch {
    // localStorage indisponível (modo privado restrito): segue o sistema
  }
  var systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  var dark = stored === 'dark' || (stored !== 'light' && systemDark)
  if (dark) document.documentElement.dataset.theme = 'dark'
  var meta = document.querySelector('meta[name="theme-color"]')
  if (meta) meta.setAttribute('content', dark ? '#070707' : '#f4f4f5')
})()
