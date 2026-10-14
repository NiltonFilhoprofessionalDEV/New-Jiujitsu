import { splitAccent } from '@/lib/accent'

/**
 * Título da marca: caixa alta condensada com a penúltima letra em vermelho. O aria-label mantém a
 * palavra inteira para leitores de tela, que leriam a letra destacada separada.
 */
export function PageTitle({ children, className }: { children: string; className?: string }) {
  const [head, accent, tail] = splitAccent(children)
  return (
    <h1 className={className ? `page-title ${className}` : 'page-title'} aria-label={children}>
      {head}
      {accent && <span className="pulse-letter">{accent}</span>}
      {tail}
    </h1>
  )
}
