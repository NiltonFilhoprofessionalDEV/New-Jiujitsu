import { Search } from 'lucide-react'

type Props = {
  value: string
  onChange: (value: string) => void
  /** Vira o placeholder e o nome acessível, salvo quando {@code placeholder} é informado. */
  label: string
  placeholder?: string
}

export function SearchField({ value, onChange, label, placeholder = label }: Props) {
  return (
    <label className="search-field grow">
      <Search size={18} aria-hidden />
      <input type="search" className="input" value={value} onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder} aria-label={label} />
    </label>
  )
}
