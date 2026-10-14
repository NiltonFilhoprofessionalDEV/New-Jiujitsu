// Aplica o tema salvo antes da primeira pintura (evita piscar claro no modo escuro).
// Arquivo separado, e não inline, para a Content-Security-Policy não precisar de 'unsafe-inline'.
try {
  if (localStorage.getItem('theme') === 'dark') document.documentElement.dataset.theme = 'dark'
} catch {
  // localStorage indisponível (modo privado restrito): fica o tema claro
}
