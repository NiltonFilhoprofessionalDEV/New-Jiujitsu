const LETTER = /\p{L}/u

/**
 * Divide o texto em [antes, letra de destaque, depois]; o destaque é a penúltima letra (sinais e
 * espaços são ignorados na contagem). Com menos de duas letras não há destaque.
 */
export function splitAccent(text: string): [string, string, string] {
  const chars = Array.from(text)
  const letterIndexes = chars.flatMap((char, i) => (LETTER.test(char) ? [i] : []))
  const index = letterIndexes.at(-2)
  if (index === undefined) return [text, '', '']
  return [chars.slice(0, index).join(''), chars[index] ?? '', chars.slice(index + 1).join('')]
}
