import { useState, type InputHTMLAttributes } from 'react'

type Props = Omit<InputHTMLAttributes<HTMLInputElement>, 'value' | 'onChange' | 'defaultValue'> & {
  mask: (value: string) => string
  defaultValue?: string | null
}

/** Input controlado que formata o valor a cada tecla e continua indo no FormData pelo `name`. */
export function MaskedInput({ mask, defaultValue, ...props }: Props) {
  const [value, setValue] = useState(() => mask(defaultValue ?? ''))
  return <input {...props} value={value} onChange={(event) => setValue(mask(event.target.value))} />
}
