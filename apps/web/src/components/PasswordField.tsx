import { Eye, EyeOff } from 'lucide-react'
import { useState, type InputHTMLAttributes } from 'react'
import { useTranslation } from 'react-i18next'

type Props = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & { id: string }

/** Campo de senha com botão para mostrar ou ocultar o que está sendo digitado. */
export function PasswordField({ id, className = 'input', ...props }: Props) {
  const { t } = useTranslation()
  const [visible, setVisible] = useState(false)
  return (
    <div className="password-field">
      <input id={id} {...props} type={visible ? 'text' : 'password'} className={className} />
      <button
        type="button"
        className="password-toggle"
        aria-pressed={visible}
        aria-controls={id}
        aria-label={visible ? t('auth.hidePassword') : t('auth.showPassword')}
        onClick={() => setVisible((current) => !current)}
      >
        {visible ? <EyeOff size={18} aria-hidden /> : <Eye size={18} aria-hidden />}
      </button>
    </div>
  )
}
