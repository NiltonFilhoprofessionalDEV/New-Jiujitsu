// Aplica o tema salvo antes da primeira pintura (evita piscar escuro no modo claro).
// Arquivo separado, e não inline, para a Content-Security-Policy não precisar de 'unsafe-inline'.
try {
  if (localStorage.getItem('theme') === 'light') document.documentElement.dataset.theme = 'light'
} catch {
  // localStorage indisponível (modo privado restrito): fica o tema escuro
}
