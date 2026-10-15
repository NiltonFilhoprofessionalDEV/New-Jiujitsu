import { useState, type ReactNode } from 'react'

import { InstallGuide } from './InstallGuide'
import { promptInstall, useInstallMode, type GuidePlatform } from './installPrompt'

/**
 * Oferece instalar o app como PWA. Cada tela desenha o próprio gatilho via {@code children};
 * nada aparece quando o app já está instalado ou o aparelho não permite instalar.
 */
export function InstallApp({ children }: { children: (install: () => void) => ReactNode }) {
  const mode = useInstallMode()
  const [guide, setGuide] = useState<GuidePlatform | null>(null)
  if (mode === 'hidden' && guide === null) return null

  function install() {
    if (mode === 'prompt') void promptInstall()
    else if (mode !== 'hidden') setGuide(mode)
  }

  return (
    <>
      {mode !== 'hidden' && children(install)}
      <InstallGuide platform={guide} onClose={() => setGuide(null)} />
    </>
  )
}
